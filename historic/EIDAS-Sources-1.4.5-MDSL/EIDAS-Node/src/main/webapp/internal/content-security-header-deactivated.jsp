<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="eu.eidas.node.package" var="i18n_eng"/>
<%/**
 * This message is displayed to the user when the CSP is not active, the content-security-policy directive when supported will indicates the browser to not execute
 * embedded javascript.
 */%>

<c:if test="configurationSecurityBean.isCspFallbackCheck()">
    <h1 id="cspMessage" class="title text-error"></h1>
    <script type="text/javascript" src="js/testCSP.js"></script>
</c:if>