package Controller.Customer;

import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "CustomerBlogsServlet", urlPatterns = {"/customer/blogs"})
public class CustomerBlogsServlet extends HttpServlet {
    private static final int PAGE_SIZE = 9;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/blogs");
            return;
        }

        // Sample blogs data
        List<Map<String, String>> allBlogs = new ArrayList<>();
        allBlogs.add(createBlog("Performance", "5 bai khoi dong giup giam chan thuong truoc tran", 
                "Nhung bai tap ngan gon, de nho va phu hop cho moi cap do nguoi choi.",
                "https://images.unsplash.com/photo-1489944440615-453fc2b6a9a9?auto=format&fit=crop&w=1200&q=80"));
        allBlogs.add(createBlog("Teamwork", "3 so do chien thuat hop cho doi phong trao",
                "Toi uu vi tri va cach pressing de doi bong cua ban choi hieu qua hon.",
                "https://images.unsplash.com/photo-1552667466-07770ae110d0?auto=format&fit=crop&w=1200&q=80"));
        allBlogs.add(createBlog("Booking Tips", "Chon khung gio vang de dat san gia tot",
                "Huong dan canh lich, dung voucher va dat som de co gia toi uu.",
                "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?auto=format&fit=crop&w=1200&q=80"));
        allBlogs.add(createBlog("Ky Thuat", "Huong dan tu day truyen cen chu yeu",
                "Tuc chi cho huong dan kham pha phuong phap dua bong hieu qua.",
                "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?auto=format&fit=crop&w=1200&q=80"));
        allBlogs.add(createBlog("Suc Khoe", "Bao ve suc khoe va phong tranh chan thuong",
                "Cac meo cham soc ca the truoc va sau khi choi bong da.",
                "https://images.unsplash.com/photo-1517836357463-d25ddfcb52c8?auto=format&fit=crop&w=1200&q=80"));
        allBlogs.add(createBlog("Thuc Hanh", "Luyen tap phat bieu coa dung ky thuat",
                "Cac bai tap don gian tang nang luc khi thi dau.",
                "https://images.unsplash.com/photo-1480029528100-0e835f9f2f34?auto=format&fit=crop&w=1200&q=80"));

        // Pagination
        int pageNum = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isBlank()) {
            try {
                pageNum = Integer.parseInt(pageParam);
                if (pageNum < 1) pageNum = 1;
            } catch (NumberFormatException e) {
                pageNum = 1;
            }
        }

        int totalItems = allBlogs.size();
        int totalPages = (totalItems + PAGE_SIZE - 1) / PAGE_SIZE;
        if (pageNum > totalPages && totalPages > 0) pageNum = totalPages;

        int startIdx = (pageNum - 1) * PAGE_SIZE;
        int endIdx = Math.min(startIdx + PAGE_SIZE, totalItems);
        List<Map<String, String>> pageBlogs = new ArrayList<>(allBlogs.subList(startIdx, endIdx));

        request.setAttribute("blogs", pageBlogs);
        request.setAttribute("currentPage", pageNum);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", totalItems);
        request.getRequestDispatcher("/View/Customer/blogs.jsp").forward(request, response);
    }

    private Map<String, String> createBlog(String category, String title, String description, String imageUrl) {
        Map<String, String> blog = new HashMap<>();
        blog.put("category", category);
        blog.put("title", title);
        blog.put("description", description);
        blog.put("imageUrl", imageUrl);
        return blog;
    }
}
