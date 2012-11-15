<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<%-- ########################################

        Formular für neue Person
        
  #########################################--%>
<h:panelGroup rendered="#{Metadaten.modusHinzufuegenPerson}">
	<htm:h3 style="margin-top:10px">
		<h:outputText value="#{msgs.personBearbeiten}" />
	</htm:h3>
	<htm:table cellpadding="3" cellspacing="0" style="width:540px"
		styleClass="eingabeBoxen">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1">
				<h:outputText value="#{msgs.personBearbeiten}" />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2">

				<h:panelGrid columns="2">
					<%-- vorname --%>
					<h:outputLabel for="vorname" value="#{msgs.vorname}" />
					<h:panelGroup>
						<h:inputText id="vorname" style="width: 400px;margin-right:15px"
							value="#{Metadaten.tempPersonVorname}" />
						<x:message for="vorname" style="color: red"
							replaceIdWithLabel="true" />
					</h:panelGroup>

					<%-- nachname --%>
					<h:outputLabel for="nachname" value="#{msgs.nachname}" />
					<h:panelGroup>
						<h:inputText id="nachname" style="width: 400px;margin-right:15px"
							value="#{Metadaten.tempPersonNachname}" />
						<x:message for="nachname" style="color: red"
							replaceIdWithLabel="true" />
					</h:panelGroup>

					<%-- Rolle --%>
					<h:outputLabel for="Rolle" value="#{msgs.rolle}" />
					<h:panelGroup>
						<h:selectOneMenu id="Rolle" value="#{Metadaten.tempPersonRolle}"
							style="width: 400px;margin-right:15px">
							<f:selectItems value="#{Metadaten.addableRollen}" />
						</h:selectOneMenu>
						<x:message for="Rolle" style="color: red"
							replaceIdWithLabel="true" />
					</h:panelGroup>

				</h:panelGrid>
			</htm:td>
		</htm:tr>

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3">
				<h:commandButton action="#{Metadaten.Abbrechen}"
					value="#{msgs.abbrechen}" immediate="true"></h:commandButton>
				<x:commandButton id="absenden2" forceId="true" type="submit"
					action="#{Metadaten.SpeichernPerson}"
					value="#{msgs.dieAenderungenSpeichern}"></x:commandButton>
			</htm:td>
		</htm:tr>

	</htm:table>
</h:panelGroup>
