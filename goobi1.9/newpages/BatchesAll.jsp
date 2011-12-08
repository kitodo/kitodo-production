<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%-- ######################################## 

							Alle Aktuellen Schritte in der �?bersicht

	#########################################--%>
<a4j:keepAlive beanName="BatchForm" />
<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body>

		<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable" align="center">
			<%@include file="inc/tbl_Kopf.jsp"%>
			<htm:tr>
				<%@include file="inc/tbl_Navigation.jsp"%>
				<htm:td valign="top" styleClass="layoutInhalt">

					<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
					<h:form id="mytaskform" rendered="#{((LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2))}">
						<%-- Breadcrumb --%>
						<h:panelGrid id="id0" width="100%" columns="1" styleClass="layoutInhaltKopf">
							<h:panelGroup id="id1">
								<h:commandLink id="id2" value="#{msgs.startseite}" action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText id="id3" value="#{msgs.batches}" />
							</h:panelGroup>
						</h:panelGrid>

						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>

									<%-- �?berschrift --%>
									<htm:h3>
										<h:outputText id="id4" value="#{msgs.batches}" />
									</htm:h3>

									<%-- globale Warn- und Fehlermeldungen --%>
									<h:messages id="id5" globalOnly="true" errorClass="text_red" infoClass="text_blue" showDetail="true" showSummary="true" tooltip="true" />


									<htm:table width="100%">
										<htm:tr>
											<htm:td>
												<h:panelGrid columns="1">
													<h:outputText value="#{msgs.batches}" />
													<h:panelGroup>
														<h:inputText value="#{BatchForm.batchfilter}" />
														<h:commandButton action="#{BatchForm.filterBatches}" title="#{msgs.filter}" value="#{msgs.filter}" />
													</h:panelGroup>
													<h:selectManyListbox value="#{BatchForm.selectedBatches}" size="20">
														<si:selectItems var="batch" value="#{BatchForm.currentBatches}" itemLabel="#{batch.batchLabel}" itemValue="#{batch.batchId}" />
													</h:selectManyListbox>


													<h:commandLink action="#{BatchForm.loadProcessData}">
														<h:graphicImage alt="/newpages/images/ajaxload_small.gif" value="/newpages/images/ajaxload_small.gif" style="vertical-align:middle" />
														<h:outputText value="#{msgs.loadProcessData}" />
													</h:commandLink>

													<h:commandLink action="#{BatchForm.downloadDocket}">
														<h:graphicImage alt="/newpages/images/buttons/laufzettel_wide.png" value="/newpages/images/buttons/laufzettel_wide.png"
															style="vertical-align:middle" />
														<h:outputText value="#{msgs.laufzettelDrucken}" />
													</h:commandLink>

													<h:commandLink action="#{BatchForm.deleteBatch}">
														<h:graphicImage alt="/newpages/images/buttons/delete.gif" value="/newpages/images/buttons/delete.gif" style="vertical-align:middle" />
														<h:outputText value="#{msgs.deleteBatch}" />
													</h:commandLink>


												</h:panelGrid>
											</htm:td>


											<htm:td>
												<h:panelGrid columns="1">
													<h:outputText value="#{msgs.prozesse}" />
													<h:panelGroup>
														<h:inputText value="#{BatchForm.processfilter}" />
														<h:commandButton action="#{BatchForm.filterProcesses}" value="#{msgs.filter}" title="#{msgs.filter}" />
													</h:panelGroup>

													<h:outputText value="#{msgs.prozesse}" />

													<h:selectManyListbox value="#{BatchForm.selectedProcesses}" converter="ProcessConverter" size="20">
														<f:selectItems value="#{BatchForm.currentProcessesAsSelectItems}" />
													</h:selectManyListbox>

													<h:commandLink action="#{BatchForm.loadBatchData}">
														<h:graphicImage alt="/newpages/images/ajaxload_small.gif" value="/newpages/images/ajaxload_small.gif" style="vertical-align:middle" />
														<h:outputText value="#{msgs.loadBatchData}" />
													</h:commandLink>

													<h:commandLink action="#{BatchForm.addProcessesToBatch}">
														<h:graphicImage alt="/newpages/images/plus.gif" value="/newpages/images/plus.gif" style="vertical-align:middle" />
														<h:outputText value="#{msgs.addToBatch}" />
													</h:commandLink>

													<h:commandLink action="#{BatchForm.removeProcessesFromBatch}">
														<h:graphicImage alt="/newpages/images/minus.gif" value="/newpages/images/minus.gif" style="vertical-align:middle" />
														<h:outputText value="#{msgs.removeFromBatch}" />
													</h:commandLink>
													
													<h:commandLink action="#{BatchForm.createNewBatch}">
														<h:graphicImage alt="/newpages/images/buttons/star_blue.gif" value="/newpages/images/buttons/star_blue.gif" style="vertical-align:middle" />
														<h:outputText value="#{msgs.createNewBatch}" />
													</h:commandLink>


												</h:panelGrid>
											</htm:td>
										</htm:tr>
									</htm:table>



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
