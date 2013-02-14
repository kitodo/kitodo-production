<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/dynamic" prefix="jd"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%--
  ~ This file is part of the Goobi Application - a Workflow tool for the support of
  ~ mass digitization.
  ~
  ~ Visit the websites for more information.
  ~     - http://gdz.sub.uni-goettingen.de
  ~     - http://www.goobi.org
  ~     - http://launchpad.net/goobi-production
  ~
  ~ This program is free software; you can redistribute it and/or modify it under
  ~ the terms of the GNU General Public License as published by the Free Software
  ~ Foundation; either version 2 of the License, or (at your option) any later
  ~ version.
  ~
  ~ This program is distributed in the hope that it will be useful, but WITHOUT ANY
  ~ WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  ~ PARTICULAR PURPOSE. See the GNU General Public License for more details. You
  ~ should have received a copy of the GNU General Public License along with this
  ~ program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  ~ Suite 330, Boston, MA 02111-1307 USA
  --%>

<a4j:loadBundle basename="messages" var="msgs" />

<htm:h4>
	<h:outputText value="#{msgs.meineAufgabenMsg}" />
</htm:h4>

<%-- +++++++++++++++++  Anzeigefilter ++++++++++++++++++++++++ --%>
<h:panelGrid width="100%"
	columnClasses="standardTable_Column,standardTable_ColumnRight"
	rowClasses="standardTable_Row_bottom" columns="2">
	<h:outputText
		value="#{msgs.treffer}: #{AktuelleSchritteForm.page.totalResults}" />
	<h:panelGroup>
		<h:outputText value="#{msgs.nurEigeneAufgabenAnzeigen}:" />
		
		<x:selectBooleanCheckbox id="check1" forceId="true"
            value="#{AktuelleSchritteForm.nurEigeneSchritte}"
            onchange="document.getElementById('check2').checked=false; document.getElementById('FilterAlle').click()"
            style="margin-right:40px" />
		
		<h:outputText value="#{msgs.nurOffeneAufgabenAnzeigen}:" />	
		<x:selectBooleanCheckbox id="check2" forceId="true"
			value="#{AktuelleSchritteForm.nurOffeneSchritte}"
			onchange="document.getElementById('check1').checked=false; document.getElementById('FilterAlle').click()"
			style="margin-right:40px" />

		<h:outputText value="#{msgs.filter}: " />

		<h:inputText value="#{AktuelleSchritteForm.filter}" id="filter"
			onkeypress="return submitEnter('FilterAlle',event)" />
		<x:commandButton type="submit" id="FilterAlle" forceId="true"
			style="display:none" action="#{AktuelleSchritteForm.FilterAlleStart}" />

		<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="reload"
			title="#{msgs.filterAnwenden}" style="margin-left:5px">
			<h:graphicImage value="/newpages/images/buttons/reload.gif" />
		</h:commandLink>

        <h:outputLink target="_blank" value="http://wiki.goobi.org/index.php/Filter_f%C3%BCr_Vorg%C3%A4nge">
            <h:graphicImage title="#{msgs.hilfeZumFilter}" value="/newpages/images/buttons/help.png"
                style="margin-left:5px" />
        </h:outputLink>

	</h:panelGroup>
</h:panelGrid>

<%-- +++++++++++++++++  // Anzeigefilter ++++++++++++++++++++++++ --%>

