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

<a4j:keepAlive beanName="ProjekteForm"/>
<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body style="margin: 0px; padding: 0px">
	<h:form id="filegroupform">

		<h:outputText value="#{msgs.filegroup}"
			style="position:absolute;left:10;top:2;color:white;font-weight:bold;font-size:12;z-index:3" />

		<%-- ===================== // Popup-Rahmen ====================== --%>

		<htm:table style="margin-top:20px" align="center" width="90%"
			border="0">
			<htm:tr>
				<htm:td>
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

									<%-- name --%>
									<h:outputLabel for="name" value="#{msgs.name}" />
									<h:panelGroup>
										<h:inputText id="name" style="width: 300px;margin-right:15px"
											value="#{ProjekteForm.myFilegroup.name}" required="true" />
										<x:message for="name" style="color: red"
											replaceIdWithLabel="true" />
									</h:panelGroup>

									<%-- path --%>
									<h:outputLabel for="path" value="#{msgs.path}" />
									<h:panelGroup>
										<h:inputText id="path" style="width: 300px;margin-right:15px"
											value="#{ProjekteForm.myFilegroup.path}" required="true" />
										<x:message for="path" style="color: red"
											replaceIdWithLabel="true" />
									</h:panelGroup>

									<%-- mimetype --%>
									<h:outputLabel for="mimetype" value="#{msgs.mimetype}" />
									<h:panelGroup>
										<h:inputText id="mimetype"
											style="width: 300px;margin-right:15px"
											value="#{ProjekteForm.myFilegroup.mimetype}" required="true" />
										<x:message for="mimetype" style="color: red"
											replaceIdWithLabel="true" />
									</h:panelGroup>

									<%-- suffix --%>
									<h:outputLabel for="suffix" value="#{msgs.suffix}" />
									<h:panelGroup>
										<h:inputText id="suffix"
											style="width: 300px;margin-right:15px"
											value="#{ProjekteForm.myFilegroup.suffix}" required="true" />
										<x:message for="suffix" style="color: red"
											replaceIdWithLabel="true" />
									</h:panelGroup>

								</h:panelGrid>
							</htm:td>
						</htm:tr>

						<htm:tr>
							<htm:td styleClass="eingabeBoxen_row3" align="left">

								<%-- Abbrechen-Schaltknopf --%>
								<jp:closePopupFrame>
									<h:commandLink value="#{msgs.abbrechen}" immediate="true"
										action="#{NavigationForm.JeniaPopupCloseAction}"></h:commandLink>
								</jp:closePopupFrame>

							</htm:td>
							<htm:td styleClass="eingabeBoxen_row3" align="right">
								<%-- uebernehmen-Schaltknopf --%>
								<jp:closePopupFrame>
									<h:commandLink action="#{ProjekteForm.filegroupSave}"
										title="#{msgs.uebernehmen}" value="#{msgs.uebernehmen}">
									</h:commandLink>
								</jp:closePopupFrame>
							</htm:td>
						</htm:tr>
					</htm:table>
					<%-- // Box für die Bearbeitung der Details --%>


				</htm:td>
			</htm:tr>
		</htm:table>
	</h:form>
	</body>
</f:view>

</html>
