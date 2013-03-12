<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
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
															value="#{LoginForm.myBenutzer.sessiontimeoutInMinutes}"
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
															value="#{LoginForm.myBenutzer.tabellengroesse}"
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
															value="#{LoginForm.myBenutzer.metadatenSprache}"
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
														value="#{LoginForm.myBenutzer.confVorgangsdatumAnzeigen}" />

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
