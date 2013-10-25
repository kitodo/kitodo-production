<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
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

							Ldap-Gruppen bearbeiten

	#########################################--%>
<a4j:keepAlive beanName="LdapGruppenForm" />
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
					<h:form id="ldapeditform" onkeypress="ifEnterClick(event, 'ldapeditform:absenden');">
						<%-- Breadcrumb --%>
						<h:panelGrid id="id0" width="100%" columns="1"
							styleClass="layoutInhaltKopf">
							<h:panelGroup id="id1">
								<h:commandLink id="id2" value="#{msgs.startseite}"
									action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:commandLink id="id3" value="#{msgs.ldapgruppen}"
									action="LdapGruppenAlle" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText id="id4" value="#{msgs.neueLdapgruppeAnlegen}"
									rendered="#{LdapGruppenForm.myLdapGruppe.id == null}" />
								<h:outputText id="id5" value="#{msgs.ldapgruppeBearbeiten}"
									rendered="#{LdapGruppenForm.myLdapGruppe.id != null}" />
							</h:panelGroup>
						</h:panelGrid>

						<htm:table border="0" align="center" width="100%" cellpadding="15"
							rendered="#{LoginForm.maximaleBerechtigung == 1}">
							<htm:tr>
								<htm:td>
									<htm:h3>
										<h:outputText id="id6" value="#{msgs.neueLdapgruppeAnlegen}"
											rendered="#{LdapGruppenForm.myLdapGruppe.id == null}" />
										<h:outputText id="id7" value="#{msgs.ldapgruppeBearbeiten}"
											rendered="#{LdapGruppenForm.myLdapGruppe.id != null}" />
									</htm:h3>

									<%-- globale Warn- und Fehlermeldungen --%>
									<h:messages id="id8" globalOnly="true" errorClass="text_red"
										infoClass="text_blue" showDetail="true" showSummary="true"
										tooltip="true" />

									<%-- Box für die Bearbeitung der Details --%>
									<htm:table cellpadding="3" cellspacing="0" width="100%"
										styleClass="eingabeBoxen">

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row1" colspan="2">
												<h:outputText id="id9" value="#{msgs.details}" />
											</htm:td>
										</htm:tr>

										<%-- Formular für die Bearbeitung der Texte --%>
										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row2" colspan="2">

												<h:panelGrid id="id10" columns="2">

													<%-- Felder --%>
													<h:outputLabel id="id11" for="titel" value="#{msgs.titel}" />
													<h:panelGroup id="id12">
														<h:inputText id="titel"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.titel}"
															required="true" />
														<x:message id="id13" for="titel" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id14" for="userDN"
														value="#{msgs.userDN}" />
													<h:panelGroup id="id15">
														<h:inputText id="userDN"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.userDN}"
															required="true" />
														<x:message id="id16" for="userDN" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id17" for="homeDirectory"
														value="homeDirectory" />
													<h:panelGroup id="id18">
														<h:inputText id="homeDirectory"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.homeDirectory}"
															required="true" />
														<x:message id="id19" for="homeDirectory"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id20" for="gidNumber" value="gidNumber" />
													<h:panelGroup id="id21">
														<h:inputText id="gidNumber"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.gidNumber}"
															required="true" />
														<x:message id="id22" for="gidNumber" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id23" for="objectClasses"
														value="objectClasses" />
													<h:panelGroup id="id24">
														<h:inputText id="objectClasses"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.objectClasses}"
															required="true" />
														<x:message id="id25" for="objectClasses"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id26" for="sambaSID" value="sambaSID" />
													<h:panelGroup id="id27">
														<h:inputText id="sambaSID"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.sambaSID}"
															required="true" />
														<x:message id="id28" for="sambaSID" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id29" for="sn" value="sn" />
													<h:panelGroup id="id30">
														<h:inputText id="sn"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.sn}"
															required="true" />
														<x:message id="id31" for="sn" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id32" for="uid" value="uid" />
													<h:panelGroup id="id33">
														<h:inputText id="uid"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.uid}"
															required="true" />
														<x:message id="id34" for="uid" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id35" for="description"
														value="description" />
													<h:panelGroup id="id36">
														<h:inputText id="description"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.description}"
															required="true" />
														<x:message id="id37" for="description" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id38" for="displayName"
														value="displayName" />
													<h:panelGroup id="id39">
														<h:inputText id="displayName"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.displayName}"
															required="true" />
														<x:message id="id40" for="displayName" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id41" for="gecos" value="gecos" />
													<h:panelGroup id="id42">
														<h:inputText id="gecos"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.gecos}"
															required="true" />
														<x:message id="id43" for="gecos" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id44" for="loginShell"
														value="loginShell" />
													<h:panelGroup id="id45">
														<h:inputText id="loginShell"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.loginShell}"
															required="true" />
														<x:message id="id46" for="loginShell" style="color: red"
															replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id47" for="sambaAcctFlags"
														value="sambaAcctFlags" />
													<h:panelGroup id="id48">
														<h:inputText id="sambaAcctFlags"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.sambaAcctFlags}"
															required="true" />
														<x:message id="id49" for="sambaAcctFlags"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id50" for="sambaLogonScript"
														value="sambaLogonScript" />
													<h:panelGroup id="id51">
														<h:inputText id="sambaLogonScript"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.sambaLogonScript}"
															required="true" />
														<x:message id="id52" for="sambaLogonScript"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id53" for="sambaPrimaryGroupSID"
														value="sambaPrimaryGroupSID" />
													<h:panelGroup id="id54">
														<h:inputText id="sambaPrimaryGroupSID"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.sambaPrimaryGroupSID}"
															required="true" />
														<x:message id="id55" for="sambaPrimaryGroupSID"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id56" for="sambaPwdMustChange"
														value="sambaPwdMustChange" />
													<h:panelGroup id="id57">
														<h:inputText id="sambaPwdMustChange"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.sambaPwdMustChange}"
															required="true" />
														<x:message id="id58" for="sambaPwdMustChange"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id59" for="sambaPasswordHistory"
														value="sambaPasswordHistory" />
													<h:panelGroup id="id60">
														<h:inputText id="sambaPasswordHistory"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.sambaPasswordHistory}"
															required="true" />
														<x:message id="id61" for="sambaPasswordHistory"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id62" for="sambaLogonHours"
														value="sambaLogonHours" />
													<h:panelGroup id="id63">
														<h:inputText id="sambaLogonHours"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.sambaLogonHours}"
															required="true" />
														<x:message id="id64" for="sambaLogonHours"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

													<h:outputLabel id="id65" for="sambaKickoffTime"
														value="sambaKickoffTime" />
													<h:panelGroup id="id66">
														<h:inputText id="sambaKickoffTime"
															style="width: 500px;margin-right:15px"
															value="#{LdapGruppenForm.myLdapGruppe.sambaKickoffTime}"
															required="true" />
														<x:message id="id67" for="sambaKickoffTime"
															style="color: red" replaceIdWithLabel="true" />
													</h:panelGroup>

												</h:panelGrid>

											</htm:td>
										</htm:tr>

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row3" align="left">
												<h:commandButton id="id68" value="#{msgs.abbrechen}"
													action="LdapGruppenAlle" immediate="true" />
											</htm:td>
											<htm:td styleClass="eingabeBoxen_row3" align="right">
												<h:commandButton id="id69" value="#{msgs.loeschen}"
													action="#{LdapGruppenForm.Loeschen}"
													onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')"
													rendered="#{LdapGruppenForm.myLdapGruppe.id != null}" />
												<h:commandButton id="absenden" value="#{msgs.speichern}"
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
