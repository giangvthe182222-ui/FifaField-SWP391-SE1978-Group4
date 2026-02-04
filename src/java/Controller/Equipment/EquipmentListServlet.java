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
    //Description:
    //  - Receive filter parameters (search, status, type, sort)
    //  - Handle pagination for equipment structure list
    //  - Call DAO to retrieve filtered data and total records
    //  - Forward data to EquipmentList.jsp
    //Note:
    //  - Page index always starts from 1
    //  - Invalid page values will be reset to 1
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //Initialize search parameter, trimToNull to avoid spaces
        String search = trimToNull(request.getParameter("search"));
        String status = trimToNull(request.getParameter("status"));
        String type = trimToNull(request.getParameter("type"));
        String sort = trimToNull(request.getParameter("sort"));

        //Ensure first page is always 1
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
        List<Equipment> equipmentList =
                dao.filter(search, status, type, sort, page, PAGE_SIZE);
        int totalRecords = dao.count(search, status, type);
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

    //Created by: Giangvthe182222
    //Function: Handle POST request
    //Description: Forward POST request to doGet(), Ensure the same logic is used for both GET and POST
    //Note: Used when form submits via POST method
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    //Created by: Giangvthe182222
    //Function: Trim string value to null
    //Description: remove spaces and set to null is empty
    //Note: Helps avoid invalid filter values caused by spaces
    private String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}
