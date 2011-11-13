<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<htm:table cellpadding="3" cellspacing="0" width="100%"
	styleClass="main_statistikboxen"
	rendered="#{ProzessverwaltungForm.modusBearbeiten!='vorlage'}">

	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row1">
			<h:outputText value="#{msgs.vorlage}" />
		</htm:td>
		<htm:td styleClass="main_statistikboxen_row1" align="right">
			<h:commandLink action="#{ProzessverwaltungForm.Reload}">
				<h:graphicImage value="/newpages/images/reload.gif" />
			</h:commandLink>
		</htm:td>
	</htm:tr>

		<htm:tr>
			<htm:td styleClass="main_statistikboxen_row2" colspan="2">

				<htm:table border="0" width="90%" cellpadding="2">
					<htm:tr>
						<htm:td width="150">
							<h:outputText value="#{msgs.herkunft}:" />
						</htm:td>
						<htm:td>
							<h:outputText value="#{ProzessverwaltungForm.myVorlage.herkunft}" />
						</htm:td>
						<htm:td rowspan="2" align="right">
							<h:commandLink title="#{msgs.bearbeiten}"
								action="#{NavigationForm.Reload}">
								<h:graphicImage value="/newpages/images/buttons/edit_20.gif" />
								<x:updateActionListener
									property="#{ProzessverwaltungForm.modusBearbeiten}"
									value="vorlage" />
							</h:commandLink>

							<h:commandLink title="#{msgs.loeschen}"
								action="#{ProzessverwaltungForm.VorlageLoeschen}"
								style="margin-right:20px">
								<h:graphicImage value="/newpages/images/buttons/waste1a_20px.gif" />
							</h:commandLink>
						</htm:td>
					</htm:tr>

				</htm:table>

			</htm:td>
		</htm:tr>
</htm:table>


<%-- Box für die Bearbeitung der Details --%>
<htm:table cellpadding="3" cellspacing="0" width="100%"
	styleClass="eingabeBoxen"
	rendered="#{ProzessverwaltungForm.modusBearbeiten=='vorlage'}">

	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row1" colspan="2">
			<h:outputText value="#{msgs.vorlage}" />
		</htm:td>
	</htm:tr>

	<h:form id="vorform" onkeypress="ifEnterClick(event, 'vorform2:vorform:absenden');">
		<%-- Formular für die Bearbeitung der Vorlage --%>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2" colspan="2">
				<h:panelGrid columns="2">

					<%-- Felder --%>
					<h:outputLabel for="titel2" value="#{msgs.herkunft}" />
					<h:panelGroup>
						<h:inputText id="titel2" style="width: 300px;margin-right:15px"
							value="#{ProzessverwaltungForm.myVorlage.herkunft}"
							required="true" />
						<x:message for="titel2" style="color: red"
							replaceIdWithLabel="true" />
					</h:panelGroup>

				</h:panelGrid>

			</htm:td>
		</htm:tr>

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3" align="left">
				<h:commandButton value="#{msgs.abbrechen}" immediate="true"
					action="#{NavigationForm.Reload}">
					<x:updateActionListener
						property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
				</h:commandButton>
			</htm:td>
			<htm:td styleClass="eingabeBoxen_row3" align="right">
				<h:commandButton value="#{msgs.uebernehmen}" id="absenden"
					action="#{ProzessverwaltungForm.VorlageUebernehmen}">
					<x:updateActionListener
						property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
				</h:commandButton>
			</htm:td>
		</htm:tr>
	</h:form>
</htm:table>
<%-- // Box für die Bearbeitung der Details --%>
