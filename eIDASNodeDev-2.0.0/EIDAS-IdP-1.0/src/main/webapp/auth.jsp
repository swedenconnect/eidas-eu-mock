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
<%@page import="eu.eidas.auth.commons.EidasParameterKeys"%>
<%@page import="eu.eidas.auth.commons.EidasStringUtil"%>
<%@ page import="member_country_specific.idp.ProcessLogin" %>
<%
	String smsspToken = request.getParameter(EidasParameterKeys.SMSSP_REQUEST.toString());

	ProcessLogin processLogin = new ProcessLogin();
	String username = request.getParameter("username");
	String password = request.getParameter("password");

	String callback = processLogin.getServiceUrl(processLogin.convertJsonRequest(smsspToken));

	String jSonRequestDecoded = new String(EidasStringUtil.decodeBytesFromBase64(smsspToken));
	String signAssertion = request.getParameter("signAssertion");
	String encryptAssertion = request.getParameter("encryptAssertion");
%>

<script language="Javascript" type="text/javascript">

    function toggle(chkbox) {

        document.getElementById("eidasnameid").disabled = (chkbox.checked) ? false : true;
    }
</script>

<html lang="en">

<head>
	<jsp:include page="htmlHead.jsp"/>
	<title>eIDAS Authentication Service (IdP)</title>
</head>
<body>
<!--START HEADER-->
<header class="header">
	<div class="container">
		<h1>eIDAS Authentication Service (IdP)</h1>
	</div>
</header>
<!--END HEADER-->
<div class="container">
	<div class="row">
		<div class="tab-content">
			<div role="tabpanel" class="tab-pane fade in active" id="tab-02">
				<div class="col-md-12">
					<h2>Authentication
					</h2>
				</div>
				<jsp:include page="leftColumn.jsp"/>
				<div class="col-md-6">
					<form id="authenticationForm" name="authentication" method="post" action="Response">
						<div class="form-group">
							<label for="username">Username</label>
							<span>
								<button type="button" class="btn btn-info" data-toggle="modal" data-target="#loaModal">Test user</button>
							</span>
							<input type="text" class="form-control" id="username" name="username"/>
						</div>
						<div class="form-group">
							<label for="password">Password</label>
							<input type="password" class="form-control" name="password" id="password" placeholder="Password"/>
						</div>
							<div class="form-group" id="eidasDiv">
								<label for="eidasloa">Level of Assurance</label>
                                <select class="form-control" name="eidasloa" id="eidasloa"
                                        title="A,B -> low; C,D -> substancial; E ->high">
									<option value="A">
										A</option>
									<option value="B">
										B</option>
									<option value="C">
										C</option>
                                    <option value="D">
                                        D</option>
                                    <option value="E">
                                        E</option>
								</select>
							</div>
                        <div class="form-group" id="eidasDivNameId">
                            <input class="form-horizontal" id="checkBoxNameId" type="checkbox" name="checkBoxNameId"
                                   onchange="toggle(this)" unchecked/>&nbsp;
                            <label for="checkBoxNameId">Add a Name Id Format</label>

                            <select class="form-control" name="eidasnameid" id="eidasnameid" disabled>
                                <option value="persistent">
                                    persistent
                                </option>
                                <option value="transient">
                                    transient
                                </option>
                            </select>
                        </div>
						<div class="form-group">
							<span>
								<input class="form-horizontal" id="addIPAddress" type="checkbox" name="checkBoxIpAddress" checked/>&nbsp;
								<label for="addIPAddress">IP Address for SubjectConfirmationData</label>
							</span>
						</div>
                        <input type="hidden" name="smsspToken" value="<%=smsspToken%>"/>
						<input type="hidden" name="username" value="<%=username%>"/>
						<input type="hidden" name="callback" value="<%=callback%>"/>

                        <label for="jSonRequestDecoded">SmsspToken Request</label>
                        <textarea name="jSonRequestDecoded" id="jSonRequestDecoded" class="form-control" required="true" rows="10"><%=jSonRequestDecoded%></textarea>
						<span>
								<input class="form-horizontal" id="doNotmodifyTheResponse" type="checkbox" name="doNotmodifyTheResponse" checked/>&nbsp;
								<label for="doNotmodifyTheResponse">Do Not Modify The Response</label>
							</span>
						<input type="hidden" name="signAssertion" value="<%=signAssertion%>"/>
						<input type="hidden" name="encryptAssertion" value="<%=encryptAssertion%>"/>
						<button type="submit" id="idpSubmitbutton" class="btn btn-default btn-lg btn-block">Submit</button>
					</form>

				</div>

			</div>

		</div>
	</div>
</div>
<jsp:include page="footer.jsp"/>
<jsp:include page="modal_user.jsp"/>
</body>
</html>