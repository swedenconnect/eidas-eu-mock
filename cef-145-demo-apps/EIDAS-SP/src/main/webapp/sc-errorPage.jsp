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


			<h3><s:property value="%{getText('errorId')}"/></h3>
			<h5><s:property value="%{exception.title}"/></h5>
			<p><s:actionerror />
				<s:if test="%{getText(exception.message)!=''}">
					<s:property value="%{getText(exception.message)}"/>
				</s:if>
				<s:else>
					<s:property value="%{exception.message}"/>
				</s:else>
			</p>
			<br/>
			<a href="populateIndexPage" class="btn btn-primary link-button">Return</a>
		</div>
	</div>
</div>



<!--END HEADER-->
<%--
<div class="container">
	<div class="row">
		<div class="tab-content">
			<div role="tabpanel" class="tab-pane fade in active" id="tab-02">
				<div class="col-md-12">
					<h2 class="text-error"><s:property value="%{getText('errorId')}"/></h2>
				</div>
				<jsp:include page="leftColumn.jsp"/>
				<div class="col-md-6">
					<h3 class="m-top-0 text-error"><s:property value="%{exception.title}"/></h3>
					<p><s:actionerror />
					<s:if test="%{getText(exception.message)!=''}">
						<s:property value="%{getText(exception.message)}"/>
					</s:if>
					<s:else>
						<s:property value="%{exception.message}"/>
					</s:else>
					</p>
					<p>
						<s:property value="%{getText('errorMessage1Id')}"/><a href="."><s:property value="%{getText('errorMessage2Id')}"/></a> <s:property value="%{getText('errorMessage3Id')}"/>
					</p>
				</div>
			</div>
		</div>
	</div>
</div>
--%>
</body>
</html>
