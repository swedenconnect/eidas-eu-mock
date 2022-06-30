<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<?xml version="1.0" encoding="ISO-8859-1" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="true" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="eu.eidas.node.package" var="i18n_eng"/>
<fmt:setBundle basename="errors" var="i18n_error"/>
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>

<%-- ***********************************************************************************
            This page is for handling server/unexpected errors
 *********************************************************************************** --%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv='pragma' content='no-cache'/>
    <meta http-equiv='cache-control' content='no-cache'/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Internal error page</title>
    <link href="resources/css/stylesheet.css" rel="stylesheet" type="text/css"/>
</head>
    <body>
        <div id="container">
            <div id="header">
                <div class="logo"></div>
                <div class="logo_ue"></div>
                <div class="headerTitle">
                    <fmt:message key="eidas.title" bundle="${i18n_eng}"/> this is internalError.jsp
                </div>
            </div>
            <div id="border">
                <div id="principal">
                    <div id="margin">
                        <h2><fmt:message key="unexpected.error" bundle="${i18n_error}"/></h2>

                        <p><fmt:message key="report.error" bundle="${i18n_error}" /></p>

                        <p><fmt:message key="thank.message" bundle="${i18n_error}" /></p>
                        <br />
                        <%
                            Logger LOG = LoggerFactory.getLogger("InternalErrorPage.jsp");
                            boolean HttpStatusLogged = false; // Set to true after loggin of the error
                            if(pageContext != null) {
                                ErrorData errorData=null;
                                try {
                                    errorData = pageContext.getErrorData();
                                } catch(NullPointerException ne) {
                                    // If the error page was accessed directly, a NullPointerException
                                    // is thrown at (PageContext.java:514).
                                    // Catch and ignore it... it effectively means we can't use the ErrorData
                                    LOG.info("ERROR : errorData is null {}", ne.getMessage());
                                    LOG.debug("ERROR : errorData is null {}", ne);
                                }
                                if (errorData!=null){
                                    LOG.info("ERROR : HTTP error {} at {}", errorData.getStatusCode(), errorData.getRequestURI());
                                    LOG.info("ERROR : HTTP error throwable {}", errorData.getThrowable());
                                }
                                HttpStatusLogged =true;
                            }
							if(request.getAttribute("javax.servlet.error.exception") instanceof ServletException)
								getServletContext().getRequestDispatcher("/InternalExceptionHandler").forward(request, response);
                            if(!HttpStatusLogged) { // when the page is directly accessed
                                LOG.info("Direct access to the error page");
                            }
                            if (exception != null){
                                LOG.debug("Exception returned {}", exception);
                            }
                        %>
                        <div id="cspMessage" class="warningCsp"></div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>