<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
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
<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="main_statistikboxen"
	rendered="#{ProzessverwaltungForm.modusBearbeiten!='schritt'}">

	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row1">
			<h:outputText value="#{msgs.details}" />
		</htm:td>
		<htm:td styleClass="main_statistikboxen_row1" align="right">
			<h:commandLink action="#{ProzessverwaltungForm.Reload}">
				<h:graphicImage value="/newpages/images/reload.gif" />
			</h:commandLink>
		</htm:td>
	</htm:tr>

	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row2" colspan="2">

			<htm:table border="0" width="90%" cellpadding="2">
				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.titel}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{ProzessverwaltungForm.mySchritt.titelLokalisiert}" />
					</htm:td>
					<htm:td rowspan="2" align="right">
						<h:commandLink title="#{msgs.bearbeiten}" action="#{NavigationForm.Reload}" style=";margin-right:20px">
							<h:graphicImage value="/newpages/images/buttons/edit_20.gif" />
							<x:updateActionListener property="#{ProzessverwaltungForm.modusBearbeiten}" value="schritt" />
						</h:commandLink>
					</htm:td>
				</htm:tr>

				<htm:tr>
					<htm:td width="150">
						<h:outputText styleClass="text_light" value="#{msgs.id}:" />
					</htm:td>
					<htm:td>
						<h:outputText styleClass="text_light" value="#{ProzessverwaltungForm.mySchritt.id}" />
					</htm:td>
				</htm:tr>
				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.reihenfolge}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{ProzessverwaltungForm.mySchritt.reihenfolge}" />
					</htm:td>
				</htm:tr>
				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.prioritaet}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{ProzessverwaltungForm.mySchritt.prioritaet}" />
					</htm:td>
				</htm:tr>

				<htm:tr>
					<htm:td width="150" style="vertical-align:top">
						<h:outputText value="#{msgs.typ}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{msgs.metadaten}, " rendered="#{ProzessverwaltungForm.mySchritt.typMetadaten}" />

						<h:outputText value="#{msgs.importMittelsFileUpload}, " rendered="#{ProzessverwaltungForm.mySchritt.typImportFileUpload}" />

						<h:outputText value="#{msgs.exportDMS}, " rendered="#{ProzessverwaltungForm.mySchritt.typExportDMS}" />

						<h:outputText value="#{msgs.schrittBeimAnnehmenAbschliessen}, " rendered="#{ProzessverwaltungForm.mySchritt.typBeimAnnehmenAbschliessen}" />

						<h:outputText value="#{msgs.beimAnnehmenModulStarten}, " rendered="#{ProzessverwaltungForm.mySchritt.typBeimAnnehmenModul}" />

						<h:outputText value="#{msgs.beimAnnehmenModulStartenUndSchrittAbschliessen}, "
							rendered="#{ProzessverwaltungForm.mySchritt.typBeimAnnehmenModulUndAbschliessen}" />
						<%-- 
						<h:outputText value="#{msgs.exportRus}, "
							rendered="#{ProzessverwaltungForm.mySchritt.typExportRus}" />
