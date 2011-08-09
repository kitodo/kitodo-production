<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<%-- ######################################## 

								Suchmaske für Prozesse

	#########################################--%>

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
				<h:form id="procmanageform">
					<%-- Breadcrumb --%>
					<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:commandLink value="#{msgs.startseite}" action="newMain"  id="mainlink"/>
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText value="#{msgs.prozessverwaltung}" />
						</h:panelGroup>
					</h:panelGrid>


					<htm:table border="0" align="center" width="100%" cellpadding="15">
						<htm:tr>
							<htm:td>

								<%-- Überschrift --%>
								<htm:h3>
									<h:outputText value="#{msgs.nachEinemBandSuchen}" />
								</htm:h3>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages globalOnly="true" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" />

								<%-- ===================== Eingabe der Suchparameter ====================== --%>
								<htm:table cellpadding="3" cellspacing="0" width="100%"
									styleClass="eingabeBoxen">

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row1" colspan="2">
											<h:outputText value="#{msgs.suche}" />
										</htm:td>
									</htm:tr>

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row2" colspan="2">
											<h:panelGrid columns="2">
												<h:outputLabel for="suchbegriff" value="#{msgs.suchbegriff}" />
												<h:panelGroup>
													<h:inputText id="suchbegriff"
														style="width: 300px;margin-right:5px"
														value="#{ProzessverwaltungForm.filter}" required="true" />
													<h:outputLink target="_blank"
														value="http://wiki.goobi.org/index.php/Filter_f%C3%BCr_Vorg%C3%A4nge">
														<h:graphicImage title="#{msgs.hilfeZumFilter}"
															value="/newpages/images/buttons/help.png"
															style="margin-right:10px" />
													</h:outputLink>

													<x:message for="suchbegriff" style="color: red"
														replaceIdWithLabel="true" />
												</h:panelGroup>
											</h:panelGrid>

										</htm:td>
									</htm:tr>
									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row3" align="right">
											<h:commandButton value="#{msgs.sucheStarten}" id="search"
												action="#{ProzessverwaltungForm.FilterAlleStart}">
												<x:updateActionListener
													property="#{ProzessverwaltungForm.modusAnzeige}"
													value="aktuell" />
											</h:commandButton>
										</htm:td>
									</htm:tr>
								</htm:table>

								<%-- ===================== // Eingabe der Suchparameter ====================== --%>

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
