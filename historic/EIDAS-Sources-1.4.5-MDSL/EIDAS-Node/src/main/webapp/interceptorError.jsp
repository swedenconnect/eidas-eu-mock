<%@ page import="eu.eidas.node.security.SecurityResponseHeaderHelper" %>
<%@ page import="eu.eidas.node.security.ExtendedServletResponseWrapper" %>
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
    This page is for handling Node errors with error message defined in properties files
 *********************************************************************************** --%>
<html>

<%
    /*  Check if the servlet has removed CSP headers added by filter before */
    if (!(response instanceof ExtendedServletResponseWrapper)
            || !((ExtendedServletResponseWrapper) response).hasCSPHeaders()) {
        /* if the response was rewritten or no CSP flags, place them back if needed */
        new SecurityResponseHeaderHelper().populateResponseHeader(request, response);
    }
%>

<head>
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
                    <div class="col-sm-6 one-column">
                        <ul class="list-unstyled check">
                            <li>${e:forHtml(exception.errorMessage)}</li>
                        </ul>
                    </div>
                    <div id="cspMessage" class="warningCsp"></div>
                    <h2 class="sub-title text-highlight"><fmt:message key="thank.message" bundle="${i18n_error}" /></h2>
                    <jsp:include page="internal/footer-img.jsp"/>
                </div>
            </div>
        </div>
    </div>
</main>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
</body>
</html>