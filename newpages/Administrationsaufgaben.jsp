<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/dynamic" prefix="jd"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi"
	prefix="si"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%-- ######################################## 

								Administrationsaufgaben

	#########################################--%>
<a4j:keepAlive beanName="AdministrationForm"/>
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
				<h:form id="myform">
					<%-- Breadcrumb --%>
					<h:panelGrid columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:commandLink value="#{msgs.startseite}" action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText value="#{msgs.administrationsaufgaben}" />
						</h:panelGroup>
					</h:panelGrid>

					<htm:table border="0" align="center" width="100%" cellpadding="15">
						<htm:tr>
							<htm:td>
								<htm:h3>
									<h:outputText value="#{msgs.administrationsaufgaben}" />
								</htm:h3>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages globalOnly="true" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" layout="table"
									style="margin-bottom:15px;display:block" />

								<%-- ++++++++++++++++     Administrationsaufgaben      ++++++++++++++++ --%>
								<htm:table cellpadding="3" cellspacing="0" width="100%"
									styleClass="eingabeBoxen">
									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row1">
											<h:outputText value="#{msgs.administrationsaufgaben}" />
										</htm:td>
									</htm:tr>
									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row2">

											<%-- Administration nur nach zweitem Login --%>
											<h:panelGroup
												rendered="#{not AdministrationForm.istPasswortRichtig && (LoginForm.maximaleBerechtigung == 1)}">
												<h:panelGrid id="panel1" columns="2">
													<h:outputText value="Passwort" />
													<h:panelGroup>
														<h:inputSecret id="pw"
															value="#{AdministrationForm.passwort}" />
														<h:message id="mess1" for="pw" style="color: red" />
													</h:panelGroup>
												</h:panelGrid>
												<h:commandButton id="button2" value="Weiter"
													action="#{AdministrationForm.Weiter}" />
											</h:panelGroup>

											<%-- Administrationsaufgaben --%>
											<h:panelGroup
												rendered="#{AdministrationForm.istPasswortRichtig && (LoginForm.maximaleBerechtigung == 1)}">

												<h:commandLink value="Prozesse durchlaufen" rendered="false"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.ProzesseDurchlaufen}" />
												<htm:br />

												<%-- Schritt zurückgeben an vorherige Station für Korrekturzwecke --%>
												<h:panelGroup>
													<jd:hideableController for="bitteAusloggen"
														title="#{msgs.benutzerZumAusloggenAuffordern}">
														<h:outputText
															value="#{msgs.benutzerZumAusloggenAuffordern}" />
													</jd:hideableController>
													<jd:hideableArea id="bitteAusloggen" saveState="view">
														<h:panelGrid columns="2" style="margin-left:40px;"
															rowClasses="top"
															columnClasses="standardTable_Column,standardTable_ColumnRight">
															<h:outputText value="#{msgs.bemerkung}" />
															<h:inputTextarea style="width:350px;height:80px"
																value="#{SessionForm.bitteAusloggen}" />

															<h:outputText value="" />
															<h:commandLink action="#{NavigationForm.Reload}"
																title="#{msgs.uebernehmen}"
																onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return">
																<h:outputText value="#{msgs.uebernehmen}" />
															</h:commandLink>
														</h:panelGrid>
													</jd:hideableArea>
												</h:panelGroup>
												<htm:br />



												<a4j:commandLink id="renderlink" reRender="myform"
													action="#{AdministrationForm.startStorageCalculationForAllProcessesNow}"
													immediate="true"
													onclick="changeToText()"
													oncomplete="changeToLink()" >
													<h:outputText value="Run complete history analyser job now" />
												</a4j:commandLink>
												
												
												<x:outputText id="rendertext" forceId="true" value="Run complete history analyser job now" style="display:none"/>
													
												<htm:br />
												
												<h:commandLink
													action="#{AdministrationForm.restartStorageCalculationScheduler}">
													<h:outputText value="Restart automatic job manager" />
												</h:commandLink>
												<htm:br />
												<htm:br />

												<htm:hr />
												<htm:br />


												<%-- 
												<h:commandLink
													action="#{AdministrationForm.startStorageCalculationForAllProcessesNow}">
													<h:outputText value="storage calculator now" />
												</h:commandLink>
												<htm:br />
 												 --%>
												<%-- 
												<h:commandLink
													action="#{AdministrationForm.OlmsOnlineBaendeAnlegen}">
													<h:outputText value="Olms Online - Baende anlegen" />
												</h:commandLink>
												<htm:br />
												 --%>

												<h:commandLink action="#{NavigationForm.Reload}"
													title="#{AdministrationForm.rusFullExport}">
													<x:updateActionListener
														value="#{AdministrationForm.rusFullExport?false:true}"
														property="#{AdministrationForm.rusFullExport}" />
													<h:outputText
														value="#{msgs.russischeMetadatenExportieren}: " />
													<h:outputText value="#{AdministrationForm.rusFullExport}" />
												</h:commandLink>
												<htm:br />
												<%-- 
												<htm:br />
												<h:commandLink value="Groovy Test"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.GroovyTest}" />
												<htm:br />
												--%>
												<h:commandLink value="Sici korrigieren" rendered="false"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.SiciKorr}" />
												<htm:br />

												<h:commandLink value="ProzesseDatumSetzen" rendered="true"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.ProzesseDatumSetzen}" />
												<htm:br />

												<h:commandLink value="RusDmlBaendeTiffPruefen"
													rendered="true"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.RusDmlBaendeTiffPruefen}" />
												<htm:br />

												<h:commandLink value="Imagepfad korrigieren" rendered="true"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.ImagepfadKorrigieren}" />
												<htm:br />

												<h:commandLink value="MesskatalogeOrigOrdner erstellen"
													rendered="true"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.MesskatalogeOrigOrdnerErstellen}" />
												<htm:br />

												<h:commandLink value="PPNs korrigieren" rendered="true"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.PPNsKorrigieren}" />
												<htm:br />

												<h:commandLink
													value="PPNs für Statistisches Jahrbuch korrigieren"
													rendered="true"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.PPNsFuerStatistischesJahrbuchKorrigieren}" />
												<htm:br />

												<h:commandLink
													value="PPNs für Statistisches Jahrbuch korrigieren2"
													rendered="true"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.PPNsFuerStatistischesJahrbuchKorrigieren2}" />
												<htm:br />

												<h:commandLink value="Anzahlen ermitteln" rendered="false"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.AnzahlenErmitteln}" />
												<htm:br />

												<h:commandLink value="LDAP testen" rendered="false"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.LDAPtest}" />
												<htm:br />

												<h:commandLink value="Regelsatz festlegen" rendered="false"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.StandardRegelsatzSetzen}" />
												<htm:br />
												<htm:br />

												<%-- Classloader-Plugin --%>
												<%-- 
												<h:outputLabel for="plugins" value="#{msgs.Plugins}" />
												<h:selectOneMenu id="plugins"
													value="#{AdministrationForm.myPlugin}">
													<si:selectItems value="#{AdministrationForm.myPluginList}"
														var="step" itemLabel="#{step}" itemValue="#{step}" />
												</h:selectOneMenu>
												<h:commandLink value="Plugin ausführen"
													onclick="if (!confirm('#{msgs.sicher}?')) return"
													action="#{AdministrationForm.startPlugin}" />

												<htm:br />
												--%>
											
											</h:panelGroup>

										</htm:td>
									</htm:tr>
								</htm:table>

								<%-- ++++++++++++++++     // Administrationsaufgaben      ++++++++++++++++ --%>

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
<script type="text/javascript"><!--
function changeToText(){	
			document.getElementById("myform:renderlink").style.display="none";
			document.getElementById("rendertext").style.display="inline";
  	}

function changeToLink(){
	document.getElementById("myform:renderlink").style.display="inline";
	document.getElementById("rendertext").style.display="none";
	
}
  //--></script>
</html>


