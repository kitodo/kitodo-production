<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi"
	prefix="si"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>


<%--
  ~ This file is part of the Goobi Application - a Workflow tool for the support of
  ~ mass digitization.
  ~
  ~ Visit the websites for more information.
  ~     - http://gdz.sub.uni-goettingen.de
  ~     - http://www.goobi.org
  ~     - http://launchpad.net/goobi-production
  ~
  ~ This program is free software; you can redistribute it and/or modify it under
  ~ the terms of the GNU General Public License as published by the Free Software
  ~ Foundation; either version 2 of the License, or (at your option) any later
  ~ version.
  ~
  ~ This program is distributed in the hope that it will be useful, but WITHOUT ANY
  ~ WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  ~ PARTICULAR PURPOSE. See the GNU General Public License for more details. You
  ~ should have received a copy of the GNU General Public License along with this
  ~ program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  ~ Suite 330, Boston, MA 02111-1307 USA
  --%>


<%-- ########################################

							Projekt bearbeiten

	#########################################--%>
<a4j:keepAlive beanName="ProjekteForm" />
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
					<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:commandLink value="#{msgs.startseite}" action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:commandLink value="#{msgs.projekte}" action="ProjekteAlle" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText value="#{msgs.neuesProjektAnlegen}"
								rendered="#{ProjekteForm.myProjekt.id == null}" />
							<h:outputText value="#{msgs.projektBearbeiten}"
								rendered="#{ProjekteForm.myProjekt.id != null}" />
						</h:panelGroup>
					</h:panelGrid>

					<htm:table border="0" align="center" width="100%" cellpadding="15">
						<htm:tr>
							<htm:td>
								<htm:h3>
									<h:outputText value="#{msgs.neuesProjektAnlegen}"
										rendered="#{ProjekteForm.myProjekt.id == null}" />
									<h:outputText value="#{msgs.projektBearbeiten}"
										rendered="#{ProjekteForm.myProjekt.id != null}" />
								</htm:h3>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages globalOnly="false" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" />

								<%-- Box für die Bearbeitung der Details --%>
								<htm:table cellpadding="3" cellspacing="0" width="100%"
									styleClass="eingabeBoxen">

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row1" align="left">
											<h:outputText value="#{msgs.details}" />
										</htm:td>
										<htm:td styleClass="eingabeBoxen_row1" align="right">
											<h:commandLink action="#{NavigationForm.Reload}">
												<h:graphicImage value="/newpages/images/reload.gif" />
											</h:commandLink>
										</htm:td>
									</htm:tr>

									<%-- Formular für die Bearbeitung der Texte --%>
									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row2" colspan="2">

											<div class="yui-skin-sam"><h:outputText
												value="#{ProjekteForm.currentTab}" />
											<div id="demo" class="yui-navset">
											<ul class="yui-nav">

												<htm:li
													styleClass="#{ProjekteForm.currentTab =='tab1'?'selected':''}">
													<h:commandLink action="#{ProjekteForm.Apply}">
														<h:outputText value="<em>" escape="false" />
														<h:outputText value="Details" />
														<h:outputText value="</em>" escape="false" />
														<x:updateActionListener value="tab1"
															property="#{ProjekteForm.currentTab}" />
													</h:commandLink>
												</htm:li>

												<htm:li
													styleClass="#{ProjekteForm.currentTab =='tab2'?'selected':''}">
													<h:commandLink action="#{ProjekteForm.Apply}">
														<h:outputText value="<em>" escape="false" />
														<h:outputText value="DMS Import" />
														<h:outputText value="</em>" escape="false" />
														<x:updateActionListener value="tab2"
															property="#{ProjekteForm.currentTab}" />
													</h:commandLink>
												</htm:li>

												<htm:li
													styleClass="#{ProjekteForm.currentTab =='tab3'?'selected':''}">
													<h:commandLink action="#{ProjekteForm.Apply}">
														<h:outputText value="<em>" escape="false" />
														<h:outputText value="Mets Parameter" />
														<h:outputText value="</em>" escape="false" />
														<x:updateActionListener value="tab3"
															property="#{ProjekteForm.currentTab}" />
													</h:commandLink>
												</htm:li>

											</ul>
											<div class="yui-content">
											<div id="tab1"><h:panelGrid columns="2"
												rowClasses="top">

												<%-- Titel --%>
												<h:outputLabel for="titel" value="#{msgs.titel}" />
												<h:panelGroup>
													<h:inputText id="titel"
														style="width: 300px;margin-right:15px"
														value="#{ProjekteForm.myProjekt.titel}" required="true" />
													<x:message for="titel" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<%-- Mets als internes Speicherformat (ansonsten xStream) 
												<h:outputText value="#{msgs.metsAlsInternesSpeicherformat}" />
												<h:selectBooleanCheckbox style="margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsFormatInternal}" /> --%>

												<%-- Mets als Exportformat (ansonsten RDF XML)
												<h:outputText value="#{msgs.metsAlsDmsExportformat}" />
												<h:selectBooleanCheckbox style="margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsFormatDmsExport}" /> --%>

												<%-- internes Speicherformat --%>
												<h:outputText value="#{msgs.internesSpeicherformat}" />
												<h:selectOneMenu style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.fileFormatInternal}">
													<si:selectItems
														value="#{HelperForm.fileFormatsInternalOnly}" var="format"
														itemLabel="#{format}" itemValue="#{format}" />
												</h:selectOneMenu>

												<%-- Speicherformate für das DMS (mehrere möglich) --%>
												<h:outputText value="#{msgs.dmsExportformat}" />
												<h:selectOneMenu style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.fileFormatDmsExport}">
													<si:selectItems value="#{HelperForm.fileFormats}"
														var="format" itemLabel="#{format}" itemValue="#{format}" />
												</h:selectOneMenu>

												<%-- test for new project data--%>

												<%-- number of pages --%>
												<h:outputText id="vwid8" value="#{msgs.numberImages}:" />
												<h:inputText style="width: 200px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.numberOfPages}">
													<%--		<a4j:support id="vwid10" event="onkeyup"
						reRender="projectForm:calcs" />  --%>
												</h:inputText>

												<%-- number of volumes --%>
												<h:outputText id="vwid11" value="#{msgs.numberVolumes}:" />
												<h:inputText style="width: 200px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.numberOfVolumes}">
													<%--				<a4j:support id="vwid13" event="onkeyup"
						reRender="projectForm:calcs" /> --%>
												</h:inputText>

												<%-- startdate --%>
												<h:outputText id="vwid14" value="#{msgs.startdate}:" />
												<h:inputText style="width: 200px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.startDate}" />

												<%-- enddate --%>
												<h:outputText id="vwid17" value="#{msgs.enddate}:" />
												<h:inputText style="width: 200px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.endDate}" />

												<%-- 
																	<f:facet name="header">
																		<h:outputText id="vwid20" value="#{msgs.berechnungen}"
																			style="font-weight:normal" />
																	</f:facet>
																	<h:panelGrid id="vwid21" columns="2" rowClasses="top">
														--%>

												<%-- pages per volume --%>
												<h:outputText id="vwid22" value="#{msgs.imagesPerVolume}:" />
												<h:outputText id="vwid23"
													value="#{ProjekteForm.calcImagesPerVolume}" />

												<%-- duration --%>
												<h:outputText id="vwid24" value="#{msgs.durationInMonth}:" />
												<h:outputText id="vwid25"
													value="#{ProjekteForm.calcDuration}" />

												<%-- throughputPerYear --%>
												<h:outputText id="vwid26" value="#{msgs.volumesPerYear}:"
													rendered="#{ProjekteForm.calcDuration > 11}" />
												<h:outputText id="vwid27"
													value="#{ProjekteForm.calcThroughputPerYear}"
													rendered="#{ProjekteForm.calcDuration > 11}" />

												<%-- throughputPerQuarter --%>
												<h:outputText id="vwid28" value="#{msgs.volumesPerQuarter}:"
													rendered="#{ProjekteForm.calcDuration > 2}" />
												<h:outputText id="vwid29"
													value="#{ProjekteForm.calcThroughputPerQuarter}"
													rendered="#{ProjekteForm.calcDuration > 2}" />

												<%-- throughputPerMonth --%>
												<h:outputText id="vwid30" value="#{msgs.volumesPerMonth}:"
													rendered="#{ProjekteForm.calcDuration > 0}" />
												<h:outputText id="vwid31"
													value="#{ProjekteForm.calcThroughputPerMonth}"
													rendered="#{ProjekteForm.calcDuration > 0}" />

												<%-- throughputPerDay --%>
												<h:outputText id="vwid32" value="#{msgs.volumesPerDay}:" />
												<h:outputText id="vwid33"
													value="#{ProjekteForm.calcThroughputPerDay}" />

											</h:panelGrid></div>
											<div id="tab2"><h:panelGrid columns="2"
												rowClasses="top">
												<%-- nutzt DMS-Import --%>
												<h:outputText value="#{msgs.automatischerDmsImport}" />
												<h:selectBooleanCheckbox style="margin-right:15px"
													value="#{ProjekteForm.myProjekt.useDmsImport}" />

												<%-- Dms-Import-XML-DATEI-Pfad --%>
												<h:outputText value="#{msgs.dmsImportPfadXmlDatei}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.dmsImportRootPath}" />

												<%-- Dms-Import-Images-Pfad --%>
												<h:outputText value="#{msgs.dmsImportImagesPfad}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.dmsImportImagesPath}" />

												<%-- Dms-Import-Success-Pfad --%>
												<h:outputText value="#{msgs.dmsImportSuccessPfad}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.dmsImportSuccessPath}" />

												<%-- Dms-Import-Error-Pfad --%>
												<h:outputText value="#{msgs.dmsImportErrorPfad}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.dmsImportErrorPath}" />

												<%-- nutzt DMS-Import --%>
												<h:outputText value="#{msgs.dmsImportCreateProcessFolder}" />
												<h:selectBooleanCheckbox style="margin-right:15px"
													value="#{ProjekteForm.myProjekt.dmsImportCreateProcessFolder}" />

												<%-- Timeout --%>
												<h:outputLabel for="timeout" value="#{msgs.timeout} (ms)" />
												<h:panelGroup>
													<h:inputText id="timeout"
														style="width: 300px;margin-right:15px"
														value="#{ProjekteForm.myProjekt.dmsImportTimeOut}"
														required="true" />
													<x:message for="timeout" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>
											</h:panelGrid></div>
											<div id="tab3"><h:panelGrid columns="2"
												rowClasses="top">

												<%-- metsRightsOwner --%>
												<h:outputText value="#{msgs.metsRightsOwner}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsRightsOwner}" />

												<%-- metsRightsOwnerLogo --%>
												<h:outputText value="#{msgs.metsRightsOwnerLogo}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsRightsOwnerLogo}" />

												<%-- metsRightsOwnerSite --%>
												<h:outputText value="#{msgs.metsRightsOwnerSite}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsRightsOwnerSite}" />

												<%-- metsRightsOwnerMail --%>
												<h:outputText value="#{msgs.metsRightsOwnerMail}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsRightsOwnerMail}" />

												<%-- metsDigiprovReference --%>
												<h:outputText value="#{msgs.metsDigiprovReference}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsDigiprovReference}" />

												<%-- metsDigiprovPresentation --%>
												<h:outputText value="#{msgs.metsDigiprovPresentation}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsDigiprovPresentation}" />

												<%-- metsDigiprovReferenceAnchor --%>
												<h:outputText value="#{msgs.metsDigiprovReferenceAnchor}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsDigiprovReferenceAnchor}" />

												<%-- metsDigiprovPresentationAnchor--%>
												<h:outputText value="#{msgs.metsDigiprovPresentationAnchor}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsDigiprovPresentationAnchor}" />

												<%-- metsPointerPath --%>
												<h:outputText value="#{msgs.metsPointerPath}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsPointerPath}" />

												<%-- metsPointerPathAnchor --%>
												<h:outputText value="#{msgs.metsPointerPathAnchor}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsPointerPathAnchor}" />

												<%-- metsPurl --%>
												<h:outputText value="#{msgs.metsPurl}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsPurl}" />

												<%-- metsContentIDs --%>
												<h:outputText value="#{msgs.metsContentIDs}" />
												<h:inputText style="width: 300px;margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsContentIDs}" />

												<%-- FileGroups --%>
												<h:outputText value="#{msgs.metsfilegroups}" />
												<h:panelGroup>
													<x:dataTable width="300"
														columnClasses="standardTable_Column,standardTable_Column,standardTable_ColumnCentered"
														var="item"
														value="#{ProjekteForm.myProjekt.filegroupsList}">
														<h:column>
															<h:outputText value="#{item.name}" />
														</h:column>
														<h:column>
															<%-- Bearbeiten-Schaltknopf --%>
															<h:panelGroup>
																<jp:popupFrame scrolling="auto" height="380px"
																	width="430px" topStyle="background: #1874CD;"
																	bottomStyleClass="popup_unten"
																	styleFrame="border-style: solid;border-color: #1874CD; border-width: 2px;"
																	styleClass="standardlink"
																	style="margin-top:2px;text-decoration:none"
																	actionOpen="#{ProjekteForm.filegroupEdit}"
																	actionClose="#{NavigationForm.Reload}" center="true"
																	title="#{msgs.filegroupBearbeiten}" immediate="true">
																	<x:updateActionListener
																		property="#{ProjekteForm.zurueck}"
																		value="FilegroupPopup" />
																	<x:updateActionListener
																		property="#{ProjekteForm.myFilegroup}" value="#{item}" />
																	<x:updateActionListener
																		property="#{ProjekteForm.myFilegroup}" value="#{item}" />
																	<h:graphicImage value="images/buttons/edit_20.gif" />
																</jp:popupFrame>
															</h:panelGroup>

															<%-- Löschen-Schaltknopf --%>
															<h:commandLink action="#{ProjekteForm.filegroupDelete}"
																title="#{msgs.filegroupLoeschen}"
																onclick="if (!confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')) return">
																<h:graphicImage value="images/buttons/waste1a_20px.gif" />
																<x:updateActionListener
																	property="#{ProjekteForm.myFilegroup}" value="#{item}" />
															</h:commandLink>
														</h:column>
													</x:dataTable>
													<%-- Hinzufügen-Schaltknopf --%>
													<h:panelGroup>
														<jp:popupFrame scrolling="auto" height="380px"
															width="430px" topStyle="background: #1874CD;"
															bottomStyleClass="popup_unten"
															styleFrame="border-style: solid;border-color: #1874CD; border-width: 2px;"
															styleClass="standardlink"
															style="margin-top:2px;display:block; text-decoration:none"
															actionOpen="#{ProjekteForm.filegroupAdd}"
															actionClose="#{NavigationForm.Reload}" center="true"
															title="#{msgs.filegroupHinzufuegen}" immediate="true">
															<x:updateActionListener
																property="#{ProjekteForm.zurueck}"
																value="FilegroupPopup" />
															<h:outputText style="border-bottom: #a24033 dashed 1px;"
																value="#{msgs.filegroupHinzufuegen}" />
														</jp:popupFrame>
													</h:panelGroup>
												</h:panelGroup>
											</h:panelGrid></div>
											</div>
											</div>
											</div>

										</htm:td>
									</htm:tr>

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row3" align="left">
											<h:commandButton value="#{msgs.abbrechen}"
												action="ProjekteAlle" immediate="true" />
										</htm:td>
										<htm:td styleClass="eingabeBoxen_row3" align="right">
											<h:commandButton value="#{msgs.loeschen}"
												action="#{ProjekteForm.Loeschen}"
												onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')"
												rendered="#{ProjekteForm.myProjekt.id != null && ProjekteForm.myProjekt.deleteAble}" />
											<h:commandButton value="#{msgs.uebernehmen}"
												action="#{ProjekteForm.Apply}" />

											<h:commandButton value="#{msgs.speichern}"
												action="#{ProjekteForm.Speichern}" />

										</htm:td>
									</htm:tr>
								</htm:table>
								<%-- // Box für die Bearbeitung der Details --%>

							</htm:td>
						</htm:tr>
					</htm:table>
				</h:form>
				<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

			</htm:td>
		</htm:tr>
		<%@include file="inc/tbl_Fuss.jsp"%>
	</htm:table>

	<script type="text/javascript"> 
    var tabView = new YAHOO.widget.TabView('demo'); 
	</script>

	</body>
</f:view>


</html>
