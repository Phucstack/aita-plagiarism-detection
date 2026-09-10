<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // Trang chuyển hướng dự phòng gốc về Dashboard
    response.sendRedirect(request.getContextPath() + "/dashboard");
%>
