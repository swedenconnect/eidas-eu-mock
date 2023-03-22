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
<fmt:setBundle basename="eu.eidas.node.package" var="i18n_eng"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>

<html>
    <head>
        <jsp:include page="/internal/htmlHead.jsp"/>
        <title></title>
    </head>
    <body>
        <main>
            <div class="wrapper">
                <jsp:include page="/internal/centralSliderNoAnim.jsp"/>
                <jsp:include page="/internal/leftColumn.jsp"/>
                <div class="col-right">
                    <div class="col-right-inner">
                        <div class="col-right-content">
                            <jsp:include page="/internal/content-security-header-deactivated.jsp"/>
                            <fmt:message var="redirectingValue" key="msSpecificProxyServiceRedirecting.text"
                                         bundle="${i18n_eng}"/>
                            <input type="hidden" id="redirectingMessageId" value="${e:forHtml(redirectingValue)}"/>
                            <h2 id="tokenRedirectLabel" class="sub-title"></h2>
                            <form id="redirectForm" name="redirectForm" action="${e:forHtml(redirectUrl)}" method="${e:forHtml(binding)}">
                                <input type="hidden" name="token" id="token" value="${e:forHtml(token)}"/>
                            </form>
                            <noscript>
                                <h2 class="sub-title"><fmt:message key="msSpecificProxyServiceRedirect.text" bundle="${i18n_eng}"/></h2>
                                <form id="noJavaScriptForm" name="noJavaScriptRedirectForm" action="${e:forHtml(redirectUrl)}" method="${e:forHtml(binding)}">
                                    <input type="hidden" name="token" id="noJavaScriptToken" value="${e:forHtml(token)}"/>
                                    <fmt:message var="btnMsg" key="accept.button" bundle="${i18n_eng}"/>
                                    <input type="submit" id="submitButton1" class="btn btn-next" value="${e:forHtml(btnMsg)}"/>
                                </form>
                            </noscript>
                        </div>
                    </div>
                </div>
            </div>
        </main>
        <script type="text/javascript" src="js/autocompleteOff.js"></script>
        <script type="text/javascript" src="js/redirectOnload.js"></script>
    </body>
</html>