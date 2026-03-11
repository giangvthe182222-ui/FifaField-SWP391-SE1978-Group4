<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Models.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    String target = request.getContextPath() + "/login";

    if (currentUser != null && currentUser.getRole() != null && currentUser.getRole().getRoleName() != null) {
        String role = currentUser.getRole().getRoleName().toLowerCase();
        if ("admin".equals(role)) {
            target = request.getContextPath() + "/admin-dashboard";
        } else if ("manager".equals(role)) {
            target = request.getContextPath() + "/manager/dashboard";
        } else if ("staff".equals(role)) {
            target = request.getContextPath() + "/View/Staff/StaffList.jsp";
        } else if ("customer".equals(role)) {
            target = request.getContextPath() + "/customer/dashboard";
        }
    }

    response.sendRedirect(target);
%>
