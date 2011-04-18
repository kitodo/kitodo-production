<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="/newpages/inc/head.jsp"%>
	<body>

	<%-- globale Warn- und Fehlermeldungen --%>
	<h:messages globalOnly="true" errorClass="text_red"
		infoClass="text_blue" showDetail="true" showSummary="true"
		tooltip="true" />


	<h:form id="testform1">
		<h:panelGrid columns="2">

			<%-- Felder --%>
			<h:outputLabel for="text1" value="Text1" />
			<h:panelGroup>
				<h:inputText id="text1" style="width: 500px;margin-right:15px"
					value="#{TestForm.text1}" />
				<x:message for="text1" style="color: red" replaceIdWithLabel="true" />
			</h:panelGroup>

			<h:outputLabel for="text2" value="Text2" />
			<h:panelGroup>
				<h:inputText id="text2" style="width: 500px;margin-right:15px"
					value="#{TestForm.text2}" />
				<x:message for="text2" style="color: red" replaceIdWithLabel="true" />
			</h:panelGroup>

			<h:outputLabel for="text3" value="Text3" />
			<h:panelGroup>
				<h:inputText id="text3" style="width: 500px;margin-right:15px"
					value="#{TestForm.text3}" />
				<x:message for="text3" style="color: red" replaceIdWithLabel="true" />
			</h:panelGroup>
		</h:panelGrid>

		<h:commandLink value="weitergaben" action="#{TestForm.TestHttp}" />

	</h:form>
	</body>
</f:view>

</html>
