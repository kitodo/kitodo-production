<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<%-- ######################################## 

												Startseite

	#########################################--%>

<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body>

	<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable"
		align="center">
		<jsp:include page="/newpages/inc/tbl_Kopf.jsp" />
		<htm:tr>
			<jsp:include page="/newpages/inc/tbl_Navigation.jsp" />
			<htm:td valign="top" styleClass="layoutInhalt">

				<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
				<h:form id="mainform">
					<%-- Breadcrumb --%>
					<h:panelGrid columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:outputText value="#{msgs.startseite}" />
						</h:panelGroup>
					</h:panelGrid>
					<%-- // Breadcrumb --%>

					<%-- Inhalt --%>
					<%-- goobi logo for version 151 --%>
					<h:panelGrid columns="2" width="100%" cellpadding="15"
						cellspacing="0" align="center" border="0" rowClasses="rowTop">

						<%-- Einführung --%>
						<x:panelGroup>

							<htm:noscript>
								<h:outputText
									style="color: red;font-weight: bold;margin-bottom:20px;display:block"
									value="#{msgs.keinJavascript}" />
							</htm:noscript>
							
							<htm:img src="#{HelperForm.servletPathWithHostAsUrl}/newpages/images/template/goobiVersionLogoBig.jpg" style="display: block; margin-top: 20px; margin-left: auto; margin-right: auto"/>
							
							<htm:h3 style="margin-top:15px">
								<h:outputText value="#{msgs.startseite}" />
							</htm:h3>

							<%-- globale Warn- und Fehlermeldungen --%>
							<h:messages globalOnly="true" errorClass="text_red"
								infoClass="text_blue" showDetail="true" showSummary="true"
								tooltip="true" />

							<htm:p style="text-align: justify;">
								<h:outputText style="text-align: justify;"
									value="#{HelperForm.applicationHomepageMsg}" escape="false"></h:outputText>
							</htm:p>
						</x:panelGroup>
						<%-- // Einführung --%>

						<%-- Kästen mit der Statistik --%>
						<h:panelGrid columns="1" cellpadding="0px" width="200 px"
							style="margin-top: 10px" align="center">
							<%@include file="inc_Main/box1.jsp"%>
							<%-- <%@include file="inc_Main/box2.jsp"%> --%>
							<%-- 
							<%@include file="inc_Main/box3.jsp"%>
							--%>
						</h:panelGrid>
						<%-- // Kästen mit der Statistik --%>

					</h:panelGrid>
					<%-- // Inhalt --%>
				</h:form>
				<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

			</htm:td>
		</htm:tr>
		<jsp:include page="/newpages/inc/tbl_Fuss.jsp" />
	</htm:table>

	</body>
</f:view>

</html>
