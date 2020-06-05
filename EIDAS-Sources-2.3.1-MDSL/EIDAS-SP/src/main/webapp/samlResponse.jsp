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
					<form>
						<div class="form-group">
							<s:if test="%{smsspUnencryptedResponseJSON != null}">
								<label for="smsspResponseJSON"><s:property value="%{getText('SMSSPDecryptedAssertion')}"/></label>
							</s:if>
							<s:else>
								<label for="smsspResponseJSON"><s:property value="%{getText('SMSSPResponseJSONId')}"/></label>
							</s:else>
							<textarea class="form-control" id="smsspResponseJSON" name="smsspResponseJSON" rows="10"><s:if test="%{smsspUnencryptedResponseJSON == null}"><s:property value="smsspResponseJSON"/></s:if><s:else><s:property value="smsspUnencryptedResponseJSON"/></s:else></textarea>

						</div>
						<s:fielderror/>
					</form>
					<form id="countrySelector" name="countrySelector" action="populateReturnPage" target="_parent" method="post">
						<input type="hidden" id="SMSSPResponse" name="SMSSPResponse" value="<s:property value="smsspResponseJSON" />">
						<div class="button-group">
							<button id="submit_saml" type="submit" class="btn btn-default btn-lg btn-block">Submit</button>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>
</div>
<jsp:include page="footer.jsp"/>
</body>
</html>
