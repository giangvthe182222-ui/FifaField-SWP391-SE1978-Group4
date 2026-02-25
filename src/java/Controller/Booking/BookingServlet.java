package Controller.Booking;

import DAO.*;
import Models.*;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
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
        }

        try {
            LocationDAO locationDAO = new LocationDAO();
            List<Location> locations = locationDAO.getAllLocations();
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

                // Fields of the location, optionally filtered by field type (5, 7, 11)
                FieldDAO fieldDAO = new FieldDAO();
                List<Field> allFields = fieldDAO.getByLocation(locationId);
                if (fieldTypeParam != null && !fieldTypeParam.isBlank()) {
                    final String ft = fieldTypeParam.trim();
                    fields = allFields.stream()
                            .filter(f -> ft.equalsIgnoreCase(f.getFieldType()) || (f.getFieldType() != null && f.getFieldType().contains(ft)))
                            .collect(Collectors.toList());
                    request.setAttribute("selectedFieldType", fieldTypeParam);
                } else {
                    fields = allFields;
                }
                request.setAttribute("fields", fields);

                // Location equipment (available only)
                LocationEquipmentDAO locEquipDAO = new LocationEquipmentDAO(new DBConnection());
                List<LocationEquipmentViewModel> allEquip = locEquipDAO.getByLocation(locationId);
                equipments = allEquip.stream()
                        .filter(e -> "available".equalsIgnoreCase(e.getStatus()) && e.getQuantity() > 0)
                        .collect(Collectors.toList());
                request.setAttribute("equipments", equipments);

                // Vouchers for the location (active and within date range)
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
                    List<Schedule> allSchedules = scheduleDAO.getScheduleByField(fieldId);
                    schedules = allSchedules.stream()
                            .filter(s -> s.getStatus() == null || "available".equalsIgnoreCase(s.getStatus()))
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
