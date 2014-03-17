<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%-- 
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
--%>
<%-- ######################################## 
				
	Prozessdaten (fuer alle DocTypes)
					
	#########################################--%>

<%--================== Daten aus einem anderen Prozess oder Opac laden ====================--%>
<h:panelGrid columns="3" border="0" width="90%" align="center" rowClasses="rowMiddle"
	rendered="#{ProzesskopieForm.useOpac || ProzesskopieForm.useTemplates}">
	<%-- aus den bereits vorhandenen Prozessen einen auswaehlen --%>
	<h:outputText value="#{msgs.AuswaehlenAusVorhandenenProzessen}" rendered="#{ProzesskopieForm.useTemplates}" />
	<h:selectOneMenu value="#{ProzesskopieForm.auswahl}" rendered="#{ProzesskopieForm.useTemplates}"
		style="margin-left:10px;margin-right:10px; width:200px">
		<f:selectItems value="#{ProzesskopieForm.prozessTemplates}" />
	</h:selectOneMenu>
	<h:commandLink action="#{ProzesskopieForm.TemplateAuswahlAuswerten}" rendered="#{ProzesskopieForm.useTemplates}"
		title="#{msgs.AuswaehlenAusVorhandenenProzessen}">
		<h:graphicImage value="/newpages/images/buttons/copy.gif" style="vertical-align:middle; margin-right:3px" />
		<h:outputText value="#{msgs.uebernehmen}" />
	</h:commandLink>
	<%-- aus dem Opac auswaehlen --%>
	<h:panelGroup rendered="#{ProzesskopieForm.useOpac}">
		<h:outputText value="#{msgs.sucheImOpac}" style="display:inline" />

		<h:selectOneMenu id="katalogauswahl" value="#{ProzesskopieForm.opacKatalog}" style="display:inline; margin-left:7px">
			<si:selectItems value="#{ProzesskopieForm.allOpacCatalogues}" var="step" itemLabel="#{step}" itemValue="#{step}" />
		</h:selectOneMenu>
		<h:outputText value="#{msgs.feld}" style="display:inline; margin-left:7px" />
		<h:selectOneMenu id="feldauswahl" value="#{ProzesskopieForm.opacSuchfeld}" style="display:inline; margin-left:10px">
			<f:selectItem itemLabel="Identifier" itemValue="12" />
			<f:selectItem itemLabel="Barcode" itemValue="8535" />
			<f:selectItem itemLabel="Barcode 8200" itemValue="8200" />
			<f:selectItem itemLabel="ISBN" itemValue="7" />
			<f:selectItem itemLabel="ISSN" itemValue="8" />
		</h:selectOneMenu>

	</h:panelGroup>
	<h:inputText value="#{ProzesskopieForm.opacSuchbegriff}" rendered="#{ProzesskopieForm.useOpac}" style="margin-left:7px;margin-right:7px; width:200px"
		onkeypress="return checkOpac('OpacRequest',event)" />
	 <h:commandLink action="#{ProzesskopieForm.OpacAuswerten}" id="performOpacQuery" rendered="#{ProzesskopieForm.useOpac}" title="#{msgs.opacAbfragen}">
		<h:graphicImage value="/newpages/images/buttons/opac.gif" style="vertical-align:middle; margin-right:3px" />
		<h:outputText value="#{msgs.uebernehmen}" />
	</h:commandLink>
</h:panelGrid>

<h:panelGroup rendered="#{ProzesskopieForm.useOpac || ProzesskopieForm.useTemplates}">
	<f:verbatim>
		<hr width="90%" />
	</f:verbatim>
</h:panelGroup>

<%--================== // Daten aus einem anderen Prozess oder Opac laden ====================--%>

<%--================== Prozessdaten ====================--%>
<h:outputText value="#{msgs.prozessdaten}" style="font-size:13;font-weight:bold;color:#00309C" />

