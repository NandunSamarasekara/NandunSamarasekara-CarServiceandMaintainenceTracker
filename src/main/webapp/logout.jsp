<%--
  Created by IntelliJ IDEA.
  User: Nandun Samarasekara
  Date: 6/27/2025
  Time: 10:49 PM
  To change this template use File | Settings | File Templates.
--%>
<<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Invalidate the session
    if (session != null) {
        session.invalidate();
    }
    // Redirect to login page
    response.sendRedirect("login.jsp");
%>
