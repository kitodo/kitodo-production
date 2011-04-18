<html>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<f:view locale="#{SpracheForm.locale}">
	<%@include file="/newpages/inc/head.jsp"%>
	<body>


	<h:form id="lockform">
		<h:outputText
			value="Die Metadaten wurden von Ihnen 30 min nicht bearbeitet und wurden daher 
		wieder f�r andere Benutzer freigegeben. Sie befinden sich gerade in Arbeit."
			style="font-size: 12px;color: red" />
		<htm:br />
		<htm:br />
		<h:commandLink action="#{Metadaten.goZurueck}" target="_parent">
			<h:outputText value="zur�ck" style="font-size: 11px" />
		</h:commandLink>

	</h:form>

	</body>
</f:view>
</html>

