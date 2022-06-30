var BrowserDetect = {
	init: function () {
		this.browser = this.searchString(this.dataBrowser) || "An unknown browser";
		this.version = this.searchVersion(navigator.userAgent)
			|| this.searchVersion(navigator.appVersion)
			|| "an unknown version";
		this.OS = this.searchString(this.dataOS) || "an unknown OS";
	},
	searchString: function (data) {
		for (var i=0;i<data.length;i++)	{
			var dataString = data[i].string;
			var dataProp = data[i].prop;
			this.versionSearchString = data[i].versionSearch || data[i].identity;
			if (dataString) {
				if (dataString.indexOf(data[i].subString) != -1)
					return data[i].identity;
			}
			else if (dataProp)
				return data[i].identity;
		}
	},
	searchVersion: function (dataString) {
		var index = dataString.indexOf(this.versionSearchString);
		if (index == -1) return;
		return parseFloat(dataString.substring(index+this.versionSearchString.length+1));
	},
	dataBrowser: [
		{ 	string: navigator.userAgent,
			subString: "OmniWeb",
			versionSearch: "OmniWeb/",
			identity: "OmniWeb"
		},
		{
			string: navigator.vendor,
			subString: "Apple",
			identity: "Safari"
		},
		{
			prop: window.opera,
			identity: "Opera"
		},
		{
			string: navigator.vendor,
			subString: "iCab",
			identity: "iCab"
		},
		{
			string: navigator.vendor,
			subString: "KDE",
			identity: "Konqueror"
		},
		{
			string: navigator.userAgent,
			subString: "Firefox",
			identity: "Firefox"
		},
		{
			string: navigator.vendor,
			subString: "Camino",
			identity: "Camino"
		},
		{		// for newer Netscapes (6+)
			string: navigator.userAgent,
			subString: "Netscape",
			identity: "Netscape"
		},
		{
			string: navigator.userAgent,
			subString: "MSIE",
			identity: "Explorer",
			versionSearch: "MSIE"
		},
		{
			string: navigator.userAgent,
			subString: "Gecko",
			identity: "Mozilla",
			versionSearch: "rv"
		},
		{ 		// for older Netscapes (4-)
			string: navigator.userAgent,
			subString: "Mozilla",
			identity: "Netscape",
			versionSearch: "Mozilla"
		}
	],
	dataOS : [
		{
			string: navigator.platform,
			subString: "Win",
			identity: "Windows"
		},
		{
			string: navigator.platform,
			subString: "Mac",
			identity: "Mac"
		},
		{
			string: navigator.platform,
			subString: "Linux",
			identity: "Linux"
		}
	]

};
BrowserDetect.init();
function writeBrowser() {

nok=false;
			if(BrowserDetect.browser.toLowerCase() == "explorer") {
			if(eval(BrowserDetect.version) < 6) {
				nok=true;
			} 
		}
		if(BrowserDetect.browser.toLowerCase() == "opera") {
			if(eval(BrowserDetect.version) < 9) {
				nok=true;
			}
		}	
		if(BrowserDetect.browser.toLowerCase() == "firefox") {
			if(eval(BrowserDetect.version) < 1.5) {
				nok=true;
			} 
		}	

 if (nok==true) {
 
		document.write("<TABLE BORDER=0 CELLPADDING=0 WIDTH='100%'><TR><TD VALIGN=TOP WIDTH='85%' BGCOLOR='#003399'><TABLE id='erlaeut' BORDER=1"); 
		document.write("CELLSPACING=5 CELLPADDING=2 WIDTH='100%' ><TR><TD WIDTH='100%' BGCOLOR='#FFFFFF'><FONT COLOR='#000000'>");
		document.write("Sehr geehrter FinanzOnline-Teilnehmer!<p>Die Men&uuml;f&uuml;hrung in FinanzOnline wurde aus technischen Gr&uuml;nden ge&auml;ndert. Da Sie m&ouml;glicherweise eine veraltete und nicht mehr dem letzten Sicherheitsstandard entsprechende Version Ihres Webbrowsers verwenden, besteht ab diesem Zeitpunkt die M&ouml;glichkeit, dass das Men&uuml; in FinanzOnline nicht richtig angezeigt werden kann.</p><p>Wir empfehlen daher bereits heute auch zu Ihrer Sicherheit, ein Update auf die aktuell g&uuml;ltige Browserversion durchzuf&uuml;hren. Die unterst&uuml;tzten Webbrowser finden Sie auf der <a href='https://www.bmf.gv.at/EGovernment/FINANZOnline/Browsereinstellungen/_start.htm' target='_blank'>BMF-Homepage unter 'E-Government'/'FinanzOnline'/'Browsereinstellungen'</a>.</p>");
		
		
/*		document.write("Verwendeter Browser: " + BrowserDetect.browser + " " + BrowserDetect.version + " / " + BrowserDetect.OS);
		if(BrowserDetect.browser.toLowerCase() == "explorer") {
			if(eval(BrowserDetect.version) < 6) {
				document.write("<font color='red'> *** nicht supported ***</font>");
			} else {
				//document.write("<font color='green'> *** supported ***</font>");
			}
		}
		if(BrowserDetect.browser.toLowerCase() == "opera") {
			if(eval(BrowserDetect.version) < 9) {
				document.write("<font color='red'> *** nicht supported ***</font>");
			} else {
				//document.write("<font color='green'> *** supported ***</font>");
			}
		}	
		if(BrowserDetect.browser.toLowerCase() == "firefox") {
			if(eval(BrowserDetect.version) < 1.5) {
				document.write("<font color='red'> *** nicht supported ***</font>");
			} else {
				//document.write("<font color='green'> *** supported ***</font>");
			}
		}	
//		document.write("<br><br>Vorschlag für unterstütze Browser:<br>");
//		document.write("<ul><li>Internet Explorer 6, PC</li>");
//		document.write("<li>Internet Explorer 7, PC</li>");
//		document.write("<li>Firefox (>= 1.5), PC + Mac + Linux</li>");
//		document.write("<li>Safari (>= 1.3+), Mac (<- WIE TESTEN??)</li>");
//		document.write("<li>Opera (>= 9), PC + Mac + Linux</li>");
//		document.write("</ul>");
*/
		document.write("</FONT></TD></TR></TABLE></TD></TR><TR><TD WIDTH='85%'>&nbsp;</TD></TR></TABLE>");
		
 }		
}