<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp"%>
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

							Benutzer bearbeiten

	#########################################--%>
<a4j:keepAlive beanName="BenutzerverwaltungForm" />
<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body>
		<htm:table styleClass="headTable" cellspacing="0" cellpadding="0" style="padding-left:5px;padding-right:5px;margin-top:5px;">
			<%@include file="inc/tbl_Kopf.jsp"%>
		</htm:table>
		<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable" align="center">

			<htm:tr>
				<%@include file="inc/tbl_Navigation.jsp"%>
				<htm:td valign="top" styleClass="layoutInhalt">

					<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
					<h:form id="usereditform" onkeypress="ifEnterClick(event, 'usereditform:absenden');">
						<%-- Breadcrumb --%>
						<h:panelGrid id="id0" width="100%" columns="1" styleClass="layoutInhaltKopf">
							<h:panelGroup id="id1">
								<h:commandLink id="id2" value="#{msgs.startseite}" action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:commandLink id="id3" value="#{msgs.benutzerverwaltung}" action="BenutzerAlle" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText id="id4" value="#{msgs.neuenBenutzerAnlegen}" rendered="#{BenutzerverwaltungForm.myClass.id == null}" />
								<h:outputText id="id5" value="#{msgs.benutzerBearbeiten}" rendered="#{BenutzerverwaltungForm.myClass.id != null}" />
							</h:panelGroup>
						</h:panelGrid>

						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>
									<htm:h3>
										<h:outputText id="id6" value="#{msgs.neuenBenutzerAnlegen}" rendered="#{BenutzerverwaltungForm.myClass.id == null}" />
										<h:outputText id="id7" value="#{msgs.benutzerBearbeiten}" rendered="#{BenutzerverwaltungForm.myClass.id != null}" />
									</htm:h3>

									<%-- globale Warn- und Fehlermeldungen --%>
									<h:messages id="id8" globalOnly="true" errorClass="text_red" infoClass="text_blue" showDetail="true" showSummary="true" tooltip="true" />

									<%-- Box für die Bearbeitung der Details --%>
									<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen">

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row1" align="left">
												<h:outputText id="id9" value="#{msgs.details}" />
											</htm:td>
											<htm:td styleClass="eingabeBoxen_row1" align="right">
												<h:commandLink id="id10" action="#{NavigationForm.Reload}">
													<h:graphicImage id="id11" value="/newpages/images/reload.gif" />
												</h:commandLink>
											</htm:td>
										</htm:tr>

										<%-- Formular für die Bearbeitung der Texte --%>
										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row2" colspan="2">

												<h:panelGrid id="id12" columns="2" rowClasses="top">

													<%-- nachname --%>
													<h:outputLabel id="id13" for="nachname" value="#{msgs.nachname}" />
													<h:panelGroup id="id14">
														<h:inputText id="nachname" style="width: 300px;margin-right:15px" value="#{BenutzerverwaltungForm.myClass.nachname}" required="true" />
														<x:message id="id15" for="nachname" style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<%-- vorname --%>
													<h:outputLabel id="id16" for="vorname" value="#{msgs.vorname}" />
													<h:panelGroup id="id17">
														<h:inputText id="vorname" style="width: 300px;margin-right:15px" value="#{BenutzerverwaltungForm.myClass.vorname}" required="true" />
														<x:message id="id18" for="vorname" style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<%-- standort --%>
													<h:outputLabel id="id19" for="standort" value="#{msgs.standort}" />
													<h:panelGroup id="id20">
														<h:inputText id="standort" style="width: 300px;margin-right:15px" value="#{BenutzerverwaltungForm.myClass.standort}" required="true" />
														<x:message id="id21" for="standort" style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<%-- login --%>
													<h:outputLabel id="id22" for="login" value="#{msgs.login}" />
													<h:panelGroup id="id23">
														<h:inputText id="login" readonly="#{BenutzerverwaltungForm.myClass.id != null}" style="width: 300px;margin-right:15px"
															value="#{BenutzerverwaltungForm.myClass.login}" required="true" />
														<x:message id="id24" for="login" style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<%-- login --%>
													<h:outputLabel id="id22a" for="ldaplogin" value="#{msgs.ldaplogin}"  rendered="#{BenutzerverwaltungForm.ldapUsage}"/>
													<h:panelGroup id="id23a" rendered="#{BenutzerverwaltungForm.ldapUsage}">
														<h:inputText id="ldaplogin" style="width: 300px;margin-right:15px" value="#{BenutzerverwaltungForm.myClass.ldaplogin}" />
														<x:message id="id24a" for="ldaplogin" style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<%-- passwort --%>
													<h:outputLabel id="id25" for="passwort" value="#{msgs.passwort}" />
													<h:panelGroup id="id26">
														<h:inputSecret redisplay="true" id="passwort" style="width: 300px;margin-right:15px"
															value="#{BenutzerverwaltungForm.myClass.passwortCrypt}" required="true" />
														<x:message id="id27" for="passwort" style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<%-- LdapGruppe --%>
													<h:outputLabel id="id28" for="ldapGruppe" value="#{msgs.ldapgruppe}" rendered="#{BenutzerverwaltungForm.ldapUsage}" />
													<h:panelGroup id="id29" rendered="#{BenutzerverwaltungForm.ldapUsage}">
														<h:selectOneMenu id="ldapGruppe" style="width: 300px;margin-right:15px" value="#{BenutzerverwaltungForm.ldapGruppeAuswahl}"
															required="true">
															<f:selectItem id="id30" itemValue="" itemLabel="#{msgs.bitteAuswaehlen}" />
															<f:selectItems value="#{BenutzerverwaltungForm.ldapGruppeAuswahlListe}" />
														</h:selectOneMenu>
														<x:message id="id31" for="ldapGruppe" style="color: red" detailFormat="#{msgs.keineLdapgruppeAngegeben}" />
														<%-- ldap configuration schreiben --%>
														<h:commandLink id="id32" title="#{msgs.ldapKonfigurationSchreiben}" action="#{BenutzerverwaltungForm.LdapKonfigurationSchreiben}"
															rendered="#{BenutzerverwaltungForm.myClass.id != null}">
															<h:graphicImage id="id33" value="/newpages/images/buttons/key3.gif" />
															<x:updateActionListener property="#{BenutzerverwaltungForm.myClass}" value="#{BenutzerverwaltungForm.myClass}" />
														</h:commandLink>
													</h:panelGroup>

													<%-- MetadatenSprache --%>
													<h:outputLabel id="id34" for="metadatenSprache" value="#{msgs.spracheFuerMetadaten}" />
													<h:panelGroup id="id35">
														<h:inputText id="metadatenSprache" style="width: 300px;margin-right:15px" value="#{BenutzerverwaltungForm.myClass.metadatenSprache}"
															required="true" />
														<x:message id="id36" for="metadatenSprache" style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<%-- ist aktiv --%>
													<h:outputLabel id="id37" for="istAktiv" value="#{msgs.istAktiv}" />
													<h:panelGroup id="id38">
														<h:selectBooleanCheckbox id="istAktiv" style="margin-right:15px" value="#{BenutzerverwaltungForm.myClass.istAktiv}" />
														<x:message id="id39" for="istAktiv" style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<%-- mit Massendownload --%>
													<h:outputLabel id="id40" for="mitMassendownload" value="#{msgs.massendownload}" />
													<h:panelGroup id="id41">
														<h:selectBooleanCheckbox id="mitMassendownload" style="margin-right:15px" value="#{BenutzerverwaltungForm.myClass.mitMassendownload}" />
														<x:message id="id42" for="mitMassendownload" style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:panelGroup id="id43">
														<jp:popupFrame scrolling="auto" height="380px" width="430px" topStyle="background: #1874CD;" bottomStyleClass="popup_unten"
															styleFrame="border-style: solid;border-color: #1874CD; border-width: 2px;" styleClass="standardlink"
															style="margin-top:2px;display:block; text-decoration:none" actionOpen="#{BenutzergruppenForm.FilterKeinMitZurueck}"
															actionClose="#{NavigationForm.Reload}" center="true" title="#{msgs.benutzergruppen}" immediate="true">
															<x:updateActionListener property="#{BenutzergruppenForm.zurueck}" value="BenutzerBearbeitenPopup" />
															<h:outputText id="id44" style="border-bottom: #a24033 dashed 1px;" value="#{msgs.benutzergruppen}" />
														</jp:popupFrame>
													</h:panelGroup>

													<x:dataTable id="id45" width="300" columnClasses="standardTable_Column,standardTable_Column,standardTable_ColumnCentered" var="item"
														value="#{BenutzerverwaltungForm.myClass.benutzergruppenList}">
														<h:column id="id46">
															<h:outputText id="id47" value="#{item.titel}" />
														</h:column>
														<h:column id="id48">
															<%-- Löschen-Schaltknopf --%>
															<h:commandLink action="#{BenutzerverwaltungForm.AusGruppeLoeschen}" title="#{msgs.ausGruppeLoeschen}">
																<h:graphicImage id="id49" value="images/buttons/waste1a_20px.gif" />
																<f:param id="id50" name="ID" value="#{item.id}" />
															</h:commandLink>
														</h:column>
													</x:dataTable>

													<h:panelGroup id="id51">
														<jp:popupFrame scrolling="auto" height="380px" width="430px" topStyle="background: #1874CD;" bottomStyleClass="popup_unten"
															styleFrame="border-style: solid;border-color: #1874CD; border-width: 2px;" styleClass="standardlink"
															style="margin-top:2px;display:block; text-decoration:none" actionOpen="#{ProjekteForm.FilterKeinMitZurueck}"
															actionClose="#{NavigationForm.Reload}" center="true" title="#{msgs.projekte}" immediate="true">
															<x:updateActionListener property="#{ProjekteForm.zurueck}" value="BenutzerBearbeitenPopupProjekte" />
															<h:outputText style="text-decoration: none;border-bottom: #a24033 dashed 1px;" value="#{msgs.projekte}" />
														</jp:popupFrame>
													</h:panelGroup>

													<x:dataTable id="id52" width="300" columnClasses="standardTable_Column,standardTable_Column,standardTable_ColumnCentered" var="item"
														value="#{BenutzerverwaltungForm.myClass.projekteList}">
														<h:column id="id53">
															<h:outputText id="id54" value="#{item.titel}" />
														</h:column>
														<h:column id="id55">
															<%-- Löschen-Schaltknopf --%>
															<h:commandLink action="#{BenutzerverwaltungForm.AusProjektLoeschen}" title="#{msgs.ausProjektLoeschen}">
																<h:graphicImage id="id56" value="images/buttons/waste1a_20px.gif" />
																<f:param id="id57" name="ID" value="#{item.id}" />
															</h:commandLink>
														</h:column>
													</x:dataTable>

												</h:panelGrid>
											</htm:td>
										</htm:tr>

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row3" align="left">
												<h:commandButton id="id58" value="#{msgs.abbrechen}" action="BenutzerAlle" immediate="true" />
											</htm:td>

											<htm:td styleClass="eingabeBoxen_row3" align="right">
												<h:commandButton id="id59" value="#{msgs.loeschen}" action="#{BenutzerverwaltungForm.Loeschen}"
													onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')" rendered="#{BenutzerverwaltungForm.myClass.id != null}" />
												<h:commandButton id="absenden" value="#{msgs.speichern}" action="#{BenutzerverwaltungForm.Speichern}" />
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
