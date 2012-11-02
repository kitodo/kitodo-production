<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://myfaces.apache.org/sandbox" prefix="s"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp"%>

<%-- nur anzeigen, wenn es ein Bild gibt --%>
<h:form id="formularBild">
	<a4j:commandButton reRender="BildArea,myBild,imageform" id="imageBack" style="display:none" action="#{Metadaten.BildBlaettern}" value="&lt;" immediate="true">
		<f:param name="Anzahl" value="-1" />
	</a4j:commandButton>
	<a4j:commandButton reRender="BildArea,myBild,imageform" id="imageNext" style="display:none" action="#{Metadaten.BildBlaettern}" value=">" immediate="true">
		<f:param name="Anzahl" value="1" />
	</a4j:commandButton>
	<a4j:commandButton reRender="BildArea,myBild,imageform" id="imageBack20" style="display:none" action="#{Metadaten.BildBlaettern}" value="&lt;&lt;"
		immediate="true">
		<f:param name="Anzahl" value="-20" />
	</a4j:commandButton>
	<a4j:commandButton reRender="BildArea,myBild,imageform" id="imageNext20" style="display:none" action="#{Metadaten.BildBlaettern}" value=">>" immediate="true">
		<f:param name="Anzahl" value="20" />
	</a4j:commandButton>
	<a4j:commandButton reRender="BildArea,myBild,imageform" id="imageFirst" style="display:none" action="#{Metadaten.BildBlaettern}" value="|&lt;" immediate="true">
		<f:param name="Anzahl" value="-#{Metadaten.bildNummer}" />
	</a4j:commandButton>
	<a4j:commandButton reRender="BildArea,myBild,imageform" id="imageLast" style="display:none" action="#{Metadaten.BildBlaettern}" value=">|" immediate="true">
		<f:param name="Anzahl" value="#{Metadaten.bildLetztes}" />
	</a4j:commandButton>

	<x:panelGrid id="BildArea" forceId="true" columns="1" rendered="#{Metadaten.bildNummer != '-1'}">
		<h:panelGroup style="text-align: left">
			<%-- Bildnavigation --%>
			<%-- nur anzeigen, wenn nicht erste Seite --%>
			<htm:table width="400px" border="0">
				<htm:tr>
					<htm:td rendered="#{Metadaten.bildNummer != '1'}">
						<%-- zurueck-Schaltknopf--%>
						<a4j:commandLink reRender="BildArea,myBild,imageform" action="#{Metadaten.BildBlaettern}" value="#{msgs.previous}" immediate="true">
							<f:param name="Anzahl" value="-1" />
						</a4j:commandLink>
						<%-- Trennzeichen --%>
						<h:outputText value=" | " />
					</htm:td>
					<htm:td align="center">
						<h:panelGroup rendered="#{Metadaten.bildNummer != '1'}">
							<%-- erstes-Schaltknopf --%>
							<a4j:commandLink reRender="BildArea,myBild,imageform" action="#{Metadaten.BildBlaettern}" value="1" immediate="true">
								<f:param name="Anzahl" value="-#{Metadaten.bildNummer}" />
							</a4j:commandLink>
							<%-- Trennzeichen --%>
							<h:outputText value=" | " />

							<%-- wenn Bild groesser als 2 dann vorherige Seiten --%>
							<h:panelGroup rendered="#{Metadaten.bildNummer > '2'}">

								<%-- wenn Bild groesser als 4 dann grosses Trennzeichen Seiten --%>
								<h:outputText value=" ... | " rendered="#{Metadaten.bildNummer > '4'}" />

								<%-- wenn Bild groesser als 3 dann vorvorherige Seiten --%>
								<h:panelGroup rendered="#{Metadaten.bildNummer > '3'}">
									<%-- vorvorheriges Bild --%>
									<a4j:commandLink reRender="BildArea,myBild,imageform" action="#{Metadaten.BildBlaettern}" value="#{Metadaten.bildNummer -2}" immediate="true">
										<f:param name="Anzahl" value="-2" />
									</a4j:commandLink>
									<%-- Trennzeichen --%>
									<h:outputText value=" | " />
								</h:panelGroup>

								<%-- vorheriges Bild --%>
								<a4j:commandLink reRender="BildArea,myBild,imageform" action="#{Metadaten.BildBlaettern}" value="#{Metadaten.bildNummer -1}" immediate="true">
									<f:param name="Anzahl" value="-1" />
								</a4j:commandLink>
								<%-- Trennzeichen --%>
								<h:outputText value=" | " />
							</h:panelGroup>
						</h:panelGroup>

						<h:outputText value="#{Metadaten.bildNummer}" style="font-weight: bold" />

						<%-- nur anzeigen, wenn nicht letzte Seite --%>
						<h:panelGroup rendered="#{Metadaten.bildNummer != Metadaten.bildLetztes}">

							<%-- wenn es mindestens das vorvorletzte Bild ist --%>
							<h:panelGroup rendered="#{Metadaten.bildNummer < (Metadaten.bildLetztes - 1)}">
								<%-- Trennzeichen --%>
								<h:outputText value=" | " />
								<%-- naechste Seite --%>
								<a4j:commandLink reRender="BildArea,myBild,imageform" action="#{Metadaten.BildBlaettern}" value="#{Metadaten.bildNummer + 1}" immediate="true">
									<f:param name="Anzahl" value="1" />
								</a4j:commandLink>
								<%-- wenn es mindestens das vorvorvorletzte Bild ist --%>
								<h:panelGroup rendered="#{Metadaten.bildNummer < (Metadaten.bildLetztes - 2)}">
									<%-- Trennzeichen --%>
									<h:outputText value=" | " />
									<%-- Uebernaechste Seite --%>
									<a4j:commandLink reRender="BildArea,myBild,imageform" action="#{Metadaten.BildBlaettern}" value="#{Metadaten.bildNummer + 2}" immediate="true">
										<f:param name="Anzahl" value="2" />
									</a4j:commandLink>
								</h:panelGroup>
								<%-- wenn noch mehr als drei Seiten folgen, dann ein grosses Trennzeichen --%>
								<h:outputText value=" | ... " rendered="#{Metadaten.bildNummer < (Metadaten.bildLetztes - 3)}" />
							</h:panelGroup>
							<%-- Trennzeichen --%>
							<h:outputText value=" | " />
							<%-- letztes-Schaltknopf --%>
							<a4j:commandLink reRender="BildArea,myBild,imageform" action="#{Metadaten.BildBlaettern}" value="#{Metadaten.bildLetztes}" immediate="true">
								<f:param name="Anzahl" value="#{Metadaten.bildLetztes}" />
							</a4j:commandLink>
						</h:panelGroup>
					</htm:td>

					<htm:td align="right" rendered="#{Metadaten.bildNummer != Metadaten.bildLetztes}">
						<%-- vorwaerts-Schaltknopf --%>
						<%-- Trennzeichen --%>
						<h:outputText value=" | " />
						<a4j:commandLink reRender="BildArea,myBild,imageform" action="#{Metadaten.BildBlaettern}" value="#{msgs.seiteVor}" immediate="true">
							<f:param name="Anzahl" value="1" />
						</a4j:commandLink>
					</htm:td>

				</htm:tr>
			</htm:table>
			<%-- Ende Bildnavigation --%>
		</h:panelGroup>

		<h:panelGroup style="text-align: center">
			<%-- Zoom --%>
			<a4j:commandLink reRender="BildArea,myBild,imageform" action="#{Metadaten.BildZoomMinus}" id="zoomMinus" style="margin-left: 0px;margin-right:5px">
				<h:graphicImage value="/newpages/images/zoom-.gif" style="border: 0px;vertical-align:middle" />
			</a4j:commandLink>

			<%-- aktuelle Seite anzeigen --%>
			<x:outputText id="txtZoom1" forceId="true" value="#{Metadaten.bildGroesse}%" style="font-size: 11px"
				onclick="document.getElementById('txtZoom2').style.display='inline';
			       document.getElementById('txtZoom1').style.display='none'; 
			       document.getElementById('txtZoom2').focus();
			       document.getElementById('txtZoom2').select();" />

			<%-- Zoom direkt eingeben --%>
			<x:inputText id="txtZoom2" forceId="true" value="#{Metadaten.bildGroesse}" style="display:none;font-size:9px;width:30px" required="true"
				onblur="document.getElementById('txtZoom2').style.display='none';
      				 document.getElementById('txtZoom1').style.display='inline';"
				onkeypress="return submitEnter('cmdZoom',event)" />
			<x:commandButton action="#{Metadaten.BildGeheZu}" id="cmdZoom" forceId="true" value="go" style="display:none" />

			<a4j:commandLink reRender="BildArea,myBild,imageform" action="#{Metadaten.BildZoomPlus}" id="zoomPlus" style="margin-left: 7px;margin-right:9px">
				<h:graphicImage value="/newpages/images/zoom+.gif" style="border: 0px;vertical-align:middle" />
			</a4j:commandLink>
			<%-- // Zoom --%>

			<%-- rotation --%>
			<a4j:commandLink reRender="BildArea,myBild,imageform" action="#{Metadaten.rotateLeft}" id="rotateLeft" style="margin-left: 7px;margin-right:0px">
				<h:graphicImage value="/newpages/images/rotateLeft.gif" style="border: 0px;vertical-align:middle" />
			</a4j:commandLink>

			<a4j:commandLink reRender="BildArea,myBild,imageform" action="#{Metadaten.rotateRight}" id="rotateRight" style="margin-left: 0px;margin-right:9px">
				<h:graphicImage value="/newpages/images/rotateRight.gif" style="border: 0px;vertical-align:middle" />
			</a4j:commandLink>
			<%-- // rotation--%>

			<%-- gehezu-Schaltknopf --%>
			<h:outputText value="#{msgs.geheZuBild}:" style="margin-left:5px;margin-right:0px;font-size: 12px" title="#{msgs.geheZuImage}" />
			<h:inputText value="#{Metadaten.bildNummerGeheZu}" onkeypress="return submitEnter('formularBild:goButton',event)"
				style="width:30px;border-style: solid;border-color: silver;border-width: 1px" />
			<a4j:commandButton reRender="BildArea,myBild,imageform" value="go" id="goButton" action="#{Metadaten.BildGeheZu}" style="margin-left:5px; display:none" />

			<%-- OCR --%>
			<a4j:commandLink reRender="BildArea,myBild" id="ocrButton" action="#{Metadaten.showOcrResult}" rendered="#{Metadaten.showOcrButton}">
				<h:graphicImage value="/newpages/images/buttons/ocr.png" style="margin-left:14px;vertical-align:middle" />
			</a4j:commandLink>

			<%-- //OCR --%>
			<%-- Verlinkung des Bildes mit dem gewaehlten Strukturelement --%>
			<h:outputText value="#{msgs.bildVerlinken}:" style="margin-left:13px;font-size: 12px" title="#{msgs.verlinkungDesBildesMitStrukturelement}" />
			<h:selectBooleanCheckbox value="#{Metadaten.bildZuStrukturelement}" onclick="document.getElementById('formularBild:goButton').click();"
				style="margin-left:4px" title="#{msgs.verlinkungDesBildesMitStrukturelement}" />


		</h:panelGroup>
	</x:panelGrid>