--%>
						<h:outputText value="#{msgs.imagesLesen}, " rendered="#{ProzessverwaltungForm.mySchritt.typImagesLesen}" />

						<h:outputText value="#{msgs.imagesSchreiben}, " rendered="#{ProzessverwaltungForm.mySchritt.typImagesSchreiben}" />

						<h:outputText value="#{msgs.beimAbschliessenVerifizieren}, " rendered="#{ProzessverwaltungForm.mySchritt.typBeimAbschliessenVerifizieren}" />

						<h:outputText value="#{msgs.automatischerSchritt}, " rendered="#{ProzessverwaltungForm.mySchritt.typAutomatisch}" />

						<h:outputText value="#{msgs.typScriptStep}(#{ProzessverwaltungForm.mySchritt.typAutomatischScriptpfad}),"
							rendered="#{ProzessverwaltungForm.mySchritt.typScriptStep}" />

						<h:outputText value="#{msgs.modulSchritt}, "
							rendered="#{ProzessverwaltungForm.mySchritt.typModulName!=null && ProzessverwaltungForm.mySchritt.typModulName!=''}" />
						<h:outputText value="#{msgs.typAutomatisch}" rendered="#{ProzessverwaltungForm.mySchritt.typAutomatisch}" />
						<h:outputText value="#{msgs.batchStep}" rendered="#{ProzessverwaltungForm.mySchritt.batchStep}" />

					</htm:td>
				</htm:tr>

				<htm:tr
					rendered="#{ProzessverwaltungForm.mySchritt.typScriptStep && ProzessverwaltungForm.mySchritt.typAutomatischScriptpfad!='' && ProzessverwaltungForm.mySchritt.typAutomatischScriptpfad!=null}">
					<htm:td width="150">
						<h:outputText value="#{msgs.script}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{ProzessverwaltungForm.mySchritt.listOfPaths}" />
					</htm:td>
				</htm:tr>

				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.status}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{ProzessverwaltungForm.mySchritt.bearbeitungsstatusEnum.title}" />
					</htm:td>
				</htm:tr>
			</htm:table>

		</htm:td>
	</htm:tr>
</htm:table>


<%-- Box für die Bearbeitung der Details --%>
<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen" rendered="#{ProzessverwaltungForm.modusBearbeiten=='schritt'}">

	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row1" colspan="2">
			<h:outputText value="#{msgs.details}" />
		</htm:td>
	</htm:tr>

	<%-- Formular für die Bearbeitung des Prozesses --%>
	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row2" colspan="2">
			<h:panelGrid columns="2">

				<%-- Felder --%>
				<h:outputLabel for="titel2" value="#{msgs.titel}" />
				<h:panelGroup>
					<h:inputText id="titel2" style="width: 300px;margin-right:15px" value="#{ProzessverwaltungForm.mySchritt.titel}" required="true" />
					<x:message for="titel2" style="color: red" replaceIdWithLabel="true" />
				</h:panelGroup>

				<h:outputLabel for="reihenfolge2" value="#{msgs.reihenfolge}" />
				<h:panelGroup>
					<h:inputText id="reihenfolge2" style="width: 300px;margin-right:15px" value="#{ProzessverwaltungForm.mySchritt.reihenfolge}" required="true" />
					<x:message for="reihenfolge2" style="color: red" replaceIdWithLabel="true" />
				</h:panelGroup>

				<h:outputLabel for="prioritaet" value="#{msgs.prioritaet}" />
				<h:panelGroup>
					<h:inputText id="prioritaet" style="width: 300px;margin-right:15px" value="#{ProzessverwaltungForm.mySchritt.prioritaet}" required="true" />
					<x:message for="prioritaet" style="color: red" replaceIdWithLabel="true" />
				</h:panelGroup>

				<h:outputText value="#{msgs.metadaten}" />
				<h:selectBooleanCheckbox value="#{ProzessverwaltungForm.mySchritt.typMetadaten}" />

				<h:outputText value="#{msgs.importMittelsFileUpload}" />
				<h:selectBooleanCheckbox value="#{ProzessverwaltungForm.mySchritt.typImportFileUpload}" />

				<h:outputText value="#{msgs.imagesLesen}" />
				<x:selectBooleanCheckbox forceId="true" id="chkLesen" onclick="if(!this.checked) document.getElementById('chkSchreiben').checked=false;"
					value="#{ProzessverwaltungForm.mySchritt.typImagesLesen}" />

				<h:outputText value="#{msgs.imagesSchreiben}" />
				<x:selectBooleanCheckbox forceId="true" id="chkSchreiben" onclick="if(this.checked) document.getElementById('chkLesen').checked=true;"
					value="#{ProzessverwaltungForm.mySchritt.typImagesSchreiben}" />

				<h:outputText value="#{msgs.beimAbschliessenVerifizieren}" />
				<h:selectBooleanCheckbox value="#{ProzessverwaltungForm.mySchritt.typBeimAbschliessenVerifizieren}" />
				<%-- 
				<h:outputText value="#{msgs.exportRus}" />
				<h:selectBooleanCheckbox
					value="#{ProzessverwaltungForm.mySchritt.typExportRus}" />
