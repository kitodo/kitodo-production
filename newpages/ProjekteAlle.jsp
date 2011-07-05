<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%-- ######################################## 

							Alle Projekte in der Übersicht

	#########################################--%>
<a4j:keepAlive beanName="ProjekteForm"/>
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
				<h:form id="projectform">
					<%-- Breadcrumb --%>
					<h:panelGrid columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:commandLink value="#{msgs.startseite}" action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText value="#{msgs.projekte}" />
						</h:panelGroup>
					</h:panelGrid>

					<htm:table border="0" align="center" width="100%" cellpadding="15">
						<htm:tr>
							<htm:td>

								<%-- Überschrift --%>
								<htm:h3>
									<h:outputText value="#{msgs.projekte}" />
								</htm:h3>

								<%-- Neu-Schaltknopf --%>
								<h:commandLink action="#{ProjekteForm.Neu}" immediate="true"
									rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
									<h:outputText value="#{msgs.neuesProjektAnlegen}" />
								</h:commandLink>


								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages globalOnly="true" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" />

								<%-- Datentabelle --%>
								<x:dataTable styleClass="standardTable" width="100%"
									cellspacing="1px" cellpadding="1px"
									headerClass="standardTable_Header"
									rowClasses="standardTable_Row1,standardTable_Row2"
									columnClasses="standardTable_Column,standardTable_ColumnCentered"
									var="item" value="#{ProjekteForm.page.listReload}">

									<h:column>
										<f:facet name="header">
											<h:outputText value="#{msgs.projekt}" />
										</f:facet>
										<h:outputText value="#{item.titel}" />
									</h:column>

								    <%-- +++++++++++++++++  Mets ++++++++++++++++++++++++ 
								     <x:column style="text-align:center">
                                        <f:facet name="header">
                                            <h:outputText value="#{msgs.metsAlsInternesSpeicherformat}" />
                                        </f:facet>
                                        <h:graphicImage value="/newpages/images/check_true.gif"
                                            rendered="#{item.metsFormatInternal}" />
                                        <h:graphicImage value="/newpages/images/check_false.gif"
                                            rendered="#{!item.metsFormatInternal}" />
                                    </x:column>
                                    <x:column style="text-align:center">
								        <f:facet name="header">
								            <h:outputText value="#{msgs.metsAlsDmsExportformat}" />
								        </f:facet>
								        <h:graphicImage value="/newpages/images/check_true.gif"
								            rendered="#{item.metsFormatDmsExport}" />
								        <h:graphicImage value="/newpages/images/check_false.gif"
								            rendered="#{!item.metsFormatDmsExport}" />
								    </x:column>--%>
								    
								    <%-- +++++++++++++++++  FileFormats ++++++++++++++++++++++++ --%>
                                     <x:column style="text-align:center">
                                        <f:facet name="header">
                                            <h:outputText value="#{msgs.internesSpeicherformat}" />
                                        </f:facet>
                                        <h:outputText value="#{item.fileFormatInternal}" />
                                    </x:column>
                                    <x:column style="text-align:center">
                                        <f:facet name="header">
                                            <h:outputText value="#{msgs.dmsExportformat}" />
                                        </f:facet>
                                        <h:outputText value="#{item.fileFormatDmsExport}" />
                                    </x:column>
                                    
									<x:column style="text-align:center"
										rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
										<f:facet name="header">
											<h:outputText value="#{msgs.auswahl}" />
										</f:facet>
										<%-- Bearbeiten-Schaltknopf --%>
										<h:commandLink action="ProjekteBearbeiten"
											title="#{msgs.projektBearbeiten}">
											<h:graphicImage value="/newpages/images/buttons/edit.gif" />
											<x:updateActionListener property="#{ProjekteForm.myProjekt}"
												value="#{item}" />
										</h:commandLink>
									</x:column>
								</x:dataTable>

								<htm:table width="100%" border="0">
									<htm:tr valign="top">
										<htm:td align="left">
	
										</htm:td>
										<htm:td align="center" >
											<%-- ===================== Datascroller für die Ergebnisse ====================== --%>
											<x:aliasBean alias="#{mypage}" value="#{ProjekteForm.page}">
												<jsp:include page="/newpages/inc/datascroller.jsp" />
											</x:aliasBean>
											<%-- ===================== // Datascroller für die Ergebnisse ====================== --%>
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
