<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html lang="en">


<head>
    <jsp:include page="htmlHead.jsp"/>
    <link href="css/dd.css" rel="stylesheet" type="text/css"/>
    <title><s:property value="%{getText('tituloId')}"/></title>
</head>

<body>
<!--START HEADER-->
<header class="header">
    <div class="container">
        <h1><s:property value="%{getText('tituloCabeceraId')}"/></h1>
    </div>
</header>
<!--END HEADER-->
<div class="container">
    <div class="row">
        <!--START NAV TAB-->

        <!--END NAV TAB-->
        <!--START TAB-->
        <div class="tab-content">

            <!--START TAB-02-->
            <!-- ******************************************************************************************************************************** -->
            <!-- ************************************************* TABBED PANEL 2 EIDAS attributes*********************************************** -->
            <!-- ******************************************************************************************************************************** -->
            <div role="tabpanel" class="tab-pane fade in active" id="tab-02">
                <div class="col-md-12">
                    <h2><s:property value="%{providerName}"/>
                        <span class="sub-title">(submits to an <span class="lowercase">e</span>IDAS Authentication Service)</span>
                    </h2>
                </div>
                <jsp:include page="leftColumn.jsp"/>
                <div class="col-md-6">
                    <s:form action="IndexPage" id="formTab2">
                        <h3 class="m-top-0">Detail messages</h3>
                        <div class="form-group" id="spCountryDiv">
                            <label for="eidasconnector"><s:property value="%{getText('spCountryId')}"/></label>
                            <select name="eidasconnector" id="eidasconnector" class="form-control">
                                <option data-description="Choose an option"></option>
                                <s:iterator value="countries">
                                    <option value="<s:property value="url"/>"
                                            data-image="img/flags/<s:property value="name"/>.gif"><s:property
                                            value="name"/></option>
                                </s:iterator>
                            </select>
                            <input type="text" name="nodeMetadataUrl" value="" id="inputEidas" class="form-control"/>
                        </div>
                        <div class="form-group" id="citizenCountryDivEidas">
                            <label for="citizenEidas"><s:property value="%{getText('citizenCountryId')}"/></label>
                            <select name="citizenEidas" id="citizeneidas" class="form-control">
                                <option data-description="Choose an option"></option>
                                <s:iterator value="countries">
                                    <option value="<s:property value="name" />"
                                            data-image="img/flags/<s:property value="name"/>.gif"><s:property
                                            value="name"/></option>
                                </s:iterator>
                            </select>
                        </div>
                        <div class="form-group" id="spReturnIdDiv">
                            <label for="spReturnUrlEidas"><s:property value="%{getText('spReturnUrlId')}"/></label>
                            <s:textfield name="returnUrl" id="spReturnUrlEidas" cssClass="form-control"/>
                        </div>
                        <div class="form-group" id="eidasNameIdentifierDiv">
                            <label for="eidasNameIdentifier"><s:property
                                    value="%{getText('eidasNameIdentifier')}"/></label>
                            <select name="eidasNameIdentifier" id="eidasNameIdentifier" class="form-control">
                                <option value="label">Choose a value</option>
                                <option value="persistent">
                                    persistent
                                </option>
                                <option value="transient">
                                    transient
                                </option>
                                <option selected value="unspecified">
                                    unspecified
                                </option>
                            </select>
                        </div>
                        <div class="form-group" id="eidasLoAIdDiv">
                            <label for="eidasloa"><s:property value="%{getText('eidasLoAId')}"/></label>
                            <select name="eidasloa" id="eidasloa" class="form-control"
                                    title="A,B -> low; C,D -> substancial; E ->high">
                                    <%--<option value="label">Choose a value</option>--%>
                                <option selected value="A">
                                    A
                                </option>
                                <option value="B">
                                    B
                                </option>
                                <option value="C">
                                    C
                                </option>
                                <option value="D">
                                    D
                                </option>
                                <option value="E">
                                    E
                                </option>
                            </select>
                        </div>
                        <div class="form-group" id="eidasloaCompareTypeDiv">
                            <label for="eidasloaCompareType"><s:property
                                    value="%{getText('eidasloaCompareType')}"/></label>
                            <select name="eidasloaCompareType" id="eidasloaCompareType" class="form-control">
                                <option value="minimum" selected>minimum</option>
                            </select>
                        </div>
                        <div class="form-group" id="eidasSPTypeDiv">
                            <label for="eidasSPType"><s:property value="%{getText('eidasSPType')}"/></label>
                            <select name="eidasSPType" id="eidasSPType" class="form-control">
                                <option value="not provided">not provided</option>
                                <option value="public" selected>public</option>
                                <option value="private">private</option>
                            </select>
                        </div>
                        <h3><s:property value="%{getText('eidasAttributesId')}"/></h3>
                        <div class="radio-inline-group">
                            <div class="radio radio-info radio-inline">
                                <input type="radio" name="allTypeEidas" id="check_all_MandatoryEidas" value="true"/>
                                <label for="check_all_MandatoryEidas"><s:property
                                        value="%{getText('mandatoryId')}"/></label>
                            </div>
                            <div class="radio radio-info radio-inline">
                                <input type="radio" name="allTypeEidas" id="check_all_OptionalEidas" value="false"
                                       checked="checked"/>
                                <label for="check_all_OptionalEidas"><s:property
                                        value="%{getText('optionalId')}"/></label>
                            </div>
                            <div class="radio radio-info radio-inline">
                                <input type="radio" name="allTypeEidas" id="check_all_NoRequestEidas" value="none"/>
                                <label for="check_all_NoRequestEidas"><s:property
                                        value="%{getText('doNotRequestId')}"/></label>
                            </div>
                            <span class="text-error">(*) eIDAS minimum data set</span>
                        </div>
                        <h4><s:property value="%{getText('eidasNaturalPersonAttributes')}"/> <a class="toggle"
                                                                                                id="tab2_toggle1"></a>
                        </h4>
                        <div class="content" id="tab2_toggle1_content">
                            <s:set var="NaturalPerson">NaturalPerson</s:set>
                            <s:iterator value="eidasAttributeList">
                                <s:if test="%{#NaturalPerson.equalsIgnoreCase(PersonType)}">
                                    <div class="form-group">
                                        <s:if test="%{value[0]!=''}">
                                            <input type="text" name="<s:property value="friendlyName"/>"
                                                   value="<s:property value="friendlyName"/>" id="input"
                                                   class="form-control"/>
                                            <input type="text" name="<s:property value="friendlyName"/>Value"
                                                   value="<s:property value="value[0]"/>" id="input"
                                                   class="form-control"/>
                                        </s:if>
                                        <s:else>
                                            <input type="text" name="<s:property value="friendlyName"/>"
                                                   value="<s:property value="friendlyName"/>" id="input"
                                                   class="form-control"/>
                                        </s:else>
                                        <div class="radio-inline-group">
                                            <div class="radio radio-info radio-inline">
                                                <input type="radio" name="<s:property value="friendlyName" />Type"
                                                       id="Mandatory_<s:property value="friendlyName" />Eidas"
                                                       value="true" <s:if test="required">checked="checked"</s:if>/>
                                                <label for="Mandatory_<s:property value="friendlyName" />Eidas"><s:property
                                                        value="%{getText('mandatoryId')}"/></label>
                                            </div>
                                            <div class="radio radio-info radio-inline">
                                                <input type="radio" name="<s:property value="friendlyName" />Type"
                                                       id="Optional_<s:property value="friendlyName" />Eidas"
                                                       value="false" <s:if test="!required">checked="checked"</s:if>/>
                                                <label for="Optional_<s:property value="friendlyName" />Eidas"><s:property
                                                        value="%{getText('optionalId')}"/></label>
                                            </div>
                                            <div class="radio radio-info radio-inline">
                                                <input type="radio" name="<s:property value="friendlyName" />Type"
                                                       id="NoRequest_<s:property value="friendlyName" />Eidas"
                                                       value="none"/>
                                                <label for="NoRequest_<s:property value="friendlyName" />Eidas"><s:property
                                                        value="%{getText('doNotRequestId')}"/></label>
                                            </div>
                                            <s:if test="%{isRequired()}"><span class="text-error">(*)</span></s:if>
                                        </div>
                                    </div>
                                </s:if>
                            </s:iterator>
                        </div>
                        <h4><s:property value="%{getText('eidasLegalPersonAttributes')}"/> <a class="toggle"
                                                                                              id="tab2_toggle2"></a>
                        </h4>
                        <div class="content" id="tab2_toggle2_content">
                            <s:set var="LegalPerson">LegalPerson</s:set>
                            <s:iterator value="eidasAttributeList">
                                <s:if test="%{#LegalPerson.equalsIgnoreCase(PersonType)}">
                                    <div class="form-group">
                                        <s:if test="%{value[0]!=''}">
                                            <input type="text" name="<s:property value="friendlyName"/>"
                                                   value="<s:property value="friendlyName"/>" id="input"
                                                   class="form-control"/>
                                            <input type="text" name="<s:property value="friendlyName"/>Value"
                                                   value="<s:property value="value[0]"/>" id="input"
                                                   class="form-control"/>
                                            <s:if test="%{required}"><span class="text-error">(*)</span></s:if>
                                        </s:if>
                                        <s:else>
                                            <input type="text" name="<s:property value="friendlyName"/>"
                                                   value="<s:property value="friendlyName"/>" id="input"
                                                   class="form-control"/>
                                        </s:else>
                                        <div class="radio-inline-group">
                                            <div class="radio radio-info radio-inline">
                                                    <%--TODO next if block regarding deprecated legal person attributes to be removed and then keep only else block content--%>
                                                <s:if test='friendlyName.toASCIIString().equalsIgnoreCase("http://eidas.europa.eu/attributes/legalperson/LegalAddress") || friendlyName.toASCIIString().equalsIgnoreCase("http://eidas.europa.eu/attributes/legalperson/VATRegistration")'>
                                                    <input type="radio" name="<s:property value="friendlyName" />Type"
                                                           id="Disabled_Mandatory_<s:property value="friendlyName" />Eidas"
                                                           value="true" <s:if test="required">checked="checked"</s:if>/>
                                                </s:if>
                                                <s:else>
                                                    <input type="radio" name="<s:property value="friendlyName" />Type"
                                                           id="Mandatory_<s:property value="friendlyName" />Eidas"
                                                           value="true" <s:if test="required">checked="checked"</s:if>/>
                                                </s:else>
                                                <label for="Mandatory_<s:property value="friendlyName" />Eidas"><s:property
                                                        value="%{getText('mandatoryId')}"/></label>
                                            </div>
                                            <div class="radio radio-info radio-inline">
                                                    <%--TODO next if block regarding deprecated legal person attributes to be removed and then keep only else block content--%>
                                                <s:if test='friendlyName.toASCIIString().equalsIgnoreCase("http://eidas.europa.eu/attributes/legalperson/LegalAddress") || friendlyName.toASCIIString().equalsIgnoreCase("http://eidas.europa.eu/attributes/legalperson/VATRegistration")'>
                                                    <input type="radio" name="<s:property value="friendlyName" />Type"
                                                           id="Disabled_Optional_<s:property value="friendlyName" />Eidas"
                                                           value="false"
                                                           <s:if test="!required">checked="checked"</s:if>/>
                                                </s:if>
                                                <s:else>
                                                    <input type="radio" name="<s:property value="friendlyName" />Type"
                                                           id="Optional_<s:property value="friendlyName" />Eidas"
                                                           value="false"
                                                           <s:if test="!required">checked="checked"</s:if>/>
                                                </s:else>
                                                <label for="Optional_<s:property value="friendlyName" />Eidas"><s:property
                                                        value="%{getText('optionalId')}"/></label>
                                            </div>
                                            <div class="radio radio-info radio-inline">
                                                    <%--TODO next if block regarding deprecated legal person attributes to be removed and then keep only else block content--%>
                                                <s:if test='friendlyName.toASCIIString().equalsIgnoreCase("http://eidas.europa.eu/attributes/legalperson/LegalAddress") || friendlyName.toASCIIString().equalsIgnoreCase("http://eidas.europa.eu/attributes/legalperson/VATRegistration")'>
                                                    <input type="radio" name="<s:property value="friendlyName" />Type"
                                                           id="NoRequest_<s:property value="friendlyName" />Eidas"
                                                           value="none" checked="checked"/>
                                                </s:if>
                                                <s:else>
                                                    <input type="radio" name="<s:property value="friendlyName" />Type"
                                                           id="NoRequest_<s:property value="friendlyName" />Eidas"
                                                           value="none"/>
                                                </s:else>
                                                <label for="NoRequest_<s:property value="friendlyName" />Eidas"><s:property
                                                        value="%{getText('doNotRequestId')}"/></label>
                                            </div>
                                            <s:if test="%{isRequired()}"><span class="text-error">(*)</span></s:if>
                                        </div>
                                    </div>
                                </s:if>
                            </s:iterator>
                        </div>
                        <h3><s:property value="%{getText('eidasRepvAttributesId')}"/></h3>
                        <div class="radio-inline-group">
                            <div class="radio radio-info radio-inline">
                                <input type="radio" name="allTypeEidas" id="check_all_MandatoryRepvEidas" value="true"/>
                                <label for="check_all_MandatoryRepvEidas"><s:property
                                        value="%{getText('mandatoryId')}"/></label>
                            </div>
                            <div class="radio radio-info radio-inline">
                                <input type="radio" name="allTypeEidas" id="check_all_OptionalRepvEidas" value="false"/>
                                <label for="check_all_OptionalRepvEidas"><s:property
                                        value="%{getText('optionalId')}"/></label>
                            </div>
                            <div class="radio radio-info radio-inline">
                                <input type="radio" name="allTypeEidas" id="check_all_NoRequestRepvEidas" value="none"
                                       checked="checked"/>
                                <label for="check_all_NoRequestRepvEidas"><s:property
                                        value="%{getText('doNotRequestId')}"/></label>
                            </div>
                        </div>
                        <h4><s:property value="%{getText('eidasRepNaturalPersonAttributes')}"/> <a class="toggle"
                                                                                                   id="tab2_toggle3"></a>
                        </h4>
                        <div class="content" id="tab2_toggle3_content">
                            <s:set var="RepresentativeNaturalPerson">RepresentativeNaturalPerson</s:set>
                            <s:iterator value="eidasAttributeList">
                                <s:if test="%{#RepresentativeNaturalPerson.equalsIgnoreCase(PersonType)}">
                                    <div class="form-group">
                                        <s:if test="%{value[0]!=''}">
                                            <input type="text" name="<s:property value="friendlyName"/>"
                                                   value="<s:property value="friendlyName"/>" id="input"
                                                   class="form-control"/>
                                            <input type="text" name="<s:property value="friendlyName"/>Value"
                                                   value="<s:property value="value[0]"/>" id="input"
                                                   class="form-control"/>
                                        </s:if>
                                        <s:else>
                                            <input type="text" name="<s:property value="friendlyName"/>"
                                                   value="<s:property value="friendlyName"/>" id="input"
                                                   class="form-control"/>
                                        </s:else>
                                        <div class="radio-inline-group">
                                            <div class="radio radio-info radio-inline">
                                                <input type="radio" name="<s:property value="friendlyName" />Type"
                                                       id="MandatoryRepv_<s:property value="friendlyName" />Eidas"
                                                       value="true"/>
                                                <label for="MandatoryRepv_<s:property value="friendlyName" />Eidas"><s:property
                                                        value="%{getText('mandatoryId')}"/></label>
                                            </div>
                                            <div class="radio radio-info radio-inline">
                                                <input type="radio" name="<s:property value="friendlyName" />Type"
                                                       id="OptionalRepv_<s:property value="friendlyName" />Eidas"
                                                       value="false"/>
                                                <label for="OptionalRepv_<s:property value="friendlyName" />Eidas"><s:property
                                                        value="%{getText('optionalId')}"/></label>
                                            </div>
                                            <div class="radio radio-info radio-inline">
                                                <input type="radio" name="<s:property value="friendlyName" />Type"
                                                       id="NoRequestRepv_<s:property value="friendlyName" />Eidas"
                                                       checked="checked" value="none"/>
                                                <label for="NoRequestRepv_<s:property value="friendlyName" />Eidas"><s:property
                                                        value="%{getText('doNotRequestId')}"/></label>
                                            </div>
                                            <s:if test="%{isRequired()}"><span class="text-error">(*)</span></s:if>
                                        </div>
                                    </div>
                                </s:if>
                            </s:iterator>
                        </div>
                        <h4><s:property value="%{getText('eidasRepLegalPersonAttributes')}"/> <a class="toggle"
                                                                                                 id="tab2_toggle4"></a>
                        </h4>
                        <div class="content" id="tab2_toggle4_content">
                            <s:set var="RepresentativeLegalPerson">RepresentativeLegalPerson</s:set>
                            <s:iterator value="eidasAttributeList">
                                <s:if test="%{#RepresentativeLegalPerson.equalsIgnoreCase(PersonType)}">
                                    <div class="form-group">
                                        <s:if test="%{value[0]!=''}">
                                            <input type="text" name="<s:property value="friendlyName"/>"
                                                   value="<s:property value="friendlyName"/>" id="input"
                                                   class="form-control"/>
                                            <input type="text" name="<s:property value="friendlyName"/>Value"
                                                   value="<s:property value="value[0]"/>" id="input"
                                                   class="form-control"/>
                                            <s:if test="%{required}"><span class="text-error">(*)</span></s:if>
                                        </s:if>
                                        <s:else>
                                            <input type="text" name="<s:property value="friendlyName"/>"
                                                   value="<s:property value="friendlyName"/>" id="input"
                                                   class="form-control"/>
                                        </s:else>
                                        <div class="radio-inline-group">
                                            <div class="radio radio-info radio-inline">
                                                <input type="radio" name="<s:property value="friendlyName" />Type"
                                                       id="MandatoryRepv_<s:property value="friendlyName" />Eidas"
                                                       value="true"/>
                                                <label for="MandatoryRepv_<s:property value="friendlyName" />Eidas"><s:property
                                                        value="%{getText('mandatoryId')}"/></label>
                                            </div>
                                            <div class="radio radio-info radio-inline">
                                                <input type="radio" name="<s:property value="friendlyName" />Type"
                                                       id="OptionalRepv_<s:property value="friendlyName" />Eidas"
                                                       value="false"/>
                                                <label for="OptionalRepv_<s:property value="friendlyName" />Eidas"><s:property
                                                        value="%{getText('optionalId')}"/></label>
                                            </div>
                                            <div class="radio radio-info radio-inline">
                                                <input type="radio" name="<s:property value="friendlyName" />Type"
                                                       id="NoRequestRepv_<s:property value="friendlyName" />Eidas"
                                                       value="none" checked="checked"/>
                                                <label for="NoRequestRepv_<s:property value="friendlyName" />Eidas"><s:property
                                                        value="%{getText('doNotRequestId')}"/></label>
                                            </div>
                                            <s:if test="%{isRequired()}"><span class="text-error">(*)</span></s:if>
                                        </div>
                                    </div>
                                </s:if>
                            </s:iterator>
                        </div>
                        <input type="hidden" id="spType" name="spType" value="public">
                        <button id="submit_tab2" type="button" class="btn btn-default btn-lg btn-block">Submit</button>
                        <s:fielderror/>
                    </s:form>
                </div>
            </div>

        </div>
    </div>
</div>
<% /*end container*/ %>
<jsp:include page="footer.jsp"/>
</body>