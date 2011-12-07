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
					<h:form id="mytaskform">
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
													<h:selectManyListbox value="#{BatchForm.selectedBatches}"  size="20">
													<%-- 	<f:selectItems value="#{BatchForm.currentBatchesAsSelectItems}" />--%>
														<si:selectItems var="bla" value="#{BatchForm.currentBatchesAsSelectItems}" itemLabel="#{bla.label}" itemValue="#{bla.value}"/>
													</h:selectManyListbox>

													<%-- 		<h:selectManyListbox value="#{BatchForm.selectedBatches}" size="20">
														<f:selectItems value="#{BatchForm.currentBatchesAsSelectItems}" />
													</h:selectManyListbox>
--%>

													<h:commandLink action="#{BatchForm.loadProcessData}" value="#{msgs.loadProcessData}" />





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

													<h:commandLink action="#{BatchForm.loadBatchData}" value="#{msgs.loadBatchData}" />




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
