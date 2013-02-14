<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>

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

<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- ++++++++++++++++     Eigenschaftentabelle      ++++++++++++++++ --%>
<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>

<htm:h4 style="margin-top:15"
	rendered="#{ProzessverwaltungForm.modusBearbeiten!='schritteeigenschaft'}">
	<h:outputText value="#{msgs.eigenschaften}" />
</htm:h4>

<x:dataTable id="eigenschaften" styleClass="standardTable" width="100%"
	cellspacing="1px" cellpadding="1px" headerClass="standardTable_Header"
	rowClasses="standardTable_Row1"
	columnClasses="standardTable_Column,standardTable_Column,standardTable_ColumnCentered"
	var="item" value="#{ProzessverwaltungForm.mySchritt.displayProperties.sortedProperties}"
	rendered="#{ProzessverwaltungForm.modusBearbeiten!='schritteeigenschaft'}">

	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.titel}" />
		</f:facet>
		<h:outputText value="#{item.titel}" />
	</h:column>

	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.wert}" />
		</f:facet>
		<h:outputText value="#{item.wert}" />
	</h:column>

	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.auswahl}" />
		</f:facet>
		<%-- Bearbeiten-Schaltknopf --%>
		<h:commandLink action="ProzessverwaltungBearbeitenSchritt"
			title="#{msgs.bearbeiten}">
			<h:graphicImage value="/newpages/images/buttons/edit.gif" />
			<x:updateActionListener
				property="#{ProzessverwaltungForm.mySchritt.displayProperties.currentProperty}"
				value="#{item}" />
			<x:updateActionListener
				property="#{ProzessverwaltungForm.modusBearbeiten}"
				value="schritteeigenschaft" />
		</h:commandLink>
	</h:column>
</x:dataTable>

<%-- Neu-Schaltknopf --%>
<h:commandLink action="#{ProzessverwaltungForm.mySchritt.displayProperties.createNewProperty}"
	value="#{msgs.eigenschaftHinzufuegen}"
	title="#{msgs.eigenschaftHinzufuegen}"
	rendered="#{ProzessverwaltungForm.modusBearbeiten!='schritteeigenschaft'}">
	<x:updateActionListener
		property="#{ProzessverwaltungForm.modusBearbeiten}"
		value="schritteeigenschaft" />
</h:commandLink>


<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- +++++++++++++++     Eigenschaft bearbeiten      ++++++++++++++++ --%>
<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>

<htm:h4 style="margin-top:15"
	rendered="#{ProzessverwaltungForm.modusBearbeiten=='schritteeigenschaft'}">
	<h:outputText value="#{msgs.eigenschaftBearbeiten}" />
