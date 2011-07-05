<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%-- ######################################## 

							Alle Benutzer in der Übersicht

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
				<h:form id="userform">
					<%-- Breadcrumb --%>
					<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:commandLink value="#{msgs.startseite}" action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText value="#{msgs.aktiveBenutzer}" />
						</h:panelGroup>
					</h:panelGrid>

					<htm:table border="0" align="center" width="100%" cellpadding="15">
						<htm:tr>
							<htm:td>

								<%-- Überschrift --%>
								<htm:h3>
									<h:outputText value="#{msgs.aktiveBenutzer}" />
								</htm:h3>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages globalOnly="true" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" />

								<a4j:poll interval="10001" reRender="serverzeit, benutzerliste"
									ajaxSingle="true" />
	
								<h:panelGrid id="serverzeit" columns="2" style="font-size:11">
									<h:outputText value="#{msgs.aktiveBenutzer}:" />
									<h:outputText value="#{SessionForm.aktiveSessions}" />
									<h:outputText value="#{msgs.aktuelleZeit}:" />
									<h:outputText value="#{SessionForm.aktuelleZeit}" />
								</h:panelGrid>
								<htm:br />

								<%-- Datentabelle --%>
								<x:dataTable id="benutzerliste" styleClass="standardTable" width="100%"
									cellspacing="1px" cellpadding="1px"
									headerClass="standardTable_Header"
									rowClasses="standardTable_Row1" var="item"
									value="#{SessionForm.alleSessions}">

									<h:column rendered="#{LoginForm.maximaleBerechtigung > 0}">
										<f:facet name="header">
											<h:outputText value="#{msgs.benutzer}" />
										</f:facet>
										<h:outputText value="#{item.user}" />
									</h:column>

									<%-- 
								<h:column>
									<f:facet name="header">
										<h:outputText value="#{msgs.id}" />
									</f:facet>
									<h:outputText value="#{item.id}" />
								</h:column>
--%>

									<h:column rendered="#{LoginForm.maximaleBerechtigung == 1}">
										<f:facet name="header">
											<h:outputText value="IP" />
										</f:facet>
										<h:outputText value="#{item.address}" />
									</h:column>

									<h:column>
										<f:facet name="header">
											<h:outputText value="Browser" />
										</f:facet>
										<h:graphicImage
											value="/newpages/images/browser/#{item.browserIcon}"
											width="30" height="30" style="float:left;margin-right:4px" />
										<h:outputText value="#{item.browser}" />
									</h:column>

									<h:column>
										<f:facet name="header">
											<h:outputText value="#{msgs.aktivSeit}" />
										</f:facet>
										<h:outputText value="#{item.created}" />
									</h:column>

									<h:column>
										<f:facet name="header">
											<h:outputText value="#{msgs.letzterZugriff}" />
										</f:facet>
										<h:outputText value="#{item.last}" />
									</h:column>

								</x:dataTable>

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
