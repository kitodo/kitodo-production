<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
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

							Alle Regelsaetze in der Übersicht

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
<h:form id="rulesetform">
				<%-- Breadcrumb --%>
				<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
					<h:panelGroup>
						<h:commandLink value="#{msgs.startseite}" action="newMain" />
						<f:verbatim> &#8250;&#8250; </f:verbatim>
						<h:outputText value="#{msgs.regelsaetze}" />
					</h:panelGroup>
				</h:panelGrid>

				<htm:table border="0" align="center" width="100%" cellpadding="15">
					<htm:tr>
						<htm:td>

							<%-- Überschrift --%>
							<htm:h3>
								<h:outputText value="#{msgs.regelsaetze}" />
							</htm:h3>

							<%-- Neu-Schaltknopf --%>
							<h:commandLink action="#{RegelsaetzeForm.Neu}"
								immediate="true" rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
								<h:outputText value="#{msgs.neuenRegelsatzAnlegen}" />
							</h:commandLink>

							<%-- globale Warn- und Fehlermeldungen --%>
							<h:messages globalOnly="true" errorClass="text_red"
								infoClass="text_blue" showDetail="true" showSummary="true"
								tooltip="true" />

							<%-- Datentabelle --%>
							<x:dataTable styleClass="standardTable" width="100%"
								cellspacing="1px" cellpadding="1px"
								headerClass="standardTable_Header"
								rowClasses="standardTable_Row1,standardTable_Row2"
								columnClasses="standardTable_Column,standardTable_Column,standardTable_ColumnCentered"
								var="item" value="#{RegelsaetzeForm.page.listReload}">

								<h:column>
									<f:facet name="header">
										<h:outputText value="#{msgs.titel}" />
									</f:facet>
									<h:outputText value="#{item.titel}" />
								</h:column>
								
								<h:column>
									<f:facet name="header">
										<h:outputText value="#{msgs.datei}" />
									</f:facet>
									<h:outputText value="#{item.datei}" />
								</h:column>

								<x:column style="text-align:center">
								        <f:facet name="header">
								            <h:outputText value="#{msgs.metadatenSortierungNachRegelsatz}" />
								        </f:facet>
								        <h:graphicImage value="/newpages/images/check_true.gif"
								            rendered="#{item.orderMetadataByRuleset}" />
								        <h:graphicImage value="/newpages/images/check_false.gif"
								            rendered="#{!item.orderMetadataByRuleset}" />
								    </x:column>

								<h:column rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
									<f:facet name="header">
										<h:outputText value="#{msgs.auswahl}" />
									</f:facet>
									<%-- Bearbeiten-Schaltknopf --%>
									<h:commandLink action="RegelsaetzeBearbeiten"
										title="#{msgs.regelsatzBearbeiten}">
										<h:graphicImage value="/newpages/images/buttons/edit.gif" />
										<x:updateActionListener
											property="#{RegelsaetzeForm.myRegelsatz}" value="#{item}" />
									</h:commandLink>
								</h:column>

							</x:dataTable>

							<htm:table width="100%" border="0">
								<htm:tr valign="top">
									<htm:td align="left">

									</htm:td>
									<htm:td align="center">
										<%-- ===================== Datascroller für die Ergebnisse ====================== --%>
												<x:aliasBean alias="#{mypage}" value="#{RegelsaetzeForm.page}">
												<jsp:include page="/newpages/inc/datascroller.jsp" />
											</x:aliasBean>
										<%-- ===================== // Datascroller für die Ergebnisse ====================== --%>
									</htm:td>
								</htm:tr>
							</htm:table>
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
