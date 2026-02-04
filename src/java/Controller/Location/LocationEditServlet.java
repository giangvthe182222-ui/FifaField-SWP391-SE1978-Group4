package Controller.Location;

import DAO.LocationDAO;
import Models.Location;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.UUID;

@MultipartConfig

@WebServlet(name = "LocationEditServlet", urlPatterns = {"/locations/edit"})
public class LocationEditServlet extends HttpServlet {
    private final LocationDAO locationDAO = new LocationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("location_id");
        if (id == null || id.isBlank()) {
            id = request.getParameter("locationId");
        }

        if (id == null || id.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing location_id");
            return;
        }

        try {
            UUID uuid = UUID.fromString(id);
            Location loc = locationDAO.getLocationById(uuid);
            if (loc == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Location not found");
                return;
            }

            request.setAttribute("location", loc);
            request.getRequestDispatcher("/View/Location/location-edit.jsp").forward(request, response);

        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID format");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String id = request.getParameter("locationId");
        String name = request.getParameter("locationName");
        String address = request.getParameter("address");
        String phone = request.getParameter("phoneNumber");
        String imageUrl = request.getParameter("imageUrl");
        String oldImage = request.getParameter("old_image");
        String status = request.getParameter("status");

        if (id == null || id.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing locationId");
            return;
        }

        if (name == null || name.trim().isEmpty()) {
            request.setAttribute("error", "Tên cụm sân không được để trống");
            doGet(request, response);
            return;
        }

        try {
            UUID uuid = UUID.fromString(id);
            // handle uploaded image (if any)
            try {
                Part imgPart = request.getPart("image");
                if (imgPart != null && imgPart.getSize() > 0) {
                    String fileName = Paths.get(imgPart.getSubmittedFileName()).getFileName().toString();
                    String uploadDir = getServletContext().getRealPath("/uploads");
                    new File(uploadDir).mkdirs();
                    imageUrl = "uploads/" + UUID.randomUUID() + "_" + fileName;
                    imgPart.write(getServletContext().getRealPath("/") + imageUrl);
                } else {
                    // keep old image if provided
                    if ((imageUrl == null || imageUrl.isBlank()) && oldImage != null && !oldImage.isBlank()) {
                        imageUrl = oldImage;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Location loc = new Location();
            loc.setLocationId(uuid);
            loc.setLocationName(name.trim());
            loc.setAddress(address);
            loc.setPhoneNumber(phone);
            loc.setImageUrl(imageUrl == null || imageUrl.isBlank() ? "default_cluster.jpg" : imageUrl);
            loc.setStatus(status == null || status.isBlank() ? "ACTIVE" : status);

            boolean updated = locationDAO.updateLocation(loc);
            if (updated) {
                response.sendRedirect(request.getContextPath() + "/locations/view?location_id=" + id);
            } else {
                request.setAttribute("error", "Cập nhật không thành công");
                request.setAttribute("location", loc);
                request.getRequestDispatcher("/View/Location/location-edit.jsp").forward(request, response);
            }

        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID format");
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi SQL: " + e.getMessage());
            doGet(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
}
