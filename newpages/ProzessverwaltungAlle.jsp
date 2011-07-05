<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%-- ######################################## 

							Alle Prozesse in der Übersicht

	#########################################--%>
<a4j:keepAlive beanName="ProzessverwaltungForm"/>
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
<h:form id="processform">
				<%-- Breadcrumb --%>
				<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
					<h:panelGroup>
						<h:commandLink value="#{msgs.startseite}" action="newMain" />
						<f:verbatim> &#8250;&#8250; </f:verbatim>
						<h:outputText value="#{msgs.prozessverwaltung}" />
					</h:panelGroup>
				</h:panelGrid>

				<htm:table border="0" align="center" width="100%" cellpadding="15">
					<htm:tr>
						<htm:td>

							<%-- Überschrift --%>
							<htm:h3>
								<h:outputText value="#{msgs.prozessverwaltung}" />
							</htm:h3>

							<%-- globale Warn- und Fehlermeldungen --%>
							<h:messages globalOnly="true" errorClass="text_red"
								infoClass="text_blue" showDetail="true" showSummary="true"
								tooltip="true" />

							<%-- Prozesse auflisten --%>
							<%@include file="inc_Prozessverwaltung/Prozesse_Liste.jsp"%>

							<%-- abgeschlossene Prozesse 
							<%@include file="inc_Prozessverwaltung/Prozesse_abgeschlossen.jsp"%> --%>

							<%-- Prozesse auflisten --%>
							<%@include file="inc_Prozessverwaltung/Prozesse_Liste_Action.jsp"%>
							
							<%-- Anzahl der Images und Artikel --%>
							<%@include file="inc_Prozessverwaltung/Prozesse_Liste_Anzahlen.jsp"%>

							<%-- Prozessstatistik --%>
							<%@include file="inc_Prozessverwaltung/Prozesse_Liste_Statistik.jsp"%>
							
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
