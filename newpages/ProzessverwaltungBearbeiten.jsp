<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%-- ######################################## 

							Prozess bearbeiten

	#########################################--%>
<a4j:keepAlive beanName="ProzessverwaltungForm" />
<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body>

	<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable" align="center">
		<%@include file="inc/tbl_Kopf.jsp"%>
		<htm:tr>
			<%@include file="inc/tbl_Navigation.jsp"%>
			<htm:td valign="top" styleClass="layoutInhalt">

				<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
				<h:form id="proceditform">
					<%-- Breadcrumb --%>
					<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:commandLink value="#{msgs.startseite}" action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:commandLink value="#{msgs.prozessverwaltung}" action="ProzessverwaltungAlle" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText value="#{msgs.einenNeuenProzessAnlegen}" rendered="#{ProzessverwaltungForm.myProzess.id == null}" />
							<h:outputText value="#{msgs.prozessDetails}" rendered="#{ProzessverwaltungForm.myProzess.id != null}" />
						</h:panelGroup>
					</h:panelGrid>

					<htm:table border="0" align="center" width="100%" cellpadding="15">
						<htm:tr>
							<htm:td>
								<htm:h3>
									<h:outputText value="#{msgs.einenNeuenProzessAnlegen}" rendered="#{ProzessverwaltungForm.myProzess.id == null}" />
									<h:outputText value="#{msgs.prozessDetails}" rendered="#{ProzessverwaltungForm.myProzess.id != null}" />
								</htm:h3>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages globalOnly="true" errorClass="text_red" infoClass="text_blue" showDetail="true" showSummary="true" tooltip="true" />

								<%-- Prozessdetails --%>
								<%@include file="inc_Prozessverwaltung/prozess_box_Prozessdetails.jsp"%>

								<%-- Schritte --%>
								<%@include file="inc_Prozessverwaltung/prozess_box_Schritte.jsp"%>

								<f:subview id="sub001" rendered="#{ProzessverwaltungForm.myProzess.id != null && not ProzessverwaltungForm.myProzess.istTemplate}">
									<%-- Vorlagen --%>
									<f:subview id="subVorlage" rendered="#{ProzessverwaltungForm.myProzess.vorlagenSize != 0}">
										<%@include file="inc_Prozessverwaltung/prozess_box_Vorlagen.jsp"%>
									</f:subview>
	
									<%-- Werkstuecke --%>
									<f:subview id="subWerk" rendered="#{ProzessverwaltungForm.myProzess.werkstueckeSize != 0}">
										<%@include file="inc_Prozessverwaltung/prozess_box_Werkstuecke.jsp"%>
									</f:subview>
								</f:subview>

								<%-- Prozesseigenschaften --%>
								<%@include file="inc_Prozessverwaltung/prozess_box_Prozesseigenschaften.jsp"%>

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
