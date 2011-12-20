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

							Regelsaetze bearbeiten

	#########################################--%>
<a4j:keepAlive beanName="RegelsaetzeForm"/>
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
<h:form id="ruleseteditform">
				<%-- Breadcrumb --%>
				<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
					<h:panelGroup>
						<h:commandLink value="#{msgs.startseite}" action="newMain" />
						<f:verbatim> &#8250;&#8250; </f:verbatim>
						<h:commandLink value="#{msgs.regelsaetze}" action="RegelsaetzeAlle" />
						<f:verbatim> &#8250;&#8250; </f:verbatim>
						<h:outputText value="#{msgs.neuenRegelsatzAnlegen}"
							rendered="#{RegelsaetzeForm.myRegelsatz.id == null}" />
						<h:outputText value="#{msgs.regelsatzBearbeiten}"
							rendered="#{RegelsaetzeForm.myRegelsatz.id != null}" />
					</h:panelGroup>
				</h:panelGrid>

				<htm:table border="0" align="center" width="100%" cellpadding="15">
					<htm:tr>
						<htm:td>
							<htm:h3>
								<h:outputText value="#{msgs.neuenRegelsatzAnlegen}"
									rendered="#{RegelsaetzeForm.myRegelsatz.id == null}" />
								<h:outputText value="#{msgs.regelsatzBearbeiten}"
									rendered="#{RegelsaetzeForm.myRegelsatz.id != null}" />
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

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row2" colspan="2">

											<h:panelGrid columns="2" rowClasses="top">

												<%-- Titel --%>
												<h:outputLabel for="titel" value="#{msgs.titel}" />
												<h:panelGroup>
													<h:inputText id="titel"
														style="width: 300px;margin-right:15px"
														value="#{RegelsaetzeForm.myRegelsatz.titel}" required="true" />
													<x:message for="titel" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>
												
												<%-- Dateiname --%>
												<h:outputLabel for="file" value="#{msgs.datei}" />
												<h:panelGroup>
													<h:inputText id="file"
														style="width: 300px;margin-right:15px"
														value="#{RegelsaetzeForm.myRegelsatz.datei}" required="true" />
													<x:message for="file" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

                                                <%-- Sortierung nach Regelsatz alphabetisch --%>
                                                <h:outputText value="#{msgs.metadatenSortierungNachRegelsatz}" />
                                                <h:selectBooleanCheckbox style="margin-right:15px"
                                                    value="#{RegelsaetzeForm.myRegelsatz.orderMetadataByRuleset}" />

											</h:panelGrid>
										</htm:td>
									</htm:tr>

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row3" align="left">
											<h:commandButton value="#{msgs.abbrechen}"
												action="RegelsaetzeAlle" immediate="true" />
										</htm:td>
										<htm:td styleClass="eingabeBoxen_row3" align="right">
											<h:commandButton value="#{msgs.loeschen}"
												action="#{RegelsaetzeForm.Loeschen}"
												onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')"
												rendered="#{RegelsaetzeForm.myRegelsatz.id != null}" />
											<h:commandButton value="#{msgs.speichern}"
												action="#{RegelsaetzeForm.Speichern}" />
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
