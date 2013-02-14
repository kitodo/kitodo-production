<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp"%>
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

							Benutzer bearbeiten

	#########################################--%>
<a4j:keepAlive beanName="BenutzerverwaltungForm"/>
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
				<h:form id="usereditform">
					<%-- Breadcrumb --%>
					<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:commandLink value="#{msgs.startseite}" action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:commandLink value="#{msgs.benutzerverwaltung}"
								action="BenutzerAlle" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText value="#{msgs.neuenBenutzerAnlegen}"
								rendered="#{BenutzerverwaltungForm.myClass.id == null}" />
							<h:outputText value="#{msgs.benutzerBearbeiten}"
								rendered="#{BenutzerverwaltungForm.myClass.id != null}" />
						</h:panelGroup>
					</h:panelGrid>

					<htm:table border="0" align="center" width="100%" cellpadding="15">
						<htm:tr>
							<htm:td>
								<htm:h3>
									<h:outputText value="#{msgs.neuenBenutzerAnlegen}"
										rendered="#{BenutzerverwaltungForm.myClass.id == null}" />
									<h:outputText value="#{msgs.benutzerBearbeiten}"
										rendered="#{BenutzerverwaltungForm.myClass.id != null}" />
								</htm:h3>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages globalOnly="true" errorClass="text_red"
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

											<h:panelGrid columns="2" rowClasses="top">

												<%-- nachname --%>
												<h:outputLabel for="nachname" value="#{msgs.nachname}" />
												<h:panelGroup>
													<h:inputText id="nachname"
														style="width: 300px;margin-right:15px"
														value="#{BenutzerverwaltungForm.myClass.nachname}"
														required="true" />
													<x:message for="nachname" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<%-- vorname --%>
												<h:outputLabel for="vorname" value="#{msgs.vorname}" />
												<h:panelGroup>
													<h:inputText id="vorname"
														style="width: 300px;margin-right:15px"
														value="#{BenutzerverwaltungForm.myClass.vorname}"
														required="true" />
													<x:message for="vorname" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<%-- standort --%>
												<h:outputLabel for="standort" value="#{msgs.standort}" />
												<h:panelGroup>
													<h:inputText id="standort"
														style="width: 300px;margin-right:15px"
														value="#{BenutzerverwaltungForm.myClass.standort}"
														required="true" />
													<x:message for="standort" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<%-- login --%>
												<h:outputLabel for="login" value="#{msgs.login}" />
												<h:panelGroup>
													<h:inputText id="login"
														readonly="#{BenutzerverwaltungForm.myClass.id != null}"
														style="width: 300px;margin-right:15px"
														value="#{BenutzerverwaltungForm.myClass.login}"
														required="true" />
													<x:message for="login" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<%-- passwort --%>
												<h:outputLabel for="passwort" value="#{msgs.passwort}" />
												<h:panelGroup>
													<h:inputSecret redisplay="true" id="passwort"
														style="width: 300px;margin-right:15px"
														value="#{BenutzerverwaltungForm.myClass.passwortCrypt}"
														required="true" />
													<x:message for="passwort" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<%-- LdapGruppe --%>
												<h:outputLabel 
													for="ldapGruppe"
													value="#{msgs.ldapgruppe}"
													rendered="#{BenutzerverwaltungForm.ldapUsage}" />
												<h:panelGroup 
													rendered="#{BenutzerverwaltungForm.ldapUsage}">
													<h:selectOneMenu id="ldapGruppe"
														style="width: 300px;margin-right:15px"
														value="#{BenutzerverwaltungForm.ldapGruppeAuswahl}"
														required="true">
														<f:selectItem itemValue=""
															itemLabel="#{msgs.bitteAuswaehlen}" />
														<f:selectItems
															value="#{BenutzerverwaltungForm.ldapGruppeAuswahlListe}" />
													</h:selectOneMenu>
													<x:message for="ldapGruppe" style="color: red"
														detailFormat="#{msgs.keineLdapgruppeAngegeben}" />
														
													<h:commandLink title="#{msgs.ldapKonfigurationSchreiben}"
														action="#{BenutzerverwaltungForm.LdapKonfigurationSchreiben}"
														rendered="#{BenutzerverwaltungForm.myClass.id != null}" >
														<h:graphicImage value="/newpages/images/buttons/key3.gif" />
														<x:updateActionListener
															property="#{BenutzerverwaltungForm.myClass}" value="#{BenutzerverwaltungForm.myClass}" />
													</h:commandLink>
												</h:panelGroup>

												<%-- MetadatenSprache --%>
												<h:outputLabel for="metadatenSprache"
													value="#{msgs.spracheFuerMetadaten}" />
												<h:panelGroup>
													<h:inputText id="metadatenSprache"
														style="width: 300px;margin-right:15px"
														value="#{BenutzerverwaltungForm.myClass.metadatenSprache}"
														required="true" />
													<x:message for="metadatenSprache" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<%-- ist aktiv --%>
												<h:outputLabel for="istAktiv" value="#{msgs.istAktiv}" />
												<h:panelGroup>
													<h:selectBooleanCheckbox id="istAktiv"
														style="margin-right:15px"
														value="#{BenutzerverwaltungForm.myClass.istAktiv}" />
													<x:message for="istAktiv" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<%-- mit Massendownload --%>
												<h:outputLabel for="mitMassendownload"
													value="#{msgs.massendownload}" />
												<h:panelGroup>
													<h:selectBooleanCheckbox id="mitMassendownload"
														style="margin-right:15px"
														value="#{BenutzerverwaltungForm.myClass.mitMassendownload}" />
													<x:message for="mitMassendownload" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:panelGroup>
													<jp:popupFrame scrolling="auto" height="380px"
														width="430px" topStyle="background: #1874CD;"
														bottomStyleClass="popup_unten"
														styleFrame="border-style: solid;border-color: #1874CD; border-width: 2px;"
														styleClass="standardlink"
														style="margin-top:2px;display:block; text-decoration:none"
														actionOpen="#{BenutzergruppenForm.FilterKeinMitZurueck}"
														actionClose="#{NavigationForm.Reload}" center="true"
														title="#{msgs.benutzergruppen}" immediate="true">
														<x:updateActionListener
															property="#{BenutzergruppenForm.zurueck}"
															value="BenutzerBearbeitenPopup" />
														<h:outputText style="border-bottom: #a24033 dashed 1px;"
															value="#{msgs.benutzergruppen}" />
													</jp:popupFrame>													
												</h:panelGroup>

												<x:dataTable width="300"
													columnClasses="standardTable_Column,standardTable_Column,standardTable_ColumnCentered"
													var="item"
													value="#{BenutzerverwaltungForm.myClass.benutzergruppenList}">
													<h:column>
														<h:outputText value="#{item.titel}" />
													</h:column>
													<h:column>
														<%-- Löschen-Schaltknopf --%>
														<h:commandLink
															action="#{BenutzerverwaltungForm.AusGruppeLoeschen}"
															title="#{msgs.ausGruppeLoeschen}">
															<h:graphicImage value="images/buttons/waste1a_20px.gif" />
															<f:param name="ID" value="#{item.id}" />
														</h:commandLink>
													</h:column>
												</x:dataTable>

												<h:panelGroup>
													<jp:popupFrame scrolling="auto" height="380px"
														width="430px" topStyle="background: #1874CD;"
														bottomStyleClass="popup_unten"
														styleFrame="border-style: solid;border-color: #1874CD; border-width: 2px;"
														styleClass="standardlink"
														style="margin-top:2px;display:block; text-decoration:none"
														actionOpen="#{ProjekteForm.FilterKeinMitZurueck}"
														actionClose="#{NavigationForm.Reload}" center="true"
														title="#{msgs.projekte}" immediate="true">
														<x:updateActionListener property="#{ProjekteForm.zurueck}"
															value="BenutzerBearbeitenPopupProjekte" />
														<h:outputText
															style="text-decoration: none;border-bottom: #a24033 dashed 1px;"
															value="#{msgs.projekte}" />
													</jp:popupFrame>			
												</h:panelGroup>

												<x:dataTable width="300"
													columnClasses="standardTable_Column,standardTable_Column,standardTable_ColumnCentered"
													var="item"
													value="#{BenutzerverwaltungForm.myClass.projekteList}">
													<h:column>
														<h:outputText value="#{item.titel}" />
													</h:column>
													<h:column>
														<%-- Löschen-Schaltknopf --%>
														<h:commandLink
															action="#{BenutzerverwaltungForm.AusProjektLoeschen}"
															title="#{msgs.ausProjektLoeschen}">
															<h:graphicImage value="images/buttons/waste1a_20px.gif" />
															<f:param name="ID" value="#{item.id}" />
														</h:commandLink>
													</h:column>
												</x:dataTable>

											</h:panelGrid>
										</htm:td>
									</htm:tr>

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row3" align="left">
											<h:commandButton value="#{msgs.abbrechen}"
												action="BenutzerAlle" immediate="true" />
										</htm:td>
					
										<htm:td styleClass="eingabeBoxen_row3" align="right">
											<h:commandButton value="#{msgs.loeschen}"
												action="#{BenutzerverwaltungForm.Loeschen}"
												onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')"
												rendered="#{BenutzerverwaltungForm.myClass.id != null}" />
											<h:commandButton value="#{msgs.speichern}"
												action="#{BenutzerverwaltungForm.Speichern}" />
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

	</body>
</f:view>
</html>
