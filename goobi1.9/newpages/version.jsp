<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/dynamic" prefix="jd"%>

<%-- ######################################## 

															Impressum
															
															does anyone use this page anymore?

	#########################################--%>

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

					<%-- Breadcrumb --%>
					<h:panelGrid id="id0" width="100%" columns="1"
						styleClass="layoutInhaltKopf">
						<h:panelGroup id="id1">
							<h:commandLink id="id2" value="#{msgs.startseite}"
								action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText id="id3" value="Version" />
						</h:panelGroup>
					</h:panelGrid>
					<%-- // Breadcrumb --%>

					<htm:div style="margin: 15">
						<htm:h3>
							<h:outputText id="id4" value="Version" />
						</htm:h3>

						<%-- globale Warn- und Fehlermeldungen --%>
						<h:messages id="id5" globalOnly="true" errorClass="text_red"
							infoClass="text_blue" showDetail="true" showSummary="true"
							tooltip="true" />

						<%-- allgemeine Informationen zur Version --%>
						<h:panelGrid id="id6" columns="2">
							<h:outputText id="id7" value="Version: " />
							<h:outputText id="id8" value="0.94" />
							<h:outputText id="id9" value="Datum: " />
							<h:outputText id="id10" value="24.01.2006" />
						</h:panelGrid>

						<h:panelGroup id="id11">
							<jd:hideableController for="tab">
								<f:facet name="show">
									<h:panelGroup id="id12">
										<h:graphicImage id="id13" value="images/plus.gif"
											style="margin-right:5px" />
										<h:outputText id="id14" value="show" />
									</h:panelGroup>
								</f:facet>
								<f:facet name="hide">
									<h:panelGroup id="id15">
										<h:graphicImage id="id16" value="images/minus.gif"
											style="margin-right:5px" />
										<h:outputText id="id17" value="hide" />
									</h:panelGroup>
								</f:facet>
							</jd:hideableController>

							<jd:hideableArea id="tab">
								<h:outputText id="id18" value="hideable area" />
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
