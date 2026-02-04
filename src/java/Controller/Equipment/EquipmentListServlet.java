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
    //Setting Page size
    private static final int PAGE_SIZE = 8;
    //Created by: Giangvthe182222
    //Function: Get Equipment List
    //Description: Get the equipment structure list by filter, pagination for equipment struture list page
    //Note:
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Initialize search parameter, trimToNull to advoid spaces
        String search = trimToNull(request.getParameter("search"));
        String status = trimToNull(request.getParameter("status"));
        String type = trimToNull(request.getParameter("type"));
        String sort = trimToNull(request.getParameter("sort"));
        //ensure first page is always 1
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
        
        EquipmentDAO dao = new EquipmentDAO(new DBConnection());
        //filter + pagination
        List<Equipment> equipmentList =
                dao.filter(search, status, type, sort, page, PAGE_SIZE);
        //total equipment structures after filter
        int totalRecords = dao.count(search, status, type);
        //total pages by total records
        int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);

        request.setAttribute("equipmentList", equipmentList);
        request.setAttribute("typeList", dao.getAllTypes());

        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.setAttribute("search", search);
        request.setAttribute("status", status);
        request.setAttribute("type", type);
        request.setAttribute("sort", sort);

        request.getRequestDispatcher("/View/Equipment/EquipmentList.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    //trim to null function
    private String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}