<%-- Datentabelle --%>
<x:dataTable id="auflistung" styleClass="standardTable" width="100%"
	cellspacing="1px" cellpadding="1px" headerClass="standardTable_Header"
	rowClasses="standardTable_Row1,standardTable_Row2,standardTable_Row1,standardTable_Row2"
	var="item" value="#{AktuelleSchritteForm.page.listReload}">

	<%-- +++++++++++++++++  SelectionBoxes ++++++++++++++++++++++++ --%>
	<x:column style="text-align:center"
		rendered="#{AktuelleSchritteForm.anzeigeAnpassen['selectionBoxes']}">
		<f:facet name="header">
			<x:div>
				<%-- Header --%>
				<h:outputText value="#{msgs.auswahl2}" />
				<h:commandLink action="#{AktuelleSchritteForm.SelectionAll}" id="selectAll"
					title="#{msgs.alleAuswaehlen}" style="margin-left:10px">
					<h:graphicImage value="/newpages/images/check_true.gif" />
				</h:commandLink>
				<h:commandLink action="#{AktuelleSchritteForm.SelectionNone}" id="selectnone"
					title="#{msgs.auswahlEntfernen}" style="margin-left:5px">
					<h:graphicImage value="/newpages/images/check_false.gif" />
				</h:commandLink>
			</x:div>
		</f:facet>
		<a4j:commandLink reRender="myself1" id="myself1">
			<h:graphicImage value="/newpages/images/check_true.gif"
				style="margin-right:4px" rendered="#{item.selected}" />
			<h:graphicImage value="/newpages/images/check_false.gif"
				style="margin-right:4px" rendered="#{!item.selected}" />
			<x:updateActionListener value="#{item.selected?false:true}"
				property="#{item.selected}" />
			<a4j:ajaxListener type="org.ajax4jsf.ajax.ForceRender" />
		</a4j:commandLink>
	</x:column>

	<%-- +++++++++++++++++  ProzessID ++++++++++++++++++++++++ --%>
	<x:column style="text-align:center"
		rendered="#{AktuelleSchritteForm.anzeigeAnpassen['processId']}">
		<f:facet name="header">
			<h:outputText value="#{msgs.id}" />
		</f:facet>
		<h:outputText value="#{item.prozess.id}" />
	</x:column>

	<x:column style="text-align:left" rendered="#{true}">
		<f:facet name="header">
			<x:div>
				<%-- Header --%>
				<h:outputText value="#{msgs.arbeitsschritt}" />
				<%-- Sortierung Asc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort1"
					rendered="#{AktuelleSchritteForm.sortierung=='schrittAsc'}">
					<h:graphicImage value="/newpages/images/sorting/asc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="schrittDesc" />
				</h:commandLink>
				<%-- Sortierung Desc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort2"
					rendered="#{AktuelleSchritteForm.sortierung=='schrittDesc'}">
					<h:graphicImage value="/newpages/images/sorting/desc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="schrittAsc" />
				</h:commandLink>
				<%-- Sortierung none --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort3"
					rendered="#{AktuelleSchritteForm.sortierung!='schrittDesc' && AktuelleSchritteForm.sortierung!='schrittAsc'}">
					<h:graphicImage value="/newpages/images/sorting/none.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="schrittAsc" />
				</h:commandLink>
			</x:div>
		</f:facet>

		<a4j:commandLink reRender="auflistungIntern,myself" id="myself"
			style="color:black">
			<h:graphicImage value="/newpages/images/plus.gif"
				style="margin-right:4px" rendered="#{!item.panelAusgeklappt}" />
			<h:graphicImage value="/newpages/images/minus.gif"
				style="margin-right:4px" rendered="#{item.panelAusgeklappt}" />
			<x:updateActionListener value="#{item.panelAusgeklappt?false:true}"
				property="#{item.panelAusgeklappt}" />
			<h:outputText value="#{item.titel}" />
			<a4j:ajaxListener type="org.ajax4jsf.ajax.ForceRender" />
		</a4j:commandLink>

		<h:panelGroup id="auflistungIntern">

			<x:div style="width:90%;margin-top;margin-left:12px;margin-top:5px"
				rendered="#{item.panelAusgeklappt}">
				<%-- Schrittdetails --%>
				<%@include file="Schritte_Liste_DetailsKlein.jsp"%>
			</x:div>
		</h:panelGroup>
	</x:column>

	<x:column style="text-align:left">
		<f:facet name="header">
			<x:div>
				<%-- Header --%>
				<h:outputText value="#{msgs.prozess}" />
				<%-- Sortierung Asc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort6"
					rendered="#{AktuelleSchritteForm.sortierung=='prozessAsc'}">
					<h:graphicImage value="/newpages/images/sorting/asc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="prozessDesc" />
				</h:commandLink>
				<%-- Sortierung Desc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort7"
					rendered="#{AktuelleSchritteForm.sortierung=='prozessDesc'}">
					<h:graphicImage value="/newpages/images/sorting/desc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="prozessAsc" />
				</h:commandLink>
				<%-- Sortierung none --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort8"
					rendered="#{AktuelleSchritteForm.sortierung!='prozessDesc' && AktuelleSchritteForm.sortierung!='prozessAsc'}">
					<h:graphicImage value="/newpages/images/sorting/none.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="prozessAsc" />
				</h:commandLink>
			</x:div>
		</f:facet>
		<h:panelGrid columns="2">
			<%-- Bearbeiten-Schaltknopf: konkrete Prozesse --%>
			<h:commandLink action="ProzessverwaltungBearbeiten" id="edit1"
				title="#{msgs.prozessBearbeiten}">
				<h:graphicImage value="/newpages/images/buttons/goInto.gif"
					style="margin-right:3px" />
				<x:updateActionListener
					property="#{ProzessverwaltungForm.myProzess}"
					value="#{item.prozess}" />
				<x:updateActionListener
					property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
			</h:commandLink>
			<h:outputText value="#{item.prozess.titel}" />
		</h:panelGrid>
	</x:column>

	<x:column style="text-align:center"
		rendered="#{AktuelleSchritteForm.anzeigeAnpassen['processDate']}">
		<f:facet name="header">
			<x:div>
				<%-- Header --%>
				<h:outputText value="#{msgs.vorgangsdatum}" />
				<%-- Sortierung Asc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort10"
					rendered="#{AktuelleSchritteForm.sortierung=='prozessdateAsc'}">
					<h:graphicImage value="/newpages/images/sorting/asc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}"
						value="prozessdateDesc" />
				</h:commandLink>
				<%-- Sortierung Desc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort11"
					rendered="#{AktuelleSchritteForm.sortierung=='prozessdateDesc'}">
					<h:graphicImage value="/newpages/images/sorting/desc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}"
						value="prozessdateAsc" />
				</h:commandLink>
				<%-- Sortierung none --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort12"
					rendered="#{AktuelleSchritteForm.sortierung!='prozessdateDesc' && AktuelleSchritteForm.sortierung!='prozessdateAsc'}">
					<h:graphicImage value="/newpages/images/sorting/none.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}"
						value="prozessdateAsc" />
				</h:commandLink>
			</x:div>
		</f:facet>
		<h:outputText value="#{item.prozess.erstellungsdatum}" />
	</x:column>

	<x:column style="text-align:center"
		rendered="#{AktuelleSchritteForm.anzeigeAnpassen['modules']}">
		<f:facet name="header">
			<x:div>
				<%-- Header --%>
				<h:outputText value="#{msgs.modules}" />
				<%-- Sortierung Asc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort13"
					rendered="#{AktuelleSchritteForm.sortierung=='modulesAsc'}">
					<h:graphicImage value="/newpages/images/sorting/asc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="modulesDesc" />
				</h:commandLink>
				<%-- Sortierung Desc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort14"
					rendered="#{AktuelleSchritteForm.sortierung=='modulesDesc'}">
					<h:graphicImage value="/newpages/images/sorting/desc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="modulesAsc" />
				</h:commandLink>
				<%-- Sortierung none --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort15"
					rendered="#{AktuelleSchritteForm.sortierung!='modulesDesc' && AktuelleSchritteForm.sortierung!='modulesAsc'}">
					<h:graphicImage value="/newpages/images/sorting/none.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="modulesAsc" />
				</h:commandLink>
			</x:div>
		</f:facet>
		<h:outputText value="#{item.typModulName}" />
	</x:column>

	<x:column style="text-align:center">
		<f:facet name="header">
			<x:div>
				<%-- Header --%>
				<h:outputText value="#{msgs.projekt}" />
				<%-- Sortierung Asc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort16"
					rendered="#{AktuelleSchritteForm.sortierung=='projektAsc'}">
					<h:graphicImage value="/newpages/images/sorting/asc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="projektDesc" />
				</h:commandLink>
				<%-- Sortierung Desc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort17"
					rendered="#{AktuelleSchritteForm.sortierung=='projektDesc'}">
					<h:graphicImage value="/newpages/images/sorting/desc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="projektAsc" />
				</h:commandLink>
				<%-- Sortierung none --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort18"
					rendered="#{AktuelleSchritteForm.sortierung!='projektDesc' && AktuelleSchritteForm.sortierung!='projektAsc'}">
					<h:graphicImage value="/newpages/images/sorting/none.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="projektAsc" />
				</h:commandLink>
			</x:div>
		</f:facet>
		<h:outputText value="#{item.prozess.projekt.titel}" />
	</x:column>

	<%-- +++++++++++++++++  Sperrungen ++++++++++++++++++++++++ --%>
	<x:column style="text-align:center"
		rendered="#{AktuelleSchritteForm.anzeigeAnpassen['lockings']}">
		<f:facet name="header">
			<x:div>
				<%-- Header --%>
				<h:outputText value="#{msgs.sperrungen}" />
				<%-- Sortierung Asc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort19"
					rendered="#{AktuelleSchritteForm.sortierung=='sperrungenAsc'}">
					<h:graphicImage value="/newpages/images/sorting/asc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}"
						value="sperrungenDesc" />
				</h:commandLink>
				<%-- Sortierung Desc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort20"
					rendered="#{AktuelleSchritteForm.sortierung=='sperrungenDesc'}">
					<h:graphicImage value="/newpages/images/sorting/desc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}"
						value="sperrungenAsc" />
				</h:commandLink>
				<%-- Sortierung none --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort21"
					rendered="#{AktuelleSchritteForm.sortierung!='sperrungenDesc' && AktuelleSchritteForm.sortierung!='sperrungenAsc'}">
					<h:graphicImage value="/newpages/images/sorting/none.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}"
						value="sperrungenAsc" />
				</h:commandLink>
			</x:div>
		</f:facet>
		<h:outputText value="#{item.benutzerGesperrt.nachVorname}"
			rendered="#{item.prozess.benutzerGesperrt != null}" />
	</x:column>

	<x:column style="text-align:center">
		<f:facet name="header">
			<x:div>
				<%-- Header --%>
				<h:outputText value="#{msgs.status}" />
				<%-- Sortierung Asc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort22"
					rendered="#{AktuelleSchritteForm.sortierung=='statusAsc'}">
					<h:graphicImage value="/newpages/images/sorting/asc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="statusDesc" />
				</h:commandLink>
				<%-- Sortierung Desc --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort23"
					rendered="#{AktuelleSchritteForm.sortierung=='statusDesc'}">
					<h:graphicImage value="/newpages/images/sorting/desc.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="statusAsc" />
				</h:commandLink>
				<%-- Sortierung none --%>
				<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" id="sort24"
					rendered="#{AktuelleSchritteForm.sortierung!='statusDesc' && AktuelleSchritteForm.sortierung!='statusAsc'}">
					<h:graphicImage value="/newpages/images/sorting/none.gif"
						style="vertical-align:middle;margin-left:5px" />
					<x:updateActionListener
						property="#{AktuelleSchritteForm.sortierung}" value="statusAsc" />
				</h:commandLink>
			</x:div>
		</f:facet>
		
		<h:graphicImage value="#{item.bearbeitungsstatusEnum.bigImagePath}"
                title="#{item.bearbeitungsstatusEnum.title}"/>
		
		<h:outputText value="!"
			style="color:red;font-weight:bold;font-size:20px;margin-left:5px"
			rendered="#{item.prioritaet == 1}" />
		<h:outputText value="!!"
			style="color:red;font-weight:bold;font-size:20px;margin-left:5px"
			rendered="#{item.prioritaet == 2}" />
		<h:outputText value="!!!"
			style="color:red;font-weight:bold;font-size:20px;margin-left:5px"
			rendered="#{item.prioritaet == 3}" />

		<%-- Popup --%>
		<x:popup id="popup" closePopupOnExitingElement="true"
			closePopupOnExitingPopup="true" displayAtDistanceX="-400"
			displayAtDistanceY="5" rendered="#{item.prioritaet == 10}">

			<f:facet name="popup">
				<htm:div>
					<h:panelGrid columns="1" width="400"
						style="background-color:#f3ebeb; font-size:11px; border: 1px solid #a24033; padding: 1px;">

						<x:dataTable var="intern" rendered="#{item.eigenschaftenSize!=0}"
							value="#{item.eigenschaftenList}">
							<x:column style="vertical-align: top;">
								<h:outputText value="#{intern.titel}:" />
							</x:column>
							<x:column style="vertical-align: top;">
								<h:outputText value="#{intern.wert}" />
							</x:column>
						</x:dataTable>

					</h:panelGrid>
				</htm:div>
			</f:facet>

			<h:outputText value="#{msgs.ideographCorrection}"
				style="color:red;font-weight:bold;font-size:20px;margin-left:5px" />
		</x:popup>


	</x:column>

	<%-- ===================== Action ====================== --%>
	<x:column style="text-align:center" styleClass="action">
		<f:facet name="header">
			<h:outputText value="#{msgs.auswahl}" styleClass="action" />
		</f:facet>

		<%-- Bearbeitung übernehmen-Schaltknopf --%>
		<h:commandLink id="take"
			action="#{AktuelleSchritteForm.SchrittDurchBenutzerUebernehmen}"
			rendered="#{item.bearbeitungsstatusEnum == 'OPEN'}"
			title="#{msgs.bearbeitungDiesesSchrittsUebernehmen}">
			<h:graphicImage value="/newpages/images/buttons/admin2a.gif" />
			<x:updateActionListener property="#{AktuelleSchritteForm.mySchritt}"
				value="#{item}" />
		</h:commandLink>

		<%-- Bearbeiten-Schaltknopf (eigener Schritt) --%>
		<h:commandLink action="AktuelleSchritteBearbeiten" id="view1"
			rendered="#{item.bearbeitungsstatusEnum == 'INWORK' && item.bearbeitungsbenutzer.id == LoginForm.myBenutzer.id}"
			title="#{msgs.inBearbeitungDurch}: #{item.bearbeitungsbenutzer!=null && item.bearbeitungsbenutzer.id!=0 ? item.bearbeitungsbenutzer.nachVorname:''}">
			<h:graphicImage value="/newpages/images/buttons/admin1b.gif" />
			<x:updateActionListener property="#{AktuelleSchritteForm.mySchritt}"
				value="#{item}" />
		</h:commandLink>

		<%-- Bearbeiten-Schaltknopf (fremder Schritt) --%>
		<h:commandLink action="AktuelleSchritteBearbeiten" id="view2"
			rendered="#{item.bearbeitungsstatusEnum == 'INWORK' && item.bearbeitungsbenutzer.id != LoginForm.myBenutzer.id}"
			title="#{msgs.inBearbeitungDurch}: #{item.bearbeitungsbenutzer!=null && item.bearbeitungsbenutzer.id!=0 ? item.bearbeitungsbenutzer.nachVorname:''}">
			<h:graphicImage value="/newpages/images/buttons/admin3b.gif" />
			<x:updateActionListener property="#{AktuelleSchritteForm.mySchritt}"
				value="#{item}" />
		</h:commandLink>

	</x:column>
	<%-- ===================== // Action ====================== --%>

</x:dataTable>

<%-- ===================== Datascroller für die Ergebnisse ====================== --%>
<x:aliasBean alias="#{mypage}" value="#{AktuelleSchritteForm.page}">
	<jsp:include page="/newpages/inc/datascroller.jsp" />
</x:aliasBean>
<%-- ===================== // Datascroller für die Ergebnisse ====================== --%>

<%-- Schritte auflisten --%>
<%@include file="Schritte_Liste_Action.jsp"%>
