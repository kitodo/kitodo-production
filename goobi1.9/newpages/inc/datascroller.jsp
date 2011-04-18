<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<h:panelGroup rendered="#{mypage.totalResults > LoginForm.myBenutzer.tabellengroesse}">
	<%-- erste und vorherige Seite --%>
	<h:commandLink action="#{mypage.cmdMoveFirst}" id="gofirst">
		<h:graphicImage url="/newpages/images/datascroller/arrow-first.gif"
			style="margin-right:4px;vertical-align: middle;" />
	</h:commandLink>
	<h:commandLink action="#{mypage.cmdMovePrevious}" id="goprevious">
		<h:graphicImage url="/newpages/images/datascroller/arrow-previous.gif"
			style="margin-right:4px;vertical-align: middle;" />
	</h:commandLink>

	<%-- aktuelle Seite anzeigen --%>
	<x:outputText id="txtMoveTo1" forceId="true"
		value="#{msgs.seite} #{mypage.pageNumberCurrent} #{msgs.von} #{mypage.pageNumberLast}"
		onclick="document.getElementById('txtMoveTo2').style.display='inline';
			       document.getElementById('txtMoveTo1').style.display='none'; 
			       document.getElementById('txtMoveTo2').focus();
			       document.getElementById('txtMoveTo2').select();" />

	<%-- Seite direkt anspringen --%>
	<x:inputText id="txtMoveTo2" forceId="true"
		value="#{mypage.txtMoveTo}"
		style="display:none;font-size:9px;width:30px" required="true"
		onblur="document.getElementById('txtMoveTo2').style.display='none';
      				 document.getElementById('txtMoveTo1').style.display='inline';" 
      				 onkeypress="return submitEnter('cmdMoveTo',event)"/>
	<x:commandButton action="#{NavigationForm.Reload}" id="cmdMoveTo" forceId="true" value="go" style="display:none"/>
	
	<%-- nächste und letzte Seite --%>
	<h:commandLink action="#{mypage.cmdMoveNext}" id="gonext">
		<h:graphicImage url="/newpages/images/datascroller/arrow-next.gif"
			style="margin-left:4px;margin-right:4px;vertical-align: middle;" />
	</h:commandLink>
	<h:commandLink action="#{mypage.cmdMoveLast}" id="golast">
		<h:graphicImage url="/newpages/images/datascroller/arrow-last.gif"
			style="margin-right:4px;vertical-align: middle;" />
	</h:commandLink>
</h:panelGroup>