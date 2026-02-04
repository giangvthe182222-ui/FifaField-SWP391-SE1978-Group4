package Controller.Equipment;

import DAO.EquipmentDAO;
import Models.Equipment;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(
        name = "EquipmentListServlet",
        urlPatterns = "/equipment-list"
)
public class EquipmentListServlet extends HttpServlet {

    private static final int PAGE_SIZE = 8;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ================= FILTER PARAM =================
        String search = trimToNull(request.getParameter("search"));
        String status = trimToNull(request.getParameter("status"));
        String type = trimToNull(request.getParameter("type"));
        String sort = trimToNull(request.getParameter("sort"));

        // ================= PAGINATION PARAM =================
        int page = 1;
        String pageRaw = request.getParameter("page");
        if (pageRaw != null) {
            try {
                page = Integer.parseInt(pageRaw);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        // ================= DAO =================
        EquipmentDAO dao = new EquipmentDAO(new DBConnection());

        List<Equipment> equipmentList =
                dao.filter(search, status, type, sort, page, PAGE_SIZE);

        int totalRecords = dao.count(search, status, type);
        int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);

        // ================= ATTRIBUTE =================
        request.setAttribute("equipmentList", equipmentList);
        request.setAttribute("typeList", dao.getAllTypes());

        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.setAttribute("search", search);
        request.setAttribute("status", status);
        request.setAttribute("type", type);
        request.setAttribute("sort", sort);

        // ================= FORWARD =================
        request.getRequestDispatcher("/View/Equipment/EquipmentList.jsp")
               .forward(request, response);

        // ================= LOG =================
        System.out.println(">>> EquipmentListServlet called");
        System.out.println("Page: " + page + "/" + totalPages);
        System.out.println("Total records: " + totalRecords);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    // ================= UTILS =================
    private String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}
