#<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html lang="de">
<head>
    <!-- [OPTIONAL] Aendern Sie hier den Titel der Seite -->
    <title>Anmeldung mit B&uuml;rgerkarte</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta http-equiv="Content-Style-Type" content="text/css">
    <meta http-equiv='pragma' content='no-cache'/>
    <meta http-equiv='cache-control' content='no-cache, no-store, must-revalidate'/>
	<meta http-equiv="Expires" content="-1"/>
    <link href="resources/css/moa.css" rel="stylesheet" type="text/css" />
    <script src="js/browser.js" type="text/javascript"></script>
    <script src="js/deployJava.js" type="text/javascript"></script>
</head>
<body>
<div id="values">
    <input type="hidden" id="MoaIdConnectionURLGlobal" name="MoaIdConnectionURL"
           value="https://apps.egiz.gv.at/moa-id-stork/StartAuthentication"/>
    <input type="hidden" id="OAGlobal" name="OA" value="https://apps.egiz.gv.at/moa.stork.web/RetrieveMOAAuthData"/>
    <input type="hidden" id="bkuURILocalGlobal" name="bkuURILocal"
           value="https://127.0.0.1:3496/https-security-layer-request"/>
    <input type="hidden" id="bkuURIOnlineGlobal" name="bkuURIOnline"
           value="https://apps.egiz.gv.at/bkuonline/http-security-layer-request"/>
    <input type="hidden" id="bkuURIMobileGlobal" name="bkuURIMobile"
           value="https://www.a-trust.at/mobile/https-security-layer-request/default.aspx"/>
    <!-- input type="hidden" id="Template" name="Template" value=""/-->
