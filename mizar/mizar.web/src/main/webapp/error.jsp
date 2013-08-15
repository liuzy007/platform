<%@page
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.io.*, com.alibaba.common.lang.*"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<%
    Integer   statusCode    = (Integer) request.getAttribute("javax.servlet.error.status_code");
    String    message       = (String) request.getAttribute("javax.servlet.error.message");
    String    uri           = (String) request.getAttribute("javax.servlet.error.request_uri");
    Throwable exception     = (Throwable) request.getAttribute("javax.servlet.error.exception");
    Class     exceptionType = (Class) request.getAttribute("javax.servlet.error.exception_type");
%><head>
    <title><%=statusCode%> <%=message%></title>
  </head>
  <body>
    <h1><%=statusCode%> <%=message%></h1>
    <!--
      <%
         if (exception != null) {
             out.write(StringEscapeUtil.escapeHtml(ExceptionUtil.getStackTrace(exception)));
             out.flush();
         }
      %>
    -->
  </body>
</html>
