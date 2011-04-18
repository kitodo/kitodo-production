<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>


<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- ++++++++++++++++     Eigenschaftentabelle      ++++++++++++++++ --%>
<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>

<htm:h4 style="margin-top:15" rendered="#{ProzessverwaltungForm.modusBearbeiten!='werkstueckeigenschaft'}">
	<h:outputText value="#{msgs.eigenschaften}" />
</htm:h4>
<htm:table  width="100%" styleClass="standardTable" cellspacing="1px" cellpadding="1px"
	rendered="#{ProzessverwaltungForm.modusBearbeiten!='werkstueckeigenschaft'}">

	<htm:thead styleClass="standardTable_Header">
		<htm:th>
			<h:outputText value="#{msgs.titel}" />
		</htm:th>
		<htm:th>
			<h:outputText value="#{msgs.wert}" />
		</htm:th>
		<htm:th>
			<h:outputText value="#{msgs.auswahl}" />
		</htm:th>
	</htm:thead>

	<x:dataList var="container" value="#{ProzessverwaltungForm.myWerkstueck.displayProperties.containers}" rowCountVar="rowCount" rowIndexVar="rowIndex">
		<x:dataList var="werk_item" value="#{ProzessverwaltungForm.myWerkstueck.displayProperties.sortedProperties}">
			<htm:tr rendered="#{werk_item.container==container}" styleClass="standardTable_Row1">
				<htm:td styleClass="standardTable_Column">
					<h:outputText value="#{werk_item.titel}" />
				</htm:td>
				<htm:td styleClass="standardTable_Column">
					<h:outputText value="#{werk_item.date}" rendered="#{werk_item.type.name == 'date'}">
						<f:convertDateTime dateStyle="medium" />
					</h:outputText>
					<h:outputText value="#{werk_item.selectedValueBeautified}" rendered="#{werk_item.type.name != 'date'}" />
				</htm:td>
				<htm:td styleClass="standardTable_ColumnCentered">
					<%-- Bearbeiten-Schaltknopf --%>
					<h:commandLink action="ProzessverwaltungBearbeitenWerkstueck" title="#{msgs.bearbeiten}">
						<h:graphicImage value="/newpages/images/buttons/edit.gif" />
						<x:updateActionListener property="#{ProzessverwaltungForm.myWerkstueck.displayProperties.currentProperty}" value="#{werk_item}" />
						<x:updateActionListener property="#{ProzessverwaltungForm.modusBearbeiten}" value="werkstueckeigenschaft" />
					</h:commandLink>
					<%-- duplicate --%>
					<h:commandLink action="#{ProzessverwaltungForm.myWerkstueck.displayProperties.duplicateProperty}">
						<h:graphicImage value="/newpages/images/buttons/copy.gif" />
						<x:updateActionListener value="#{werk_item}" property="#{ProzessverwaltungForm.myWerkstueck.displayProperties.currentProperty}" />
					</h:commandLink>
				</htm:td>
			</htm:tr>
		</x:dataList>
		<htm:tr rendered="#{rowIndex + 1 < rowCount}">
			<htm:td colspan="3" styleClass="standardTable_Row1">
				<h:outputText value="&nbsp;" escape="false" />
			</htm:td>
		</htm:tr>
	</x:dataList>
</htm:table>


<%-- Neu-Schaltknopf --%>
<h:commandLink action="#{ProzessverwaltungForm.myWerkstueck.displayProperties.createNewProperty}" value="#{msgs.eigenschaftHinzufuegen}"
	title="#{msgs.eigenschaftHinzufuegen}" rendered="#{ProzessverwaltungForm.modusBearbeiten!='werkstueckeigenschaft'}">
	<x:updateActionListener property="#{ProzessverwaltungForm.modusBearbeiten}" value="werkstueckeigenschaft" />
</h:commandLink>


<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- +++++++++++++++     Eigenschaft bearbeiten      ++++++++++++++++ --%>
<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>

<htm:h4 style="margin-top:15" rendered="#{ProzessverwaltungForm.modusBearbeiten=='werkstueckeigenschaft'}">
	<h:outputText value="#{msgs.eigenschaftBearbeiten}" />
