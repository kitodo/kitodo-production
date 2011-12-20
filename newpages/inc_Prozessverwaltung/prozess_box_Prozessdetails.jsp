<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<%--
  ~ This file is part of the Goobi Application - a Workflow tool for the support of
  ~ mass digitization.
  ~
  ~ Visit the websites for more information.
  ~     - http://gdz.sub.uni-goettingen.de
  ~     - http://www.goobi.org
  ~     - http://launchpad.net/goobi-production
  ~
  ~ This program is free software; you can redistribute it and/or modify it under
  ~ the terms of the GNU General Public License as published by the Free Software
  ~ Foundation; either version 2 of the License, or (at your option) any later
  ~ version.
  ~
  ~ This program is distributed in the hope that it will be useful, but WITHOUT ANY
  ~ WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  ~ PARTICULAR PURPOSE. See the GNU General Public License for more details. You
  ~ should have received a copy of the GNU General Public License along with this
  ~ program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  ~ Suite 330, Boston, MA 02111-1307 USA
  --%>

<%-- Box für die Darstellung der Details --%>
<htm:table cellpadding="3" cellspacing="0" width="100%"
	styleClass="main_statistikboxen"
	rendered="#{ProzessverwaltungForm.modusBearbeiten!='prozess'}">

	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row1">
			<h:outputText value="#{msgs.prozess}" />
		</htm:td>
		<htm:td styleClass="main_statistikboxen_row1" align="right">
			<h:commandLink action="#{ProzessverwaltungForm.Reload}">
				<h:graphicImage value="/newpages/images/reload.gif" />
			</h:commandLink>
		</htm:td>
	</htm:tr>


	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row2" colspan="2">
			<htm:table border="0" width="100%" cellpadding="2">
				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.prozessTitel}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{ProzessverwaltungForm.myProzess.titel}" />
					</htm:td>
					<htm:td rowspan="2" align="right"
						rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
						<h:commandLink title="#{msgs.prozessdetailsBearbeiten}"
							action="#{NavigationForm.Reload}" style=";margin-right:20px">
							<h:graphicImage value="/newpages/images/buttons/edit_20.gif" />
							<x:updateActionListener
								property="#{ProzessverwaltungForm.modusBearbeiten}"
								value="prozess" />
						</h:commandLink>
					</htm:td>
				</htm:tr>

				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.projekt}:" />
					</htm:td>
					<htm:td>
						<h:outputText
							value="#{ProzessverwaltungForm.myProzess.projekt.titel}" />
					</htm:td>
				</htm:tr>

	<%-- 			<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.ausgabename}:" />
					</htm:td>
					<htm:td>
						<h:outputText
							value="#{ProzessverwaltungForm.myProzess.ausgabename}" />
					</htm:td>
				</htm:tr>
--%>
				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.erstellungsdatum}:" />
					</htm:td>
					<htm:td>
						<h:outputText
							value="#{ProzessverwaltungForm.myProzess.erstellungsdatum}" />
					</htm:td>
				</htm:tr>

				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.regelsatz}:" />
					</htm:td>
					<htm:td>
						<h:outputText
							value="#{ProzessverwaltungForm.myProzess.regelsatz.titel}" />
					</htm:td>
				</htm:tr>

				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.inAuswahllisteAnzeigen}:" />
					</htm:td>
					<htm:td>
						<h:graphicImage value="/newpages/images/check_false.gif"
							rendered="#{not ProzessverwaltungForm.myProzess.inAuswahllisteAnzeigen}" />
						<h:graphicImage value="/newpages/images/check_true.gif"
							rendered="#{ProzessverwaltungForm.myProzess.inAuswahllisteAnzeigen}" />
					</htm:td>
				</htm:tr>

				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.istTemplate}:" />
					</htm:td>
					<htm:td>
						<h:graphicImage value="/newpages/images/check_false.gif"
							rendered="#{not ProzessverwaltungForm.myProzess.istTemplate}" />
						<h:graphicImage value="/newpages/images/check_true.gif"
							rendered="#{ProzessverwaltungForm.myProzess.istTemplate}" />
					</htm:td>
				</htm:tr>

				<htm:tr>
					<htm:td width="150">
						<h:outputText styleClass="text_light" value="#{msgs.id}:" />
					</htm:td>
					<htm:td>
						<h:outputText styleClass="text_light"
							value="#{ProzessverwaltungForm.myProzess.id}" />
					</htm:td>
				</htm:tr>
			</htm:table>

		</htm:td>
	</htm:tr>
