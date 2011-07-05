<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi"
	prefix="si"%>

<%-- ######################################## 
				
								Prozessdaten (für alle DocTypes)
					
	#########################################--%>

<%--================== Daten aus einem anderen Prozess oder Opac laden ====================--%>
<h:panelGrid columns="3" border="0" width="90%" align="center"
	rowClasses="rowMiddle"
	rendered="#{ProzesskopieForm.useOpac || ProzesskopieForm.useTemplates}">
	<%-- aus den bereits vorhandenen Prozessen einen auswählen --%>
	<h:outputText value="#{msgs.AuswaehlenAusVorhandenenProzessen}"
		rendered="#{ProzesskopieForm.useTemplates}" />
	<h:selectOneMenu value="#{ProzesskopieForm.auswahl}"
		rendered="#{ProzesskopieForm.useTemplates}"
		style="margin-left:10px;margin-right:10px; width:200px">
		<f:selectItems value="#{ProzesskopieForm.prozessTemplates}" />
	</h:selectOneMenu>
	<h:commandLink action="#{ProzesskopieForm.TemplateAuswahlAuswerten}"
		rendered="#{ProzesskopieForm.useTemplates}"
		title="#{msgs.AuswaehlenAusVorhandenenProzessen}">
		<h:graphicImage value="/newpages/images/buttons/copy.gif"
			style="vertical-align:middle; margin-right:3px" />
		<h:outputText value="#{msgs.uebernehmen}" />
	</h:commandLink>
	<%-- aus dem Opac auswählen --%>
	<h:panelGroup rendered="#{ProzesskopieForm.useOpac}">
		<h:outputText value="#{msgs.sucheImOpac}" style="display:inline" />

		<h:selectOneMenu id="katalogauswahl"
			value="#{ProzesskopieForm.opacKatalog}"
			style="display:inline; margin-left:7px">
		  <si:selectItems value="#{ProzesskopieForm.allOpacCatalogues}"
            var="step" itemLabel="#{step}" itemValue="#{step}" />
    	</h:selectOneMenu>
		<h:outputText value="#{msgs.feld}"
			style="display:inline; margin-left:7px" />
		<h:selectOneMenu id="feldauswahl"
			value="#{ProzesskopieForm.opacSuchfeld}"
			style="display:inline; margin-left:10px">
			<f:selectItem itemLabel="PPN" itemValue="12" />
			<f:selectItem itemLabel="Barcode" itemValue="8535" />
			<f:selectItem itemLabel="Barcode 8200" itemValue="8200" />
			<f:selectItem itemLabel="ISBN" itemValue="7" />
			<f:selectItem itemLabel="ISSN" itemValue="8" />
		</h:selectOneMenu>

	</h:panelGroup>
	<h:inputText value="#{ProzesskopieForm.opacSuchbegriff}"
		rendered="#{ProzesskopieForm.useOpac}"
		style="margin-left:7px;margin-right:7px; width:200px"
		onkeypress="return checkOpac('OpacRequest',event)" />
	<h:commandLink action="#{ProzesskopieForm.OpacAuswerten}"
		rendered="#{ProzesskopieForm.useOpac}" title="#{msgs.opacAbfragen}">
		<h:graphicImage value="/newpages/images/buttons/opac.gif"
			style="vertical-align:middle; margin-right:3px" />
		<h:outputText value="#{msgs.uebernehmen}" />
	</h:commandLink>
</h:panelGrid>

<x:commandButton action="#{ProzesskopieForm.OpacAuswerten}"
	id="OpacRequest" forceId="true" style="display:none" />

<h:panelGroup
	rendered="#{ProzesskopieForm.useOpac || ProzesskopieForm.useTemplates}">
	<f:verbatim>
		<hr width="90%" />
	</f:verbatim>
</h:panelGroup>

<%--================== // Daten aus einem anderen Prozess oder Opac laden ====================--%>

<%--================== Prozessdaten ====================--%>
<h:outputText value="#{msgs.prozessdaten}"
	style="font-size:13;font-weight:bold;color:#00309C" />