</htm:h4>
<%-- Box für die Bearbeitung der Details --%>
<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen"
	rendered="#{ProzessverwaltungForm.modusBearbeiten=='werkstueckeigenschaft'}">

	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row1" colspan="2">
			<h:outputText value="#{msgs.eigenschaft}" />
		</htm:td>
	</htm:tr>

	<%-- Formular für die Bearbeitung der Eigenschaft --%>
	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row2" colspan="2">
			<x:aliasBean alias="#{mywerk_item}" value="#{ProzessverwaltungForm.myWerkstueck.displayProperties.currentProperty}">
				<h:panelGrid columns="2">


					<%-- Felder --%>
					<h:outputLabel for="eigenschafttitel1" value="#{msgs.titel}" />
					<h:panelGroup>
						<h:inputText id="eigenschafttitel1" style="width: 500px;margin-right:15px"
							value="#{ProzessverwaltungForm.myWerkstueck.displayProperties.currentProperty.titel}" required="true" />
						<x:message for="eigenschafttitel1" style="color: red" detailFormat="#{msgs.keinTitelAngegeben}" />
					</h:panelGroup>



					<h:outputText value="#{msgs.wert}" />
					<%-- textarea --%>
					<h:panelGroup id="prpvw15_1" rendered="#{((mywerk_item.type.name == 'string') || (mywerk_item.type.name == 'null'))}">
						<h:inputText id="file" style="width: 500px;margin-right:15px" value="#{mywerk_item.selectedValue}" required="#{mywerk_item.required}" />
					</h:panelGroup>

					<%-- numbers only --%>
					<h:panelGroup id="prpvw15_1mnk" rendered="#{mywerk_item.type.name == 'integer' || mywerk_item.type.name == 'number'}">

						<h:inputText id="numberstuff122334mnktodo" style="width: 500px;margin-right:15px" value="#{mywerk_item.selectedValue}" required="#{mywerk_item.required}">
							<f:validateLongRange minimum="0" />
						</h:inputText>
					</h:panelGroup>

					<%--  SelectOneMenu --%>
					<h:panelGroup id="prpvw15_2" rendered="#{(mywerk_item.type.name == 'list')}">
						<h:selectOneMenu value="#{mywerk_item.selectedValue}" style="width: 500px;margin-right:15px" id="prpvw15_2_1">
							<si:selectItems id="prpvw15_2_2" value="#{mywerk_item.valuesList}" var="mywerk_items" itemLabel="#{mywerk_items}" itemValue="#{mywerk_items}" />
						</h:selectOneMenu>
					</h:panelGroup>

					<%--  SelectManyMenu --%>
					<h:panelGroup id="prpvw15_3" rendered="#{(mywerk_item.type.name == 'listmultiselect')}">
						<h:selectManyListbox id="prpvw15_3_1" style="width: 500px;margin-right:15px" value="#{mywerk_item.selectedValuesList}" required="#{mywerk_item.required}"
							size="10">
							<si:selectItems id="prpvw15_3_2" value="#{mywerk_item.valuesList}" var="mywerk_items" itemLabel="#{mywerk_items}" itemValue="#{mywerk_items}" />
						</h:selectManyListbox>
					</h:panelGroup>

					<%--  Boolean --%>
					<h:panelGroup id="prpvw15_4" rendered="#{(mywerk_item.type.name == 'boolean')}">
						<h:selectOneMenu value="#{mywerk_item.selectedValue}" style="width: 500px;margin-right:15px" id="prpvw15_4_1" required="#{mywerk_item.required}">
							<f:selectItem id="prpvw15_4_2" itemValue="true" itemLabel="#{msgs.yes}" />
							<f:selectItem id="prpvw15_4_3" itemValue="false" itemLabel="#{msgs.no}" />
						</h:selectOneMenu>
					</h:panelGroup>

					<%--  Date  --%>
					<h:panelGroup id="prpvw15_5" style="width: 500px;margin-right:15px" rendered="#{(mywerk_item.type.name == 'date')}">
						<rich:calendar id="prpvw15_5_1" datePattern="dd.MM.yyyy" value="#{mywerk_item.date}" enableManualInput="true">
						</rich:calendar>
					</h:panelGroup>

				</h:panelGrid>
			</x:aliasBean>
		</htm:td>
	</htm:tr>

	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row3" align="left">
			<h:commandButton value="#{msgs.abbrechen}" action="#{NavigationForm.Reload}" immediate="true">
				<x:updateActionListener property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
			</h:commandButton>
		</htm:td>
		<htm:td styleClass="eingabeBoxen_row3" align="right">
			<h:commandButton value="#{msgs.loeschen}" action="#{ProzessverwaltungForm.myWerkstueck.displayProperties.deleteProperty}"
				onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')"
				rendered="#{ProzessverwaltungForm.myWerkstueck.displayProperties.currentProperty.id != null}">
				<x:updateActionListener property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
			</h:commandButton>
			<h:commandButton value="#{msgs.uebernehmen}" action="#{ProzessverwaltungForm.WerkstueckEigenschaftUebernehmen}">
				<x:updateActionListener property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
			</h:commandButton>
		</htm:td>
	</htm:tr>
</htm:table>


