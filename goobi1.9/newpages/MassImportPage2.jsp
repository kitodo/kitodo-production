<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>



<%-- ######################################## 

							Add mass import

	#########################################--%>
<a4j:keepAlive beanName="MassImportForm" />
<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body>
		<htm:table styleClass="headTable" cellspacing="0" cellpadding="0" style="padding-left:5px;padding-right:5px;margin-top:5px;">
			<%@include file="inc/tbl_Kopf.jsp"%>
		</htm:table>
		<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable" align="center">
			<link href="../css/tabbedPane.css" rel="stylesheet" type="text/css" />
			<htm:tr>
				<%@include file="inc/tbl_Navigation.jsp"%>
				<htm:td valign="top" styleClass="layoutInhalt">

					<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
					<h:form enctype="multipart/form-data" id="formupload">
						<%-- Breadcrumb --%>
						<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf" id="projgrid112">
							<h:panelGroup id="id1">
								<h:commandLink value="#{msgs.startseite}" action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:commandLink value="#{msgs.prozessverwaltung}" action="ProzessverwaltungAlle" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText value="#{msgs.MassImport}" />
							</h:panelGroup>
						</h:panelGrid>


						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>
									<htm:h3>
										<h:outputText value="#{msgs.MassImport}" />
									</htm:h3>

									<%-- globale Warn- und Fehlermeldungen --%>
									<h:messages id="id8" globalOnly="false" errorClass="text_red" infoClass="text_blue" showDetail="true" showSummary="true" tooltip="true" />

									<%-- Box für die Bearbeitung der Details --%>
									<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen">

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row1" align="left">
												<h:outputText id="idnp1" value="#{msgs.details}" />
											</htm:td>
											<htm:td styleClass="eingabeBoxen_row1" align="right">
												<h:commandLink id="idnp2" action="#{NavigationForm.Reload}">
													<h:graphicImage id="idnp4" value="/newpages/images/reload.gif" />
												</h:commandLink>
											</htm:td>
										</htm:tr>

										<%-- Formular für die Bearbeitung der Texte --%>
										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row2" colspan="2">

												<h:outputText value="#{msgs.processProperties}" />



												<x:dataTable var="property" id="processProperties" value="#{MassImportForm.properties}">

													<h:column>
														<h:panelGroup id="prpvw15_1" rendered="#{property.type.name == 'text'}">
															<h:outputText value="#{property.name}" />
															<h:inputText id="file" style="width: 500px;margin-right:15px" value="#{property.value}" />
														</h:panelGroup>

														<%-- numbers only --%>
														<h:panelGroup id="prpvw15_1m" rendered="#{property.type.name == 'integer' || property.type.name == 'number'}">
															<h:outputText value="#{property.name}" />
															<h:inputText id="Number" style="width: 500px;margin-right:15px" value="#{property.value}">
																<f:validateLongRange minimum="0" />
															</h:inputText>
														</h:panelGroup>

														<%--  SelectOneMenu --%>
														<h:panelGroup id="prpvw15_2" rendered="#{(property.type.name == 'list')}">
															<h:outputText value="#{property.name}" />
															<h:selectOneMenu value="#{property.value}" id="prpvw15_2_1" style="width: 500px;margin-right:15px">
																<si:selectItems id="prpvw15_2_2" value="#{property.possibleValues}" var="propertys" itemLabel="#{propertys}"
																	itemValue="#{propertys}" />
															</h:selectOneMenu>
														</h:panelGroup>

														<%--  SelectManyMenu --%>
														<h:panelGroup id="prpvw15_3" rendered="#{(property.type.name == 'listmultiselect')}">
															<h:outputText value="#{property.name}" />
															<h:selectManyListbox id="prpvw15_3_1" style="width: 500px;margin-right:15px" value="#{property.valueList}"
																 size="5">
																<si:selectItems id="prpvw15_3_2" value="#{property.possibleValues}" var="propertys" itemLabel="#{propertys}"
																	itemValue="#{propertys}" />
															</h:selectManyListbox>
														</h:panelGroup>

														<%--  Boolean --%>
														<h:panelGroup id="prpvw15_4" rendered="#{(property.type.name == 'boolean')}">
															<h:outputText value="#{property.name}" />
															<h:selectBooleanCheckbox value="#{property.booleanValue}"/>
														
														</h:panelGroup>

														<%--  Date  --%>
														<h:panelGroup id="prpvw15_5" rendered="#{(property.type.name == 'date')}">
															<h:outputText value="#{property.name}" />
															<rich:calendar id="prpvw15_5_1" datePattern="dd.MM.yyyy" value="#{property.value}" enableManualInput="true">
															</rich:calendar>
														</h:panelGroup>
													</h:column>
												</x:dataTable>



											</htm:td>
										</htm:tr>

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row3" align="left">
												<h:commandButton id="id121" value="#{msgs.abbrechen}" action="ProzessverwaltungAlle" immediate="true" />
											</htm:td>
											<htm:td styleClass="eingabeBoxen_row3" align="right">



												<h:commandButton id="id124" value="#{msgs.speichern}" action="#{MassImportForm.convertData}" />

											</htm:td>

										</htm:tr>

									</htm:table>
									<%-- // Box für die Bearbeitung der Details --%>

								</htm:td>
							</htm:tr>
						</htm:table>
					</h:form>

				</htm:td>
			</htm:tr>
			<%@include file="inc/tbl_Fuss.jsp"%>
		</htm:table>



	</body>
</f:view>

</html>
