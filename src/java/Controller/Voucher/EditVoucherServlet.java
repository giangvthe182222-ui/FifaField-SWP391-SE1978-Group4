package Controller.Voucher;

import DAO.VoucherDAO;
import Models.Voucher;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

public class EditVoucherServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id == null || id.isBlank()) { response.sendRedirect(request.getContextPath() + "/voucher/list"); return; }
        VoucherDAO dao = new VoucherDAO();
        try {
            Voucher v = dao.getVoucherById(id);
            if (v == null) { response.sendRedirect(request.getContextPath() + "/voucher/list"); return; }
            request.setAttribute("voucher", v);
            request.getRequestDispatcher("/View/Voucher/EditVoucher.jsp").forward(request, response);
        } catch (SQLException ex) { throw new ServletException(ex); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String id = request.getParameter("voucherId");
        String code = request.getParameter("code");
        String discountStr = request.getParameter("discountValue");
        String description = request.getParameter("description");
        String start = request.getParameter("startDate");
        String end = request.getParameter("endDate");
        String status = request.getParameter("status");

        java.math.BigDecimal discount = null;
        try { discount = new java.math.BigDecimal(discountStr); } catch (Exception e) {}

        Date sd = null, ed = null;
        try { if (start != null && !start.isBlank()) sd = Date.valueOf(start); } catch (Exception e) {}
        try { if (end != null && !end.isBlank()) ed = Date.valueOf(end); } catch (Exception e) {}

        VoucherDAO dao = new VoucherDAO();
        try {
            boolean ok = dao.updateVoucher(id, code, discount, description, sd, ed, status);
            if (ok) response.sendRedirect(request.getContextPath() + "/voucher/detail?id=" + id);
            else { request.setAttribute("error", "Cập nhật thất bại"); doGet(request, response); }
        } catch (SQLException ex) { throw new ServletException(ex); }
    }
}
