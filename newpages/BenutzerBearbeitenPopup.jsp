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

<a4j:keepAlive beanName="BenutzerverwaltungForm"/>
<a4j:keepAlive beanName="BenutzergruppenForm"/>

<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body style="margin:0px;padding:0px">
	<h:form id="userpopupform">
		<%-- ===================== Popup-Rahmen ======================
	<h:graphicImage value="/newpages/images/popup/oben.gif" width="430"
		style="position:absolute;left:0;top:0;z-index:1" />
	<h:graphicImage value="/newpages/images/popup/links.gif" height="360"
		style="position:absolute;left:0;top:19" />
	<h:graphicImage value="/newpages/images/popup/rechts.gif" height="360"
		style="position:absolute;left:426;top:19" />
	<h:graphicImage value="/newpages/images/popup/unten.gif" width="430"
		style="position:absolute;left:0;top:377" />

	<h:form id="hmmmForm1">
		<jp:closePopupFrame>
			<h:commandLink action="#{NavigationForm.JeniaPopupCloseAction}">
				<h:graphicImage value="/newpages/images/popup/close.gif"
					style="position:absolute;left:410;top:2;z-index:2" />
			</h:commandLink>
		</jp:closePopupFrame>
	</h:form> --%>

		<h:outputText value="#{msgs.benutzergruppenHinzufuegen}"
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

					<%-- Datentabelle --%>
					<x:dataTable styleClass="standardTable" width="100%"
						cellspacing="1px" cellpadding="1px"
						headerClass="standardTable_Header"
						rowClasses="standardTable_Row1,standardTable_Row2"
						columnClasses="standardTable_Column,standardTable_ColumnCentered"
						var="item" value="#{BenutzergruppenForm.page.listReload}">

						<h:column>
							<f:facet name="header">
								<h:outputText value="#{msgs.benutzergruppe}" />
							</f:facet>
							<h:outputText value="#{item.titel}" />
						</h:column>

						<h:column>
							<f:facet name="header">
								<h:outputText value="#{msgs.auswahl}" />
							</f:facet>
							<%-- Hinzufügen-Schaltknopf --%>
							<h:commandLink
								action="#{BenutzerverwaltungForm.ZuGruppeHinzufuegen}"
								title="#{msgs.uebernehmen}">
								<h:graphicImage value="/newpages/images/buttons/addUser.gif" />
								<f:param name="ID" value="#{item.id}" />
							</h:commandLink>

							<jp:closePopupFrame rendered="false">
								<h:commandLink action="#{NavigationForm.JeniaPopupCloseAction}"
									title="#{msgs.uebernehmen}">
									<h:graphicImage value="/newpages/images/buttons/addUser.gif" />
									<f:param name="ID" value="#{item.id}" />
								</h:commandLink>
							</jp:closePopupFrame>
						</h:column>

					</x:dataTable>

					<htm:table width="100%" border="0">
						<htm:tr valign="top">
							<htm:td align="left">
								<%-- ===================== Datascroller für die Ergebnisse ====================== --%>
								<x:aliasBean alias="#{mypage}"
									value="#{BenutzergruppenForm.page}">
									<jsp:include page="/newpages/inc/datascroller.jsp" />
								</x:aliasBean>
								<%-- ===================== // Datascroller für die Ergebnisse ====================== --%>
							</htm:td>
							<htm:td align="center">
								<%-- Schliessen-Schaltknopf --%>
								<jp:closePopupFrame>
								<%-- TODO: Use massage files here --%>
									<h:commandLink value="schließen"
										action="#{NavigationForm.JeniaPopupCloseAction}"></h:commandLink>
								</jp:closePopupFrame>
							</htm:td>
						</htm:tr>
					</htm:table>


				</htm:td>
			</htm:tr>
		</htm:table>
	</h:form>
	</body>
</f:view>

</html>
