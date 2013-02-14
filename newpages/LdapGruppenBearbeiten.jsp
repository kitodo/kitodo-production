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

							Ldap-Gruppen bearbeiten

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
				<h:form id="ldapeditform">
					<%-- Breadcrumb --%>
					<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:commandLink value="#{msgs.startseite}" action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:commandLink value="#{msgs.ldapgruppen}"
								action="LdapGruppenAlle" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText value="#{msgs.neueLdapgruppeAnlegen}"
								rendered="#{LdapgruppenForm.myLdapGruppe.id == null}" />
							<h:outputText value="#{msgs.ldapgruppeBearbeiten}"
								rendered="#{LdapGruppenForm.myLdapGruppe.id != null}" />
						</h:panelGroup>
					</h:panelGrid>

					<htm:table border="0" align="center" width="100%" cellpadding="15" rendered="#{LoginForm.maximaleBerechtigung == 1}">
						<htm:tr>
							<htm:td>
								<htm:h3>
									<h:outputText value="#{msgs.neueLdapgruppeAnlegen}"
										rendered="#{LdapGruppenForm.myLdapGruppe.id == null}" />
									<h:outputText value="#{msgs.ldapgruppeBearbeiten}"
										rendered="#{LdapGruppenForm.myLdapGruppe.id != null}" />
								</htm:h3>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages globalOnly="true" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" />

								<%-- Box für die Bearbeitung der Details --%>
								<htm:table cellpadding="3" cellspacing="0" width="100%"
									styleClass="eingabeBoxen">

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row1" colspan="2">
											<h:outputText value="#{msgs.details}" />
										</htm:td>
									</htm:tr>

									<%-- Formular für die Bearbeitung der Texte --%>
									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row2" colspan="2">

											<h:panelGrid columns="2">

												<%-- Felder --%>
												<h:outputLabel for="titel" value="#{msgs.titel}" />
												<h:panelGroup>
													<h:inputText id="titel"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.titel}"
														required="true" />
													<x:message for="titel" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="userDN" value="#{msgs.userDN}" />
												<h:panelGroup>
													<h:inputText id="userDN"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.userDN}"
														required="true" />
													<x:message for="userDN" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="homeDirectory" value="homeDirectory" />
												<h:panelGroup>
													<h:inputText id="homeDirectory"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.homeDirectory}"
														required="true" />
													<x:message for="homeDirectory" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="gidNumber" value="gidNumber" />
												<h:panelGroup>
													<h:inputText id="gidNumber"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.gidNumber}"
														required="true" />
													<x:message for="gidNumber" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="objectClasses" value="objectClasses" />
												<h:panelGroup>
													<h:inputText id="objectClasses"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.objectClasses}"
														required="true" />
													<x:message for="objectClasses" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="sambaSID" value="sambaSID" />
												<h:panelGroup>
													<h:inputText id="sambaSID"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.sambaSID}"
														required="true" />
													<x:message for="sambaSID" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="sn" value="sn" />
												<h:panelGroup>
													<h:inputText id="sn" style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.sn}" required="true" />
													<x:message for="sn" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="uid" value="uid" />
												<h:panelGroup>
													<h:inputText id="uid"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.uid}"
														required="true" />
													<x:message for="uid" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="description" value="description" />
												<h:panelGroup>
													<h:inputText id="description"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.description}"
														required="true" />
													<x:message for="description" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="displayName" value="displayName" />
												<h:panelGroup>
													<h:inputText id="displayName"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.displayName}"
														required="true" />
													<x:message for="displayName" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="gecos" value="gecos" />
												<h:panelGroup>
													<h:inputText id="gecos"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.gecos}"
														required="true" />
													<x:message for="gecos" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="loginShell" value="loginShell" />
												<h:panelGroup>
													<h:inputText id="loginShell"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.loginShell}"
														required="true" />
													<x:message for="loginShell" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="sambaAcctFlags" value="sambaAcctFlags" />
												<h:panelGroup>
													<h:inputText id="sambaAcctFlags"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.sambaAcctFlags}"
														required="true" />
													<x:message for="sambaAcctFlags" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="sambaLogonScript"
													value="sambaLogonScript" />
												<h:panelGroup>
													<h:inputText id="sambaLogonScript"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.sambaLogonScript}"
														required="true" />
													<x:message for="sambaLogonScript" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

												<h:outputLabel for="sambaPrimaryGroupSID"
													value="sambaPrimaryGroupSID" />
												<h:panelGroup>
													<h:inputText id="sambaPrimaryGroupSID"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.sambaPrimaryGroupSID}"
														required="true" />
													<x:message for="sambaPrimaryGroupSID" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>
												
												<h:outputLabel for="sambaPwdMustChange"
													value="sambaPwdMustChange" />
												<h:panelGroup>
													<h:inputText id="sambaPwdMustChange"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.sambaPwdMustChange}"
														required="true" />
													<x:message for="sambaPwdMustChange" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>
												
												<h:outputLabel for="sambaPasswordHistory"
													value="sambaPasswordHistory" />
												<h:panelGroup>
													<h:inputText id="sambaPasswordHistory"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.sambaPasswordHistory}"
														required="true" />
													<x:message for="sambaPasswordHistory" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>
												
												<h:outputLabel for="sambaLogonHours"
													value="sambaLogonHours" />
												<h:panelGroup>
													<h:inputText id="sambaLogonHours"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.sambaLogonHours}"
														required="true" />
													<x:message for="sambaLogonHours" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>
												
												<h:outputLabel for="sambaKickoffTime"
													value="sambaKickoffTime" />
												<h:panelGroup>
													<h:inputText id="sambaKickoffTime"
														style="width: 500px;margin-right:15px"
														value="#{LdapGruppenForm.myLdapGruppe.sambaKickoffTime}"
														required="true" />
													<x:message for="sambaKickoffTime" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>

											</h:panelGrid>

										</htm:td>
									</htm:tr>

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row3" align="left">
											<h:commandButton value="#{msgs.abbrechen}"
												action="LdapGruppenAlle" immediate="true" />
										</htm:td>
										<htm:td styleClass="eingabeBoxen_row3" align="right">
											<h:commandButton value="#{msgs.loeschen}"
												action="#{LdapGruppenForm.Loeschen}"
												onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')"
												rendered="#{LdapGruppenForm.myLdapGruppe.id != null}" />
											<h:commandButton value="#{msgs.speichern}"
												action="#{LdapGruppenForm.Speichern}" />
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
