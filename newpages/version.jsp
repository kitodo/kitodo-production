<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/dynamic" prefix="jd"%>

<%-- ######################################## 

															Impressum

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

				<%-- Breadcrumb --%>
				<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
					<h:panelGroup>
						<h:commandLink value="#{msgs.startseite}" action="newMain" />
						<f:verbatim> &#8250;&#8250; </f:verbatim>
						<h:outputText value="Version" />
					</h:panelGroup>
				</h:panelGrid>
				<%-- // Breadcrumb --%>

				<htm:div style="margin: 15">
					<htm:h3>
						<h:outputText value="Version" />
					</htm:h3>

					<%-- globale Warn- und Fehlermeldungen --%>
					<h:messages globalOnly="true" errorClass="text_red"
						infoClass="text_blue" showDetail="true" showSummary="true"
						tooltip="true" />

					<%-- allgemeine Informationen zur Version --%>
					<h:panelGrid columns="2">
						<h:outputText value="Version: " />
						<h:outputText value="0.94" />
						<h:outputText value="Datum: " />
						<h:outputText value="24.01.2006" />
					</h:panelGrid>

					<h:panelGroup>
						<jd:hideableController for="tab">
							<f:facet name="show">
								<h:panelGroup>
									<h:graphicImage value="images/plus.gif" style="margin-right:5px"/>
									<h:outputText value="show" />
								</h:panelGroup>
							</f:facet>
							<f:facet name="hide">
								<h:panelGroup>
									<h:graphicImage value="images/minus.gif" style="margin-right:5px"/>
									<h:outputText value="hide" />
								</h:panelGroup>
							</f:facet>
						</jd:hideableController>

						<jd:hideableArea id="tab">
							<h:outputText value="hideable area" />
						</jd:hideableArea>
					</h:panelGroup>

				</htm:div>


				<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

			</htm:td>
		</htm:tr>
		<%@include file="inc/tbl_Fuss.jsp"%>
	</htm:table>




	</body>
</f:view>

</html>
