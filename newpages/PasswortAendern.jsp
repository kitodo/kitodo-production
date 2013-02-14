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

								Passwortänderung durch den Benutzer

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
					<h:form id="passwordform">
						<%-- Breadcrumb --%>
						<h:panelGrid id="id0" columns="1" styleClass="layoutInhaltKopf">
							<h:panelGroup id="id1">
								<h:commandLink id="id2" value="#{msgs.startseite}"
									action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText id="id3" value="#{msgs.passwortAendern}" />
							</h:panelGroup>
						</h:panelGrid>
					</h:form>
					<htm:table border="0" align="center" width="100%" cellpadding="15">
						<htm:tr>
							<htm:td>
								<htm:h3>
									<h:outputText id="id4" value="#{msgs.passwortAendern}" />
								</htm:h3>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages id="id5" globalOnly="true" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" layout="table"
									style="margin-bottom:15px;display:block" />

								<%-- ++++++++++++++++     Passwort-Parameter      ++++++++++++++++ --%>
								<h:form id="passwortform" onkeypress="ifEnterClick(event, 'absenden');">
									<htm:table cellpadding="3" cellspacing="0" width="100%"
										styleClass="eingabeBoxen">
										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row1">
												<h:outputText id="id6"
													value="#{msgs.neuesPasswortFestlegen}" />
											</htm:td>
										</htm:tr>
										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row2">

												<h:panelGrid id="grid1" columns="2">
<%--
													<h:outputText id="id7" value="#{msgs.altesPasswort}" />
													<h:panelGroup id="id8">
														<h:inputSecret id="passwortAendernAlt"
															style="width: 200px;margin-right:15px"
															value="#{LoginForm.passwortAendernAlt}"
															 />
														<h:message id="mess1" for="passwortAendernAlt"
															errorClass="text_red" infoClass="text_blue" />
													</h:panelGroup>
--%>
													
											


													<%-- neues passwort --%>
													<h:outputLabel id="id11" for="passwortAendernNeu1"
														value="#{msgs.neuesPasswort}" />
													<h:panelGroup id="id12">
														<h:inputSecret redisplay="true" id="passwortAendernNeu1"
															style="width: 200px;margin-right:15px"
															value="#{LoginForm.passwortAendernNeu1}" required="true" />
														<x:message id="id13" for="passwortAendernNeu1"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id14" for="passwortAendernNeu2"
														value="#{msgs.neuesPasswort}" />
													<h:panelGroup id="id15">
														<h:inputSecret redisplay="true" id="passwortAendernNeu2"
															style="width: 200px;margin-right:15px"
															value="#{LoginForm.passwortAendernNeu2}" required="true" />
														<x:message id="id16" for="passwortAendernNeu2"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

												</h:panelGrid>
											</htm:td>
										</htm:tr>

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row3">
												<h:commandButton
													action="#{LoginForm.PasswortAendernAbbrechen}"
													value="#{msgs.abbrechen}" immediate="true"></h:commandButton>
												<x:commandButton id="absenden" forceId="true" type="submit"
													action="#{LoginForm.PasswortAendernSpeichern}"
													value="#{msgs.passwortAendern}"></x:commandButton>
											</htm:td>
										</htm:tr>

									</htm:table>
								</h:form>
								<%-- ++++++++++++++++     // Passwort-Parameter      ++++++++++++++++ --%>


							</htm:td>
						</htm:tr>
					</htm:table>
					<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

				</htm:td>
			</htm:tr>
			<%@include file="inc/tbl_Fuss.jsp"%>
		</htm:table>

	</body>
</f:view>
</html>
