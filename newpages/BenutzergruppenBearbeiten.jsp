<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
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

							Alle Literatureinträge in der �?bersicht

	#########################################--%>
<a4j:keepAlive beanName="BenutzergruppenForm" />
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
					<h:form id="groupeditform" onkeypress="ifEnterClick(event, 'groupeditform:absenden');">
						<%-- Breadcrumb --%>
						<h:panelGrid id="id0" width="100%" columns="1"
							styleClass="layoutInhaltKopf">
							<h:panelGroup id="id1">
								<h:commandLink id="id2" value="#{msgs.startseite}"
									action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:commandLink id="id3" value="#{msgs.benutzergruppen}"
									action="BenutzergruppenAlle" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText id="id4" value="#{msgs.neueBenutzergruppeAnlegen}"
									rendered="#{BenutzergruppenForm.myBenutzergruppe.id == null}" />
								<h:outputText id="id5" value="#{msgs.benutzergruppeBearbeiten}"
									rendered="#{BenutzergruppenForm.myBenutzergruppe.id != null}" />
							</h:panelGroup>
						</h:panelGrid>

						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>
									<htm:h3>
										<h:outputText id="id6"
											value="#{msgs.neueBenutzergruppeAnlegen}"
											rendered="#{BenutzergruppenForm.myBenutzergruppe.id == null}" />
										<h:outputText id="id7"
											value="#{msgs.benutzergruppeBearbeiten}"
											rendered="#{BenutzergruppenForm.myBenutzergruppe.id != null}" />
									</htm:h3>

									<%-- globale Warn- und Fehlermeldungen --%>
									<h:messages id="id8" globalOnly="true" errorClass="text_red"
										infoClass="text_blue" showDetail="true" showSummary="true"
										tooltip="true" />

									<%-- Box für die Bearbeitung der Details --%>
									<htm:table cellpadding="3" cellspacing="0" width="100%"
										styleClass="eingabeBoxen">

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row1" colspan="2">
												<h:outputText id="id9" value="#{msgs.details}" />
											</htm:td>
										</htm:tr>

										<%-- Formular für die Bearbeitung der Texte --%>
										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row2" colspan="2">

												<h:panelGrid id="id10" columns="2">

													<%-- Felder --%>
													<h:outputLabel id="id11" for="titel" value="#{msgs.titel}" />
													<h:panelGroup id="id12">
														<h:inputText id="titel"
															style="width: 300px;margin-right:15px"
															value="#{BenutzergruppenForm.myBenutzergruppe.titel}"
															required="true" />
														<x:message id="id13" for="titel" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id14" for="berechtigung"
														value="#{msgs.berechtigung}" />
													<h:panelGroup id="id15">

														<x:selectOneMenu id="berechtigung"
															style="width: 300px;margin-right:15px"
															value="#{BenutzergruppenForm.myBenutzergruppe.berechtigungAsString}"
															required="true">
															<f:selectItem id="id16" itemValue="1"
																itemLabel="#{msgs.administration}" />
															<f:selectItem id="id17" itemValue="2"
																itemLabel="#{msgs.prozessverwaltung}" />
															<f:selectItem id="id19" itemValue="4"
																itemLabel="#{msgs.normaleNutzerBerechtigung}" />
														</x:selectOneMenu>

														<x:message id="id20" for="berechtigung" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>
												</h:panelGrid>

											</htm:td>
										</htm:tr>

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row3" align="left">
												<h:commandButton id="id21" value="#{msgs.abbrechen}"
													action="BenutzergruppenAlle" immediate="true" />
											</htm:td>
											<htm:td styleClass="eingabeBoxen_row3" align="right">
											
												<h:commandButton id="id22" value="#{msgs.loeschen}"
													action="#{BenutzergruppenForm.Loeschen}"
													onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')"
													rendered="#{BenutzergruppenForm.myBenutzergruppe.id != null}" />
									
												<h:commandButton id="absenden" value="#{msgs.speichern}"
													action="#{BenutzergruppenForm.Speichern}" />
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
