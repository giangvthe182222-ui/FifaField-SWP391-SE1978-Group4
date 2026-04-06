package Controller.Booking;

import DAO.FieldDAO;
import DAO.LocationDAO;
import DAO.LocationEquipmentDAO;
import DAO.ScheduleDAO;
import DAO.VoucherDAO;
import Models.Field;
import Models.Location;
import Models.LocationEquipmentViewModel;
import Models.Schedule;
import Models.User;
import Models.Voucher;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(name = "BookingServlet", urlPatterns = {"/booking", "/BookingServlet"})
public class BookingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        jakarta.servlet.http.HttpSession session = request.getSession(false);
        boolean isStaffUser = false;
        if (session != null) {
            String flashSuccess = (String) session.getAttribute("flash_success");
            if (flashSuccess != null) {
                request.setAttribute("flashSuccess", flashSuccess);
                session.removeAttribute("flash_success");
            }
            String flashError = (String) session.getAttribute("flash_error");
            if (flashError != null) {
                request.setAttribute("flashError", flashError);
                session.removeAttribute("flash_error");
            }

            Object userObj = session.getAttribute("user");
            if (userObj instanceof User) {
                User user = (User) userObj;
                if (user.getRole() != null && user.getRole().getRoleName() != null) {
                    isStaffUser = "STAFF".equalsIgnoreCase(user.getRole().getRoleName());
                }
            }
        }
        request.setAttribute("isStaffUser", isStaffUser);

        try {
            LocalDate minBookingDate = LocalDate.now().plusDays(1);
            request.setAttribute("minBookingDate", minBookingDate);

            LocalDate selectedWeekStart = minBookingDate;
            String weekStartParam = request.getParameter("weekStart");
            if (weekStartParam != null && !weekStartParam.isBlank()) {
                try {
                    selectedWeekStart = LocalDate.parse(weekStartParam);
                } catch (Exception ignored) {
                    selectedWeekStart = minBookingDate;
                }
            }
            if (selectedWeekStart.isBefore(minBookingDate)) {
                selectedWeekStart = minBookingDate;
            }

            LocalDate selectedWeekEnd = selectedWeekStart.plusDays(6);
            LocalDate prevWeekStart = selectedWeekStart.minusWeeks(1);
            boolean canGoPrevWeek = !prevWeekStart.isBefore(minBookingDate);
            if (!canGoPrevWeek) {
                prevWeekStart = minBookingDate;
            }

            request.setAttribute("selectedWeekStart", selectedWeekStart);
            request.setAttribute("selectedWeekEnd", selectedWeekEnd);
            request.setAttribute("prevWeekStart", prevWeekStart);
            request.setAttribute("nextWeekStart", selectedWeekStart.plusWeeks(1));
            request.setAttribute("canGoPrevWeek", canGoPrevWeek);

            LocationDAO locationDAO = new LocationDAO();
            List<Location> allLocations = locationDAO.getAllLocations();
            // Filter out inactive locations for customer
            List<Location> locations = allLocations.stream()
                    .filter(loc -> loc.getStatus() != null && "ACTIVE".equalsIgnoreCase(loc.getStatus()))
                    .collect(Collectors.toList());
            request.setAttribute("locations", locations);

            String locationIdParam = request.getParameter("locationId");
            String fieldTypeParam = request.getParameter("fieldType");
            String fieldIdParam = request.getParameter("fieldId");

            List<Field> fields = new ArrayList<>();
            List<LocationEquipmentViewModel> equipments = new ArrayList<>();
            List<Voucher> vouchers = new ArrayList<>();
            List<Schedule> schedules = new ArrayList<>();

            if (locationIdParam != null && !locationIdParam.isBlank()) {
                UUID locationId = UUID.fromString(locationIdParam);
                request.setAttribute("selectedLocationId", locationId);

                // Fields of the location, optionally filtered by field type (7, 11)
                FieldDAO fieldDAO = new FieldDAO();
                List<Field> allFields = fieldDAO.getByLocation(locationId);
                // Filter out inactive/unavailable fields
                List<Field> availableFields = allFields.stream()
                        .filter(f -> f.getStatus() != null && 
                               ("ACTIVE".equalsIgnoreCase(f.getStatus()) || "AVAILABLE".equalsIgnoreCase(f.getStatus())))
                        .collect(Collectors.toList());
                
                if (fieldTypeParam != null && !fieldTypeParam.isBlank()) {
                    final String ft = fieldTypeParam.trim();
                    fields = availableFields.stream()
                            .filter(f -> ft.equalsIgnoreCase(f.getFieldType()) || (f.getFieldType() != null && f.getFieldType().contains(ft)))
                            .collect(Collectors.toList());
                    request.setAttribute("selectedFieldType", fieldTypeParam);
                } else {
                    fields = availableFields;
                }
                request.setAttribute("fields", fields);

                // Load equipments and vouchers as soon as location is selected
                // so customer can see related info right away.
                LocationEquipmentDAO locEquipDAO = new LocationEquipmentDAO(new DBConnection());
                List<LocationEquipmentViewModel> allEquip = locEquipDAO.getByLocation(locationId);
                equipments = allEquip.stream()
                    .filter(e -> "available".equalsIgnoreCase(e.getStatus()) && e.getQuantity() > 0)
                    .collect(Collectors.toList());
                request.setAttribute("equipments", equipments);

                VoucherDAO voucherDAO = new VoucherDAO();
                List<Voucher> allVouchers = voucherDAO.getByLocation(locationId);
                LocalDate today = LocalDate.now();
                vouchers = allVouchers.stream()
                    .filter(v -> v.getStatus() != null && "active".equalsIgnoreCase(v.getStatus()))
                    .filter(v -> (v.getStartDate() == null || !v.getStartDate().isAfter(today))
                    && (v.getEndDate() == null || !v.getEndDate().isBefore(today)))
                    .collect(Collectors.toList());
                request.setAttribute("vouchers", vouchers);

                if (fieldIdParam != null && !fieldIdParam.isBlank()) {

                    UUID fieldId = UUID.fromString(fieldIdParam);
                    request.setAttribute("selectedFieldId", fieldId);

                    ScheduleDAO scheduleDAO = new ScheduleDAO();
                    List<Schedule> allSchedules;
                    String bookingDateParam = request.getParameter("bookingDate");
                    if (bookingDateParam != null && !bookingDateParam.isBlank()) {
                        LocalDate selectedDate = LocalDate.parse(bookingDateParam);
                        if (selectedDate.isBefore(minBookingDate)) {
                            selectedDate = minBookingDate;
                        }
                        allSchedules = scheduleDAO.getScheduleByFieldAndDate(fieldId, selectedDate);
                    } else {
                        LocalDate fromDate = selectedWeekStart;
                        LocalDate toDate = selectedWeekEnd;
                        allSchedules = scheduleDAO.getScheduleByFieldInRange(fieldId, fromDate, toDate);
                    }

                    LocalDateTime now = LocalDateTime.now();

                    schedules = allSchedules.stream()
                            // Chỉ hiển thị lịch từ ngày mai trở đi và chưa quá thời điểm hiện tại.
                            .filter(s -> !s.getBookingDate().isBefore(minBookingDate))
                            .filter(s -> {
                                LocalDateTime scheduleDateTime = LocalDateTime.of(
                                        s.getBookingDate(),
                                        s.getStartTime()
                                );
                                return scheduleDateTime.isAfter(now);
                            })
                            .collect(Collectors.toList());
                }
            }

            request.setAttribute("schedules", schedules);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading booking data: " + e.getMessage());
            request.setAttribute("fields", new ArrayList<Field>());
            request.setAttribute("equipments", new ArrayList<LocationEquipmentViewModel>());
            request.setAttribute("vouchers", new ArrayList<Voucher>());
            request.setAttribute("schedules", new ArrayList<Schedule>());
        }

        request.getRequestDispatcher("/View/Booking/Booking.jsp").forward(request, response);
    }
}
