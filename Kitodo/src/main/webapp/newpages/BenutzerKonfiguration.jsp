<%--
 *
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 *
--%>

<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<%-- ######################################## 

							individuelle Benutzereinstellungen

	#########################################--%>

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
					<h:form id="userconfigform" onkeypress="ifEnterClick(event, 'userconfigform:absenden');">
						<%-- Breadcrumb --%>
						<h:panelGrid id="id0" width="100%" columns="1"
							styleClass="layoutInhaltKopf">
							<h:panelGroup id="id1">
								<h:commandLink id="id2" value="#{msgs.startseite}"
									action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText id="id3" value="#{msgs.benutzerkonfiguration}" />
							</h:panelGroup>
						</h:panelGrid>

						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>
									<htm:h3>
										<h:outputText id="id4" value="#{msgs.benutzerkonfiguration}" />
									</htm:h3>

									<%-- globale Warn- und Fehlermeldungen --%>
									<h:messages id="id5" globalOnly="true" errorClass="text_red"
										infoClass="text_blue" showDetail="true" showSummary="true"
										tooltip="true" />

									<%-- Box für die Bearbeitung der Details --%>
									<htm:table cellpadding="3" cellspacing="0" width="100%"
										styleClass="eingabeBoxen">

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row1" colspan="2">
												<h:outputText id="id6" value="#{msgs.details}" />
											</htm:td>
										</htm:tr>

										<%-- Formular für die Bearbeitung der Benutzereinstellungen --%>
										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row2" colspan="2">

												<h:panelGrid id="id7" columns="2">

													<%-- Felder --%>
													<h:outputLabel id="id8" for="sessiontimeout"
														value="#{msgs.timeoutForSession}" />
													<h:panelGroup id="id9">
														<h:inputText id="sessiontimeout"
															style="width: 300px;margin-right:15px"
															value="#{LoginForm.myBenutzer.sessionTimeoutInMinutes}"
															required="true" />
														<x:message id="id10" for="sessiontimeout"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<%-- Felder --%>
													<h:outputLabel id="id11" for="tabellengroesse"
														value="#{msgs.tabellengroesse}" />
													<h:panelGroup id="id12">
														<h:inputText id="tabellengroesse"
															style="width: 300px;margin-right:15px"
															value="#{LoginForm.myBenutzer.tableSize}"
															required="true">
															<f:validateLongRange minimum="1" maximum="100" />
														</h:inputText>
														<x:message id="id13" for="tabellengroesse"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<%-- MetadatenSprache --%>
													<h:outputLabel id="id14" for="metadatenSprache"
														value="#{msgs.spracheFuerMetadaten}" />
													<h:panelGroup id="id15">
														<h:inputText id="metadatenSprache"
															style="width: 300px;margin-right:15px"
															value="#{LoginForm.myBenutzer.metadataLanguage}"
															required="true" />
														<x:message id="id16" for="metadatenSprache"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<%-- CSS für den Nutzer --%>
													<h:outputLabel id="id17" for="css"
														value="#{msgs.farbschema}" />
													<h:panelGroup id="id18">

														<h:selectOneMenu id="css"
															value="#{LoginForm.myBenutzer.css}"
															style="width: 300px;margin-right:15px">
															<f:selectItems id="id19" value="#{HelperForm.cssFiles}" />
														</h:selectOneMenu>

														<x:message id="id20" for="css" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>

													<%-- Vorgangsdatum in den Eigenen Aufgaben anzeigen --%>
													<h:outputLabel id="id21" for="confVorgangsdatumAnzeigen"
														value="#{msgs.spalteVorgangsdatumInEigenenAufgabenAnzeigen}" />
													<h:selectBooleanCheckbox id="confVorgangsdatumAnzeigen"
														value="#{LoginForm.myBenutzer.configProductionDateShow}" />

												</h:panelGrid>

											</htm:td>
										</htm:tr>

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row3" align="left">
												<h:commandButton id="id22" value="#{msgs.abbrechen}"
													action="newMain" immediate="true" />
											</htm:td>
											<htm:td styleClass="eingabeBoxen_row3" align="right">
												<h:commandButton id="absenden" value="#{msgs.speichern}"
													action="#{LoginForm.BenutzerkonfigurationSpeichern}" />
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
