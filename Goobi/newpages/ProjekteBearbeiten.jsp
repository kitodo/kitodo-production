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

							Projekt bearbeiten

	#########################################--%>
<a4j:keepAlive beanName="ProjekteForm" />
<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<%-- css filepath configured for mac os x --%>
	<link href="../css/tabbedPane.css" rel="stylesheet" type="text/css" />

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
					<h:form id="projectform">
						<%-- Breadcrumb --%>
						<h:panelGrid width="100%" columns="1"
							styleClass="layoutInhaltKopf" id="projectgrid1">
							<h:panelGroup id="id1">
								<h:commandLink id="id2" value="#{msgs.startseite}"
									action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:commandLink id="id3" value="#{msgs.projekte}"
									action="ProjekteAlle" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText id="id4" value="#{msgs.neuesProjektAnlegen}"
									rendered="#{ProjekteForm.myProjekt.id == null}" />
								<h:outputText id="id5" value="#{msgs.projektBearbeiten}"
									rendered="#{ProjekteForm.myProjekt.id != null}" />
							</h:panelGroup>
						</h:panelGrid>

						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>
									<htm:h3>
										<h:outputText id="id6" value="#{msgs.neuesProjektAnlegen}"
											rendered="#{ProjekteForm.myProjekt.id == null}" />
										<h:outputText id="id7" value="#{msgs.projektBearbeiten}"
											rendered="#{ProjekteForm.myProjekt.id != null}" />
									</htm:h3>

									<%-- globale Warn- und Fehlermeldungen --%>
									<h:messages id="id8" globalOnly="true" errorClass="text_red"
										infoClass="text_blue" showDetail="true" showSummary="true"
										tooltip="true" />

									<%-- Box für die Bearbeitung der Details --%>
									<htm:table cellpadding="3" cellspacing="0" width="100%"
										styleClass="eingabeBoxen">

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row1" align="left">
												<h:outputText id="id9" value="#{msgs.details}" />
											</htm:td>
											<htm:td styleClass="eingabeBoxen_row1" align="right">
												<h:commandLink id="id10" action="#{NavigationForm.Reload}">
													<h:graphicImage id="id11"
														value="/newpages/images/reload.gif" />
												</h:commandLink>
											</htm:td>
										</htm:tr>

										<%-- Formular für die Bearbeitung der Texte --%>
										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row2" colspan="2">

												<%--<htm:div styleClass="yui-skin-sam"><%-- <htm:div styleClass="yui-skin-sam"> --%>
												<%--<h:outputText id="id12" value="#{ProjekteForm.currentTab}" />--%>
												<x:panelTabbedPane serverSideTabSwitch="true"
													immediateTabChange="false" styleClass="tabbedPane"
													activeTabStyleClass="activeTab"
													inactiveTabStyleClass="inactiveTab"
													disabledTabStyleClass="disabledTab"
													activeSubStyleClass="activeSub"
													inactiveSubStyleClass="inactiveSub"
													tabContentStyleClass="tabContent">
													<%--<htm:div id="demo" styleClass="yui-navset"> <%--YUI obere Navigation Start --%>
													<%--
													<ul class="yui-nav">
													--%>
													<x:panelTab label="#{msgs.details}">
														<%--<h:form id="formTabDetails">--%>
														<%--
														<htm:li
															styleClass="#{ProjekteForm.currentTab =='tab1'?'selected':''}">
															<h:commandLink id="id13" action="#{ProjekteForm.Apply}">
																<h:outputText id="id14" value="<em>" escape="false" />
																<h:outputText id="id15" value="Details" />
																<h:outputText id="id16" value="</em>" escape="false" />
																<x:updateActionListener value="tab1"
																	property="#{ProjekteForm.currentTab}" />
															</h:commandLink>
														</htm:li>
													 --%>
														<htm:div id="tab1">
															<h:panelGrid columns="2" rowClasses="top" id="projgrid2"
																styleClass="tableDetails ">

																<%-- Titel --%>
																<h:outputLabel id="id17" for="titel"
																	value="#{msgs.titel}" />
																<h:panelGroup id="id18">
																	<h:inputText id="titel"
																		style="width: 200px;margin-right:15px"
																		value="#{ProjekteForm.myProjekt.titel}"
																		required="true" />
																	<x:message id="id19" for="titel" style="color: red"
																		replaceIdWithLabel="true" />
																</h:panelGroup>

																<%-- Mets als internes Speicherformat (ansonsten xStream) 
												<h:outputText id="id20" value="#{msgs.metsAlsInternesSpeicherformat}" />
												<h:selectBooleanCheckbox id="id21" style="margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsFormatInternal}" /> --%>

																<%-- Mets als Exportformat (ansonsten RDF XML)
												<h:outputText id="id22" value="#{msgs.metsAlsDmsExportformat}" />
												<h:selectBooleanCheckbox id="id23" style="margin-right:15px"
													value="#{ProjekteForm.myProjekt.metsFormatDmsExport}" /> --%>


																<%-- test for new project data--%>

																<%-- number of pages --%>

																<h:outputText id="vwid8" value="#{msgs.numberImages}:" />
																<h:panelGroup>
																	<h:inputText id="id24"
																		style="width: 200px;margin-right:15px"
																		value="#{ProjekteForm.myProjekt.numberOfPages}">
																		<%--		<a4j:support id="vwid10" event="onkeyup" reRender="projectForm:calcs" />  --%>
																	</h:inputText>
																	<h:commandLink
																		action="#{ProjekteForm.GenerateValuesForStatistics}">
																		<h:graphicImage alt="#{msgs.generateValues}"
																			value="/newpages/images/buttons/reload.gif"
																			style="margin-right:10px" />
																	</h:commandLink>
																</h:panelGroup>

																<%-- number of volumes --%>
																<h:outputText id="vwid11" value="#{msgs.numberVolumes}:" />
																<h:inputText id="id25"
																	style="width: 200px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.numberOfVolumes}">
																	<%--				<a4j:support id="vwid13" event="onkeyup" reRender="projectForm:calcs" /> --%>
																</h:inputText>

																<%-- startdate --%>
																<h:outputText id="vwid14" value="#{msgs.startdate}:" />
																<%--
																	<h:inputText id="id26" style="width: 200px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.startDate}" />
																--%>
																<x:inputCalendar id="vwid15" style="width:200px"
																	value="#{ProjekteForm.myProjekt.startDate}"
																	renderAsPopup="true" renderPopupButtonAsImage="true"
																	popupTodayString="#{msgs.heute}"
																	popupWeekString="#{msgs.kw}"
																	styleClass="projekteBearbeiten"
																	imageLocation="/newpages/images/calendarImages"
																	popupButtonImageUrl="/newpages/images/calendarImages/calendar.gif" />

																<%-- enddate --%>
																<h:outputText id="vwid17" value="#{msgs.enddate}:" />
																<%--
																<h:inputText id="id27" style="width: 200px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.endDate}" />
																--%>
																<x:inputCalendar id="vwid18" style="width:200px"
																	value="#{ProjekteForm.myProjekt.endDate}"
																	renderAsPopup="true" renderPopupButtonAsImage="true"
																	popupTodayString="#{msgs.heute}"
																	popupWeekString="#{msgs.kw}"
																	styleClass="projekteBearbeiten"
																	monthYearRowClass="monthYearRowClass"
																	imageLocation="/newpages/images/calendarImages"
																	popupButtonImageUrl="/newpages/images/calendarImages/calendar.gif" />
																<%--popupButtonImageUrl="images/calender.gif"--%>

																<h:outputText id="vwid36a"
																	value="#{msgs.projectIsArchived}: " />
																<h:selectBooleanCheckbox id="vwid37b"
																	style="margin-right:15px"
																	value="#{ProjekteForm.myProjekt.projectIsArchived}" />

															</h:panelGrid>
															<%-- 
												<f:facet name="header">
													<h:outputText id="vwid20" value="#{msgs.berechnungen}"
														style="font-weight:normal" />
												</f:facet>
												<h:panelGrid id="vwid21" columns="2" rowClasses="top">
												--%>
															<htm:table styleClass="tableCalcStat tableDetails">

																<htm:tbody>
																	<htm:tr styleClass="header">
																		<htm:td>
																			<%-- pages per volume --%>
																			<h:outputText id="vwid22"
																				value="#{msgs.imagesPerVolume}:" />
																		</htm:td>
																		<htm:td styleClass="left">
																			<h:outputText id="vwid23"
																				value="#{ProjekteForm.calcImagesPerVolume}" />
																		</htm:td>
																		<htm:td></htm:td>
																		<htm:td></htm:td>
																	</htm:tr>
																	<htm:tr>
																		<%-- duration --%>
																		<htm:td>
																			<h:outputText id="vwid24"
																				value="#{msgs.durationInMonth}:" />
																		</htm:td>
																		<htm:td styleClass="left">
																			<h:outputText id="vwid25"
																				value="#{ProjekteForm.calcDuration}" />
																		</htm:td>
																		<htm:td></htm:td>
																		<htm:td></htm:td>
																	</htm:tr>
																	<htm:tr rendered="#{ProjekteForm.calcDuration > 11}">
																		<%-- throughputPerYear --%>
																		<htm:td>
																			<h:outputText id="vwid26"
																				value="#{msgs.volumesPerYear}:"
																				rendered="#{ProjekteForm.calcDuration > 11}" />
																		</htm:td>
																		<htm:td styleClass="left">
																			<h:outputText id="vwid27"
																				value="#{ProjekteForm.calcThroughputPerYear}"
																				rendered="#{ProjekteForm.calcDuration > 11}" />
																		</htm:td>
																		<htm:td styleClass="right">
																			<h:outputText id="id28" value="#{msgs.pagesPerYear}:" />
																		</htm:td>
																		<htm:td>
																			<h:outputText id="id29"
																				value="#{ProjekteForm.calcThroughputPagesPerYear}" />
																		</htm:td>
																	</htm:tr>
																	<htm:tr rendered="#{ProjekteForm.calcDuration > 2}">
																		<%-- throughputPerQuarter --%>
																		<htm:td>
																			<h:outputText id="vwid28"
																				value="#{msgs.volumesPerQuarter}:"
																				rendered="#{ProjekteForm.calcDuration > 2}" />
																		</htm:td>
																		<htm:td styleClass="left">
																			<h:outputText id="vwid29"
																				value="#{ProjekteForm.calcThroughputPerQuarter}"
																				rendered="#{ProjekteForm.calcDuration > 2}" />
																		</htm:td>
																		<htm:td styleClass="right">
																			<%--pages per Quarter --%>
																			<h:outputText id="vwid38"
																				value="#{msgs.pagesPerQuarter}:" />
																		</htm:td>
																		<htm:td>
																			<h:outputText id="vwid39"
																				value="#{ProjekteForm.calcTroughputPagesPerQuarter}" />
																		</htm:td>

																	</htm:tr>
																	<htm:tr rendered="#{ProjekteForm.calcDuration > 0}">
																		<%-- throughputPerMonth --%>
																		<htm:td>
																			<h:outputText id="vwid30"
																				value="#{msgs.volumesPerMonth}:"
																				rendered="#{ProjekteForm.calcDuration > 0}" />
																		</htm:td>
																		<htm:td styleClass="left">
																			<h:outputText id="vwid31"
																				value="#{ProjekteForm.calcThroughputPerMonth}"
																				rendered="#{ProjekteForm.calcDuration > 0}" />
																		</htm:td>
																		<%-- average pages per month --%>
																		<htm:td styleClass="right">

																			<h:outputText id="vwid36"
																				value="#{msgs.pagesPerMonth}:" />
																		</htm:td>
																		<htm:td>
																			<h:outputText id="vwid37"
																				value="#{ProjekteForm.calcThroughputPagesPerMonth}" />
																		</htm:td>
																	</htm:tr>
																	<htm:tr>

																		<%-- throughputPerDay --%>
																		<htm:td>
																			<h:outputText id="vwid32"
																				value="#{msgs.volumesPerDay}:" />
																		</htm:td>
																		<htm:td styleClass="left">
																			<h:outputText id="vwid33"
																				value="#{ProjekteForm.calcThroughputPerDay}" />
																		</htm:td>
																		<htm:td styleClass="right">
																			<%--pages per day --%>
																			<h:outputText id="vwid34"
																				value="#{msgs.pagesPerDay}:" />
																		</htm:td>
																		<htm:td>
																			<h:outputText id="vwid35"
																				value="#{ProjekteForm.calcPagesPerDay}" />
																		</htm:td>
																	</htm:tr>

																</htm:tbody>
															</htm:table>
														</htm:div>

														<%--</h:form> <%--END form id="formTabDetails" --%>
													</x:panelTab>
													<%-- END label="Details" --%>

													<x:panelTab label="#{msgs.technischeDaten}">
														<%--	
														<htm:li
															styleClass="#{ProjekteForm.currentTab =='tab2'?'selected':''}">
															<h:commandLink id="id30" action="#{ProjekteForm.Apply}">
																<h:outputText id="id31" value="<em>" escape="false" />
																<h:outputText id="id32" value="DMS Import" />
																<h:outputText id="id33" value="</em>" escape="false" />
																<x:updateActionListener value="tab2"
																	property="#{ProjekteForm.currentTab}" />
															</h:commandLink>
														</htm:li>
														 --%>
														<htm:div id="tab2">
															<h:panelGrid columns="2" rowClasses="top" id="projgrid3">
																<%-- internes Speicherformat --%>
																<h:outputText id="id34"
																	value="#{msgs.internesSpeicherformat}" />
																<h:selectOneMenu id="id35"
																	style="width: 300px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.fileFormatInternal}">
																	<si:selectItems
																		value="#{HelperForm.fileFormatsInternalOnly}"
																		var="format" itemLabel="#{format}"
																		itemValue="#{format}" />
																</h:selectOneMenu>

																<%-- Speicherformate für das DMS (mehrere möglich) --%>
																<h:outputText id="id36" value="#{msgs.dmsExportformat}" />
																<h:selectOneMenu id="id37"
																	style="width: 300px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.fileFormatDmsExport}">
																	<si:selectItems id="id38"
																		value="#{HelperForm.fileFormats}" var="format"
																		itemLabel="#{format}" itemValue="#{format}" />
																</h:selectOneMenu>

																<%-- nutzt DMS-Import --%>
																<h:outputText id="id39"
																	value="#{msgs.automatischerDmsImport}" />
																<h:selectBooleanCheckbox id="id40"
																	style="margin-right:15px"
																	value="#{ProjekteForm.myProjekt.useDmsImport}" />

																<%-- Dms-Import-XML-DATEI-Pfad --%>
																<h:outputText id="id41"
																	value="#{msgs.dmsImportPfadXmlDatei}" />
																<h:inputText id="id42"
																	style="width: 300px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.dmsImportRootPath}" />

																<%-- Dms-Import-Images-Pfad --%>
																<h:outputText id="id43"
																	value="#{msgs.dmsImportImagesPfad}" />
																<h:inputText id="id44"
																	style="width: 300px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.dmsImportImagesPath}" />

																<%-- Dms-Import-Success-Pfad --%>
																<h:outputText id="id45"
																	value="#{msgs.dmsImportSuccessPfad}" />
																<h:inputText id="id46"
																	style="width: 300px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.dmsImportSuccessPath}" />

																<%-- Dms-Import-Error-Pfad --%>
																<h:outputText id="id47"
																	value="#{msgs.dmsImportErrorPfad}" />
																<h:inputText id="id48"
																	style="width: 300px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.dmsImportErrorPath}" />

																<%-- nutzt DMS-Import --%>
																<h:outputText
																	value="#{msgs.dmsImportCreateProcessFolder}" />
																<h:selectBooleanCheckbox id="id49"
																	style="margin-right:15px"
																	value="#{ProjekteForm.myProjekt.dmsImportCreateProcessFolder}" />

																<%-- Timeout --%>
																<h:outputLabel id="id50" for="timeout"
																	value="#{msgs.timeout} (ms)" />
																<h:panelGroup id="id51">
																	<h:inputText id="timeout"
																		style="width: 300px;margin-right:15px"
																		value="#{ProjekteForm.myProjekt.dmsImportTimeOut}"
																		required="true" />
																	<x:message id="id52" for="timeout" style="color: red"
																		replaceIdWithLabel="true" />
																</h:panelGroup>
															</h:panelGrid>
														</htm:div>
													</x:panelTab>
													<%--END  label="DMS Import"--%>

													<x:panelTab label="#{msgs.metsParamater}">
														<%--
														<htm:li
															styleClass="#{ProjekteForm.currentTab =='tab3'?'selected':''}">
															<h:commandLink id="id53" action="#{ProjekteForm.Apply}">
																<h:outputText id="id54" value="<em>" escape="false" />
																<h:outputText id="id55" value="Mets Parameter" />
																<h:outputText id="id56" value="</em>" escape="false" />
																<x:updateActionListener value="tab3"
																	property="#{ProjekteForm.currentTab}" />
															</h:commandLink>
														</htm:li>
														 --%>
														<htm:div id="tab3">
															<h:panelGrid columns="2" rowClasses="top" id="projgrid4">

																<%-- metsRightsOwner --%>
																<h:outputText id="id57" value="#{msgs.metsRightsOwner}" />
																<h:inputText id="id58"
																	style="width: 650px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.metsRightsOwner}" />

																<%-- metsRightsOwnerLogo --%>
																<h:outputText id="id59"
																	value="#{msgs.metsRightsOwnerLogo}" />
																<h:inputText id="id60"
																	style="width: 650px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.metsRightsOwnerLogo}" />

																<%-- metsRightsOwnerSite --%>
																<h:outputText id="id61"
																	value="#{msgs.metsRightsOwnerSite}" />
																<h:inputText id="id62"
																	style="width: 650px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.metsRightsOwnerSite}" />

																<%-- metsRightsOwnerMail --%>
																<h:outputText id="id63"
																	value="#{msgs.metsRightsOwnerMail}" />
																<h:inputText id="id64"
																	style="width: 650px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.metsRightsOwnerMail}" />

																<%-- metsDigiprovReference --%>
																<h:outputText id="id65"
																	value="#{msgs.metsDigiprovReference}" />
																<h:inputText id="id66"
																	style="width: 650px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.metsDigiprovReference}" />

																<%-- metsDigiprovPresentation --%>
																<h:outputText id="id67"
																	value="#{msgs.metsDigiprovPresentation}" />
																<h:inputText id="id68"
																	style="width: 650px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.metsDigiprovPresentation}" />

																<%-- metsDigiprovReferenceAnchor --%>
																<h:outputText
																	value="#{msgs.metsDigiprovReferenceAnchor}" />
																<h:inputText id="id69"
																	style="width: 650px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.metsDigiprovReferenceAnchor}" />

																<%-- metsDigiprovPresentationAnchor--%>
																<h:outputText
																	value="#{msgs.metsDigiprovPresentationAnchor}" />
																<h:inputText id="id70"
																	style="width: 650px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.metsDigiprovPresentationAnchor}" />

																<%-- metsPointerPath --%>
																<h:outputText id="id71" value="#{msgs.metsPointerPath}" />
																<h:inputText id="id72"
																	style="width: 650px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.metsPointerPath}" />

																<%-- metsPointerPathAnchor --%>
																<h:outputText id="id73"
																	value="#{msgs.metsPointerPathAnchor}" />
																<h:inputText id="id74"
																	style="width: 650px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.metsPointerPathAnchor}" />

																<%-- metsPurl --%>

																<h:outputText id="id75" value="#{msgs.metsPurl}" />
																<h:inputText id="id76"
																	style="width: 650px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.metsPurl}" />

																<%-- metsContentIDs --%>
																<h:outputText id="id77" value="#{msgs.metsContentIDs}" />
																<h:inputText id="id78"
																	style="width: 650px;margin-right:15px"
																	value="#{ProjekteForm.myProjekt.metsContentIDs}" />

																<%-- FileGroups --%>
																<h:outputText id="id79" value="#{msgs.metsfilegroups}" />
																<h:panelGroup id="id80">
																	<x:dataTable id="id81" width="300"
																		columnClasses="standardTable_Column,standardTable_Column,standardTable_ColumnCentered"
																		var="item" value="#{ProjekteForm.fileGroupList}">
																		<h:column id="id82">
																			<h:outputText id="id83" value="#{item.name}" />
																		</h:column>
																		<h:column id="id84">
																			<%-- Bearbeiten-Schaltknopf --%>
																			<h:panelGroup id="id85">
																				<jp:popupFrame scrolling="auto" height="270px"
																					width="700px" topStyle="background: #1874CD;"
																					bottomStyleClass="popup_unten"
																					styleFrame="border-style: solid;border-color: #1874CD; border-width: 2px;"
																					styleClass="standardlink"
																					style="margin-top:2px;text-decoration:none"
																					actionOpen="#{ProjekteForm.filegroupEdit}"
																					actionClose="#{NavigationForm.Reload}"
																					center="true" title="#{msgs.filegroupBearbeiten}"
																					immediate="true">
																					<x:updateActionListener
																						property="#{ProjekteForm.zurueck}"
																						value="FilegroupPopup" />
																					<x:updateActionListener
																						property="#{ProjekteForm.myFilegroup}"
																						value="#{item}" />
																					<x:updateActionListener
																						property="#{ProjekteForm.myFilegroup}"
																						value="#{item}" />
																					<h:graphicImage id="id86"
																						value="images/buttons/edit_20.gif" />
																				</jp:popupFrame>
																			</h:panelGroup>

																			<%-- Löschen-Schaltknopf --%>
																			<h:commandLink
																				action="#{ProjekteForm.filegroupDelete}"
																				title="#{msgs.filegroupLoeschen}"
																				onclick="if (!confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')) return">
																				<h:graphicImage
																					value="images/buttons/waste1a_20px.gif" />
																				<x:updateActionListener
																					property="#{ProjekteForm.myFilegroup}"
																					value="#{item}" />
																			</h:commandLink>
																		</h:column>
																	</x:dataTable>
																	<%-- Hinzufügen-Schaltknopf --%>
																	<h:panelGroup id="id87">
																		<jp:popupFrame scrolling="auto" height="270px"
																		
																			width="700px" topStyle="background: #1874CD;"
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
																			<h:outputText
																				style="border-bottom: #a24033 dashed 1px;"
																				value="#{msgs.filegroupHinzufuegen}" />
																		</jp:popupFrame>
																	</h:panelGroup>
																</h:panelGroup>
															</h:panelGrid>
														</htm:div>
													</x:panelTab>
													<%--END label="Mets Parameter" --%>

													<x:panelTab label="#{msgs.statistik}"
														rendered="#{ProjekteForm.myProjekt.id != null && !HelperForm.anonymized}">
														<%--
														<htm:li
															styleClass="#{ProjekteForm.currentTab =='tab4'?'selected':''}">
															<h:commandLink id="id88" action="#{ProjekteForm.Apply}">
																<h:outputText id="id89" value="<em>" escape="false" />
																<h:outputText id="id90" value="Statistik" />
																<h:outputText id="id91" value="</em>" escape="false" />
																<x:updateActionListener value="tab4"
																	property="#{ProjekteForm.currentTab}" />
															</h:commandLink>
														</htm:li>
														 --%>
														<htm:div id="tab4">

															<h:panelGroup id="qvzid2">

																<f:facet name="header">
																	<h:outputText id="qvzid3" value="#{msgs.vorgaben}"
																		style="font-weight:normal" />
																</f:facet>

																<h:panelGrid id="projgrid6" columns="3" rowClasses="top"
																	styleClass="tableStatistik">

																	<h:panelGrid id="qqvzid4" columns="2" rowClasses="top"
																		styleClass="leftTableStatistik">

																		<h:outputText id="qvwid8"
																			value="#{msgs.numberImages}:" />
																		<h:outputText id="id92"
																			style="width: 200px;margin-right:15px"
																			value="#{ProjekteForm.myProjekt.numberOfPages}">

																		</h:outputText>

																		<%-- number of volumes --%>
																		<h:outputText id="qvwid11"
																			value="#{msgs.numberVolumes}:" />
																		<h:outputText id="id93"
																			style="width: 200px;margin-right:15px"
																			value="#{ProjekteForm.myProjekt.numberOfVolumes}">
																		</h:outputText>

																		<%-- startdate --%>
																		<h:outputText id="qvwid14" value="#{msgs.startdate}:" />
																		<h:outputText id="id94"
																			style="width: 200px;margin-right:15px"
																			value="#{ProjekteForm.myProjekt.startDate}" />

																		<%-- enddate --%>
																		<h:outputText id="qvwid17" value="#{msgs.enddate}:" />
																		<h:outputText id="id95"
																			style="width: 200px;margin-right:15px"
																			value="#{ProjekteForm.myProjekt.endDate}" />

																		<%-- pages per volume --%>
																		<h:outputText id="qvqwid22"
																			value="#{msgs.imagesPerVolume}:" />
																		<h:outputText id="qvwid23"
																			value="#{ProjekteForm.calcImagesPerVolume}" />

																		<%-- duration --%>
																		<h:outputText id="qvwid24"
																			value="#{msgs.durationInMonth}:" />
																		<h:outputText id="qvwid25"
																			value="#{ProjekteForm.calcDuration}" />
																	</h:panelGrid>
																	<h:panelGrid id="qwevzid4" columns="2" rowClasses="top">



																		<%-- throughputPerYear --%>
																		<h:outputText id="qvwid26"
																			value="#{msgs.volumesPerYear}:"
																			rendered="#{ProjekteForm.calcDuration > 11}" />
																		<h:outputText id="qvwid27"
																			value="#{ProjekteForm.calcThroughputPerYear}"
																			rendered="#{ProjekteForm.calcDuration > 11}" />

																		<%-- throughputPerQuarter --%>
																		<h:outputText id="vqwid28"
																			value="#{msgs.volumesPerQuarter}:"
																			rendered="#{ProjekteForm.calcDuration > 2}" />
																		<h:outputText id="qvwid29"
																			value="#{ProjekteForm.calcThroughputPerQuarter}"
																			rendered="#{ProjekteForm.calcDuration > 2}" />

																		<%-- throughputPerMonth --%>
																		<h:outputText id="qvwid30"
																			value="#{msgs.volumesPerMonth}:"
																			rendered="#{ProjekteForm.calcDuration > 0}" />
																		<h:outputText id="qvwid31"
																			value="#{ProjekteForm.calcThroughputPerMonth}"
																			rendered="#{ProjekteForm.calcDuration > 0}" />

																		<%-- throughputPerDay --%>
																		<h:outputText id="qvwid32"
																			value="#{msgs.volumesPerDay}:" />
																		<h:outputText id="qvwid33"
																			value="#{ProjekteForm.calcThroughputPerDay}" />
																	</h:panelGrid>
																	<h:panelGrid id="qwevzidscfhbf4" columns="2"
																		rowClasses="top">
																		<%-- throughputPerYear --%>
																		<h:outputText id="qqvwid26"
																			value="#{msgs.pagesPerYear}:"
																			rendered="#{ProjekteForm.calcDuration > 11}" />
																		<h:outputText id="qqvwid27"
																			value="#{ProjekteForm.calcThroughputPagesPerYear}"
																			rendered="#{ProjekteForm.calcDuration > 11}" />

																		<%-- throughputPerQuarter --%>
																		<h:outputText id="qvqwid28"
																			value="#{msgs.pagesPerQuarter}:"
																			rendered="#{ProjekteForm.calcDuration > 2}" />
																		<h:outputText id="qqvwid29"
																			value="#{ProjekteForm.calcTroughputPagesPerQuarter}"
																			rendered="#{ProjekteForm.calcDuration > 2}" />

																		<%-- throughputPerMonth --%>
																		<h:outputText id="qqvwid30"
																			value="#{msgs.pagesPerMonth}:"
																			rendered="#{ProjekteForm.calcDuration > 0}" />
																		<h:outputText id="qqvwid31"
																			value="#{ProjekteForm.calcThroughputPagesPerMonth}"
																			rendered="#{ProjekteForm.calcDuration > 0}" />

																		<%-- throughputPerDay --%>
																		<h:outputText id="qqvwid32"
																			value="#{msgs.pagesPerDay}:" />
																		<h:outputText id="qqvwid33"
																			value="#{ProjekteForm.calcPagesPerDay}" />
																	</h:panelGrid>
																</h:panelGrid>
															</h:panelGroup>

															<%--<htm:div id="staticticdemo" styleClass="yui-navset">--%>
															<x:panelTabbedPane serverSideTabSwitch="true"
																immediateTabChange="false" styleClass="subTabbedPane"
																activeTabStyleClass="activeTab"
																inactiveTabStyleClass="inactiveTab"
																disabledTabStyleClass="disabledTab"
																activeSubStyleClass="activeSub"
																inactiveSubStyleClass="inactiveSub"
																tabContentStyleClass="tabContent">

																<%-- <h:outputText id="id96" value="#{ProjekteForm.currentStatisticTab}" />--%>

																<x:panelTab label="#{msgs.projektstand}">
																	<%-- <htm:li
																			styleClass="#{ProjekteForm.currentStatisticTab =='stab1'?'selected':''}">
																			<h:commandLink id="id97" action="#{ProjekteForm.Apply}">
																				<h:outputText id="id98" value="<em>" escape="false" />
																				<h:outputText id="id99" value="#{msgs.projektstand}" />
																				<h:outputText id="id100" value="</em>" escape="false" />
																				<x:updateActionListener value="stab1"
																					property="#{ProjekteForm.currentStatisticTab}" />
																			</h:commandLink>
																		</htm:li>
															
															--%>
																	<htm:div id="stab1" styleClass="subTab1">

																		<htm:p style="width: 50px;">
																			<h:outputText id="vzid34" value="#{msgs.volumes}"
																				style="font-weight:normal;" />
																		</htm:p>

																		<h:graphicImage id="vzid36" alt="volumes"
																			title="#{ProjekteForm.myProjekt.titel}"
																			value="#{HelperForm.servletPathWithHostAsUrl}/pages/imagesTemp/#{ProjekteForm.projectStatVolumes}" />

																		<htm:p style="width: 50px;">
																			<h:outputText id="vzid39" value="#{msgs.seiten}"
																				style="font-weight:normal" />
																		</htm:p>

																		<h:graphicImage id="vzid41" alt="images"
																			title="#{ProjekteForm.myProjekt.titel}"
																			value="#{HelperForm.servletPathWithHostAsUrl}/pages/imagesTemp/#{ProjekteForm.projectStatImages}" />

																	</htm:div>
																</x:panelTab>

																<%-- <htm:div styleClass="yui-skin-sam"> --%>


																<%-- <ul class="yui-nav"> --%>
																<x:panelTab label="#{msgs.projectProgress}">
																	<%-- 		<htm:li
																			styleClass="#{ProjekteForm.currentStatisticTab =='stab2'?'selected':''}">
																			<h:commandLink id="id101" action="#{ProjekteForm.Apply}">
																				<h:outputText id="id102" value="<em>" escape="false" />
																				<h:outputText id="id103" value="#{msgs.projectProgress}" />
																				<h:outputText id="id104" value="</em>" escape="false" />
																				<x:updateActionListener value="stab2"
																					property="#{ProjekteForm.currentStatisticTab}" />
																			</h:commandLink>
																		</htm:li>
																	--%>
																	<htm:div id="stab2" styleClass="subTab2">
																		<h:outputText id="vzid34_1"
																			value="#{msgs.projectProgress}"
																			style="font-weight:normal" />

																		<h:panelGrid id="pcid3" columns="4"
																			columnClasses="standardTable_ColumnLeft,standardTable_Column,standardTable_ColumnRight"
																			rowClasses="rowTop">
																			<%-- SelectManyMenu Workflow --%>

																			<h:selectManyListbox id="vinput5"
																				value="#{ProjekteForm.projectProgressInterface.selectedSteps}">
																				<si:selectItems
																					value="#{ProjekteForm.projectProgressInterface.selectableSteps}"
																					var="item" itemLabel="#{item}" itemValue="#{item}" />
																			</h:selectManyListbox>

																			<h:selectOneMenu id="pcid10" style="height:20px"
																				value="#{ProjekteForm.projectProgressInterface.timeUnit}"
																				converter="StatisticsTimeUnitConverter">
																				<si:selectItems id="pcid11"
																					value="#{ProjekteForm.projectProgressInterface.selectableTimeUnits}"
																					var="unit" itemLabel="#{unit.title}"
																					itemValue="#{unit}" />
																			</h:selectOneMenu>

																			<h:panelGroup id="pcid12" style="height:20px">
																				<h:selectBooleanCheckbox id="pcid13"
																					value="#{ProjekteForm.projectProgressInterface.referenceCurve}"
																					title="#{msgs.refCurve}">
																				</h:selectBooleanCheckbox>
																				<h:outputLabel id="pcid14" value="#{msgs.refCurve}"
																					for="pcid13" />
																			</h:panelGroup>

																			<h:commandButton id="pcid20" value="#{msgs.rerender}">
																				<a4j:support id="vwid13" event="onclick"
																					reRender="vzid36" />
																			</h:commandButton>

																		</h:panelGrid>

																		<h:panelGroup id="pcid16">
																			<x:graphicImage forceId="true" id="vzid36"
																				rendered="#{ProjekteForm.projectProgressImage != ''}"
																				value="#{HelperForm.servletPathWithHostAsUrl}/pages/imagesTemp/#{ProjekteForm.projectProgressImage}" />

																			<%--  		<h:commandLink action="#{Form.CreateExcel}" title="#{msgs.createExcel}">
				<h:graphicImage value="/newpages/images/buttons/excel20.png" />
				<h:outputText value="  #{msgs.createExcel}" />
				<x:updateActionListener value="#{ProjekteForm.projectProgressInterface}" property="#{ProjekteForm.myCurrentTable}" />
			</h:commandLink>--%>

																		</h:panelGroup>

																	</htm:div>
																</x:panelTab>
																<x:panelTab label="#{msgs.productionStatistics}">
																	<%-- 
																		<htm:li
																			styleClass="#{ProjekteForm.currentStatisticTab =='stab3'?'selected':''}">
																			<h:commandLink id="id105" action="#{ProjekteForm.Apply}">
																				<h:outputText id="id106" value="<em>" escape="false" />
																				<h:outputText id="id107" value="#{msgs.productionStatistics}" />
																				<h:outputText id="id108" value="</em>" escape="false" />
																				<x:updateActionListener value="stab3"
																					property="#{ProjekteForm.currentStatisticTab}" />
																			</h:commandLink>
																		</htm:li>
																		--%>
																	<htm:div id="stab3" styleClass="subTab3">
																		<x:aliasBeansScope>
																			<x:aliasBean alias="#{Form}" value="#{ProjekteForm}" />
																			<f:subview id="vzid420101">
																				<jsp:include
																					page="/newpages/inc_statistic/StatisticProduction.jsp" />
																			</f:subview>
																		</x:aliasBeansScope>
																	</htm:div>
																</x:panelTab>
																<x:panelTab label="#{msgs.productionThroughput}">
																	<%--
																		<htm:li
																			styleClass="#{ProjekteForm.currentStatisticTab =='stab4'?'selected':''}">
																			<h:commandLink id="id109" action="#{ProjekteForm.Apply}">
																				<h:outputText id="id110" value="<em>" escape="false" />
																				<h:outputText id="id111" value="#{msgs.productionThroughput}" />
																				<h:outputText id="id112" value="</em>" escape="false" />
																				<x:updateActionListener value="stab4"
																					property="#{ProjekteForm.currentStatisticTab}" />
																			</h:commandLink>
																		</htm:li>
																	--%>
																	<htm:div id="stab4" styleClass="subTab4">
																		<x:aliasBeansScope>
																			<x:aliasBean alias="#{Form}" value="#{ProjekteForm}" />
																			<f:subview id="vzid430101">
																				<jsp:include
																					page="/newpages/inc_statistic/StatisticThroughput.jsp" />
																			</f:subview>
																		</x:aliasBeansScope>
																	</htm:div>
																</x:panelTab>
																<x:panelTab label="#{msgs.errorTracking}">
																	<%--
																		<htm:li
																			styleClass="#{ProjekteForm.currentStatisticTab =='stab5'?'selected':''}">
																			<h:commandLink id="id113" action="#{ProjekteForm.Apply}">
																				<h:outputText id="id114" value="<em>" escape="false" />
																				<h:outputText id="id115" value="#{msgs.errorTracking}" />
																				<h:outputText id="id116" value="</em>" escape="false" />
																				<x:updateActionListener value="stab5"
																					property="#{ProjekteForm.currentStatisticTab}" />
																			</h:commandLink>
																		</htm:li>
																 --%>
																	<htm:div id="stab5" styleClass="subTab5">
																		<x:aliasBeansScope>
																			<x:aliasBean alias="#{Form}" value="#{ProjekteForm}" />
																			<f:subview id="vzid440101">
																				<jsp:include
																					page="/newpages/inc_statistic/StatisticCorrection.jsp" />
																			</f:subview>
																		</x:aliasBeansScope>
																	</htm:div>
																</x:panelTab>
																<x:panelTab label="#{msgs.storageCalculator}">
																	<%--
																		<htm:li
																			styleClass="#{ProjekteForm.currentStatisticTab =='stab6'?'selected':''}">
																			<h:commandLink id="id117" action="#{ProjekteForm.Apply}">
																				<h:outputText id="id118" value="<em>" escape="false" />
																				<h:outputText id="id119" value="#{msgs.storageCalculator}" />
																				<h:outputText id="id120" value="</em>" escape="false" />
																				<x:updateActionListener value="stab6"
																					property="#{ProjekteForm.currentStatisticTab}" />
																			</h:commandLink>
																		</htm:li>
																	 --%>
																	<htm:div id="stab6" styleClass="subTab6">
																		<x:aliasBeansScope>
																			<x:aliasBean alias="#{Form}" value="#{ProjekteForm}" />
																			<f:subview id="vzid450101">
																				<jsp:include
																					page="/newpages/inc_statistic/StatisticStorage.jsp" />
																			</f:subview>
																		</x:aliasBeansScope>
																	</htm:div>
																</x:panelTab>

																<%-- </ul> --%>

																<%--<htm:div styleClass="yui-content">
																		
																		
																		
																		
																		
																		
																	</htm:div>
																	--%>

																<%-- </htm:div>  END: <htm:div styleClass="yui-skin-sam">--%>
															</x:panelTabbedPane>
															<%--</htm:div>--%>

														</htm:div>


														<%-- </ul> --%>
														<%--<htm:div styleClass="yui-content">
														
														
														
														
													</htm:div>--%>
														<%--</htm:div>--%>

													</x:panelTab>
													<%-- END label="Statistik" --%>
												</x:panelTabbedPane>
												<%-- END Gro�?es pane Tab --%>

												<%--  <htm:div styleClass="yui-skin-sam"> --%>


											</htm:td>
										</htm:tr>

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row3" align="left">
												<h:commandButton id="id121" value="#{msgs.abbrechen}"
													action="ProjekteAlle" immediate="true" />
											</htm:td>
											<htm:td styleClass="eingabeBoxen_row3" align="right">
												<h:commandButton id="id122" value="#{msgs.loeschen}"
													action="#{ProjekteForm.Loeschen}"
													onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')"
													rendered="#{ProjekteForm.myProjekt.id != null && ProjekteForm.myProjekt.deleteAble}" />
												<h:commandButton id="id123" value="#{msgs.uebernehmen}"
													action="#{ProjekteForm.Apply}" />

												<h:commandButton id="id124" value="#{msgs.speichern}"
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
    var tabView1 = new YAHOO.widget.TabView('projectform:demo'); 
    var tabView2 = new YAHOO.widget.TabView('projectform:staticticdemo');
	</script>

	</body>
</f:view>


</html>
