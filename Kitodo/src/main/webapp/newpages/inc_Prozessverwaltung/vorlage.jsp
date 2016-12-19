<%--
 *
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 *
--%>

<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<%-- ######################################## 

									Vorlage bearbeiten

	#########################################--%>

<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="/newpages/inc/head.jsp"%>
	<body>

	<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable"
		align="center">
		<%@include file="/newpages/inc/tbl_Kopf.jsp"%>
		<htm:tr>
			<%@include file="/newpages/inc/tbl_Navigation.jsp"%>
			<htm:td valign="top" styleClass="layoutInhalt">

				<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
<h:form id="vorform2" >
				<%-- Breadcrumb --%>
				<h:panelGrid columns="1" styleClass="layoutInhaltKopf">
					<h:panelGroup>
						<h:commandLink value="#{msgs.startseite}" action="newMain" />
						<f:verbatim> &#8250;&#8250; </f:verbatim>
						<h:commandLink value="#{msgs.prozessverwaltung}"
							action="ProzessverwaltungAlle" />
						<f:verbatim> &#8250;&#8250; </f:verbatim>
						<h:commandLink value="#{msgs.prozessDetails}"
							action="ProzessverwaltungBearbeiten" >
						</h:commandLink>
						<f:verbatim> &#8250;&#8250; </f:verbatim>
						<h:outputText value="#{msgs.vorlageDetails}" />
					</h:panelGroup>
				</h:panelGrid>

				<htm:table border="0" align="center" width="100%" cellpadding="15">
					<htm:tr>
						<htm:td>
							<htm:h3>
								<h:outputText value="#{msgs.neueVorlageAnlegen}"
									rendered="#{ProzessverwaltungForm.myVorlage.id == null}" />
								<h:outputText value="#{msgs.vorlageDetails}"
									rendered="#{ProzessverwaltungForm.myVorlage.id != null}" />
							</htm:h3>

							<%-- globale Warn- und Fehlermeldungen --%>
							<h:messages globalOnly="true" errorClass="text_red"
								infoClass="text_blue" showDetail="true" showSummary="true"
								tooltip="true" />

							<%-- Vorlagedetails --%>
							<%@include file="/newpages/inc_Prozessverwaltung/vorlage_box_Details.jsp"%>

							<%-- Vorlageeigenschaften --%>
							<%@include
								file="/newpages/inc_Prozessverwaltung/vorlage_box_Eigenschaften.jsp"%>

						</htm:td>
					</htm:tr>
				</htm:table>
				</h:form>
				<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

			</htm:td>
		</htm:tr>
		<%@include file="/newpages/inc/tbl_Fuss.jsp"%>
	</htm:table>

	</body>
</f:view>

</html>
