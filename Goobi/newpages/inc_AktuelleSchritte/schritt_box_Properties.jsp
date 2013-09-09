<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%-- 
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
--%>
<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- ++++++++++++++++     Properties      ++++++++++++++++ --%>
<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>

<h:form id="propform2" rendered="#{AktuelleSchritteForm.propertyListSize>0}">
	<%-- Box für die Bearbeitung der Details --%>
	<%-- 
	<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen">

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1" colspan="2">
				<h:outputText value="#{msgs.erweiterteEigenschaften}" />
			</htm:td>
		</htm:tr>

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2" colspan="3">

				<x:dataTable id="eigenschaften" var="prop" value="#{AktuelleSchritteForm.processProperties}" style="border-bottom: 1px solid #F4BBA5;">

					<h:column>
						<h:outputText value="#{prop.name}" />
					</h:column>

					<h:column>
						<h:outputText value="#{prop.type}" />
					</h:column>

					<h:column>
						<h:inputText value="#{prop.value}" />
					</h:column>

					<h:column>
						<h:outputText value="#{prop.possibleValues}" />
					</h:column>

					<h:column>
						<h:outputText value="#{prop.container}" />
					</h:column>

				</x:dataTable>
--%>
	<htm:table cellspacing="1px" cellpadding="1px" width="100%" styleClass="standardTable"
		rendered="#{AktuelleSchritteForm.modusBearbeiten!='eigenschaft' && AktuelleSchritteForm.propertyListSize>0}">

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
		<x:dataList var="container" value="#{AktuelleSchritteForm.containerList}" rowCountVar="rowCount" rowIndexVar="rowIndex">
			<x:dataList var="proc" value="#{AktuelleSchritteForm.containerlessProperties}" rowCountVar="propCount" rowIndexVar="propInd">
				<htm:tr styleClass="standardTable_Row1" rendered="#{container==0 }">
					<htm:td>
						<h:outputText value="#{proc.name}" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{proc.value}" rendered="#{proc.type.name !=  'link'}" />

						<h:outputLink title="#{proc.value}" value="#{proc.value}" rendered="#{proc.type.name ==  'link'}" target="_blank">
							<h:outputText value="#{proc.value}" />
						</h:outputLink>
					</htm:td>
					<htm:td styleClass="standardTable_ColumnCentered">

						<h:commandLink action="AktuelleSchritteBearbeiten" title="#{msgs.bearbeiten}" 
							rendered="#{AktuelleSchritteForm.mySchritt.bearbeitungsbenutzer.id == LoginForm.myBenutzer.id && proc.currentStepAccessCondition != 'READ'}">
							<h:graphicImage value="/newpages/images/buttons/edit.gif" />
							<x:updateActionListener property="#{AktuelleSchritteForm.processProperty}" value="#{proc}" />
							<x:updateActionListener property="#{AktuelleSchritteForm.container}" value="0" />
							<x:updateActionListener property="#{AktuelleSchritteForm.modusBearbeiten}" value="eigenschaft" />
							<a4j:support event="onchange" reRender="editBatch" />
						</h:commandLink>



					<h:commandLink action="#{AktuelleSchritteForm.duplicateProperty}" title="#{msgs.duplicate}"  rendered="#{proc.duplicationAllowed}">
						<h:graphicImage value="/newpages/images/buttons/copy.gif" />
						<x:updateActionListener property="#{AktuelleSchritteForm.processProperty}" value="#{proc}" />
						<x:updateActionListener property="#{AktuelleSchritteForm.container}" value="0" />
					</h:commandLink>


					</htm:td>
				</htm:tr>
			</x:dataList>

			<htm:tr rendered="#{rowIndex < rowCount && rowIndex != 0}">
				<htm:td colspan="3" styleClass="standardTable_Row1">
					<h:outputText value="&nbsp;" escape="false" />
				</htm:td>
			</htm:tr>

			<x:dataList var="process_item" value="#{AktuelleSchritteForm.containers[container].propertyList}" rowCountVar="propCount" rowIndexVar="propInd">
				<htm:tr styleClass="standardTable_Row1" rendered="#{container!=0 }">
					<htm:td>
						<h:outputText value="#{process_item.name}" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{process_item.value}" rendered="#{process_item.type.name !=  'link'}" />
						<h:outputLink title="#{process_item.value}" value="#{process_item.value}" rendered="#{process_item.type.name ==  'link'}" target="_blank">
							<h:outputText value="#{process_item.value}" />
						</h:outputLink>
						<%-- 						<h:outputText value="#{process_item.value}" /> --%>
					</htm:td>
					<htm:td styleClass="standardTable_ColumnCentered" rowspan="#{AktuelleSchritteForm.containers[container].propertyListSizeString}"
						rendered="#{propInd ==0}">
						<h:panelGroup rendered="#{AktuelleSchritteForm.mySchritt.bearbeitungsbenutzer.id == LoginForm.myBenutzer.id && process_item.currentStepAccessCondition != 'READ'}">
							<h:commandLink action="AktuelleSchritteBearbeiten" title="#{msgs.bearbeiten}">
								<h:graphicImage value="/newpages/images/buttons/edit.gif" />
								<x:updateActionListener property="#{AktuelleSchritteForm.container}" value="#{container}" />
								<x:updateActionListener property="#{AktuelleSchritteForm.modusBearbeiten}" value="eigenschaft" />
								<a4j:support event="onchange" reRender="editBatch" />
							</h:commandLink>
							
							
							
							<h:commandLink action="#{AktuelleSchritteForm.duplicateContainer}" title="#{msgs.duplicate}" rendered="#{process_item.duplicationAllowed}">
									<h:graphicImage value="/newpages/images/buttons/copy.gif" />
									<x:updateActionListener property="#{AktuelleSchritteForm.container}" value="#{container}" />
							</h:commandLink>
							
						</h:panelGroup>
					</htm:td>
				</htm:tr>
			</x:dataList>

		</x:dataList>
	</htm:table>


	<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen"
		rendered="#{AktuelleSchritteForm.modusBearbeiten=='eigenschaft' && AktuelleSchritteForm.mySchritt.bearbeitungsbenutzer.id == LoginForm.myBenutzer.id}">

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1" colspan="2">
				<h:outputText value="#{msgs.eigenschaft}" />
			</htm:td>
		</htm:tr>

		<%-- Formular für die Bearbeitung der Eigenschaft --%>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2" colspan="2">
				<htm:table>

					<x:dataList var="myprocess_item" value="#{AktuelleSchritteForm.containerProperties}">

						<%-- 	<x:aliasBean alias="#{myprocess_item}" value="#{AktuelleSchritteForm.processProperty}">--%>
						<htm:tr>
							<htm:td>
								<h:outputText id="eigenschafttitel" style="width: 500px;margin-right:15px" value="#{myprocess_item.name}: " />
							</htm:td>
							<htm:td>
								<%-- textarea --%>
								<h:panelGroup id="prpvw15_1" rendered="#{((myprocess_item.type.name == 'text') || (myprocess_item.type.name == 'null') || (myprocess_item.type.name == 'link'))}">
									<h:inputText id="file" style="width: 500px;margin-right:15px" value="#{myprocess_item.value}"
										readonly="#{myprocess_item.currentStepAccessCondition == 'READ'}" />
								</h:panelGroup>

								<%-- numbers only --%>
								<h:panelGroup id="prpvw15_1mnk" rendered="#{myprocess_item.type.name == 'integer' || myprocess_item.type.name == 'number'}">

									<h:inputText id="numberstuff122334mnktodo" style="width: 500px;margin-right:15px" value="#{myprocess_item.value}"
										readonly="#{myprocess_item.currentStepAccessCondition == 'READ'}">
										<f:validateLongRange minimum="0" />
									</h:inputText>
								</h:panelGroup>

								<%--  SelectOneMenu --%>
								<h:panelGroup id="prpvw15_2" rendered="#{(myprocess_item.type.name == 'list')}">
									<h:selectOneMenu value="#{myprocess_item.value}" style="width: 500px;margin-right:15px" id="prpvw15_2_1"
										readonly="#{myprocess_item.currentStepAccessCondition == 'READ'}">
										<si:selectItems id="prpvw15_2_2" value="#{myprocess_item.possibleValues}" var="myprocess_items" itemLabel="#{myprocess_items}"
											itemValue="#{myprocess_items}" />
									</h:selectOneMenu>
								</h:panelGroup>

								<%--  SelectManyMenu --%>
								<h:panelGroup id="prpvw15_3" rendered="#{(myprocess_item.type.name == 'listmultiselect')}">
									<h:selectManyListbox id="prpvw15_3_1" style="width: 500px;margin-right:15px" value="#{myprocess_item.valueList}" size="5"
										readonly="#{myprocess_item.currentStepAccessCondition == 'READ'}">
										<si:selectItems id="prpvw15_3_2" value="#{myprocess_item.possibleValues}" var="myprocess_items" itemLabel="#{myprocess_items}"
											itemValue="#{myprocess_items}" />
									</h:selectManyListbox>
								</h:panelGroup>

								<%--  Boolean --%>
								<h:panelGroup id="prpvw15_4" rendered="#{(myprocess_item.type.name == 'boolean')}">
									<h:selectBooleanCheckbox value="#{myprocess_item.booleanValue}" readonly="#{myprocess_item.currentStepAccessCondition == 'READ'}" />
								</h:panelGroup>

								<%--  Date  --%>
								<h:panelGroup id="prpvw15_5" rendered="#{(myprocess_item.type.name == 'date')}">
									<rich:calendar id="prpvw15_5_1" style="width: 500px;margin-right:15px" datePattern="dd.MM.yyyy" value="#{myprocess_item.dateValue}"
										enableManualInput="true">
									</rich:calendar>
								</h:panelGroup>

							</htm:td>
						</htm:tr>
					</x:dataList>
				</htm:table>
			</htm:td>
		</htm:tr>

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3" align="left">
				<h:commandButton value="#{msgs.abbrechen}" action="#{NavigationForm.Reload}" immediate="true">
					<x:updateActionListener property="#{AktuelleSchritteForm.modusBearbeiten}" value="" />
				</h:commandButton>
			</htm:td>
			<htm:td styleClass="eingabeBoxen_row3" align="right">
				<%-- 
				<h:commandButton value="#{msgs.loeschen}" action="#{AktuelleSchritteForm.deleteProperty}"
					onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')">
					<x:updateActionListener property="#{AktuelleSchritteForm.modusBearbeiten}" value="" />
				</h:commandButton>
			--%>

				<h:commandButton value="#{msgs.applyToThisProcess}" action="#{AktuelleSchritteForm.saveCurrentProperty}">

					<x:updateActionListener property="#{AktuelleSchritteForm.modusBearbeiten}" value="" />
				</h:commandButton>

			</htm:td>
		</htm:tr>
	</htm:table>
</h:form>