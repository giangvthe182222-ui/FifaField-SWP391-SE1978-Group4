package Controller.Auth;

import DAO.AuthDAO;
import DAO.GoogleAuthDAO;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class GoogleCallbackServlet extends HttpServlet {

    private static final String CLIENT_ID = "740421080506-eprsofumm8uoc5dasvbde1v8l8sq4hd2.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8080/FifaField/oauth2/callback";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String code = req.getParameter("code");
        String state = req.getParameter("state");
        String savedState = (String) req.getSession().getAttribute("oauth_state");

        if (code == null || state == null || savedState == null || !state.equals(savedState)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            // doi code lay access_token
            String tokenJson = postForm("https://oauth2.googleapis.com/token",
                    "code=" + enc(code)
                    + "&client_id=" + enc(CLIENT_ID)
                    + "&client_secret=" + enc(CLIENT_SECRET)
                    + "&redirect_uri=" + enc(REDIRECT_URI)
                    + "&grant_type=authorization_code");

            String accessToken = JsonMini.get(tokenJson, "access_token"); // helper o duoi
            if (accessToken == null) {
                throw new RuntimeException("No access_token");
            }

            // goi userinfo lay profile
            String userInfo = getJson("https://openidconnect.googleapis.com/v1/userinfo",
                    "Authorization", "Bearer " + accessToken);

            String sub = JsonMini.get(userInfo, "sub");
            String email = JsonMini.get(userInfo, "email");
            String name = JsonMini.get(userInfo, "name");

            // CHAN: neu email da ton tai thi khong cho login Google
            AuthDAO authDao = new AuthDAO();
            if (authDao.emailExists(email.trim())) {
                HttpSession s = req.getSession(true);
                s.setAttribute("flash_error", "Email đã tồn tại. Vui lòng đăng nhập bằng mật khẩu.");
                req.getSession(true).setAttribute("flash_error",
                        "Email đã tồn tại. Vui lòng đăng nhập bằng mật khẩu.");
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            if (sub == null || email == null) {
                throw new RuntimeException("Missing sub/email");
            }

            // upsert user vao DB (tu tao neu chua co)
            GoogleAuthDAO dao = new GoogleAuthDAO();
            String userId = dao.findOrCreateUserByGoogle(sub, email, name);

            req.getSession(true).setAttribute("userId", userId);
            resp.sendRedirect(req.getContextPath() + "/View/Auth/homepage.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    private String postForm(String url, String body) throws Exception {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            os.write(bytes);
        }
        try (InputStream is = con.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String getJson(String url, String headerKey, String headerVal) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestProperty(headerKey, headerVal);
        try (InputStream is = con.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    // parse JSON. Sau nay nang cap Jackson/Gson.
    static class JsonMini {

        static String get(String json, String key) {
            String pattern = "\"" + key + "\"";
            int i = json.indexOf(pattern);
            if (i < 0) {
                return null;
            }
            int colon = json.indexOf(":", i);
            if (colon < 0) {
                return null;
            }
            int start = json.indexOf("\"", colon + 1);
            if (start < 0) {
                return null;
            }
            int end = json.indexOf("\"", start + 1);
            if (end < 0) {
                return null;
            }
            return json.substring(start + 1, end);
        }
    }
}
