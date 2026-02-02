
package Controller.Location;

import DAO.LocationDAO;
import Models.Location;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LocationListServlet", urlPatterns = {"/locations"})
public class LocationListServlet extends HttpServlet {
    private final LocationDAO locationDAO = new LocationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Lấy danh sách tất cả cụm sân từ Database
            List<Location> list = locationDAO.getAllLocations();
            
            // Đưa dữ liệu vào request attribute để JSP sử dụng
            request.setAttribute("locations", list);
            
            // Forward tới trang JSP danh sách (Lưu ý đường dẫn có chữ Location viết hoa)
            request.getRequestDispatcher("/View/Location/location-list.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống khi tải danh sách: " + e.getMessage());
        }
    }
}
