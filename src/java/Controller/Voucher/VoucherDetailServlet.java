package Controller.Voucher;

import DAO.VoucherDAO;
import Models.Voucher;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class VoucherDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id == null || id.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/voucher/list");
            return;
        }
        VoucherDAO dao = new VoucherDAO();
        try {
            Voucher v = dao.getVoucherById(id);
            if (v == null) { response.sendRedirect(request.getContextPath() + "/voucher/list"); return; }
            request.setAttribute("voucher", v);
            request.getRequestDispatcher("/View/Voucher/VoucherDetail.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
}
