<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<htm:table cellpadding="3" cellspacing="0" width="100%"
	styleClass="main_statistikboxen" rendered="#{StatistikForm.showStatistics}">
	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row1">
			<h:outputText value="#{msgs.statistikDatenbank}" />
		</htm:td>
	</htm:tr>
	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row2">
			<h:panelGrid width="100%" columns="2" columnClasses="columnLinks,columnRechts">
				<h:outputText value="#{msgs.benutzer}:" />
				<h:outputText value="#{StatistikForm.anzahlBenutzer}" />
				<h:outputText value="#{msgs.benutzergruppen}:" />
				<h:outputText value="#{StatistikForm.anzahlBenutzergruppen}" />
				<h:outputText value="#{msgs.prozesse}:" />
				<h:outputText value="#{StatistikForm.anzahlProzesse}" />
				<h:outputText value="#{msgs.schritte}:" />
				<h:outputText value="#{StatistikForm.anzahlSchritte}" />
				<h:outputText value="#{msgs.vorlagen}:" />
				<h:outputText value="#{StatistikForm.anzahlVorlagen}" />
				<h:outputText value="#{msgs.werkstuecke}:" />
				<h:outputText value="#{StatistikForm.anzahlWerkstuecke}" />				
			</h:panelGrid>
		</htm:td>
	</htm:tr>
</htm:table>
