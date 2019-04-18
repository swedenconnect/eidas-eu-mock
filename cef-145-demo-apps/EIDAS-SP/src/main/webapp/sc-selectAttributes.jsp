<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html lang="en">

<head>
    <jsp:include page="sc-htmlHead.jsp"/>
    <title><s:property value="%{getText('tituloId')}"/></title>
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
                    <tr>
                        <td style="width: 100%">
                            <div class="row">
                                <div class="col-lg-4 col-sm-12">
                                    <label for="selectedCountry">Citizen Country:</label>
                                </div>
                                <div class="col-lg-8 col-sm-12">
                                    <select class="selectpicker show-menu-arrow" data-width="90%" name="selectedCountry" id="selectedCountry">
                                        <option data-content="<img src='sc-img/flags/EU.png'> XC" value="XC" selected></option>
                                    </select>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td style="width: 100%">
                            <div class="row">
                                <div class="col-lg-4 col-sm-12">
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
                    <tr>
                        <td style="width: 100%">
                            <div class="row">
                                <div class="col-lg-4 col-sm-12">
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

                        <div class="drop-down-info">
                            <p>Select requested attributes</p>
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
