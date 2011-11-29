<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>


<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- ++++++++++++++++     Properties      ++++++++++++++++ --%>
<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>

<h:form id="propform2">
	<%-- Box für die Bearbeitung der Details --%>
	<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen">

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1" colspan="2">
				<h:outputText value="#{msgs.erweiterteEigenschaften}" />
			</htm:td>
		</htm:tr>

		<%-- Formular für die Bearbeitung der Texte --%>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2" colspan="3">

<%-- 
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


				<x:dataTable id="container" var="container" value="#{AktuelleSchritteForm.containers}">
					<h:column>
						<x:dataTable id="property" var="property" value="#{AktuelleSchritteForm.sortedProperties}" style="border-bottom: 1px solid #F4BBA5;">
							<h:column rendered="#{property.container==0 && property.container==container}">
								<h:outputText value="#{property.name}" />
							</h:column>
							<h:column rendered="#{property.container==0 && property.container==container}">
								<h:panelGroup id="prpvw15_1" rendered="#{property.type.name == 'text'}">
									<h:inputText id="file" style="width: 500px;margin-right:15px" value="#{property.value}" />
								</h:panelGroup>

								<%-- numbers only --%>
								<h:panelGroup id="prpvw15_1m" rendered="#{property.type.name == 'integer' || property.type.name == 'number'}">
									<h:inputText id="Number" style="width: 500px;margin-right:15px" value="#{property.value}">
										<f:validateLongRange minimum="0" />
									</h:inputText>
								</h:panelGroup>

								<%--  SelectOneMenu --%>
								<h:panelGroup id="prpvw15_2" rendered="#{(property.type.name == 'list')}">
									<h:selectOneMenu value="#{property.value}" id="prpvw15_2_1" style="width: 500px;margin-right:15px">
										<si:selectItems id="prpvw15_2_2" value="#{property.possibleValues}" var="propertys" itemLabel="#{propertys}" itemValue="#{propertys}" />
									</h:selectOneMenu>
								</h:panelGroup>

								<%--  SelectManyMenu --%>
								<h:panelGroup id="prpvw15_3" rendered="#{(property.type.name == 'listmultiselect')}">
									<h:selectManyListbox id="prpvw15_3_1" style="width: 500px;margin-right:15px" value="#{property.valueList}" size="5">
										<si:selectItems id="prpvw15_3_2" value="#{property.possibleValues}" var="propertys" itemLabel="#{propertys}" itemValue="#{propertys}" />
									</h:selectManyListbox>
								</h:panelGroup>

								<%--  Boolean --%>
								<h:panelGroup id="prpvw15_4" rendered="#{(property.type.name == 'boolean')}">
									<h:selectBooleanCheckbox value="#{property.booleanValue}" />
								</h:panelGroup>

								<%--  Date  --%>
								<h:panelGroup id="prpvw15_5" rendered="#{(property.type.name == 'date')}">
									<rich:calendar id="prpvw15_5_1" datePattern="dd.MM.yyyy" value="#{property.value}" enableManualInput="true">
									</rich:calendar>
								</h:panelGroup>


							</h:column>
							<%-- delete --%>
							<h:column rendered="#{property.container==0 && property.container==container}">
								<h:panelGroup>
									<h:commandLink action="#{AktuelleSchritteForm.deleteProperty}"
										rendered="#{property.type.name != 'messageerror' && property.type.name != 'messageimportant' && property.type.name != 'messagenormal'}">
										<h:graphicImage value="images/buttons/waste1a_20px.gif" />
										<x:updateActionListener value="#{property}" property="#{AktuelleSchritteForm.processProperty}" />
									</h:commandLink>

								</h:panelGroup>
								<%-- duplicate --%>
								<h:panelGroup>
									<h:commandLink action="#{AktuelleSchritteForm.duplicateProperty}"
										rendered="#{property.type.name != 'messageerror' && property.type.name != 'messageimportant' && property.type.name != 'messagenormal'}">
										<h:graphicImage value="/newpages/images/buttons/copy.gif" />
										<x:updateActionListener value="#{property}" property="#{AktuelleSchritteForm.processProperty}" />
									</h:commandLink>
								</h:panelGroup>
							</h:column>



							<%-- groupings of properties --%>

							<h:column rendered="#{property.container!=0 && property.container==container}">
								<h:outputText value="#{property.name}" />

							</h:column>

							<h:column rendered="#{property.container!=0 && property.container==container}">
								<h:panelGroup rendered="#{property.type.name == 'text'}">
									<h:inputText style="width: 500px;margin-right:15px" value="#{property.value}" />
								</h:panelGroup>

								<%-- numbers only --%>
								<h:panelGroup rendered="#{property.type.name == 'integer' || property.type.name == 'number'}">
									<h:inputText style="width: 500px;margin-right:15px" value="#{property.value}">
										<f:validateLongRange minimum="0" />
									</h:inputText>
								</h:panelGroup>

								<%--  SelectOneMenu --%>
								<h:panelGroup rendered="#{(property.type.name == 'list')}">
									<h:selectOneMenu value="#{property.value}" style="width: 500px;margin-right:15px">
										<si:selectItems value="#{property.possibleValues}" var="propertys" itemLabel="#{propertys}" itemValue="#{propertys}" />
									</h:selectOneMenu>
								</h:panelGroup>

								<%--  SelectManyMenu --%>
								<h:panelGroup rendered="#{(property.type.name == 'listmultiselect')}">
									<h:selectManyListbox style="width: 500px;margin-right:15px" value="#{property.valueList}" size="5">
										<si:selectItems value="#{property.possibleValues}" var="propertys" itemLabel="#{propertys}" itemValue="#{propertys}" />
									</h:selectManyListbox>
								</h:panelGroup>

								<%--  Boolean --%>
								<h:panelGroup rendered="#{(property.type.name == 'boolean')}">
									<h:selectBooleanCheckbox value="#{property.booleanValue}" />
								</h:panelGroup>

								<%--  Date  --%>
								<h:panelGroup rendered="#{(property.type.name == 'date')}">
									<rich:calendar datePattern="dd.MM.yyyy" value="#{property.value}" enableManualInput="true">
									</rich:calendar>
								</h:panelGroup>
							</h:column>

							<h:column rendered="#{property.container!=0 && property.container==container}">
								<h:panelGroup>
									<h:commandLink action="#{AktuelleSchritteForm.deleteProperty}"
										rendered="#{property.type.name != 'messageerror' && property.type.name != 'messageimportant' && property.type.name != 'messagenormal'}">
										<h:graphicImage value="images/buttons/waste1a_20px.gif" />
										<x:updateActionListener value="#{property}" property="#{AktuelleSchritteForm.processProperty}" />
									</h:commandLink>

								</h:panelGroup>
								<%-- duplicate --%>
								<h:panelGroup>
									<h:commandLink action="#{AktuelleSchritteForm.duplicateContainer}"
										rendered="#{property.type.name != 'messageerror' && property.type.name != 'messageimportant' && property.type.name != 'messagenormal'}">
										<h:graphicImage value="/newpages/images/buttons/copy.gif" />
										<x:updateActionListener value="#{property}" property="#{AktuelleSchritteForm.processProperty}" />
									</h:commandLink>

								</h:panelGroup>

							</h:column>
							<htm:br rendered="#{property.container!=0 && property.container==container}" />

						</x:dataTable>
					</h:column>
				</x:dataTable>


			</htm:td>
		</htm:tr>

		<htm:tr>
			<htm:td colspan="2" styleClass="eingabeBoxen_row3" align="right">
				<h:commandButton value="#{msgs.speichern}" action="#{AktuelleSchritteForm.saveProcessProperties}" />
			</htm:td>
		</htm:tr>
	</htm:table>
	<%-- // Box für die Bearbeitung der Details --%>

</h:form>