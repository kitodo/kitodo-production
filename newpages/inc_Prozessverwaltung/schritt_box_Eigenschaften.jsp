<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

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
	var="item" value="#{ProzessverwaltungForm.mySchritt.eigenschaftenList}"
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
				property="#{ProzessverwaltungForm.mySchrittEigenschaft}"
				value="#{item}" />
			<x:updateActionListener
				property="#{ProzessverwaltungForm.modusBearbeiten}"
				value="schritteeigenschaft" />
		</h:commandLink>
	</h:column>
</x:dataTable>

<%-- Neu-Schaltknopf --%>
<h:commandLink action="#{ProzessverwaltungForm.SchrittEigenschaftNeu}"
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
			<h:panelGrid columns="2">

				<%-- Felder --%>
				<h:outputLabel for="eigenschafttitel" value="#{msgs.titel}" />
				<h:panelGroup>
					<h:inputText id="eigenschafttitel"
						style="width: 300px;margin-right:15px"
						value="#{ProzessverwaltungForm.mySchrittEigenschaft.titel}"
						required="true" />
					<x:message for="eigenschafttitel" style="color: red"
						detailFormat="#{msgs.keinTitelAngegeben}" />
				</h:panelGroup>

				<h:outputLabel for="eigenschaftwert" value="#{msgs.wert}" />
				<h:panelGroup>
					<h:inputText id="eigenschaftwert"
						style="width: 300px;margin-right:15px"
						value="#{ProzessverwaltungForm.mySchrittEigenschaft.wert}" />
				</h:panelGroup>
			</h:panelGrid>

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
				action="#{ProzessverwaltungForm.SchrittEigenschaftLoeschen}"
				onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')"
				rendered="#{ProzessverwaltungForm.mySchrittEigenschaft.id != null}">
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