</htm:h4>
<%-- Box für die Bearbeitung der Details --%>
<htm:table cellpadding="3" cellspacing="0" width="100%"
	styleClass="eingabeBoxen"
	rendered="#{ProzessverwaltungForm.modusBearbeiten=='schritteeigenschaft'}">

	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row1" colspan="2">
			<h:outputText value="#{msgs.eigenschaft}" />
		</htm:td>
	</htm:tr>

	<%-- Formular für die Bearbeitung der Eigenschaft --%>
	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row2" colspan="2">
			<x:aliasBean alias="#{myitem}" value="#{ProzessverwaltungForm.mySchritt.displayProperties.currentProperty}">
				<h:panelGrid columns="2">


					<%-- Felder --%>
					<h:outputLabel for="eigenschafttitel" value="#{msgs.titel}" />
					<h:panelGroup>
						<h:inputText id="eigenschafttitel" style="width: 500px;margin-right:15px"
							value="#{ProzessverwaltungForm.mySchritt.displayProperties.currentProperty.titel}" required="true" />
						<x:message for="eigenschafttitel" style="color: red" detailFormat="#{msgs.keinTitelAngegeben}" />
					</h:panelGroup>



					<h:outputText value="#{msgs.wert}" />
					<%-- textarea --%>
					<h:panelGroup id="prpvw15_1" rendered="#{((myitem.type.name == 'string') || (myitem.type.name == 'null'))}">
						<h:inputText id="file" style="width: 500px;margin-right:15px" value="#{myitem.selectedValue}" required="#{myitem.required}" />
					</h:panelGroup>

					<%-- numbers only --%>
					<h:panelGroup id="prpvw15_1mnk" rendered="#{myitem.type.name == 'integer' || myitem.type.name == 'number'}">

						<h:inputText id="numberstuff122334mnktodo" style="width: 500px;margin-right:15px" value="#{myitem.selectedValue}" required="#{myitem.required}">
							<f:validateLongRange minimum="0" />
						</h:inputText>
					</h:panelGroup>

					<%--  SelectOneMenu --%>
					<h:panelGroup id="prpvw15_2" rendered="#{(myitem.type.name == 'list')}">
						<h:selectOneMenu value="#{myitem.selectedValue}" style="width: 500px;margin-right:15px" id="prpvw15_2_1">
							<si:selectItems id="prpvw15_2_2" value="#{myitem.valuesList}" var="myitems" itemLabel="#{myitems}" itemValue="#{myitems}" />
						</h:selectOneMenu>
					</h:panelGroup>

					<%--  SelectManyMenu --%>
					<h:panelGroup id="prpvw15_3" rendered="#{(myitem.type.name == 'listmultiselect')}">
						<h:selectManyListbox id="prpvw15_3_1" style="width: 500px;margin-right:15px" value="#{myitem.selectedValuesList}" required="#{myitem.required}"
							size="10">
							<si:selectItems id="prpvw15_3_2" value="#{myitem.valuesList}" var="myitems" itemLabel="#{myitems}" itemValue="#{myitems}" />
						</h:selectManyListbox>
					</h:panelGroup>

					<%--  Boolean --%>
					<h:panelGroup id="prpvw15_4" rendered="#{(myitem.type.name == 'boolean')}">
						<h:selectOneMenu value="#{myitem.selectedValue}" style="width: 500px;margin-right:15px" id="prpvw15_4_1" required="#{myitem.required}">
							<f:selectItem id="prpvw15_4_2" itemValue="true" itemLabel="#{msgs.yes}" />
							<f:selectItem id="prpvw15_4_3" itemValue="false" itemLabel="#{msgs.no}" />
						</h:selectOneMenu>
					</h:panelGroup>

					<%--  Date  --%>
					<h:panelGroup id="prpvw15_5" style="width: 500px;margin-right:15px" rendered="#{(myitem.type.name == 'date')}">
						<rich:calendar id="prpvw15_5_1" datePattern="dd.MM.yyyy" value="#{myitem.date}" enableManualInput="true">
						</rich:calendar>
					</h:panelGroup>

				</h:panelGrid>
			</x:aliasBean>
		</htm:td>
	</htm:tr>

	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row3" align="left">
			<h:commandButton value="#{msgs.abbrechen}"
				action="#{NavigationForm.Reload}" immediate="true">
				<x:updateActionListener
					property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
			</h:commandButton>
		</htm:td>
		<htm:td styleClass="eingabeBoxen_row3" align="right">
			<h:commandButton value="#{msgs.loeschen}"
				action="#{ProzessverwaltungForm.mySchritt.displayProperties.deleteProperty}"
				onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')"
				rendered="#{ProzessverwaltungForm.mySchritt.displayProperties.currentProperty.id != null}">
				<x:updateActionListener
					property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
			</h:commandButton>
			<h:commandButton value="#{msgs.uebernehmen}"
				action="#{ProzessverwaltungForm.SchrittEigenschaftUebernehmen}">
				<x:updateActionListener
					property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
			</h:commandButton>
		</htm:td>
	</htm:tr>
</htm:table>
<%-- // Box für die Bearbeitung der Details --%>
