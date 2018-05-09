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
<fmt:setBundle basename="eu.eidas.specific.proxyservice.package" var="i18n_eng"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>

<html>

<head>
    <jsp:include page="/include/htmlHeadSpecific.jsp"/>
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
                    <h2><fmt:message key="idpRedirecting.text" bundle="${i18n_eng}"/></h2>
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
                        <input type="hidden" id="SMSSPRequest" name="SMSSPRequest" value="${e:forHtml(smsspToken)}"/>
                    </form>
                    <noscript>
                        <h2><fmt:message key="idpRedirect.text" bundle="${i18n_eng}"/></h2>
                        <form name="redirectFormNoJS" method="${e:forHtml(binding)}" action="${e:forHtml(idpUrl)}">
                            <input type="hidden" id="SMSSPRequest_noJS" name="SMSSPRequest" value="${e:forHtml(smsspToken)}"/>
                            <p class="box-btn">
                                <input type="submit" id="ConsentValue_button_noJS" class="btn btn-next" value="<fmt:message key='accept.button' bundle="${i18n_eng}"/>"/>
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
<c:if test="${empty idp_number}">
    <script type="text/javascript" src="js/redirectOnload.js"></script>
</c:if>
</body>
</html>