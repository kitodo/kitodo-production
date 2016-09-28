<%--
 *
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 *
--%>

<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%-- ######################################## 

							Alle Benutzer in der Übersicht

	#########################################--%>

<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body>
		<htm:table styleClass="headTable" cellspacing="0" cellpadding="0" style="padding-left:5px;padding-right:5px;margin-top:5px;">
			<%@include file="inc/tbl_Kopf.jsp"%>
		</htm:table>
		<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable"
			align="center">
			<htm:tr>
				<%@include file="inc/tbl_Navigation.jsp"%>
				<htm:td valign="top" styleClass="layoutInhalt">

					<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
					<h:form id="modulform">
						<%-- Breadcrumb --%>
						<h:panelGrid id="id0" columns="1" styleClass="layoutInhaltKopf">
							<h:panelGroup id="id1">
								<h:commandLink id="id2" value="#{msgs.startseite}"
									action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText id="id3" value="#{msgs.aktiveModule}" />
							</h:panelGroup>
						</h:panelGrid>
						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>
									<h:panelGroup id="id4"
										rendered="#{LoginForm.maximaleBerechtigung == 1}">

										<h:commandButton id="button1" value="Read configuration"
											rendered="#{ModuleServerForm.running==false}"
											action="#{ModuleServerForm.readAllModulesFromConfiguration}" />
										<h:commandButton id="button2" value="Start all modules"
											rendered="#{ModuleServerForm.running==true}"
											action="#{ModuleServerForm.startAllModules}" />
										<h:commandButton id="button3" value="Stop all modules"
											rendered="#{ModuleServerForm.running==true}"
											action="#{ModuleServerForm.stopAllModules}" />


										<htm:table border="0" align="center" width="100%"
											cellpadding="15">
											<htm:tr>
												<htm:td>

													<%-- Überschrift --%>
													<htm:h3>
														<h:outputText id="id5" value="#{msgs.aktiveModule}" />
													</htm:h3>

													<%-- globale Warn- und Fehlermeldungen --%>
													<h:messages id="id6" globalOnly="true"
														errorClass="text_red" title="Meldungen" layout="table"
														infoClass="text_blue" showDetail="true" showSummary="true"
														tooltip="true" />

													<%-- Datentabelle --%>
													<x:dataTable id="benutzerliste" styleClass="standardTable"
														width="100%" cellspacing="1px" cellpadding="1px"
														headerClass="standardTable_Header"
														rowClasses="standardTable_Row1" var="item"
														value="#{ModuleServerForm.modulmanager}">

														<h:column id="id7">
															<f:facet name="header">
																<h:outputText id="id8" value="#{msgs.titel}" />
															</f:facet>
															<h:outputText id="id9" value="#{item.name}" />
														</h:column>

														<h:column id="id10">
															<f:facet name="header">
																<h:outputText id="id11" value="#{msgs.bemerkung}" />
															</f:facet>
															<h:outputText id="id12" value="#{item.comments}" />
														</h:column>

														<h:column id="id13">
															<f:facet name="header">
																<h:outputText id="id14" value="#{msgs.status}" />
															</f:facet>
															<h:outputText id="id15"
																value="#{item.moduleClient.status}" />
														</h:column>

														<h:column id="id16"
															rendered="#{ModuleServerForm.running==true}">
															<f:facet name="header">
																<h:outputText id="id17" value="#{msgs.shortsession}" />
															</f:facet>
															<h:commandLink id="id18"
																action="#{ModuleServerForm.startModule}"
																rendered="#{item.moduleClient.status=='not active'}"
																title="initialize">
																<h:graphicImage
																	value="/newpages/images/icons/start_task.gif" />
																<x:updateActionListener value="#{item}"
																	property="#{ModuleServerForm.myModule}" />
															</h:commandLink>
															<h:commandLink id="id19"
																action="#{ModuleServerForm.stopModule}"
																rendered="#{item.moduleClient.status=='active'}"
																title="shutdown">
																<h:graphicImage id="id20"
																	value="/newpages/images/icons/stop_task.gif" />
																<x:updateActionListener value="#{item}"
																	property="#{ModuleServerForm.myModule}" />
															</h:commandLink>
															<h:commandLink
																rendered="#{item.moduleClient.status=='active'}"
																action="#{ModuleServerForm.startShortSessionTest}"
																title="shortsession">
																<h:graphicImage
																	value="/newpages/images/icons/execute_task.gif" />
																<x:updateActionListener value="#{item}"
																	property="#{ModuleServerForm.myModule}" />
															</h:commandLink>
														</h:column>
													</x:dataTable>

												</htm:td>
											</htm:tr>
										</htm:table>

									</h:panelGroup>
								</htm:td>
							</htm:tr>
						</htm:table>
					</h:form>
					<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

				</htm:td>
			</htm:tr>
			<%@include file="inc/tbl_Fuss.jsp"%>
		</htm:table>

	</body>
</f:view>

</html>