<h:panelGrid columns="2" width="100%" border="0"
	style="font-size:12;margin-left:30px" rowClasses="rowTop"
	columnClasses="prozessKopieSpalte1,prozessKopieSpalte2">

	<%-- Prozessvorlage --%>
	<h:outputText value="#{msgs.prozessvorlage}" />
	<h:outputText value="#{ProzesskopieForm.prozessVorlage.titel}" />

	<%-- ProzessTitel --%>
	<h:outputText value="#{msgs.prozessTitel}" />
	<h:panelGroup>
		<h:inputText value="#{ProzesskopieForm.prozessKopie.titel}"
			styleClass="prozessKopieFeldbreite" />
		<h:commandLink action="#{ProzesskopieForm.CalcProzesstitel}"
			value="#{msgs.generieren}" />
	</h:panelGroup>

	<%-- DocType --%>
	<h:outputText value="DocType"
		rendered="#{ProzesskopieForm.standardFields.doctype}" />
	<h:selectOneMenu value="#{ProzesskopieForm.docType}"
		rendered="#{ProzesskopieForm.standardFields.doctype}"
		onchange="submit()" styleClass="prozessKopieFeldbreite">
	<%-- 	
	   <f:selectItem itemLabel="#{msgs.keineAngabe}" itemValue="none" />
		<f:selectItem itemLabel="#{msgs.monographie}" itemValue="monograph" />
		<f:selectItem itemLabel="#{msgs.enthaltenesWerk}" itemValue="containedwork" />
		<f:selectItem itemLabel="#{msgs.multivolume}" itemValue="multivolume" />
		<f:selectItem itemLabel="#{msgs.zeitschrift}" itemValue="periodical" />
	 --%>
	    <si:selectItems value="#{ProzesskopieForm.allDoctypes}"
            var="step" itemLabel="#{step.labels.de}" itemValue="#{step.title}" />
	</h:selectOneMenu>

	<%-- Preferences --%>
	<h:outputLabel for="Regelsatz"
		rendered="#{ProzesskopieForm.standardFields.preferences}"
		value="#{msgs.regelsatz}" />
	<h:panelGroup rendered="#{ProzesskopieForm.standardFields.preferences}">
		<h:selectOneMenu id="Regelsatz"
			value="#{ProzesskopieForm.prozessKopie.regelsatz}"
			converter="RegelsatzConverter"
			onchange="document.getElementById('OpacRequest').click()"
			styleClass="prozessKopieFeldbreite" required="true">
			<f:selectItems value="#{HelperForm.regelsaetze}" />
		</h:selectOneMenu>
		<x:message for="Regelsatz" style="color: red"
			replaceIdWithLabel="true" />
	</h:panelGroup>

	<%-- digitale Kollektion --%>
	<h:outputLabel for="digitaleKollektionen"
		rendered="#{ProzesskopieForm.standardFields.collections}"
		value="#{msgs.digitaleKollektionen}" />
	<h:selectManyListbox id="digitaleKollektionen"
		styleClass="prozessKopieFeldbreite"
		rendered="#{ProzesskopieForm.standardFields.collections}"
		value="#{ProzesskopieForm.digitalCollections}">
		<si:selectItems value="#{ProzesskopieForm.possibleDigitalCollections}"
			var="step" itemLabel="#{step}" itemValue="#{step}" />
	</h:selectManyListbox>

	<%-- Tifheader - Documentname --%>
	<h:outputText value="#{msgs.tifheaderdocumentname}" />
	<h:inputText value="#{ProzesskopieForm.tifHeader_documentname}"
		styleClass="prozessKopieFeldbreite" />

	<%-- Tifheader - Imagedescription --%>
	<h:outputText value="#{msgs.tifheaderimagedescription}" />
	<h:inputText value="#{ProzesskopieForm.tifHeader_imagedescription}"
		styleClass="prozessKopieFeldbreite" />

	<h:outputText value="#{msgs.inAuswahllisteAnzeigen}"
		rendered="#{ProzesskopieForm.useTemplates}" />
	<h:selectBooleanCheckbox rendered="#{ProzesskopieForm.useTemplates}"
		value="#{ProzesskopieForm.prozessKopie.inAuswahllisteAnzeigen}" />
</h:panelGrid>


<%--================== // Prozessdaten ====================--%>
