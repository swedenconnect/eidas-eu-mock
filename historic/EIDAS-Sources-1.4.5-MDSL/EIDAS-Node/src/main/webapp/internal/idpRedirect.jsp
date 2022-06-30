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
                    <h2><fmt:message key="IdPRedirect.text" bundle="${i18n_eng}"/></h2>
                    <c:if test="${idp_number>0}">
                        <div class="form-group" id="citizenIdpDiv">
                            <label for="idpList"><fmt:message key='IdpChoose.text' bundle="${i18n_eng}"/></label>
                            <select name="idpList" id="idpList" class="form-control">
                                <option data-description="Choose an option"></option>
                                <c:forEach var="idpView" items="${idp_views}">
                                    <option value="${idpView.idpUrl}">${idpView.idpDescription}</option>
                                </c:forEach>
                            </select>
                            <p class="box-btn">
                                <input type="submit" id="submit_idp_choice" class="btn btn-next" value="<fmt:message key='common.next' bundle="${i18n_eng}"/>"/>
                            </p>
                            <script type="text/javascript" src="js/idpRedirect.js"></script>
                        </div>
                    </c:if>
                    <form name="redirectForm" method="${e:forHtml(binding)}" action="${e:forHtml(idpUrl)}">
                        <input type="hidden" id="SAMLRequest" name="SAMLRequest" value="${e:forHtml(samlToken)}"/>
                        <input type="hidden" id="messageFormat" name="messageFormat" value="${e:forHtml(requestScope["request.format"])}"/>
                    </form>
                    <noscript>
                        <form name="redirectFormNoJS" method="${e:forHtml(binding)}" action="${e:forHtml(idpUrl)}">
                            <input type="hidden" id="SAMLRequest_noJS" name="SAMLRequest" value="${e:forHtml(samlToken)}"/>
                            <input type="hidden" id="messageFormat_noJS" name="messageFormat" value="${e:forHtml(requestScope["request.format"])}"/>
                            <p class="box-btn">
                                <input type="submit" id="ConsentValue_button_noJS" class="btn btn-next" value="<fmt:message key='accept.button' bundle="${i18n_eng}"/>"/>
                            </p>
                        </form>
                    </noscript>
                    <jsp:include page="footer-img.jsp"/>
                </div>
            </div>
        </div>
    </div>
</main>
<jsp:include page="footerScripts.jsp"/>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
<c:if test="${empty idp_number}">
    <script type="text/javascript" src="js/redirectOnload.js"></script>
</c:if>
</body>
</html>