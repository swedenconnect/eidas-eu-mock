<%@ page import="eu.eidas.node.security.SecurityResponseHeaderHelper" %>
<%@ page import="eu.eidas.node.security.ExtendedServletResponseWrapper" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="eu.eidas.node.package" var="i18n_eng"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<%@ taglib prefix="token" uri="https://eidas.europa.eu/" %>
<%/* this page is displayed for redirecting error to ServiceProvider*/%>

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
    <jsp:include page="internal/htmlHead.jsp"/>
    <title><fmt:message key="eidas.title" bundle="${i18n_eng}"/></title>
</head>
<body>
<main>

    <div class="wrapper">
        <jsp:include page="internal/centralSliderNoAnim.jsp"/>
        <jsp:include page="internal/leftColumnNoAnim.jsp"/>
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
                    <c:choose>
                        <c:when test="${empty errorRedirectUrl}">
                            <h2><fmt:message key="missing.parameter.text" bundle="${i18n_eng}"/></h2>
                        </c:when>
                        <c:otherwise>
                            <form id="redirectForm" name="redirectForm" method="post" action="${e:forHtml(errorRedirectUrl)}">
                                <input type="hidden" id="errorCode" name="errorCode" value="${e:forHtml(exception.errorCode)}"/>
                                <input type="hidden" id="errorMessage" name="errorMessage" value="${e:forHtml(exception.errorMessage)}"/>
                                <input type="hidden" id="SAMLResponse" name="SAMLResponse" value="${e:forHtml(exception.samlTokenFail)}"/>
                                <c:if test="RelayState!=null">
                                    <input type="hidden" id="relayState" name="RelayState" value="${e:forHtml(RelayState)}"/>
                                </c:if>
                            </form>
                        </c:otherwise>
                    </c:choose>
                    <noscript>
                        <h2><fmt:message key="ConnectorRedirect.text" bundle="${i18n_eng}"/></h2>
                        <br/>
                        <form id="redirectFormNoScript" name="redirectFormNoScript" method="post" action="${e:forHtml(errorRedirectUrl)}">
                            <input type="hidden" id="errorCode" name="errorCode" value="${e:forHtml(exception.errorCode)}"/>
                            <input type="hidden" id="errorMessage" name="errorMessage" value="${e:forHtml(exception.errorMessage)}"/>
                            <input type="hidden" id="SAMLResponse" name="SAMLResponse" value="${e:forHtml(exception.samlTokenFail)}"/>
                            <c:if test="RelayState!=null">
                                <input type="hidden" id="relayState" name="RelayState" value="${e:forHtml(RelayState)}"/>
                            </c:if>
                            <p class="box-btn">
								<input type="submit" id="redirectValue_button" class="btn btn-next" value="<fmt:message key='accept.button' bundle="${i18n_eng}"/>"/>
							</p>
                        </form>
                    </noscript>
                    <h2 class="sub-title text-highlight"><fmt:message key="eidas.cooperation" bundle="${i18n_eng}"/></h2>
                </div>
            </div>
        </div>
    </div>
</main>
<jsp:include page="internal/footerScripts.jsp"/>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
<script type="text/javascript" src="js/redirectOnload.js"></script>
</body>
</html>