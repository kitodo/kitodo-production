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
<%-- ++++++++++++++++     Eigenschaftentabelle      ++++++++++++++++ --%>
<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>

<h:form id="propform">
	<%-- Box für die Bearbeitung der Details --%>
	<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen"
		rendered="#{AktuelleSchritteForm.mySchritt.displayProperties.propertySize > 0}">

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1" colspan="2">
				<h:outputText value="#{msgs.erweiterteEigenschaften}" />
			</htm:td>
		</htm:tr>

		<%-- Formular für die Bearbeitung der Texte --%>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2" colspan="3">

				<x:dataTable id="container" var="container" value="#{AktuelleSchritteForm.mySchritt.displayProperties.containers}">
					<h:column>
						<x:dataTable id="eigenschaften" var="mystep_item" value="#{AktuelleSchritteForm.mySchritt.displayProperties.sortedProperties}" style="border-bottom: 1px solid #F4BBA5;">

							<h:column rendered="#{mystep_item.container==0 && mystep_item.container==container}">
								<h:outputText value="#{mystep_item.titel}" />

							</h:column>

							<h:column rendered="#{mystep_item.container==0 && mystep_item.container==container}">

								<%-- textarea --%>
								<h:panelGroup id="prpvw15_1"
									rendered="#{((mystep_item.type.name == 'string') || (mystep_item.type.name == 'unknown') || (mystep_item.type.name == 'null') || (mystep_item.type.name == 'messagenormal'))}">

									<h:inputText id="file" style="width: 500px;margin-right:15px" value="#{mystep_item.selectedValue}" required="#{mystep_item.required}" />
									<x:message id="prpvw15_1_1" for="file" style="color: red" replaceIdWithLabel="true" />
								</h:panelGroup>

								<%-- only text --%>
								<h:panelGroup id="prpvw15_1e" rendered="#{((mystep_item.type.name == 'messageimportant') || (mystep_item.type.name == 'messageerror'))}">
									<htm:div id="test" style="width: 500px;margin-right:15px">
										<h:outputText id="filee" value="#{mystep_item.selectedValue}" />
									</htm:div>
								</h:panelGroup>

								<%-- numbers only --%>
								<h:panelGroup id="prpvw15_1m" rendered="#{mystep_item.type.name == 'integer' || mystep_item.type.name == 'number'}">
									<h:outputLabel for="Number" value="#{mystep_item.titel}" style="display:none" />
									<h:inputText id="Number" style="width: 500px;margin-right:15px" value="#{mystep_item.selectedValue}" required="#{mystep_item.required}">
										<f:validateLongRange minimum="0" />
									</h:inputText>
									<x:message id="prpvw15_1_12" for="Number" style="color: red" showSummary="true" />
								</h:panelGroup>

								<%--  SelectOneMenu --%>
								<h:panelGroup id="prpvw15_2" rendered="#{(mystep_item.type.name == 'list')}">
									<h:selectOneMenu value="#{mystep_item.selectedValue}" id="prpvw15_2_1" style="width: 500px;margin-right:15px">
										<si:selectItems id="prpvw15_2_2" value="#{mystep_item.valuesList}" var="mystep_items" itemLabel="#{mystep_items}" itemValue="#{mystep_items}" />
									</h:selectOneMenu>
								</h:panelGroup>

								<%--  SelectManyMenu --%>
								<h:panelGroup id="prpvw15_3" rendered="#{(mystep_item.type.name == 'listmultiselect')}">
									<h:selectManyListbox id="prpvw15_3_1" style="width: 500px;margin-right:15px" value="#{mystep_item.selectedValuesList}"
										required="#{mystep_item.required}" size="5">
										<si:selectItems id="prpvw15_3_2" value="#{mystep_item.valuesList}" var="mystep_items" itemLabel="#{mystep_items}" itemValue="#{mystep_items}" />
									</h:selectManyListbox>
								</h:panelGroup>

								<%--  Boolean --%>
								<h:panelGroup id="prpvw15_4" rendered="#{(mystep_item.type.name == 'boolean')}">
									<h:selectOneMenu value="#{mystep_item.selectedValue}" id="prpvw15_4_1" required="#{mystep_item.required}" style="width: 500px;margin-right:15px">
										<f:selectItem id="prpvw15_4_2" itemValue="true" itemLabel="#{msgs.yes}" />
										<f:selectItem id="prpvw15_4_3" itemValue="false" itemLabel="#{msgs.no}" />
									</h:selectOneMenu>
								</h:panelGroup>

								<%--  Date  --%>
								<h:panelGroup id="prpvw15_5" rendered="#{(mystep_item.type.name == 'date')}">
									<rich:calendar id="prpvw15_5_1" datePattern="dd.MM.yyyy" value="#{mystep_item.date}" enableManualInput="true">
									</rich:calendar>
								</h:panelGroup>
							</h:column>
							<%-- delete --%>
							<h:column rendered="#{mystep_item.container==0 && mystep_item.container==container}">
								<h:panelGroup>
									<h:commandLink action="#{AktuelleSchritteForm.mySchritt.displayProperties.deleteProperty}"
										rendered="#{mystep_item.type.name != 'messageerror' && mystep_item.type.name != 'messageimportant' && mystep_item.type.name != 'messagenormal'}">
										<h:graphicImage value="images/buttons/waste1a_20px.gif" />
										<x:updateActionListener value="#{mystep_item}" property="#{AktuelleSchritteForm.mySchritt.displayProperties.currentProperty}" />
									</h:commandLink>

								</h:panelGroup>
								<%-- duplicate --%>
								<h:panelGroup>
									<h:commandLink action="#{AktuelleSchritteForm.mySchritt.displayProperties.duplicateProperty}"
										rendered="#{mystep_item.type.name != 'messageerror' && mystep_item.type.name != 'messageimportant' && mystep_item.type.name != 'messagenormal'}">
										<h:graphicImage value="/newpages/images/buttons/copy.gif" />
										<x:updateActionListener value="#{mystep_item}" property="#{AktuelleSchritteForm.mySchritt.displayProperties.currentProperty}" />
									</h:commandLink>
								</h:panelGroup>
							</h:column>
							

