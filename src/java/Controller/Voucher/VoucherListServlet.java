package Controller.Voucher;

import DAO.VoucherDAO;
import Models.Voucher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/voucher/list")
public class VoucherListServlet extends HttpServlet {

    private final VoucherDAO voucherDAO = new VoucherDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Voucher> vouchers = voucherDAO.getAllVouchers();
            request.setAttribute("vouchers", vouchers);
            request.setAttribute("locationId", "");
            request.getRequestDispatcher("/View/Voucher/location-voucher-list.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải danh sách voucher: " + e.getMessage());
            request.setAttribute("locationId", "");
            request.getRequestDispatcher("/View/Voucher/location-voucher-list.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
