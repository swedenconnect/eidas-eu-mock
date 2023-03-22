<%-- 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
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
    <jsp:include page="include/htmlHeadProxyServiceSpecific.jsp"/>
    <title><fmt:message key="consent.page.title" bundle="${i18n_eng}"/></title>
</head>

<body>
<main>
    <div class="wrapper">
        <jsp:include page="include/proxyServiceCentralSlider.jsp"/>
        <jsp:include page="include/proxyServiceLeftColumn.jsp"/>
        <div class="col-right">
            <div class="col-right-inner">
                <div class="clearfix">
                    <div class="menu-top">
                        <a class="item text-minus" href="#"></a>
                        <a class="item text-plus" href="#"></a>
                        <a class="item contrast" href="#"></a>
                    </div>
                </div>
                <div class="col-right-content">
                    <jsp:include page="include/proxyService-content-security-header-deactivated.jsp"/>
                    <form id="consentSelector" name="consentSelector" method="${e:forHtml(binding)}"
                          action="${e:forHtml(citizenConsentUrl)}" class="jsOK" disabled="true">
                        <input type="hidden" id="requestId" name="requestId"
                               value="<c:out value='${e:forHtml(requestId)}'/>"/>
                        <% /** Slider 1 */ %>
                        <div id="slider1">
                            <jsp:include page="include/proxyServiceTitleWithAssurance.jsp"/>
                            <p class="step-status"><fmt:message key="common.step" bundle="${i18n_eng}"/><span>1</span> |
                                3</p>
                            <h2 class="sub-title"><fmt:message key="presentConsent.basicInformation"
                                                               bundle="${i18n_eng}"/></h2>
                            <div class="row"><% /** Mandatory attributes are here */ %>
                                <div class="col-sm-6"><% /** Natural person */ %>
                                    <c:set var="categoryIsDisplayed" value="false"/>
                                    <c:forEach items="${attrList}" var="attrItem">
                                    <c:if test="${attrItem.key.required && attrItem.key.personType.value=='NaturalPerson'}">
                                    <c:if test="${categoryIsDisplayed=='false'}">
                                    <h3><fmt:message key="presentConsent.natural" bundle="${i18n_eng}"/>
                                        <span><fmt:message key="presentConsent.person" bundle="${i18n_eng}"/></span>
                                    </h3>
                                    <c:set var="categoryIsDisplayed" value="true"/>
                                    <ul class="list-unstyled check">
                                        </c:if>
                                        <li>
                                            <fmt:message var="displayAttr"
                                                         key="${attrItem.key.nameUri}.display"
                                                         bundle="${i18n_eng}"/>
                                            <c:if test="${!fn:startsWith(fn:toLowerCase(displayAttr), 'false')}">
                                                <fmt:message key="${attrItem.key.friendlyName}"
                                                             bundle="${i18n_eng}"/>
                                                <fmt:message key="${attrItem.key.friendlyName}.Description"
                                                             bundle="${i18n_eng}" var="tmpDesc"/>
                                                <c:set var="tmpEmptyValue"
                                                       value="???${attrItem.key.friendlyName}.Description???"/>
                                                <c:if test="${tmpEmptyValue ne tmpDesc}">
                                                            <span>
                                                                <button type="button" class="btn btn-info"
                                                                        data-toggle="modal"
                                                                        data-target="#attributeModal"
                                                                        data-attribute-name="<fmt:message key="${attrItem.key.friendlyName}" bundle="${i18n_eng}"/>"
                                                                        data-attribute-desc="${tmpDesc}">?
                                                                </button>
                                                            </span>
                                                </c:if>
                                            </c:if>
                                            <input id="consentSelector_${attrItem.key.friendlyName}"
                                                   type="hidden" name="${attrItem.key.nameUri}"
                                                   value="${attrItem.key.nameUri}"/>
                                        </li>
                                        </c:if>
                                        </c:forEach>
                                        <c:if test="${categoryIsDisplayed=='true'}"></ul>
                                    </c:if>
                                </div>
                                <div class="col-sm-6"><% /** Legal person */ %>
                                    <c:set var="categoryIsDisplayed" value="false"/>
                                    <c:forEach items="${attrList}" var="attrItem">
                                    <c:if test="${attrItem.key.required && attrItem.key.personType.value == 'LegalPerson'}">
                                    <c:if test="${categoryIsDisplayed=='false'}">
                                    <h3><fmt:message key="presentConsent.legal" bundle="${i18n_eng}"/>
                                        <span><fmt:message key="presentConsent.person" bundle="${i18n_eng}"/></span>
                                    </h3>
                                    <c:set var="categoryIsDisplayed" value="true"/>
                                    <ul class="list-unstyled check">
                                        </c:if>
                                        <li>
                                            <fmt:message var="displayAttr"
                                                         key="${attrItem.key.nameUri}.display"
                                                         bundle="${i18n_eng}"/>
                                            <c:if test="${!fn:startsWith(fn:toLowerCase(displayAttr), 'false')}">
                                                <fmt:message key="${attrItem.key.friendlyName}"
                                                             bundle="${i18n_eng}"/>
                                                <%--<label class="checkboxLabel" for="consentSelector_${attrItem.key.nameUri}"><fmt:message key="${attrItem.key.nameUri}" bundle="${i18n_eng}"/></label>--%>
                                                <input id="consentSelector_${attrItem.key.friendlyName}"
                                                       type="hidden" name="${attrItem.key.nameUri}"
                                                       value="${attrItem.key.nameUri}"/>
                                                <fmt:message key="${attrItem.key.friendlyName}.Description"
                                                             bundle="${i18n_eng}" var="tmpDesc"/>
                                                <c:set var="tmpEmptyValue"
                                                       value="???${attrItem.key.friendlyName}.Description???"/>
                                                <c:if test="${tmpEmptyValue ne tmpDesc}">
                                                            <span>
                                                                <button type="button" class="btn btn-info"
                                                                        data-toggle="modal"
                                                                        data-target="#attributeModal"
                                                                        data-attribute-name="<fmt:message key="${attrItem.key.friendlyName}" bundle="${i18n_eng}"/>"
                                                                        data-attribute-desc="${tmpDesc}">?
                                                                </button>
                                                            </span>
                                                </c:if>
                                            </c:if>
                                        </li>
                                        </c:if>
                                        </c:forEach>
                                        <c:if test="${categoryIsDisplayed=='true'}"></ul>
                                    </c:if>
                                </div>
                            </div>
                            <p class="box-btn">
                                <button type="button" class="btn btn-opposite" id="buttonCancelSlide1"
                                        name="buttonCancelSlide1"><span><fmt:message key="common.cancel"
                                                                                     bundle="${i18n_eng}"/></span>
                                </button>
                                <button type="button" class="btn btn-next" id="buttonNextSlide1"
                                        name="buttonNextSlide1"><span><fmt:message key="common.next"
                                                                                   bundle="${i18n_eng}"/></span>
                                </button>
                            </p>
                            <jsp:include page="include/proxyServiceFooter-img.jsp"/>
                        </div>
                        <% /** Slider 2 */ %>
                        <div id="slider2">
                            <jsp:include page="include/proxyServiceTitleWithAssurance.jsp"/>
                            <p class="step-status"><fmt:message key="common.step" bundle="${i18n_eng}"/> <span>2</span>
                                | 3</p>
                            <h2 class="sub-title"><fmt:message key="presentConsent.additionalInformation"
                                                               bundle="${i18n_eng}"/></h2>
                            <div class="row"><% /** optional  attributes are here */ %>
                                <div class="col-sm-6"><% /** Natural person */ %>
                                    <c:set var="categoryIsDisplayed" value="false"/>
                                    <c:forEach items="${attrList}" var="attrItem">
                                    <c:if test="${!attrItem.key.required && attrItem.key.personType.value=='NaturalPerson'}">
                                    <c:if test="${categoryIsDisplayed=='false'}">
                                    <h3><fmt:message key="presentConsent.natural" bundle="${i18n_eng}"/>
                                        <span><fmt:message key="presentConsent.person" bundle="${i18n_eng}"/></span>
                                    </h3>
                                    <c:set var="categoryIsDisplayed" value="true"/>
                                    <ul class="toogle-switch list-unstyled">
                                        </c:if>
                                        <li>
                                            <c:if test="${fn:startsWith(fn:toLowerCase(displayAttr), 'false')}">
                                                <input id="consentSelector_${attrItem.key.friendlyName}"
                                                       type="hidden" name="${attrItem.key.nameUri}"
                                                       value="${attrItem.key.nameUri}"/>
                                            </c:if>
                                            <c:if test="${!fn:startsWith(fn:toLowerCase(displayAttr), 'false')}">
                                                <p><fmt:message key="${attrItem.key.friendlyName}"
                                                                bundle="${i18n_eng}"/></p>
                                                <fmt:message key="${attrItem.key.friendlyName}.Description"
                                                             bundle="${i18n_eng}" var="tmpDesc"/>
                                                <c:set var="tmpEmptyValue"
                                                       value="???${attrItem.key.friendlyName}.Description???"/>
                                                <c:if test="${tmpEmptyValue ne tmpDesc}">
                                                            <span>
                                                                <button type="button" class="btn btn-info"
                                                                        data-toggle="modal"
                                                                        data-target="#attributeModal"
                                                                        data-attribute-name="<fmt:message key="${attrItem.key.friendlyName}" bundle="${i18n_eng}"/>"
                                                                        data-attribute-desc="${tmpDesc}">?
                                                                </button>
                                                            </span>
                                                </c:if>
                                                <input class="js-switch"
                                                       id="consentSelector_${attrItem.key.friendlyName}"
                                                       type="checkbox" name="${attrItem.key.nameUri}"
                                                       value="true"/>
                                                <input id="__checkbox_consentSelector_${attrItem.key.friendlyName}"
                                                       type="hidden" name="__checkbox_${attrItem.key.nameUri}"
                                                       value="true"/>
                                            </c:if>
                                        </li>
                                        </c:if>
                                        </c:forEach>
                                        <c:if test="${categoryIsDisplayed=='true'}"></ul>
                                    </c:if>
                                </div>
                                <div class="col-sm-6"><% /** Legal person */ %>
                                    <c:set var="categoryIsDisplayed" value="false"/>
                                    <c:forEach items="${attrList}" var="attrItem">
                                    <c:if test="${!attrItem.key.required && attrItem.key.personType.value=='LegalPerson'}">
                                    <c:if test="${categoryIsDisplayed=='false'}">
                                    <h3><fmt:message key="presentConsent.legal" bundle="${i18n_eng}"/>
                                        <span><fmt:message key="presentConsent.person" bundle="${i18n_eng}"/></span>
                                    </h3>
                                    <c:set var="categoryIsDisplayed" value="true"/>
                                    <ul class="toogle-switch list-unstyled">
                                        </c:if>
                                        <li>
                                            <c:if test="${fn:startsWith(fn:toLowerCase(displayAttr), 'false')}">
                                                <input id="consentSelector_${attrItem.key.friendlyName}"
                                                       type="hidden" name="${attrItem.key.nameUri}"
                                                       value="${attrItem.key.nameUri}"/>
                                            </c:if>
                                            <c:if test="${!fn:startsWith(fn:toLowerCase(displayAttr), 'false')}">
                                                <p><fmt:message key="${attrItem.key.friendlyName}"
                                                                bundle="${i18n_eng}"/></p>
                                                <fmt:message key="${attrItem.key.friendlyName}.Description"
                                                             bundle="${i18n_eng}" var="tmpDesc"/>
                                                <c:set var="tmpEmptyValue"
                                                       value="???${attrItem.key.friendlyName}.Description???"/>
                                                <c:if test="${tmpEmptyValue ne tmpDesc}">
                                                            <span>
                                                                <button type="button" class="btn btn-info"
                                                                        data-toggle="modal"
                                                                        data-target="#attributeModal"
                                                                        data-attribute-name="<fmt:message key="${attrItem.key.friendlyName}" bundle="${i18n_eng}"/>"
                                                                        data-attribute-desc="${tmpDesc}">?
                                                                </button>
                                                            </span>
                                                </c:if>
                                                <input class="js-switch"
                                                       id="consentSelector_${attrItem.key.friendlyName}"
                                                       type="checkbox" name="${attrItem.key.nameUri}"
                                                       value="true"/>
                                                <input id="__checkbox_consentSelector_${attrItem.key.friendlyName}"
                                                       type="hidden" name="__checkbox_${attrItem.key.nameUri}"
                                                       value="true"/>
                                            </c:if>
                                        </li>
                                        </c:if>
                                        </c:forEach>
                                        <c:if test="${categoryIsDisplayed=='true'}"></ul>
                                    </c:if>
                                </div>
                            </div>
                            <div id="checkbox_Confirmation_div" class="checkbox checkbox-custom">
                                <p class="information-message"><fmt:message key="presentConsent.deniedConfirmation"
                                                                            bundle="${i18n_eng}"/></p>
                            </div>
                            <p class="box-btn">
                                <button type="button" class="btn btn-opposite" id="buttonCancelSlide2"
                                        name="buttonCancelSlide2"><span><fmt:message key="common.cancel"
                                                                                     bundle="${i18n_eng}"/></span>
                                </button>
                                <button type="button" class="btn btn-back" id="buttonBackSlide2"
                                        name="buttonBackSlide2"><span><fmt:message key="common.back"
                                                                                   bundle="${i18n_eng}"/></span>
                                </button>
                                <button type="button" class="btn btn-next" id="buttonNextSlide2"
                                        name="buttonNextSlide2"><span><fmt:message key="common.next"
                                                                                   bundle="${i18n_eng}"/></span>
                                </button>
                            </p>
                            <jsp:include page="include/proxyServiceFooter-img.jsp"/>
                        </div>
                        <input type="hidden" id="binaryLightToken" name="binaryLightToken"
                               value="<c:out value='${e:forHtml(binaryLightToken)}'/>"/>
                    </form>
                    <form id="cancelForm" name="cancelForm" method="${e:forHtml(binding)}"
                          action="${e:forHtml(citizenConsentUrl)}" class="jsOK" disabled="true">
                        <input type="hidden" name="cancel" value="true"/>
                        <input type="hidden" name="binaryLightToken"
                               value="<c:out value='${e:forHtml(binaryLightToken)}'/>"/>
                    </form>
                    <noscript>
                        <span id="mergedSlider">
                            <div class="row">
                                <form id="consentSelectornojs" name="consentSelectornojs" method="${e:forHtml(binding)}"
                                      action="${e:forHtml(citizenConsentUrl)}">
                                    <div id="slider3">
                                        <h1 class="title">
                                            <c:out value="${e:forHtml(spId)}"/>
                                            <span>
                                                <fmt:message key="presentConsent.levelOfAssurance"
                                                             bundle="${i18n_eng}"/>
                                                <c:out value="${e:forHtml(LoA)}"/>
                                            </span>
                                            <fmt:message key="presentConsent.isRequesting" bundle="${i18n_eng}"/>
                                        </h1>
                                        <div class="step-number">1</div>
                                        <h2 class="sub-title"><fmt:message key="presentConsent.basicInformation"
                                                                           bundle="${i18n_eng}"/></h2>
                                        <div class="row"><% /** Mandatory attributes are here */ %>
                                            <div class="col-sm-6"><% /** Natural person */ %>
                                                <c:set var="categoryIsDisplayed" value="false"/>
                                                <c:forEach items="${attrList}" var="attrItem">
                                                <c:if test="${attrItem.key.required && attrItem.key.personType.value=='NaturalPerson'}">
                                                <c:if test="${categoryIsDisplayed=='false'}">
                                                <h3><fmt:message key="presentConsent.natural" bundle="${i18n_eng}"/>
                                                    <span><fmt:message key="presentConsent.person"
                                                                       bundle="${i18n_eng}"/></span>
                                                </h3>
                                                <c:set var="categoryIsDisplayed" value="true"/>
                                                <ul class="list-unstyled check">
                                                    </c:if>
                                                    <li>
                                                        <fmt:message var="displayAttr"
                                                                     key="${attrItem.key.nameUri}.display"
                                                                     bundle="${i18n_eng}"/>
                                                        <c:if test="${!fn:startsWith(fn:toLowerCase(displayAttr), 'false')}">
                                                            <fmt:message key="${attrItem.key.friendlyName}"
                                                                         bundle="${i18n_eng}"/>
                                                        </c:if>
                                                        <input id="consentSelector_${attrItem.key.friendlyName}"
                                                               type="hidden" name="${attrItem.key.nameUri}"
                                                               value="${attrItem.key.nameUri}"/>
                                                    </li>
                                                    </c:if>
                                                    </c:forEach>
                                                    <c:if test="${categoryIsDisplayed=='true'}"></ul>
                                                </c:if>
                                            </div>
                                            <div class="col-sm-6"><% /** Legal person */ %>
                                                <c:set var="categoryIsDisplayed" value="false"/>
                                                <c:forEach items="${attrList}" var="attrItem">
                                                <c:if test="${attrItem.key.required && attrItem.key.personType.value=='LegalPerson'}">
                                                <c:if test="${categoryIsDisplayed=='false'}">
                                                <h3><fmt:message key="presentConsent.legal" bundle="${i18n_eng}"/>
                                                    <span><fmt:message key="presentConsent.person"
                                                                       bundle="${i18n_eng}"/></span>
                                                </h3>
                                                <c:set var="categoryIsDisplayed" value="true"/>
                                                <ul class="list-unstyled check">
                                                    </c:if>
                                                    <li>
                                                        <fmt:message var="displayAttr"
                                                                     key="${attrItem.key.nameUri}.display"
                                                                     bundle="${i18n_eng}"/>
                                                        <c:if test="${!fn:startsWith(fn:toLowerCase(displayAttr), 'false')}">
                                                            <fmt:message key="${attrItem.key.friendlyName}"
                                                                         bundle="${i18n_eng}"/>
                                                            <input id="consentSelector_${attrItem.key.friendlyName}"
                                                                   type="hidden"
                                                                   name="${attrItem.key.nameUri}"
                                                                   value="${attrItem.key.nameUri}"/>
                                                        </c:if>
                                                    </li>
                                                    </c:if>
                                                    </c:forEach>
                                                    <c:if test="${categoryIsDisplayed=='true'}"></ul>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                    <div id="slider4">
                                        <h2 class="sub-title"><fmt:message key="presentConsent.additionalInformation"
                                                                           bundle="${i18n_eng}"/></h2>
                                        <div class="row"><% /** optional  attributes are here */ %>
                                            <div class="col-sm-6"><% /** Natural person */ %>
                                                <c:set var="categoryIsDisplayed" value="false"/>
                                                <c:forEach items="${attrList}" var="attrItem">
                                                <c:if test="${!attrItem.key.required && attrItem.key.personType.value=='NaturalPerson'}">
                                                <c:if test="${categoryIsDisplayed=='false'}">
                                                <h3><fmt:message key="presentConsent.natural" bundle="${i18n_eng}"/>
                                                    <span><fmt:message key="presentConsent.person"
                                                                       bundle="${i18n_eng}"/></span>
                                                </h3>
                                                <c:set var="categoryIsDisplayed" value="true"/>
                                                <ul class="toogle-switch list-unstyled">
                                                    </c:if>
                                                    <li>
                                                        <c:if test="${fn:startsWith(fn:toLowerCase(displayAttr), 'false')}">
                                                            <input id="consentSelector_${attrItem.key.nameUri}"
                                                                   type="hidden"
                                                                   name="${attrItem.key.nameUri}"
                                                                   value="${attrItem.key.nameUri}"/>
                                                        </c:if>
                                                        <c:if test="${!fn:startsWith(fn:toLowerCase(displayAttr), 'false')}">
                                                            <p><fmt:message key="${attrItem.key.nameUri}"
                                                                            bundle="${i18n_eng}"/></p>
                                                            <input class="js-switch"
                                                                   id="consentSelector_${attrItem.key.friendlyName}"
                                                                   type="checkbox"
                                                                   name="${attrItem.key.nameUri}"
                                                                   value="true"/>
                                                            <input id="__checkbox_consentSelector_${attrItem.key.friendlyName}"
                                                                   type="hidden"
                                                                   name="__checkbox_${attrItem.key.nameUri}"
                                                                   value="true"/>
                                                        </c:if>
                                                    </li>
                                                    </c:if>
                                                    </c:forEach>
                                                    <c:if test="${categoryIsDisplayed=='true'}"></ul>
                                                </c:if>
                                            </div>
                                            <div class="col-sm-6"><% /** Legal person */ %>
                                                <c:set var="categoryIsDisplayed" value="false"/>
                                                <c:forEach items="${attrList}" var="attrItem">
                                                <c:if test="${!attrItem.key.required && attrItem.key.personType.value=='LegalPerson'}">
                                                <c:if test="${categoryIsDisplayed=='false'}">
                                                <h3><fmt:message key="presentConsent.legal" bundle="${i18n_eng}"/>
                                                    <span><fmt:message key="presentConsent.person"
                                                                       bundle="${i18n_eng}"/></span>
                                                </h3>
                                                <c:set var="categoryIsDisplayed" value="true"/>
                                                <ul class="toogle-switch list-unstyled">
                                                    </c:if>
                                                    <li>
                                                        <c:if test="${fn:startsWith(fn:toLowerCase(displayAttr), 'false')}">
                                                            <input id="consentSelector_${attrItem.key.friendlyName}"
                                                                   type="hidden"
                                                                   name="${attrItem.key.nameUri}"
                                                                   value="${attrItem.key.nameUri}"/>
                                                        </c:if>
                                                        <c:if test="${!fn:startsWith(fn:toLowerCase(displayAttr), 'false')}">
                                                            <p><fmt:message key="${attrItem.key.nameUri}"
                                                                            bundle="${i18n_eng}"/></p>
                                                            <input class="js-switch"
                                                                   id="consentSelector_${attrItem.key.friendlyName}"
                                                                   type="checkbox"
                                                                   name="${attrItem.key.nameUri}"
                                                                   value="true"/>
                                                            <input id="__checkbox_consentSelector_${attrItem.key.friendlyName}"
                                                                   type="hidden"
                                                                   name="__checkbox_${attrItem.key.nameUri}"
                                                                   value="true"/>
                                                        </c:if>
                                                    </li>
                                                    </c:if>
                                                    </c:forEach>
                                                    <c:if test="${categoryIsDisplayed=='true'}"></ul>
                                                </c:if>
                                            </div>
                                        </div>
                                        <div id="checkbox_Confirmation_div2" class="checkbox checkbox-custom">
                                            <p class="information-message"><fmt:message
                                                    key="presentConsent.deniedConfirmation" bundle="${i18n_eng}"/></p>
                                        </div>
                                        <p class="step-status"><fmt:message key="common.step" bundle="${i18n_eng}"/>
                                            <span>1</span> | 2</p>
                                        <p class="box-btn">
                                            <button type="submit" class="btn btn-next" id="buttonNextNoScript"
                                                    name="buttonNextNoScript"><span>Next</span></button>
                                        </p>
                                    </div>
                                    <input type="hidden" id="binaryLightTokenNoJS" name="binaryLightToken"
                                           value="<c:out value='${e:forHtml(binaryLightToken)}'/>"/>
                                </form>
                                <form id="cancelForm2" name="cancelForm" method="${e:forHtml(binding)}"
                                      action="${e:forHtml(citizenConsentUrl)}">
                                    <input type="hidden" name="cancel" value="true"/>
                                    <input type="hidden" name="binaryLightToken"
                                           value="<c:out value='${e:forHtml(binaryLightToken)}'/>"/>
                                    <p class="box-btn">
                                        <button type="submit" class="btn btn-opposite" id="buttonCancelNoScript"
                                                name="buttonCancelNoScript"><span>Cancel</span></button>
                                    </p>
                                    <jsp:include page="include/proxyServiceFooter-img.jsp"/>
                                </form>

                            </div>
                        </span>
                    </noscript>
                </div>
            </div>
        </div>
    </div>
</main>
<jsp:include page="helpPages/proxyServiceModal_loa.jsp"/>
<jsp:include page="helpPages/proxyServiceModal_attribute.jsp"/>
<jsp:include page="include/proxyServiceFooterScripts.jsp"/>
<script type="text/javascript" src="js/presentConsent.js"></script>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
</body>
</html>
