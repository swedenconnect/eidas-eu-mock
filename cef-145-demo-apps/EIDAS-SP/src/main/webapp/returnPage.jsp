<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html lang="en">

<head>
	<jsp:include page="htmlHead.jsp"/>
	<title><s:property value="%{getText('tituloId')}"/></title>
	<script type="text/javascript" src="js/sp.js"></script>
	<script type="text/javascript" src="js/base64.js"></script>
	<%--<script type="text/javascript" src="js/script.js"></script>--%>
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
					<h2><s:property value="%{providerName}"/>&nbsp;
						<s:property value="%{getText('loginSucceededId')}"/>

					</h2>
				</div>
				<jsp:include page="leftColumn.jsp"/>
				<div class="col-md-6">
					<h3 class="m-top-0"><s:property value="%{getText('requestedDataId')}"/></h3>
					<div class="table-responsive">
						<table class="table table-striped">
							<thead>
								<tr>
									<th>
										<s:property value="%{getText('attributeId')}"/>
									</th>
									<th>
										<s:property value="%{getText('valuesId')}"/>
									</th>
									<th>
										<s:property value="%{getText('complexValuesId')}"/>
									</th>
									<th>
										<s:property value="%{getText('statusId')}"/>
									</th>
								</tr>
							</thead>
							<tbody>
								<s:iterator value="attrMap.keySet()" var="definition">
									<tr>
										<td>
											<s:property value="#definition.getNameUri()"/>
										</td>
										<td>
											<s:property value="attrMap.get(#definition)"/>
										</td>
										<td>
											<!--s:property value="attrList[#idx.index].complexValue"/-->
										</td>
										<td>
											<!--s:property value="attrList[#idx.index].status"/-->
										</td>
									</tr>
								</s:iterator>
							</tbody>
						</table>
					</div>
					<p><s:property value="%{getText('errorMessage1Id')}"/><a href="."><s:property value="%{getText('errorMessage2Id')}"/> </a><s:property value="%{getText('errorMessage3Id')}"/></p>
				</div>
			</div>
		</div>
	</div>
</div>
<jsp:include page="footer.jsp"/>
</body>
</html>