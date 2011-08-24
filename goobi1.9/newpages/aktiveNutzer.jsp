<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%-- ######################################## 

							Alle Benutzer in der �?bersicht

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
					<h:form id="userform">
						<%-- Breadcrumb --%>
						<h:panelGrid id="id0" width="100%" columns="1"
							styleClass="layoutInhaltKopf">
							<h:panelGroup id="id1">
								<h:commandLink id="id2" value="#{msgs.startseite}"
									action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText id="id3" value="#{msgs.aktiveBenutzer}" />
							</h:panelGroup>
						</h:panelGrid>

						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>

									<%-- �?berschrift --%>
									<htm:h3>
										<h:outputText id="id4" value="#{msgs.aktiveBenutzer}" />
									</htm:h3>

									<%-- globale Warn- und Fehlermeldungen --%>
									<h:messages id="id5" globalOnly="true" errorClass="text_red"
										infoClass="text_blue" showDetail="true" showSummary="true"
										tooltip="true" />

									<a4j:poll id="id6" interval="10001"
										reRender="serverzeit, benutzerliste" ajaxSingle="true" />

									<h:panelGrid id="serverzeit" columns="2" style="font-size:11">
										<h:outputText id="id7" value="#{msgs.aktiveBenutzer}:" />
										<h:outputText id="id8" value="#{SessionForm.aktiveSessions}" />
										<h:outputText id="id9" value="#{msgs.aktuelleZeit}:" />
										<h:outputText id="id10" value="#{SessionForm.aktuelleZeit}" />
									</h:panelGrid>
									<htm:br />

									<%-- Datentabelle --%>
									<x:dataTable id="benutzerliste" styleClass="standardTable"
										width="100%" cellspacing="1px" cellpadding="1px"
										headerClass="standardTable_Header"
										rowClasses="standardTable_Row1" var="item"
										value="#{SessionForm.alleSessions}">

										<h:column id="id11"
											rendered="#{LoginForm.maximaleBerechtigung > 0}">
											<f:facet name="header">
												<h:outputText id="id12" value="#{msgs.benutzer}" />
											</f:facet>
											<h:outputText id="id13" value="#{item.user}" />
										</h:column>

										<%-- 
								<h:column id="id14">
									<f:facet name="header">
										<h:outputText id="id15" value="#{msgs.id}" />
									</f:facet>
									<h:outputText id="id16" value="#{item.id}" />
								</h:column>
--%>

										<h:column id="id17"
											rendered="#{LoginForm.maximaleBerechtigung == 1}">
											<f:facet name="header">
												<h:outputText id="id18" value="IP" />
											</f:facet>
											<h:outputText id="id19" value="#{item.address}" />
										</h:column>

										<h:column id="id20">
											<f:facet name="header">
												<h:outputText id="id21" value="Browser" />
											</f:facet>
											<h:graphicImage
												value="/newpages/images/browser/#{item.browserIcon}"
												width="30" height="30" style="float:left;margin-right:4px" />
											<h:outputText id="id22" value="#{item.browser}" />
										</h:column>

										<h:column id="id23">
											<f:facet name="header">
												<h:outputText id="id24" value="#{msgs.aktivSeit}" />
											</f:facet>
											<h:outputText id="id25" value="#{item.created}" />
										</h:column>

										<h:column id="id26">
											<f:facet name="header">
												<h:outputText id="id27" value="#{msgs.letzterZugriff}" />
											</f:facet>
											<h:outputText id="id28" value="#{item.last}" />
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