<h:panelGrid columns="2" width="100%" border="0" style="font-size:12;margin-left:30px" rowClasses="rowTop"
	columnClasses="prozessKopieSpalte1,prozessKopieSpalte2">

	<%-- Prozessvorlage --%>
	<h:outputText value="#{msgs.prozessvorlage}" />
	<h:outputText value="#{ProzesskopieForm.prozessVorlage.titel}" />

	<%-- ProzessTitel --%>
	<h:outputText value="#{msgs.prozessTitel}" />
	<h:panelGroup>
		<h:inputText value="#{ProzesskopieForm.prozessKopie.titel}" styleClass="prozessKopieFeldbreite" />
		<h:commandLink action="#{ProzesskopieForm.CalcProzesstitel}" value="#{msgs.generieren}" />
	</h:panelGroup>

	<%-- DocType --%>
	<h:outputText value="DocType" rendered="#{ProzesskopieForm.standardFields.doctype}" />
	<h:selectOneMenu value="#{ProzesskopieForm.docType}" rendered="#{ProzesskopieForm.standardFields.doctype}" onchange="submit()"
		styleClass="prozessKopieFeldbreite">

		<si:selectItems value="#{ProzesskopieForm.allDoctypes}" var="step" itemLabel="#{step.localizedLabel}" itemValue="#{step.title}" />
	</h:selectOneMenu>

	<%-- Preferences --%>
	<h:outputLabel for="Regelsatz" rendered="#{ProzesskopieForm.standardFields.preferences}" value="#{msgs.regelsatz}" />
	<h:panelGroup rendered="#{ProzesskopieForm.standardFields.preferences}">
		<h:selectOneMenu id="Regelsatz" value="#{ProzesskopieForm.prozessKopie.regelsatz}" converter="RegelsatzConverter"
			onchange="document.getElementById('OpacRequest').click()" styleClass="prozessKopieFeldbreite" required="true">
			<f:selectItems value="#{HelperForm.regelsaetze}" />
		</h:selectOneMenu>
		<x:message for="Regelsatz" style="color: red" replaceIdWithLabel="true" />
	</h:panelGroup>

	<%-- digitale Kollektion --%>
	<h:outputLabel for="digitaleKollektionen" rendered="#{ProzesskopieForm.standardFields.collections}" value="#{msgs.digitaleKollektionen}" />
	<h:selectManyListbox id="digitaleKollektionen" styleClass="prozessKopieFeldbreite" rendered="#{ProzesskopieForm.standardFields.collections}"
		value="#{ProzesskopieForm.digitalCollections}">
		<si:selectItems value="#{ProzesskopieForm.possibleDigitalCollections}" var="step" itemLabel="#{step}" itemValue="#{step}" />
	</h:selectManyListbox>

	<%-- Tifheader - Documentname --%>
	<h:outputText value="#{msgs.tifheaderdocumentname}" />
	<h:inputText value="#{ProzesskopieForm.tifHeader_documentname}" styleClass="prozessKopieFeldbreite" />

	<%-- Tifheader - Imagedescription --%>
	<h:outputText value="#{msgs.tifheaderimagedescription}" />
	<h:inputText value="#{ProzesskopieForm.tifHeader_imagedescription}" styleClass="prozessKopieFeldbreite" />

	<h:outputText value="#{msgs.inAuswahllisteAnzeigen}" rendered="#{ProzesskopieForm.useTemplates}" />
	<h:selectBooleanCheckbox rendered="#{ProzesskopieForm.useTemplates}" value="#{ProzesskopieForm.prozessKopie.inAuswahllisteAnzeigen}" />


	<h:outputText value="#{msgs.guessImages}" rendered="#{ProzesskopieForm.standardFields.images}" />
	<h:inputText value="#{ProzesskopieForm.imagesGuessed}" rendered="#{ProzesskopieForm.standardFields.images}" styleClass="prozessKopieFeldbreite" />

</h:panelGrid>


<%--================== // Prozessdaten ====================--%>
