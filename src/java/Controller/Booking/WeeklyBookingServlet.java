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
import Models.Voucher;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "WeeklyBookingServlet", urlPatterns = {"/booking/weekly"})
public class WeeklyBookingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=booking/weekly");
            return;
        }

        // Flash messages
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

        try {
            // --- Locations ---
            LocationDAO locationDAO = new LocationDAO();
            List<Location> locations = locationDAO.getAllLocations().stream()
                    .filter(l -> l.getStatus() != null && "ACTIVE".equalsIgnoreCase(l.getStatus()))
                    .collect(Collectors.toList());
            request.setAttribute("locations", locations);

            // --- Week navigation ---
            String weekStartParam = request.getParameter("weekStart");
            LocalDate weekStart;
            if (weekStartParam != null && !weekStartParam.isBlank()) {
                weekStart = LocalDate.parse(weekStartParam).with(DayOfWeek.MONDAY);
            } else {
                weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
            }
            LocalDate weekEnd = weekStart.plusDays(6);
            request.setAttribute("weekStart", weekStart.toString());
            request.setAttribute("weekEnd", weekEnd.toString());
            request.setAttribute("prevWeekStart", weekStart.minusWeeks(1).toString());
            request.setAttribute("nextWeekStart", weekStart.plusWeeks(1).toString());

            List<LocalDate> weekDates = new ArrayList<>();
            for (int i = 0; i < 7; i++) weekDates.add(weekStart.plusDays(i));
            request.setAttribute("weekDates", weekDates);

            // --- Location / FieldType / Field selection ---
            String locationIdParam = request.getParameter("locationId");
            String fieldTypeParam  = request.getParameter("fieldType");
            String fieldIdParam    = request.getParameter("fieldId");
            request.setAttribute("selectedLocationId", locationIdParam);
            request.setAttribute("selectedFieldType",  fieldTypeParam);
            request.setAttribute("selectedFieldId",    fieldIdParam);

            List<Field> fields = new ArrayList<>();
            List<LocationEquipmentViewModel> equipments = new ArrayList<>();
            List<Voucher> vouchers = new ArrayList<>();

            if (locationIdParam != null && !locationIdParam.isBlank()) {
                UUID locationId = UUID.fromString(locationIdParam);
                FieldDAO fieldDAO = new FieldDAO();
                List<Field> allFields = fieldDAO.getByLocation(locationId).stream()
                        .filter(f -> f.getStatus() != null &&
                                ("ACTIVE".equalsIgnoreCase(f.getStatus())
                                || "AVAILABLE".equalsIgnoreCase(f.getStatus())))
                        .collect(Collectors.toList());

                if (fieldTypeParam != null && !fieldTypeParam.isBlank()) {
                    final String ft = fieldTypeParam.trim();
                    fields = allFields.stream()
                            .filter(f -> ft.equalsIgnoreCase(f.getFieldType()) ||
                                    (f.getFieldType() != null && f.getFieldType().contains(ft)))
                            .collect(Collectors.toList());
                } else {
                    fields = allFields;
                }

                // Equipment available at this location
                LocationEquipmentDAO locEquipDAO = new LocationEquipmentDAO(new DBConnection());
                equipments = locEquipDAO.getByLocation(locationId).stream()
                        .filter(e -> "available".equalsIgnoreCase(e.getStatus()) && e.getQuantity() > 0)
                        .collect(Collectors.toList());

                // Active vouchers at this location
                VoucherDAO voucherDAO = new VoucherDAO();
                LocalDate today = LocalDate.now();
                vouchers = voucherDAO.getByLocation(locationId).stream()
                        .filter(v -> v.getStatus() != null && "active".equalsIgnoreCase(v.getStatus()))
                        .filter(v -> (v.getStartDate() == null || !v.getStartDate().isAfter(today))
                                && (v.getEndDate() == null || !v.getEndDate().isBefore(today)))
                        .collect(Collectors.toList());
            }
            request.setAttribute("fields",     fields);
            request.setAttribute("equipments", equipments);
            request.setAttribute("vouchers",   vouchers);

            // --- Schedule grid (only when field is chosen) ---
            if (fieldIdParam != null && !fieldIdParam.isBlank()
                    && locationIdParam != null && !locationIdParam.isBlank()) {

                UUID fieldId = UUID.fromString(fieldIdParam);
                FieldDAO fd = new FieldDAO();
                request.setAttribute("selectedField", fd.getById(fieldId));

                ScheduleDAO scheduleDAO = new ScheduleDAO();
                List<Schedule> allSchedules = scheduleDAO.getScheduleByFieldInRange(fieldId, weekStart, weekEnd);

                LocalDate today = LocalDate.now();

                // Collect all distinct start-times (sorted)
                Set<LocalTime> timeSlotSet = new TreeSet<>();
                for (Schedule s : allSchedules) timeSlotSet.add(s.getStartTime());

                // Build grid rows: each row = one time slot, 7 cells (one per day)
                List<Map<String, Object>> gridRows = new ArrayList<>();
                for (LocalTime slot : timeSlotSet) {
                    Map<String, Object> row = new LinkedHashMap<>();

                    // find end-time from any schedule at this slot
                    LocalTime endTime = null;
                    for (Schedule s : allSchedules) {
                        if (s.getStartTime().equals(slot)) { endTime = s.getEndTime(); break; }
                    }
                    row.put("startTime", slot.toString().substring(0, 5));  // "HH:mm"
                    row.put("endTime",   endTime != null ? endTime.toString().substring(0, 5) : "");

                    List<Map<String, Object>> cells = new ArrayList<>();
                    for (LocalDate date : weekDates) {
                        Map<String, Object> cell = new LinkedHashMap<>();
                        Schedule found = null;
                        for (Schedule s : allSchedules) {
                            if (s.getStartTime().equals(slot) && s.getBookingDate().equals(date)) {
                                found = s;
                                break;
                            }
                        }
                        if (found == null) {
                            cell.put("exists", false);
                        } else {
                            boolean isPast = date.isBefore(today) ||
                                    (date.equals(today) && slot.isBefore(java.time.LocalTime.now()));
                            cell.put("exists",      true);
                            cell.put("scheduleId",  found.getScheduleId().toString());
                            cell.put("price",       found.getPrice());
                            cell.put("available",   "available".equalsIgnoreCase(found.getStatus()) && !isPast);
                            cell.put("past",        isPast);
                            cell.put("status",      found.getStatus());
                        }
                        cells.add(cell);
                    }
                    row.put("cells", cells);
                    gridRows.add(row);
                }
                request.setAttribute("gridRows", gridRows);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi tải dữ liệu lịch sân: " + e.getMessage());
        }

        request.getRequestDispatcher("/View/Booking/WeeklyBooking.jsp").forward(request, response);
    }
}
