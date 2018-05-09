<%--
  ~ Copyright (c) 2017 by European Commission
  ~
  ~ Licensed under the EUPL, Version 1.2 or - as soon they will be
  ~ approved by the European Commission - subsequent versions of the
  ~ EUPL (the "Licence");
  ~ You may not use this work except in compliance with the Licence.
  ~ You may obtain a copy of the Licence at:
  ~ https://joinup.ec.europa.eu/page/eupl-text-11-12
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the Licence is distributed on an "AS IS" basis,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  ~ implied.
  ~ See the Licence for the specific language governing permissions and
  ~ limitations under the Licence.
  --%>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="eu.eidas.specific.connector.package" var="i18n_eng"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<%@ page errorPage="ErrorPage.jsp" %>

<html>

<head>
    <jsp:include page="include/htmlHeadSpecific.jsp"/>
</head>
<body>
<main>
    <div class="wrapper">
        <jsp:include page="include/centralSliderNoAnim.jsp"/>
        <jsp:include page="include/leftColumn.jsp"/>
        <div class="col-right">
            <div class="col-right-inner">
                <div class="col-right-content">
                    <jsp:include page="include/content-security-header-deactivated.jsp"/>
                    <h2 class="sub-title"><fmt:message key="spRedirecting.text" bundle="${i18n_eng}"/></h2>
                    <form name="redirectForm" method="post" action="${e:forHtml(spUrl)}">
                        <input type="hidden" id="SMSSPResponse" name="SMSSPResponse" value="${e:forHtml(smsspToken)}"/>
                        <c:if test="${not empty relayState}">
                            <input type="hidden" id="relayState" name="RelayState" value="${e:forHtml(relayState)}"/>
                        </c:if>
                    </form>
                    <noscript>
                        <h2 class="sub-title"><fmt:message key="spRedirect.text" bundle="${i18n_eng}"/></h2>
                        <form name="redirectForm" method="post" action="${e:forHtml(spUrl)}">
                            <input type="hidden" id="SMSSPResponseNoJS" name="SMSSPResponse" value="${e:forHtml(smsspToken)}"/>
                            <c:if test="${not empty relayState}">
                                <input type="hidden" id="relayState" name="RelayState" value="${e:forHtml(relayState)}"/>
                            </c:if>
                            <p class="box-btn">
                                <input type="submit" id="redirectValue_button" class="btn btn-next" value="<fmt:message key='accept.button' bundle="${i18n_eng}"/>"/>
                            </p>
                        </form>
                    </noscript>
                    <jsp:include page="include/footer-img.jsp"/>
                </div>
            </div>
        </div>
    </div>

</main>
<jsp:include page="include/footerScripts.jsp"/>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
<script type="text/javascript" src="js/redirectOnload.js"></script>
</body>
</html>