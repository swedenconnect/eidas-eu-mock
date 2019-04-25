<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html lang="en">

<head>
    <jsp:include page="sc-htmlHead.jsp"/>
    <title><s:property value="%{getText('tituloId')}"/></title>

    <script>
    </script>

</head>

<body>

<jsp:include page="sc-htmlTitle.jsp"/>

<div class="container main">
    <jsp:include page="sc-langMock.jsp"/>

    <div class="row">
        <div class="col-sm-12 content-container">

            <div class="row">
                <div class="col-sm-12 content-heading">
                    <h2 class="article-header">Demo SP - CEF eIDAS node version 1.4.5</h2>
                    <h5><%=request.getAttribute("spId")%>
                    </h5>
                </div>
            </div>

            <hr class="full-width"/>
            <br>
            <s:form action="IndexPage" id="formTab1">

                <strong>Select authentication options:</strong>
                <table class="table">
                    <!-- Selector for selecting eID country -->
                    <tr>
                        <td style="width: 100%">
                            <div class="row">
                                <div class="col-lg-4 col-sm-12" style="padding-top: 6px">
                                    <label for="selectedCountry">Citizen Country:</label>
                                </div>
                                <div class="col-lg-8 col-sm-12">
                                    <select class="selectpicker show-menu-arrow" data-width="90%" name="selectedCountry"
                                            id="selectedCountry">
                                        <s:iterator value="displayCountries" status="dispCountryStatus">
                                            <s:if test="#dispCountryStatus.count == 1">
                                                <%--<option value="<s:property value="langCode"/>" data-content="<img src='<s:property value="flag"/>'> <s:property value="displayName"/>" selected></option>--%>
                                                <option value="<s:property value="langCode"/>"
                                                        data-content="<s:property value="dataContent"/>" selected></option>
                                            </s:if>
                                            <s:else>
                                                <option value="<s:property value="langCode"/>"
                                                        data-content="<s:property value="dataContent"/>"></option>
                                                <%--<option value="<s:property value="langCode"/>" data-content="<img src='<s:property value="flag"/>'> <s:property value="displayName"/>"></option>--%>
                                            </s:else>
                                        </s:iterator>
                                    </select>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <!-- Selector for selecting sector type -->
                    <tr>
                        <td style="width: 100%">
                            <div class="row">
                                <div class="col-lg-4 col-sm-12" style="padding-top: 6px">
                                    <label for="spType">Sector type</label>
                                </div>
                                <div class="col-lg-8 col-sm-12">
                                    <select class="selectpicker show-menu-arrow" data-width="90%" name="spType" id="spType">
                                        <option value="public" selected>public</option>
                                        <option value="private">private</option>
                                    </select>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <!-- Selector for selecting requested LoA -->
                    <tr>
                        <td style="width: 100%">
                            <div class="row">
                                <div class="col-lg-4 col-sm-12" style="padding-top: 6px">
                                    <label for="reqLoa">Requested LoA</label>
                                </div>
                                <div class="col-lg-8 col-sm-12">
                                    <select class="selectpicker show-menu-arrow" data-width="90%" name="reqLoa" id="reqLoa">
                                        <option value="http://eidas.europa.eu/LoA/low">low</option>
                                        <option value="http://eidas.europa.eu/LoA/substantial" selected>substantial</option>
                                        <option value="http://eidas.europa.eu/LoA/high">high</option>
                                    </select>
                                </div>
                            </div>
                        </td>
                    </tr>
                </table>

                <!-- Requested attributes -->
                <div class="drop-down-container">
                    <div class="col-sm-12 drop-down">
                        <p id="uiTextWhyEidNotSupported">Requested attributes</p>

                        <div class="drop-down-info" style="padding: 10px">
                            <!-- Natural Person requested attribute table -->
                            <strong>Natural Person Attributes:</strong>
                            <table class="table table-sm">
                                <tr>
                                    <td style="width: 100%" class="attribute-heading">
                                        <div class="row">
                                            <div class="col-lg-8 col-sm-12" style="padding-top: 3px">Attribute</div>
                                            <div class="col-lg-4 col-sm-12">
                                                <div style="float: right">
                                                    <span class="btn btn-sm btn-light attr-no-req" style="cursor: pointer" onclick="selectAllAttr('reqNpAttr','n')">No</span>&nbsp;
                                                    <span class="btn btn-sm btn-light attr-request" style="cursor: pointer" onclick="selectAllAttr('reqNpAttr','o')">Opt</span>&nbsp;
                                                    <span class="btn btn-sm btn-light attr-require" style="cursor: pointer" onclick="selectAllAttr('reqNpAttr','r')">Req</span>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                                <s:iterator value="npEidasAttributeList" status="npAttrStatus">
                                    <tr>
                                        <td style="width: 100%">
                                            <div class="row">
                                                <div class="col-lg-8 col-sm-12"><s:property
                                                        value="friendlyName"/></div>
                                                <div class="col-lg-4 col-sm-12">
                                                    <div style="float: right">
                                                        <label class="radio-inline attr-no-req"><input type="radio" name="reqNpAttr-<s:property value="#npAttrStatus.count"/>" onclick="processAttrsRd('reqNpAttr');" value="n">N</label>
                                                        <s:if test="%{isRequired()}">
                                                            <label class="radio-inline attr-request"><input type="radio" name="reqNpAttr-<s:property value="#npAttrStatus.count"/>" onclick="processAttrsRd('reqNpAttr');" value="o">O</label>
                                                            <label class="radio-inline attr-require"><input type="radio" name="reqNpAttr-<s:property value="#npAttrStatus.count"/>" onclick="processAttrsRd('reqNpAttr');" value="r" checked>R</label>
                                                        </s:if>
                                                        <s:else>
                                                            <label class="radio-inline attr-request"><input type="radio" name="reqNpAttr-<s:property value="#npAttrStatus.count"/>" onclick="processAttrsRd('reqNpAttr');" value="o" checked>O</label>
                                                            <label class="radio-inline attr-require"><input type="radio" name="reqNpAttr-<s:property value="#npAttrStatus.count"/>" onclick="processAttrsRd('reqNpAttr');" value="r">R</label>
                                                        </s:else>
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                </s:iterator>
                            </table>

                            <!-- Legal Person requested attribute table -->
                            <strong>Legal Person Attributes:</strong>
                            <table class="table table-sm">
                                <tr>
                                    <td style="width: 100%" class="attribute-heading">
                                        <div class="row">
                                            <div class="col-lg-8 col-sm-12" style="padding-top: 3px">Attribute</div>
                                            <div class="col-lg-4 col-sm-12">
                                                <div style="float: right">
                                                    <span class="btn btn-sm btn-light attr-no-req" style="cursor: pointer" onclick="selectAllAttr('reqLpAttr','n')">No</span>&nbsp;
                                                    <span class="btn btn-sm btn-light attr-request" style="cursor: pointer" onclick="selectAllAttr('reqLpAttr','o')">Opt</span>&nbsp;
                                                    <span class="btn btn-sm btn-light attr-require" style="cursor: pointer" onclick="selectAllAttr('reqLpAttr','r')">Req</span>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                                <s:iterator value="lpEidasAttributeList" status="npAttrStatus">
                                    <tr>
                                        <td style="width: 100%">
                                            <div class="row">
                                                <div class="col-lg-8 col-sm-12"><s:property
                                                        value="friendlyName"/></div>
                                                <div class="col-lg-4 col-sm-12">
                                                    <div style="float: right">
                                                        <label class="radio-inline attr-no-req"><input type="radio" name="reqLpAttr-<s:property value="#npAttrStatus.count"/>" onclick="processAttrsRd('reqLpAttr');" value="n"  checked>N</label>
                                                        <label class="radio-inline attr-request"><input type="radio" name="reqLpAttr-<s:property value="#npAttrStatus.count"/>" onclick="processAttrsRd('reqLpAttr');" value="o">O</label>
                                                        <label class="radio-inline attr-require"><input type="radio" name="reqLpAttr-<s:property value="#npAttrStatus.count"/>" onclick="processAttrsRd('reqLpAttr');" value="r">R</label>
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                </s:iterator>
                            </table>

                        </div>
                    </div> <!-- /.drop-down -->

                </div>

                <div style="min-height: 20px"></div>
                <input type="submit" id="submit_tab1" type="button" class="btn btn-primary" value="Authenticate"/>
            </s:form>
        </div>
    </div>
    <jsp:include page="sc-footer.jsp"/>

</div>


</body>
</html>
