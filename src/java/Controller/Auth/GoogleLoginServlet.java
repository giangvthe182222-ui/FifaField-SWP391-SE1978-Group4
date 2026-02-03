package Controller.Auth;

import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleLoginServlet extends HttpServlet {

    // TODO: thay bang CLIENT_ID cua ban 
    private static final String CLIENT_ID = "740421080506-eprsofumm8uoc5dasvbde1v8l8sq4hd2.apps.googleusercontent.com";
    private static final String REDIRECT_URI = "http://localhost:8080/FifaField/oauth2/callback";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String state = java.util.UUID.randomUUID().toString(); 
        req.getSession(true).setAttribute("oauth_state", state);

        String scope = "openid email profile";

        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + enc(CLIENT_ID)
                + "&redirect_uri=" + enc(REDIRECT_URI)
                + "&response_type=code"
                + "&scope=" + enc(scope)
                + "&state=" + enc(state)
                + "&access_type=online"
                + "&prompt=select_account";

        resp.sendRedirect(url);
    }

    private String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