--%>
				<h:outputText value="#{msgs.exportDMS}" />
				<h:selectBooleanCheckbox value="#{ProzessverwaltungForm.mySchritt.typExportDMS}" />

				<h:outputText value="#{msgs.schrittBeimAnnehmenAbschliessen}" />
				<x:selectBooleanCheckbox forceId="true" id="chkmanuell1" onclick="chkManuellAutomatischSetzen(this)"
					value="#{ProzessverwaltungForm.mySchritt.typBeimAnnehmenAbschliessen}" />

				<h:outputText value="#{msgs.beimAnnehmenModulStarten}" rendered="#{NavigationForm.showModuleManager}" />
				<x:selectBooleanCheckbox forceId="true" id="chkmanuell2" onclick="chkManuellAutomatischSetzen(this)"
					value="#{ProzessverwaltungForm.mySchritt.typBeimAnnehmenModul}" rendered="#{NavigationForm.showModuleManager}" />

				<h:outputText value="#{msgs.beimAnnehmenModulStartenUndSchrittAbschliessen}" rendered="#{NavigationForm.showModuleManager}" />
				<x:selectBooleanCheckbox forceId="true" id="chkmanuell3" onclick="chkManuellAutomatischSetzen(this)"
					value="#{ProzessverwaltungForm.mySchritt.typBeimAnnehmenModulUndAbschliessen}" rendered="#{NavigationForm.showModuleManager}" />


				<h:outputText value="#{msgs.automatischerSchritt}" />
				<x:selectBooleanCheckbox forceId="true" id="chkmanuell4" onclick="chkManuellAutomatischSetzen(this)"
					value="#{ProzessverwaltungForm.mySchritt.typAutomatisch}" />

				<h:outputLabel for="chkautomatisch" value="#{msgs.ScriptSchritt}" />
				<h:panelGroup>
					<x:selectBooleanCheckbox style="vertical-align:top" forceId="true" id="chkautomatisch" onclick="txtAutomatischAnzeigen()"
						value="#{ProzessverwaltungForm.mySchritt.typScriptStep}" />
					<x:panelGrid columns="2" forceId="true" id="scripttable">

						<h:outputText id="scriptname" value="#{msgs.scriptname}" />

						<h:outputText id="scrpitpfad" value="#{msgs.scriptpath}" />

						<x:inputText id="nameautomatisch" forceId="true" style="width: 150px;margin-right:15px" value="#{ProzessverwaltungForm.mySchritt.scriptname1}" />

						<x:inputText id="txtautomatisch" forceId="true" style="width: 500px;margin-right:15px"
							value="#{ProzessverwaltungForm.mySchritt.typAutomatischScriptpfad}" />


						<x:inputText id="nameautomatisch2" forceId="true" style="width: 150px;margin-right:15px" value="#{ProzessverwaltungForm.mySchritt.scriptname2}" />
						<x:inputText id="txtautomatisch2" forceId="true" style="width: 500px;margin-right:15px"
							value="#{ProzessverwaltungForm.mySchritt.typAutomatischScriptpfad2}" />

						<x:inputText id="nameautomatisch3" forceId="true" style="width: 150px;margin-right:15px" value="#{ProzessverwaltungForm.mySchritt.scriptname3}" />
						<x:inputText id="txtautomatisch3" forceId="true" style="width: 500px;margin-right:15px"
							value="#{ProzessverwaltungForm.mySchritt.typAutomatischScriptpfad3}" />

						<x:inputText id="nameautomatisch4" forceId="true" style="width: 150px;margin-right:15px" value="#{ProzessverwaltungForm.mySchritt.scriptname4}" />
						<x:inputText id="txtautomatisch4" forceId="true" style="width: 500px;margin-right:15px"
							value="#{ProzessverwaltungForm.mySchritt.typAutomatischScriptpfad4}" />

						<x:inputText id="nameautomatisch5" forceId="true" style="width: 150px;margin-right:15px" value="#{ProzessverwaltungForm.mySchritt.scriptname5}" />
						<x:inputText id="txtautomatisch5" forceId="true" style="width: 500px;margin-right:15px"
							value="#{ProzessverwaltungForm.mySchritt.typAutomatischScriptpfad5}" />


					</x:panelGrid>
					<x:message for="chkautomatisch" style="color: red" replaceIdWithLabel="true" />
				</h:panelGroup>

				<h:outputText value="#{msgs.modul}" rendered="#{NavigationForm.showModuleManager}" />
				<h:inputText value="#{ProzessverwaltungForm.mySchritt.typModulName}" rendered="#{NavigationForm.showModuleManager}" />

				<h:outputLabel for="status" value="#{msgs.status}" />
				<h:panelGroup>
					<h:selectOneMenu id="status" style="width: 300px;margin-right:15px" value="#{ProzessverwaltungForm.mySchritt.bearbeitungsstatusAsString}"
						required="true">
						<f:selectItem itemValue="" itemLabel="#{msgs.bitteAuswaehlen}" />
						<f:selectItem itemValue="0" itemLabel="#{msgs.statusGesperrt}" />
						<f:selectItem itemValue="1" itemLabel="#{msgs.statusOffen}" />
						<f:selectItem itemValue="2" itemLabel="#{msgs.statusInBearbeitung}" />
						<f:selectItem itemValue="3" itemLabel="#{msgs.statusAbgeschlossen}" />
					</h:selectOneMenu>
					<x:message for="status" style="color: red" replaceIdWithLabel="true" />
				</h:panelGroup>

				<h:outputText value="#{msgs.batchStep}" />
				<h:selectBooleanCheckbox value="#{ProzessverwaltungForm.mySchritt.batchStep}" />

				<h:outputText value="#{msgs.stepPlugin}" />

				<h:inputText value="#{ProzessverwaltungForm.mySchritt.stepPlugin}" style="width: 300px;margin-right:15px;" />


				<h:outputText value="#{msgs.validationPlugin}" />
				<h:inputText value="#{ProzessverwaltungForm.mySchritt.validationPlugin}" style="width: 300px;margin-right:15px" />
			</h:panelGrid>

		</htm:td>
	</htm:tr>

	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row3" align="left">
			<h:commandButton value="#{msgs.abbrechen}" immediate="true" action="#{NavigationForm.Reload}">
				<x:updateActionListener property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
			</h:commandButton>
		</htm:td>
		<htm:td styleClass="eingabeBoxen_row3" align="right">
			<h:commandButton value="#{msgs.loeschen}" action="#{ProzessverwaltungForm.SchrittLoeschen}"
				onclick="return confirm('#{msgs.sollDieserEintragWirklichGeloeschtWerden}?')" rendered="#{ProzessverwaltungForm.mySchritt.id != null}" />
			<h:commandButton value="#{msgs.uebernehmen}" id="absenden" action="#{ProzessverwaltungForm.SchrittUebernehmen}">
				<x:updateActionListener property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
			</h:commandButton>
		</htm:td>
	</htm:tr>

</htm:table>
<%-- // Box für die Bearbeitung der Details --%>

<script type="text/javascript">
<!--
	// Funktion, die Änderungen prüft
	function chkManuellAutomatischSetzen(element) {
		//alert(element.id);
		if (element.id != "chkmanuell1")
			document.getElementById("chkmanuell1").checked = false;
		if (element.id != "chkmanuell2")
			document.getElementById("chkmanuell2").checked = false;
		if (element.id != "chkmanuell3")
			document.getElementById("chkmanuell3").checked = false;
		if (element.id != "chkmanuell4")
			document.getElementById("chkmanuell4").checked = false;
		//  		if (element.id != "chkautomatisch") document.getElementById("chkautomatisch").checked=false;
		txtAutomatischAnzeigen();
		//element.checked=true;
	}

	function txtAutomatischAnzeigen() {
		var myelement = document.getElementById("chkautomatisch");
		if (myelement != null) {
			if (document.getElementById("chkautomatisch").checked) {
				document.getElementById("scripttable").style.display = "inline";
			} else {
				document.getElementById("scripttable").style.display = "none";
			}
		}

	}
//-->
</script>