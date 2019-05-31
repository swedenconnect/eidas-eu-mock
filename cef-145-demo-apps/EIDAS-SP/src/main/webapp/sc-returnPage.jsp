<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html lang="en">

<head>
	<jsp:include page="sc-htmlHead.jsp"/>
	<title><s:property value="%{getText('tituloId')}"/></title>
    <script src="webjars/highlightjs/9.8.0/highlight.min.js"></script>
    <link rel="stylesheet" href="webjars/highlightjs/9.8.0/styles/default.css"/>
    <script src="sc-js/sc-result.js"></script>

    <script></script>
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
            <div style="margin-top: 10px">
                <div style="float: right">
                    <span class="btn btn-sm btn-light" style="cursor: pointer" onclick="$('#responseViewDiv').fadeIn(700);">Response</span>&nbsp;
                    <span class="btn btn-sm btn-light" style="cursor: pointer" onclick="$('#assertionViewDiv').fadeIn(700);">Assertion</span>
                </div>

                <div style="height: 10px"></div>
                <h5>Authentication result</h5>
                <table class="table table-sm" style="table-layout: fixed">
                    <tr>
                        <td style="width: 100%">
                            <div class="row">
                                <div class="col-lg-5 col-sm-12">Level of Assurance</div>
                                <div class="col-lg-7 col-sm-12 wrap-div">
                                    <b><s:property value="authnContext.getLoa()"></s:property></b>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <%--
                                    <tr>
                                        <td style="width: 100%">
                                            <div class="row">
                                                <div class="col-lg-5 col-sm-12">Issuer</div>
                                                <div class="col-lg-7 col-sm-12 wrap-div">
                                                    <b><s:property value="authnContext.getIdp()"></s:property></b>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                    --%>
                    <tr>
                        <td style="width: 100%">
                            <div class="row">
                                <div class="col-lg-5 col-sm-12">Time</div>
                                <div class="col-lg-7 col-sm-12 wrap-div">
                                    <b><s:property value="authnContext.getNotBefore()"></s:property></b>
                                </div>
                            </div>
                        </td>
                    </tr>

                </table>
                <br/>
                <table class="table table-sm table-striped"  style="table-layout: fixed">
                    <tr>
                        <td style="width: 100%">
                            <div class="row">
                                <div class="col-lg-5 col-sm-12">
                                    <b>Attribute</b>
                                </div>
                                <div class="col-lg-7 col-sm-12">
                                    <b>Value</b>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <s:iterator value="resultAttributeList" var="attr">
                        <tr>
                            <td style="width: 100%">
                                <div class="row">
                                    <div class="col-lg-5 col-sm-12">
                                        <s:property value="friendlyName"/>
                                    </div>
                                    <div class="col-lg-7 col-sm-12 wrap-div">
                                        <s:property escapeHtml="false" value="value"/>
                                    </div>
                                </div>
                            </td>
                        </tr>
                    </s:iterator>
                </table>
                <a href="populateIndexPage" class="btn btn-primary link-button">Return</a>
            </div>
		</div>
	</div>
	<jsp:include page="sc-footer.jsp"/>

</div>


<!-- Response display box -->
<div id="responseViewDiv" class="xml-data-bgr">
	<div class="card xml-data-panel">
		<div id="responseTitleDiv" class="card-header">
			<table style="width: 100%">
				<tr>
					<td id="responseTitleCell">
						<h5>SAML response</h5>
					</td>
					<td style="text-align: right">
						<button class="btn btn-primary" style="margin-top: 10px" onclick="$('#responseViewDiv').fadeOut(700);">Close</button>
					</td>
				</tr>
			</table>
		</div>
		<div id="responseBodyDiv" class="card-body">
			<div id="responseDisplayDiv">
				<pre><code><s:property value="samlResponseXML"/></code></pre>
			</div>
		</div>
	</div>
</div>

<!-- Assertion display box -->
<div id="assertionViewDiv" class="xml-data-bgr">
	<div class="card xml-data-panel">
		<div id="assertionTitleDiv" class="card-header">
			<table style="width: 100%">
				<tr>
					<td id="assertionTitleCell">
						<h5>SAML Assertion</h5>
					</td>
					<td style="text-align: right">
						<button class="btn btn-primary" style="margin-top: 10px" onclick="$('#assertionViewDiv').fadeOut(700);">Close</button>
					</td>
				</tr>
			</table>
		</div>
		<div id="assertionBodyDiv" class="card-body">
			<div id="assertionDisplayDiv">
				<pre><code><s:property value="samlUnencryptedResponseXML"/></code></pre>
			</div>
		</div>
	</div>
</div>

</body>
</html>