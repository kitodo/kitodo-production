<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>


<%-- ######################################## 

	Kopie einer Prozessvorlage anlegen mit Berücksichtigung komplexer Projekteinstellungen

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
				<h:form id="pageform1">
					<%-- Breadcrumb --%>
					<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup>
							<h:commandLink value="#{msgs.startseite}" action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:commandLink value="#{msgs.prozessverwaltung}"
								action="ProzessverwaltungAlle" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText value="#{msgs.einenNeuenProzessAnlegen}" />
						</h:panelGroup>
					</h:panelGrid>

					<htm:table border="0" align="center" width="100%" cellpadding="15">
						<htm:tr>
							<htm:td>
								<htm:h3>
									<h:outputText value="#{msgs.einenNeuenProzessAnlegen}" />
								</htm:h3>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages globalOnly="false" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" />

								<%-- ===================== Eingabe der Details ====================== --%>
								<htm:table cellpadding="3" cellspacing="0" width="100%"
									styleClass="eingabeBoxen">

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row1" colspan="2">
											<h:outputText value="#{msgs.details}" />
										</htm:td>
									</htm:tr>

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row2" colspan="2">
											<h:panelGrid columns="1" width="100%">
												<%-- Formular für Eingabe der Prozess-Metadaten --%>
												<%@include file="inc_process.jsp"%>
												<%@include file="inc_config.jsp"%>
											</h:panelGrid>
										</htm:td>
									</htm:tr>

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row3" align="left">
											<h:commandButton value="#{msgs.abbrechen}" immediate="true"
												action="ProzessverwaltungAlle">
											</h:commandButton>
										</htm:td>
										<htm:td styleClass="eingabeBoxen_row3" align="right">
											<h:commandButton value="#{msgs.weiter}"
												rendered="#{ProzesskopieForm.prozessKopie.eigenschaftenSize>0}"
												action="#{ProzesskopieForm.GoToSeite2}">
											</h:commandButton>
											<h:commandButton value="#{msgs.speichern}"
												rendered="#{ProzesskopieForm.prozessKopie.eigenschaftenSize==0}"
												action="#{ProzesskopieForm.NeuenProzessAnlegen}">
											</h:commandButton>
										</htm:td>
									</htm:tr>
								</htm:table>
								
								<%-- ===================== // Eingabe der Details ====================== --%>

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
<script language="javascript">
    function checkOpac(commandId,e){
        var keycode;
        if (window.event) 
        	keycode = window.event.keyCode;
        else if (e) 
        	keycode = e.which;
        else 
        	return true;
        
        //alert (keycode);
        if (keycode == 36)
        	return false;
        else
        	return true;
	}
</script>
</html>
