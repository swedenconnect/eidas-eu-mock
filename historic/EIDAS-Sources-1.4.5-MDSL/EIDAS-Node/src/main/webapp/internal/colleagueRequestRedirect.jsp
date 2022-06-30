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
                <div class="col-right-content">
                    <jsp:include page="content-security-header-deactivated.jsp"/>
                    <h1 class="title">
                        <span><fmt:message key="eidas.title" bundle="${i18n_eng}"/></span>
                    </h1>

                    <h2><fmt:message key="colleagueRequestRedirect.text" bundle="${i18n_eng}"/></h2>

                    <form name="redirectForm" method="${e:forHtml(binding)}" action="${e:forHtml(serviceUrl)}">
                        <input type="hidden" id="SAMLRequest" name="SAMLRequest" value="${e:forHtml(SAMLRequest)}"/>
                        <input type="hidden" id="relayState" name="RelayState" value="${e:forHtml(RelayState)}"/>
                        <token:token/>
                    </form>
                    <noscript>
                        <form id="redirectFormNoJs" name="redirectFormNoJs" method="post" action="${e:forHtml(serviceUrl)}">
                            <input type="hidden" id="consentValue_SAMLRequest" name="SAMLRequest" value="${e:forHtml(SAMLRequest)}"/>
                            <input type="hidden" id="consentValue_relayState" name="RelayState" value="${e:forHtml(RelayState)}"/>
                            <p class="box-btn">
                                <input type="submit" id="ConsentValue_button" class="btn btn-next" value="<fmt:message key='accept.button' bundle="${i18n_eng}"/>"/>
                            </p>
                        </form>
                    </noscript>
                    <jsp:include page="footer-img.jsp"/>
                </div>
            </div>
        </div>
    </div>
</main>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
<script type="text/javascript" src="js/redirectOnload.js"></script>
</body>
</html>