</htm:table>



<%-- Box für die Bearbeitung der Details --%>
<htm:table cellpadding="3" cellspacing="0" width="100%"
	styleClass="eingabeBoxen"
	rendered="#{ProzessverwaltungForm.modusBearbeiten=='prozess'}">

	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row1" colspan="2">
			<h:outputText value="#{msgs.prozess}" />
		</htm:td>
	</htm:tr>
	<%-- Formular für die Bearbeitung des Prozesses --%>
	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row2" colspan="2">
			<h:panelGrid columns="2">

				<%-- Felder --%>
				<h:outputLabel for="titel" value="#{msgs.prozessTitel}" />
				<h:panelGroup>
					<h:inputText id="titel" style="width: 300px;margin-right:15px"
						value="#{ProzessverwaltungForm.myNewProcessTitle}" required="true" />
					<x:message for="titel" style="color: red"
						detailFormat="#{msgs.keinTitelAngegeben}" />
				</h:panelGroup>

				<h:outputLabel for="prozess" value="#{msgs.projekt}" />
				<h:panelGroup>
					<h:selectOneMenu id="prozess"
						style="width: 300px;margin-right:15px"
						value="#{ProzessverwaltungForm.projektAuswahl}" required="true">
						<f:selectItem itemValue="" itemLabel="#{msgs.bitteAuswaehlen}" />
						<f:selectItems
							value="#{ProzessverwaltungForm.projektAuswahlListe}" />
					</h:selectOneMenu>
					<x:message for="prozess" style="color: red"
						detailFormat="#{msgs.keinProzessAngegeben}" />
				</h:panelGroup>

				<%-- Ausgabename
				<h:outputLabel for="ausgabename" value="#{msgs.ausgabename}" />
				<h:panelGroup>
					<h:inputText id="ausgabename"
						style="width: 300px;margin-right:15px"
						value="#{ProzessverwaltungForm.myProzess.ausgabename}" />
					<x:message for="ausgabename" style="color: red" />
				</h:panelGroup>
 --%>
				<%-- Preferences --%>
				<h:outputLabel for="Regelsatz"
					value="#{msgs.regelsatz}" />
				<h:panelGroup>
					<h:selectOneMenu id="Regelsatz"
						value="#{ProzessverwaltungForm.myProzess.regelsatz}"
						converter="RegelsatzConverter"
						style="width: 300px;margin-right:15px" required="true">
						<f:selectItems value="#{HelperForm.regelsaetze}" />
					</h:selectOneMenu>
					<x:message for="Regelsatz" style="color: red"
						replaceIdWithLabel="true" />
				</h:panelGroup>

				<h:outputText value="#{msgs.inAuswahllisteAnzeigen}" />
				<h:selectBooleanCheckbox
					value="#{ProzessverwaltungForm.myProzess.inAuswahllisteAnzeigen}" />

				<h:outputText value="#{msgs.istTemplate}" />
				<h:selectBooleanCheckbox id="check"
					value="#{ProzessverwaltungForm.myProzess.istTemplate}" />
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
			<h:commandButton value="#{msgs.contentLoeschen}"
				action="#{ProzessverwaltungForm.ContentLoeschen}"
				onclick="return confirm('#{msgs.sollDerContentWirklichGeloeschtWerden}?')"
				rendered="#{ProzessverwaltungForm.myProzess.id != null}" />
			<h:commandButton value="#{msgs.loeschen}"
				action="#{ProzessverwaltungForm.Loeschen}"
				onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')"
				rendered="#{ProzessverwaltungForm.myProzess.id != null}" />
			<h:commandButton value="#{msgs.speichern}"
				action="#{ProzessverwaltungForm.Speichern}">
				<x:updateActionListener
					property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
			</h:commandButton>
		</htm:td>
	</htm:tr>

</htm:table>
<%-- // Box für die Bearbeitung der Details --%>
