<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="eu.eidas.node.package" var="i18n_eng"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<%@ taglib prefix="token" uri="https://eidas.europa.eu/" %>
<html>
<head>
    <jsp:include page="htmlHead.jsp"/>
    <title><fmt:message key="eidas.title" bundle="${i18n_eng}"/></title>
</head>
<body>
<main>
    <div class="wrapper">
        <jsp:include page="centralSliderNoAnim.jsp"/>
        <jsp:include page="leftColumnNoAnim.jsp"/>
        <div class="col-right">
            <div class="col-right-inner">
                <div class="clearfix">
                    <div class="menu-top"> <a class="item text-minus" href="#"></a> <a class="item text-plus" href="#"></a> <a class="item contrast" href="#"></a> </div>
                </div>
                <div class="col-right-content">
                    <jsp:include page="content-security-header-deactivated.jsp"/>
                    <h1 class="title">
                        <span><fmt:message key="eidas.title" bundle="${i18n_eng}"/></span>
                    </h1>
                    <h2 class="sub-title">Redirecting ...</h2>
                    <form name="redirectForm" action="${e:forHtml(sigCreatorModuleURL)}" method="get">
                        <input type="hidden" name="DataURL" value="${e:forHtml(DataURL)}" id="http-security-layer-request_DataURL"/>
                        <input type="hidden" name="XMLRequest" value="${e:forHtml(data)}" id="http-security-layer-request_XMLRequest"/>
                        <input type="hidden" name="appletBackgroundColor" value="#FFFFFF" id="http-security-layer-request_appletBackgroundColor"/>
                        <input type="hidden" name="appletHeight" value="250" id="http-security-layer-request_appletHeight"/>
                        <input type="hidden" name="appletWidth" value="300" id="http-security-layer-request_appletWidth"/>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>
<jsp:include page="footerScripts.jsp"/>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
<script type="text/javascript" src="js/redirectOnload.js"></script>
</body>
</html>