</div>
<div id="wrapper">
    <p id="skiplinks">
        <a href="#content">Zum Inhalt springen</a>
    </p>

    <div id="banner">
        <!-- [OPTIONAL] Aendern Sie hier die Titelueberschrift der Seite) -->
        <div id="bannerleft">
            <h1>Anmeldung mit B&uuml;rgerkarte</h1>
            <!-- Meldung im Browser, wenn JavaScript nicht aktiviert -->
            <noscript>
                <p>
                    Bitte aktivieren Sie JavaScript.
                </p>
            </noscript>
        </div>
        <!-- [OPTIONAL] Aendern Sie hier das Logo der Seite (und Alternativtext fuer das Bild) -->
        <div id="bannerright">
            <img src="resources/img/storkLogo.png" alt="Logo">
        </div>
    </div>
    <div id="main">
        <div id="leftcontent">
            <h2 id="tabheader" class="dunkel">
                Login mit B&uuml;rgerkarte
            </h2>

            <div id="bkulogin" class="hell">
                <div id="bkukarte" class="hell">
                    <button name="bkuButton" type="button">KARTE</button>
                </div>
                <div id="bkuhandy" class="hell">
                    <button name="bkuButton" type="button">HANDY</button>
                </div>
            </div>

            <div id="installJava" style="display:none" class="hell">
                Um die Online BKU verwenden zu koennen benoetigen Sie die aktuellste Version von Java 1.6 oder hoeher.
                Unter <a href="http://java.sun.com/webapps/getjava/BrowserRedirect?host=java.com" target="_blank">http://java.sun.com/</a>
                koennen sie Java herunterladen. Sie bekommen diese Meldung auch, wenn Java in den Browser Einstellungen
                deaktiviert wurde.
            </div>

            <div id="BrowserNOK" style="display:none" class="hell">
                Der von Ihnen verwendete Browser wird derzeit nicht unterstuetzt. Fuer nicht kompatible Browser
                verwenden Sie bitte den Button Lokale BKU. Um die Online BKU verwenden zu koennen verwenden Sie bitte
                Internet Explorer, Firefox, Safari oder Google Chrome in der aktuellsten Version.
            </div>

            <div id="sessionCookiesNotAvailable" style="display:none" class="hell">
                The application server you use doesn't support session cookies. <br/>
                At the current moment the application won't work if they are not allowed to track session.
                <bold>Please contact your system administrator for further informations.</bold>
            </div>

            <div id="localBKU" style="display:none" class="hell">
                <hr>
                <!-- [MUSS] Geben Sie hier die URL zum Aufruf von MOA-ID an -->
                <!-- z.B.: action="https://yoururl.at/moa-id-auth/StartAuthentication?Target=IT&OA=https://youronlineapplication.at"-->
                <form action="${moaConfigData.moaAuthURI}" id="moaLocal" method="post">
                    <input name="show" value="false" type="hidden">
                    <!-- [MUSS] Geben Sie hier die URL zum MOA-ID Template fuer die lokale BKU an -->
                    <!-- z.B.: value="https://yoururl.at/moa-id-auth/template_localBKU.html"-->
                    <input id="OALocal" name="OA" value="${e:forHtml(moaConfigData.oaURI)}"
                           type="hidden">
                    <input id="bkuURILocal" name="bkuURI"
                           value="${e:forHtml(moaConfigData.bkuLocalURI)}" type="hidden">
                    <input id="TemplateLocal" name="Template"
                           value="${e:forHtml(moaConfigData.templateLocalURI)}" type="hidden">
                    <input size="400" value="Lokale BKU" class="sendButton" type="submit">
                </form>
                <p>
                    <small>Alternativ k&ouml;nnen Sie eine lokal installierte BKU verwenden.</small>
                </p>
            </div>

            <div id="tab" class="hell">
                <a href="info_bk.html" class="link_nav">Informationen zur B&uuml;rgerkarte</a>
            </div>

            <div id="tab" class="hell">
                <a href="privacy.html" class="link_nav">Datenschutzerkl&auml;rung / Privacy Statement</a>
            </div>

            <div id="navlist" class="hell">
                <ul>
                    <li>
                        <a href="http://www.buergerkarte.at" target="_blank">B&uuml;rgerkarte.at</a></li>
                    <li>
                        <a href="http://www.digitales.oesterreich.gv.at/" target="_blank">Digitales &Ouml;sterreich</a>
                    </li>
                    <li>
                        <a href="http://www.a-sit.at/" target="_blank">A-SIT</a>
                    </li>
                    <li>
                        <a href="http://www.a-trust.at/" target="_blank">A-Trust</a>
                    </li>
                    <li>
                        <a href="http://www.egiz.gv.at/" target="_blank">EGIZ</a>
                    </li>
                </ul>
            </div>
        </div>

        <div id="rightcontent">
            <p>
                <a href="http://www.buergerkarte.at/de/aktivieren/online.html" target="_blank"><img
                        src="resources/img/ecard_aktivieren.jpg" border="0" alt="eCard online aktivieren" width="210"></a>
            </p>

            <p>
                <a href="http://www.buergerkarte.at/de/aktivieren/mobil.html" target="_blank"><img
                        src="resources/img/mobilsig_aktivieren.jpg" border="0" alt="Mobile Signatur aktivieren" width="210"></a>
            </p>

            <p>
                <a href="http://validator.w3.org/check?uri=referer" target="_blank">
                    <img src="resources/img/valid-html401.png" alt="Valid HTML 4.01 Transitional" height="31" width="88"> </a>
            </p>
        </div>

        <div id="centercontent">

            <h2 id="contentheader" class="dunkel">
                Hinweise zur Anmeldung </h2>

            <div id="content" class="hell">
                <p>Sie melden sich bei einer Anwendung im Rahmen des EU Pilotprojekts STORK an. </p>

                <p>

                <div style="padding-left: 20px"><img src="resources/img/info.png" alt="Informationen zu STORK" width="16"
                                                     height="16" style="vertical-align: middle">&nbsp;<a
                        href="http://www.eid-stork.eu/" target="_blank">Informationen zu STORK</a></div>
                </p>
                <p>

                <div><img src="resources/img/Feedback.jpg" alt="Feedback" width="26" height="26" style="vertical-align: middle">&nbsp;Geben
                    Sie uns Ihr Feedback - ben&uuml;tzen Sie dazu folgende Online-Feedbackformulare:
                </div>
                </p>
                <div style="padding-top: 20px">
                    <p><a href="https://www.eid-stork.eu/pilots/pilot1.htm" target="_blank">Pilotanwendung 1: Giant
                        Portals</a></p>

                    <p><a href="https://www.eid-stork.eu/pilots/pilot2.htm" target="_blank">Pilotanwendung 2:
                        Safer-Chat</a></p>

                    <p><a href="https://www.eid-stork.eu/pilots/pilot3.htm" target="_blank">Pilotanwendung 3: Student
                        Mobility</a></p>

                    <p><a href="https://www.eid-stork.eu/pilots/pilot4.htm" target="_blank">Pilotanwendung 4:
                        Cross-Border eDelivery</a></p>

                    <p><a href="https://www.eid-stork.eu/pilots/pilot6.htm" target="_blank">Pilotanwendung 6: Dienste
                        der EU Kommission via ECAS</a></p>

                    <p>&nbsp;</p>

                    <p style="text-decoration: underline">Weitere Pilotanwendungen:</p>

                    <p>Adress&auml;nderung (&Ouml;sterreich nimmt an diesem Piloten nicht teil)</p>

                    <p>&nbsp;</p>

                    <p>Hilfe bei Problemen finden Sie &uuml;ber das <a
                            href="https://www.buergerkarte.at/mvnforum/mvnforum/listthreads?forum=5" target="_blank">B&uuml;rgerkarten-Support-Forum</a>.
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
<script src="js/moaanmeldung.js" type="text/javascript"></script>
</html>