</h:form>

<x:panelGroup id="myBild" forceId="true">

	<h:panelGroup rendered="#{Metadaten.bildNummer != '-1' && Metadaten.ocrResult!=''}">
		<htm:div style="background-color: #ffffff;border-style: solid; border-width: 1px; border-color: #CCCCCC;margin-bottom:15px;padding:5px;">
			<htm:h3>
				<h:outputText value="OCR" />
			</htm:h3>
			<h:outputText value="#{Metadaten.ocrResult}" escape="false" />
		</htm:div>
	</h:panelGroup>

	<%-- das Bild selbst --%>
	<h:graphicImage value="#{Metadaten.bild}" rendered="#{Metadaten.bildNummer != '-1'}" onclick="focusForPicture()" />
</x:panelGroup>

<h:form id="formularOrdner" rendered="#{Metadaten.bildNummer != '-1'}" style="margin-top:15px">
	<h:outputText value="#{msgs.aktuellerOrdner}: " />
	<h:selectOneMenu id="TifFolders" value="#{Metadaten.currentTifFolder}">
		<si:selectItems value="#{Metadaten.allTifFolders}" var="step" itemLabel="#{step}" itemValue="#{step}" />

		<s:submitOnEvent for="cmdOrdnerWechseln" event="change" />
	</h:selectOneMenu>
	<a4j:commandLink id="cmdOrdnerWechseln" reRender="BildArea,myBild,imageform" action="#{Metadaten.BildBlaettern}" value="#{msgs.ordnerWechseln}"
		style="display: none">
		<f:param name="Anzahl" value="0" />
	</a4j:commandLink>

</h:form>

<h:form id="imageform">
	<x:inputHidden id="hiddenBildNummer" forceId="true" value=" #{Metadaten.bildNummer}" />
</h:form>


<%-- wenn kein Bild vorhanden, dann nur Meldung --%>
<h:outputText value="- #{msgs.keinBildVorhanden} -" rendered="#{Metadaten.bildNummer == '-1'}" />


