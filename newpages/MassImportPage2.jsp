<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
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

												<x:dataTable var="property" id="processProperties" value="#{MassImportForm.properties}">
	
													<h:column>
														<h:outputText value="#{property.name}:" />
													</h:column>

													<h:column>
														<h:panelGroup id="prpvw15_1" rendered="#{property.type.name == 'text'}">
															<h:inputText id="inputText" style="width: 500px;margin-right:15px" value="#{property.value}" required="#{property.required}"  />
														</h:panelGroup>

														<%-- numbers only --%>
														<h:panelGroup id="prpvw15_1m" rendered="#{property.type.name == 'integer' || property.type.name == 'number'}">
														<h:inputText id="number" style="width: 500px;margin-right:15px" value="#{property.value}"  required="#{property.required}" >
																<f:validateLongRange minimum="0" />
															</h:inputText>
														</h:panelGroup>

														<%--  SelectOneMenu --%>
														<h:panelGroup id="prpvw15_2" rendered="#{(property.type.name == 'list')}">
															<h:selectOneMenu value="#{property.value}" id="selectMenu" style="width: 500px;margin-right:15px"  required="#{property.required}" >
																<si:selectItems id="prpvw15_2_2" value="#{property.possibleValues}" var="propertys" itemLabel="#{propertys}"
																	itemValue="#{propertys}" />
															</h:selectOneMenu>
														</h:panelGroup>

														<%--  SelectManyMenu --%>
														<h:panelGroup id="prpvw15_3" rendered="#{(property.type.name == 'listmultiselect')}">
															<h:selectManyListbox id="selectManyMenu" style="width: 500px;margin-right:15px" value="#{property.valueList}"
																 size="5" required="#{property.required}">
																<si:selectItems id="prpvw15_3_2" value="#{property.possibleValues}" var="propertys" itemLabel="#{propertys}"
																	itemValue="#{propertys}" />
															</h:selectManyListbox>
														</h:panelGroup>

														<%--  Boolean --%>
														<h:panelGroup id="prpvw15_4" rendered="#{(property.type.name == 'boolean')}"  >
															<h:selectBooleanCheckbox id="checkbox" value="#{property.booleanValue}" required="#{property.required}"/>		
														</h:panelGroup>

														<%--  Date  --%>
														<h:panelGroup id="prpvw15_5" rendered="#{(property.type.name == 'date')}" >
															<rich:calendar id="calendar" datePattern="dd.MM.yyyy" value="#{property.value}" enableManualInput="true"  required="#{property.required}">
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
