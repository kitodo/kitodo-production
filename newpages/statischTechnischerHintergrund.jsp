<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<%-- ######################################## 

							Alle Literatureinträge in der Übersicht

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
				<h:form id="tecnicform">
					<%-- Breadcrumb --%>
					<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:commandLink value="#{msgs.startseite}" action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText value="#{msgs.technischerHintergrund}" />
						</h:panelGroup>
					</h:panelGrid>
					<%-- // Breadcrumb --%>

					<htm:div style="margin: 15">
						<htm:h3>
							<h:outputText value="#{msgs.technischerHintergrund}" />
						</htm:h3>

						<%-- globale Warn- und Fehlermeldungen --%>
						<h:messages globalOnly="true" errorClass="text_red"
							infoClass="text_blue" showDetail="true" showSummary="true"
							tooltip="true" />

						<htm:p style="text-align: justify;">
							<h:outputText style="text-align: justify;"
								value="#{HelperForm.applicationTechnicalBackgroundMsg}"
								escape="false"></h:outputText>
						</htm:p>
					</htm:div>
				</h:form>
				<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

			</htm:td>
		</htm:tr>
		<%@include file="inc/tbl_Fuss.jsp"%>
	</htm:table>

	</body>
</f:view>

</html>
