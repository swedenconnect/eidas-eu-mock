<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html lang="en">

<head>
	<jsp:include page="htmlHead.jsp"/>
	<title><s:property value="%{getText('tituloId')}"/></title>
	<script type="text/javascript" src="js/sp.js"></script>
	<script type="text/javascript" src="js/script.js"></script>
	<script type="text/javascript" src="js/base64.js"></script>
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
		<div class="tab-content">
			<div role="tabpanel" class="tab-pane fade in active" id="tab-02">
				<div class="col-md-12">
					<h2><s:property value="%{providerName}"/>
						<span class="sub-title"><s:property value="%{getText('samlResponseRecivedId')}"/></span>
					</h2>
				</div>
				<jsp:include page="leftColumn.jsp"/>
				<div class="col-md-6">
					<form id="countrySelector" name="countrySelector" action="populateReturnPage" target="_parent" method="post">
						<div class="form-group">
							<label for="SAMLResponse"><s:property value="%{getText('SAMLResponseId')}"/></label>
							<textarea class="form-control" name="SAMLResponse" id="SAMLResponse" rows="8"><s:property value="SAMLResponse"/></textarea>
						</div>
						<div class="button-group">
							<button id="submit_saml" type="submit" class="btn btn-default btn-lg btn-block">Submit</button>
						</div>
					</form>
					<form>
						<%--<div class="button-group">
							<button type="button" class="btn btn-default btn-lg btn-block" OnClick="encodeSAMLResponse();">Encode</button>
							<button type="button" class="btn btn-default btn-lg btn-block" OnClick="decodeSAMLResponse();">Decode</button>
						</div>--%>
							<div class="form-group">
								<label for="RelayState"><s:property value="%{getText('RelayState')}"/></label>
								<textarea id="RelayState" class="form-control" name="RelayState" rows="1"><s:property value="RelayState"/></textarea>
						    </div>

							<s:if test="%{samlUnencryptedResponseXML != null}">
							<div class="form-group">
								<label for="samlEncryptedResponseXML"><s:property value="%{getText('SAMLEncryptedResponseXMLId')}"/></label>
								<textarea id="samlEncryptedResponseXML" class="form-control" name="samlEncryptedResponseXML" rows="10"><s:property value="samlResponseXML"/></textarea>
							</div>
						</s:if>
						<div class="form-group">
							<s:if test="%{samlUnencryptedResponseXML != null}">
								<label for="samlResponseXML"><s:property value="%{getText('SAMLDecryptedAssertion')}"/></label>
							</s:if>
							<s:else>
								<label for="samlResponseXML"><s:property value="%{getText('SAMLResponseXMLId')}"/></label>
							</s:else>
							<textarea class="form-control" id="samlResponseXML" name="samlResponseXML" rows="10"><s:if test="%{samlUnencryptedResponseXML == null}"><s:property value="samlResponseXML"/></s:if><s:else><s:property value="samlUnencryptedResponseXML"/></s:else></textarea>
						</div>
						<s:fielderror/>
					</form>
				</div>
			</div>
		</div>
	</div>
</div>
<jsp:include page="footer.jsp"/>
</body>
</html>