<%-- container with properties --%>
							<h:column rendered="#{mystep_item.container!=0 && mystep_item.container==container}">
								<h:outputText value="#{mystep_item.titel}" />

							</h:column>

							<h:column rendered="#{mystep_item.container!=0 && mystep_item.container==container}">

								<%-- textarea --%>
								<h:panelGroup id="prpvw15_12"
									rendered="#{((mystep_item.type.name == 'string') || (mystep_item.type.name == 'unknown') || (mystep_item.type.name == 'null') || (mystep_item.type.name == 'messagenormal'))}">

									<h:inputText id="file2" style="width: 500px;margin-right:15px" value="#{mystep_item.selectedValue}" required="#{mystep_item.required}" />
									<x:message id="prpvw15_1_122246" for="file2" style="color: red" replaceIdWithLabel="true" />
								</h:panelGroup>

								<%-- only text --%>
								<h:panelGroup id="prpvw15_12e" rendered="#{((mystep_item.type.name == 'messageimportant') || (mystep_item.type.name == 'messageerror'))}">
									<htm:div id="test2" style="width: 500px;margin-right:15px">
										<h:outputText id="filee2" value="#{mystep_item.selectedValue}" />
									</htm:div>
								</h:panelGroup>

								<%-- numbers only --%>
								<h:panelGroup id="prpvw15_12m" rendered="#{mystep_item.type.name == 'integer' || mystep_item.type.name == 'number'}">
									<h:outputLabel for="Number2" value="#{mystep_item.titel}" style="display:none" />
									<h:inputText id="Number2" style="width: 500px;margin-right:15px" value="#{mystep_item.selectedValue}" required="#{mystep_item.required}">
										<f:validateLongRange minimum="0" />
									</h:inputText>
									<x:message id="prpvw15_1_122" for="Number2" style="color: red" showSummary="true" />
								</h:panelGroup>

								<%--  SelectOneMenu --%>
								<h:panelGroup id="prpvw15_22" rendered="#{(mystep_item.type.name == 'list')}">
									<h:selectOneMenu value="#{mystep_item.selectedValue}" id="prpvw15_2_12" style="width: 500px;margin-right:15px">
										<si:selectItems id="prpvw15_2_22" value="#{mystep_item.valuesList}" var="mystep_items" itemLabel="#{mystep_items}" itemValue="#{mystep_items}" />
									</h:selectOneMenu>
								</h:panelGroup>

								<%--  SelectManyMenu --%>
								<h:panelGroup id="prpvw15_32" rendered="#{(mystep_item.type.name == 'listmultiselect')}">
									<h:selectManyListbox id="prpvw15_3_12" style="width: 500px;margin-right:15px" value="#{mystep_item.selectedValuesList}"
										required="#{mystep_item.required}" size="5">
										<si:selectItems id="prpvw15_3_22" value="#{mystep_item.valuesList}" var="mystep_items" itemLabel="#{mystep_items}" itemValue="#{mystep_items}" />
									</h:selectManyListbox>
								</h:panelGroup>

								<%--  Boolean --%>
								<h:panelGroup id="prpvw15_42" rendered="#{(mystep_item.type.name == 'boolean')}">
									<h:selectOneMenu value="#{mystep_item.selectedValue}" id="prpvw15_4_145" required="#{mystep_item.required}" style="width: 500px;margin-right:15px">
										<f:selectItem id="prpvw15_4_22" itemValue="true" itemLabel="#{msgs.yes}" />
										<f:selectItem id="prpvw15_4_32" itemValue="false" itemLabel="#{msgs.no}" />
									</h:selectOneMenu>
								</h:panelGroup>

								<%--  Date  --%>
								<h:panelGroup id="prpvw15_52" rendered="#{(mystep_item.type.name == 'date')}">
									<rich:calendar id="prpvw15_5_12" datePattern="dd.MM.yyyy" value="#{mystep_item.date}" enableManualInput="true">
									</rich:calendar>
								</h:panelGroup>


							</h:column>
							<%-- delete --%>
							<h:column rendered="#{mystep_item.container!=0 && mystep_item.container==container}">
								<h:panelGroup>
									<h:commandLink action="#{AktuelleSchritteForm.mySchritt.displayProperties.deleteProperty}"
										rendered="#{mystep_item.type.name != 'messageerror' && mystep_item.type.name != 'messageimportant' && mystep_item.type.name != 'messagenormal'}">
										<h:graphicImage value="images/buttons/waste1a_20px.gif" />
										<x:updateActionListener value="#{mystep_item}" property="#{AktuelleSchritteForm.mySchritt.displayProperties.currentProperty}" />
									</h:commandLink>

								</h:panelGroup>
								<%-- duplicate --%>
								<h:panelGroup>
									<h:commandLink action="#{AktuelleSchritteForm.mySchritt.displayProperties.duplicateContainer}"
										rendered="#{mystep_item.type.name != 'messageerror' && mystep_item.type.name != 'messageimportant' && mystep_item.type.name != 'messagenormal'}">
										<h:graphicImage value="/newpages/images/buttons/copy.gif" />
										<x:updateActionListener value="#{mystep_item}" property="#{AktuelleSchritteForm.mySchritt.displayProperties.currentProperty}" />
									</h:commandLink>

								</h:panelGroup>

							</h:column>
							<htm:br rendered="#{mystep_item.container!=0 && mystep_item.container==container}" />
							<htm:br rendered="#{mystep_item.container!=0 && mystep_item.container==container}" />

							<htm:hr rendered="#{mystep_item.container!=0 && mystep_item.container==container}" />
							<htm:br rendered="#{mystep_item.container!=0 && mystep_item.container==container}" />
						</x:dataTable>
					</h:column>

				</x:dataTable>



			</htm:td>
		</htm:tr>

		<htm:tr>
			<htm:td colspan="2" styleClass="eingabeBoxen_row3" align="right">
				<h:commandButton value="#{msgs.speichern}" action="#{AktuelleSchritteForm.saveProperties}" />
			</htm:td>
		</htm:tr>
	</htm:table>
	<%-- // Box für die Bearbeitung der Details --%>

</h:form>


















