package Controller.Customer;

import DAO.FeedbackDAO;
import DAO.FieldDAO;
import Models.Field;
import Models.FieldFeedbackViewModel;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "CustomerFieldDetailServlet", urlPatterns = {"/customer/field-detail"})
public class CustomerFieldDetailServlet extends HttpServlet {
    private final FieldDAO fieldDAO = new FieldDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/dashboard");
            return;
        }

        String id = request.getParameter("fieldId");
        if (id == null || id.isBlank()) {
            id = request.getParameter("field_id");
        }
        if (id == null || id.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard");
            return;
        }

        try {
            UUID fieldId = UUID.fromString(id);
            Field field = fieldDAO.getById(fieldId);
            if (field == null) {
                response.sendRedirect(request.getContextPath() + "/customer/dashboard");
                return;
            }

            Double averageRating = feedbackDAO.getAverageRatingByField(fieldId);
            int feedbackCount = feedbackDAO.getFeedbackCountByField(fieldId);
            List<FieldFeedbackViewModel> feedbacks = feedbackDAO.getFeedbacksByField(fieldId);

            request.setAttribute("field", field);
            request.setAttribute("averageRating", averageRating);
            request.setAttribute("feedbackCount", feedbackCount);
            request.setAttribute("feedbacks", feedbacks);
            request.setAttribute("currentUserId", user.getUserId().toString());
            request.getRequestDispatcher("/View/Customer/field-detail.jsp").forward(request, response);
        } catch (Exception ex) {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard");
        }
    }
}