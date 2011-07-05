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

	<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable"
		align="center">
		<%@include file="inc/tbl_Kopf.jsp"%>
		<htm:tr>
			<%@include file="inc/tbl_Navigation.jsp"%>
			<htm:td valign="top" styleClass="layoutInhalt">

				<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
				<h:form id="modulform">
					<%-- Breadcrumb --%>
					<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:commandLink value="#{msgs.startseite}" action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText value="#{msgs.aktiveModule}" />
						</h:panelGroup>
					</h:panelGrid>

					<h:panelGroup rendered="#{LoginForm.maximaleBerechtigung == 1}">

						<h:commandButton id="button1" value="Read configuration"
							rendered="#{ModuleServerForm.running==false}"
							action="#{ModuleServerForm.readAllModulesFromConfiguraion}" />
						<h:commandButton id="button2" value="Start all modules"
							rendered="#{ModuleServerForm.running==true}"
							action="#{ModuleServerForm.startAllModules}" />
						<h:commandButton id="button3" value="Stop all modules"
							rendered="#{ModuleServerForm.running==true}"
							action="#{ModuleServerForm.stopAllModules}" />


						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>

									<%-- Überschrift --%>
									<htm:h3>
										<h:outputText value="#{msgs.aktiveModule}" />
									</htm:h3>

									<%-- globale Warn- und Fehlermeldungen --%>
									<h:messages globalOnly="true" errorClass="text_red"
										title="Meldungen" layout="table" infoClass="text_blue"
										showDetail="true" showSummary="true" tooltip="true" />

									<%-- Datentabelle --%>
									<x:dataTable id="benutzerliste" styleClass="standardTable"
										width="100%" cellspacing="1px" cellpadding="1px"
										headerClass="standardTable_Header"
										rowClasses="standardTable_Row1" var="item"
										value="#{ModuleServerForm.modulmanager}">

										<h:column>
											<f:facet name="header">
												<h:outputText value="#{msgs.titel}" />
											</f:facet>
											<h:outputText value="#{item.name}" />
										</h:column>

										<h:column>
											<f:facet name="header">
												<h:outputText value="#{msgs.bemerkung}" />
											</f:facet>
											<h:outputText value="#{item.comments}" />
										</h:column>

										<h:column>
											<f:facet name="header">
												<h:outputText value="#{msgs.status}" />
											</f:facet>
											<h:outputText value="#{item.moduleClient.status}" />
										</h:column>

										<h:column rendered="#{ModuleServerForm.running==true}">
											<f:facet name="header">
												<h:outputText value="#{msgs.shortsession}" />
											</f:facet>
											<h:commandLink action="#{ModuleServerForm.startModule}"
												rendered="#{item.moduleClient.status=='not active'}"
												title="initialize">
												<h:graphicImage
													value="/newpages/images/icons/start_task.gif" />
												<x:updateActionListener value="#{item}"
													property="#{ModuleServerForm.myModule}" />
											</h:commandLink>
											<h:commandLink action="#{ModuleServerForm.stopModule}"
												rendered="#{item.moduleClient.status=='active'}"
												title="shutdown">
												<h:graphicImage value="/newpages/images/icons/stop_task.gif" />
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
				</h:form>
				<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

			</htm:td>
		</htm:tr>
		<%@include file="inc/tbl_Fuss.jsp"%>
	</htm:table>

	</body>
</f:view>

</html>
