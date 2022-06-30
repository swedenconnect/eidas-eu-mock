<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="eu.eidas.node.security.ExtendedServletResponseWrapper" %>
<%@ page import="eu.eidas.node.security.SecurityResponseHeaderHelper" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="eu.eidas.node.package" var="i18n_eng"/>
<fmt:setBundle basename="errors" var="i18n_error"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<%@ taglib prefix="token" uri="https://eidas.europa.eu/" %>

<%-- ***********************************************************************************
            This page is for handling server/unexpected errors
 *********************************************************************************** --%>

<%
    /*  Check if the servlet has removed CSP headers added by filter before */
    if (!(response instanceof ExtendedServletResponseWrapper)
            || !((ExtendedServletResponseWrapper) response).hasCSPHeaders()) {
        /* if the response was rewritten or no CSP flags, place them back if needed */
        new SecurityResponseHeaderHelper().populateResponseHeader(request, response);
    }
%>

<html>

<head>
    <base href="${pageContext.request.contextPath}/"/>
    <jsp:include page="internal/htmlHead.jsp"/>
    <title><fmt:message key="eidas.title" bundle="${i18n_eng}"/></title>
</head>
<body>
<main>
    <div class="wrapper">
        <jsp:include page="internal/centralSlider.jsp"/>
        <jsp:include page="internal/leftColumn.jsp"/>
        <div class="col-right">
            <div class="col-right-inner">
                <div class="clearfix">
                    <div class="menu-top"> <a class="item text-minus" href="#"></a> <a class="item text-plus" href="#"></a> <a class="item contrast" href="#"></a> </div>
                </div>
                <div class="col-right-content">
                    <jsp:include page="internal/content-security-header-deactivated.jsp"/>
                    <h1 class="title">
                        <span><fmt:message key="eidas.title" bundle="${i18n_eng}"/></span>
                    </h1>
                    <h2 class="sub-title">
                        <span><fmt:message key="unexpected.error" bundle="${i18n_error}"/></span>
                    </h2>
                    <h3><fmt:message key="report.error" bundle="${i18n_error}" /></h3>
                    <h3><span><fmt:message key="thank.message" bundle="${i18n_error}" /></span></h3>
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
                                LOG.info("errorData is null {}", ne.getMessage());
                                LOG.debug("errorData is null {}", ne);
                            }
                            if (errorData!=null){
                                LOG.info("HTTP error {} at {}", errorData.getStatusCode(), errorData.getRequestURI());
                                LOG.info("HTTP error throwable {}", errorData.getThrowable());
                            }
                            HttpStatusLogged =true;
                        }
                        if(!HttpStatusLogged) { // when the page is directly accessed
                            LOG.info("Direct access to the error page");
                        }
                    %>
                    <div id="cspMessage" class="warningCsp"></div>
                    <jsp:include page="internal/footer-img.jsp"/>
                 </div>
            </div>
        </div>
    </div>
</main>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
</body>
</html>