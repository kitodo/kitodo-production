<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%-- ######################################## 

							Alle Benutzer in der Übersicht

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
				<h:form id="userform">
					<%-- Breadcrumb --%>
					<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:commandLink value="#{msgs.startseite}" action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText value="#{msgs.benutzerverwaltung}" />
						</h:panelGroup>
					</h:panelGrid>

					<htm:table border="0" align="center" width="100%" cellpadding="15">
						<htm:tr>
							<htm:td>

								<%-- Überschrift --%>
								<htm:h3>
									<h:outputText value="#{msgs.benutzer}" />
								</htm:h3>
								<h:commandLink action="#{BenutzerverwaltungForm.Neu}"
									immediate="true"
									rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
									<h:outputText value="#{msgs.neuenBenutzerAnlegen}" />
								</h:commandLink>
								<%-- globale Warn- und Fehlermeldungen --%>
								<htm:span style="text-align: right;">
								<h:messages globalOnly="true" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" />
								</htm:span>
								

								<%-- +++++++++++++++++  Anzeigefilter ++++++++++++++++++++++++ --%>
								<h:panelGrid width="100%"
									columnClasses="standardTable_Column,standardTable_ColumnRight"
									rowClasses="standardTable_Row_bottom" columns="2">
									<h:outputText
										value="#{msgs.treffer}: #{BenutzerverwaltungForm.page.totalResults}" />
									<h:panelGroup>
										<h:outputText value="#{msgs.nurAktiveNutzerZeigen}:" />
										<x:selectBooleanCheckbox id="check1" forceId="true"
											value="#{BenutzerverwaltungForm.hideInactiveUsers}"
											onchange="document.getElementById('FilterAlle').click()"
											style="margin-right:40px" />

										<h:outputText value="#{msgs.filter}: " />

										<h:inputText value="#{BenutzerverwaltungForm.filter}"
											onkeypress="return submitEnter('FilterAlle',event)" />
										<x:commandButton type="submit" id="FilterAlle" forceId="true"
											style="display:none"
											action="#{BenutzerverwaltungForm.FilterAlleStart}" />

										<h:commandLink
											action="#{BenutzerverwaltungForm.FilterAlleStart}"
											title="#{msgs.filterAnwenden}" style="margin-left:5px">
											<h:graphicImage value="/newpages/images/buttons/reload.gif" />
										</h:commandLink>


									</h:panelGroup>
								</h:panelGrid>

								<%-- +++++++++++++++++  // Anzeigefilter ++++++++++++++++++++++++ --%>


								<%-- Datentabelle --%>
								<x:dataTable styleClass="standardTable" width="100%"
									cellspacing="1px" cellpadding="1px"
									headerClass="standardTable_Header"
									rowClasses="standardTable_Row1,standardTable_Row2"
									columnClasses="standardTable_Column,standardTable_Column,standardTable_Column,standardTable_Column, standardTable_ColumnCentered"
									var="item" value="#{BenutzerverwaltungForm.page.listReload}">

									<h:column>
										<f:facet name="header">
											<h:outputText value="#{msgs.benutzer}" />
										</f:facet>
										<h:outputText value="#{item.nachname}, #{item.vorname}"
											styleClass="#{not item.istAktiv?'text_light':''}" />
									</h:column>

									<h:column>
										<f:facet name="header">
											<h:outputText value="#{msgs.standort}" />
										</f:facet>
										<h:outputText value="#{item.standort}"
											styleClass="#{not item.istAktiv?'text_light':''}" />
									</h:column>

									<h:column>
										<f:facet name="header">
											<h:outputText value="#{msgs.benutzergruppen}" />
										</f:facet>
										<x:dataList var="intern"
											styleClass="#{not item.istAktiv?'text_light':''}"
											rendered="#{item.benutzergruppenSize != 0}"
											value="#{item.benutzergruppenList}" layout="ordered list"
											rowCountVar="rowCount" rowIndexVar="rowIndex">
											<h:outputText value="#{intern.titel}" />
											<h:outputText value="," rendered="#{rowIndex + 1 < rowCount}" />
										</x:dataList>
									</h:column>

									<h:column>
										<f:facet name="header">
											<h:outputText value="#{msgs.projekte}" />
										</f:facet>
										<x:dataList var="intern"
											styleClass="#{not item.istAktiv?'text_light':''}"
											rendered="#{item.projekteSize != 0}"
											value="#{item.projekteList}" layout="ordered list"
											rowCountVar="rowCount" rowIndexVar="rowIndex">
											<h:outputText value="#{intern.titel}" />
											<h:outputText value="," rendered="#{rowIndex + 1 < rowCount}" />
										</x:dataList>
									</h:column>

									<h:column
										rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
										<f:facet name="header">
											<h:outputText value="#{msgs.auswahl}" />
										</f:facet>
										<%-- Bearbeiten-Schaltknopf --%>
										<h:commandLink action="BenutzerBearbeiten"
											title="#{msgs.benutzerBearbeiten}">
											<h:graphicImage value="/newpages/images/buttons/edit.gif" />
											<x:updateActionListener
												property="#{BenutzerverwaltungForm.myClass}" value="#{item}" />
										</h:commandLink>

										<%-- LdapKonfiguration schreiben-Schaltknopf --%>
<%-- 										<h:commandLink title="#{msgs.ldapKonfigurationSchreiben}"
											action="#{BenutzerverwaltungForm.LdapKonfigurationSchreiben}">
											<h:graphicImage value="/newpages/images/buttons/key3.gif" />
											<x:updateActionListener
												property="#{BenutzerverwaltungForm.myClass}" value="#{item}" />
										</h:commandLink>
--%>
										<%-- Benutzerprofil laden-Schaltknopf --%>
										<h:commandLink title="#{msgs.benutzerprofilLaden}"
											action="#{LoginForm.EinloggenAls}" style="margin-left:15px">
											<h:graphicImage
												value="/newpages/images/buttons/change_user3_20px.gif" />
											<f:param name="ID" value="#{item.id}" />
										</h:commandLink>

									</h:column>

								</x:dataTable>

								<htm:table width="100%" border="0">
									<htm:tr valign="top">
										<htm:td align="left">
											<%-- Neu-Schaltknopf --%>
							<%-- 				<h:commandLink action="#{BenutzerverwaltungForm.Neu}"
												immediate="true"
												rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
												<h:outputText value="#{msgs.neuenBenutzerAnlegen}" />
											</h:commandLink> --%>
										</htm:td>
										<htm:td align="center">
											<%-- ===================== Datascroller für die Ergebnisse ====================== --%>
											<x:aliasBean alias="#{mypage}"
												value="#{BenutzerverwaltungForm.page}">
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
