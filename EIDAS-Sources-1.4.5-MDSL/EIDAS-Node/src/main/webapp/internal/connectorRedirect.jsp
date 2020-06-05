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
                    <h2 class="sub-title"><fmt:message key="eidas.redirecting" bundle="${i18n_eng}"/></h2>
                    <form id="ColleagueResponse" name="redirectForm" action="${e:forHtml(redirectUrl)}" method="post">
                        <c:if test="${!empty samlTokenFail}" >
                            <input type="hidden" name="SAMLResponse" value="${e:forHtml(samlTokenFail)}" id="ColleagueResponse_SAMLResponse"/>
                        </c:if>
                        <c:if test="${empty samlTokenFail}" >
                            <input type="hidden" name="SAMLResponse" value="${e:forHtml(samlToken)}" id="ColleagueResponse_SAMLResponse"/>
                        </c:if>
                        <input type="hidden" name="RelayState" value="${e:forHtml(RelayState)}" id="relayState"/>
                    </form>
                    <noscript>
                        <h2><fmt:message key="APRedirect.text" bundle="${i18n_eng}"/></h2>
                        <br/>
                        <form id="ColleagueResponse_NoJs" name="redirectForm" action="${e:forHtml(redirectUrl)}" method="post">
                            <c:if test="${not empty samlTokenFail}" >a
                                <input type="hidden" name="SAMLResponse" value="${e:forHtml(samlTokenFail)}" id="ColleagueResponse_SAMLResponse"/>
                            </c:if>
                            <c:if test="${empty samlTokenFail}" >b
                                <input type="hidden" name="SAMLResponse" value="${e:forHtml(samlToken)}" id="ColleagueResponse_SAMLResponse"/>
                            </c:if>

                            <fmt:message var="btnMsg" key="accept.button" bundle="${i18n_eng}"/>
                            <input type="submit" id="ColleagueResponse_0" class="btn btn-next" value="${btnMsg}"/>
                            <input type="hidden" name="RelayState" value="${e:forHtml(RelayState)}" id="relayState"/>
                        </form>
                    </noscript>
                </div>
            </div>
        </div>
    </div>
</main>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
<script type="text/javascript" src="js/redirectOnload.js"></script>
<jsp:include page="footerScripts.jsp"/>
</body>
</html>