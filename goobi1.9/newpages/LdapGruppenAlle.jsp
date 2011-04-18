<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%-- ######################################## 

							Alle Ldapgruppen in der �?bersicht

	#########################################--%>
<a4j:keepAlive beanName="LdapGruppenForm"/>
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
				<h:form id="ldapform">
					<%-- Breadcrumb --%>
					<h:panelGrid id="id0" columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup id="id1">
							<h:commandLink id="id2" value="#{msgs.startseite}" action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText id="id3" value="#{msgs.ldapgruppen}" />
						</h:panelGroup>
					</h:panelGrid>

					<htm:table border="0" align="center" width="100%" cellpadding="15" rendered="#{LoginForm.maximaleBerechtigung == 1}">
						<htm:tr>
							<htm:td>

								<%-- �?berschrift --%>
								<htm:h3>
									<h:outputText id="id4" value="#{msgs.ldapgruppen}" />
								</htm:h3>

								<h:commandLink id="id5" action="#{LdapGruppenForm.Neu}"
									immediate="true"
									rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
									<h:outputText id="id6" value="#{msgs.neueLdapgruppeAnlegen}" />
								</h:commandLink>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages id="id7" globalOnly="true" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" />

								<%-- Datentabelle --%>
								<x:dataTable id="id8" styleClass="standardTable" width="100%"
									cellspacing="1px" cellpadding="1px"
									headerClass="standardTable_Header"
									rowClasses="standardTable_Row1,standardTable_Row2"
									columnClasses="standardTable_Column" var="item"
									value="#{LdapGruppenForm.page.listReload}">

									<h:column id="id9">
										<f:facet name="header">
											<h:outputText id="id10" value="#{msgs.ldapgruppe}" />
										</f:facet>
										<h:outputText id="id11" value="#{item.titel}" />
									</h:column>

									<h:column id="id12">
										<f:facet name="header">
											<h:outputText id="id13" value="#{msgs.homeVerzeichnis}" />
										</f:facet>
										<h:outputText id="id14" value="#{item.homeDirectory}" />
									</h:column>

									<h:column id="id15">
										<f:facet name="header">
											<h:outputText id="id16" value="gidNumber" />
										</f:facet>
										<h:outputText id="id17" value="#{item.gidNumber}" />
									</h:column>

									<x:column id="id18" style="text-align:center"
										rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
										<f:facet name="header">
											<h:outputText id="id19" value="#{msgs.auswahl}" />
										</f:facet>
										<%-- Bearbeiten-Schaltknopf --%>
										<h:commandLink id="id20" action="LdapGruppenBearbeiten"
											title="#{msgs.ldapgruppeBearbeiten}">
											<h:graphicImage id="id21" value="/newpages/images/buttons/edit.gif" />
											<x:updateActionListener
												property="#{LdapGruppenForm.myLdapGruppe}" value="#{item}" />
										</h:commandLink>
									</x:column>
								</x:dataTable>

								<htm:table width="100%" border="0">
									<htm:tr valign="top">
										<htm:td align="left">
											<%-- Neu-Schaltknopf --%>
					
										</htm:td>
										<htm:td align="center">
											<%-- ===================== Datascroller für die Ergebnisse ====================== --%>
											<x:aliasBean alias="#{mypage}"
												value="#{LdapGruppenForm.page}">
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
