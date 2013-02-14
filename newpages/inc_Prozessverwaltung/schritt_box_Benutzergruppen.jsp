<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp"%>

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

<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- ++++++++++     Benutzergruppenberechtigungentabelle      +++++++++++++++ --%>
<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>

<htm:h4 style="margin-top:15">
	<h:outputText value="#{msgs.benutzergruppen}" />
</htm:h4>


<x:dataTable id="benutzergruppen" styleClass="standardTable"
	width="100%" cellspacing="1px" cellpadding="1px"
	headerClass="standardTable_Header" rowClasses="standardTable_Row1"
	columnClasses="standardTable_Column,standardTable_ColumnCentered"
	var="item"
	value="#{ProzessverwaltungForm.mySchritt.benutzergruppenList}">

	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.titel}" />
		</f:facet>
		<h:outputText value="#{item.titel}" />
	</h:column>
	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.loeschen}" />
		</f:facet>
		<%-- Löschen-Schaltknopf --%>
		<h:commandLink
			action="#{ProzessverwaltungForm.BenutzergruppeLoeschen}"
			title="#{msgs.berechtigungLoeschen}">
			<h:graphicImage value="/newpages/images/buttons/waste1a_20px.gif" />
			<x:updateActionListener
				property="#{ProzessverwaltungForm.myBenutzergruppe}" value="#{item}" />
		</h:commandLink>
	</h:column>
</x:dataTable>

<%-- Neu-Schaltknopf --%>
<h:panelGroup>
	<%-- Benutzergruppen mittels IFrame zuweisen --%>
	<jp:popupFrame scrolling="auto" height="380px" width="430px"
		topStyle="background: #1874CD;" bottomStyleClass="popup_unten"
		styleFrame="border-style: solid;border-color: #1874CD; border-width: 2px;"
		styleClass="standardlink"
		style="margin-top:2px;display:block; text-decoration:none"
		actionOpen="#{BenutzergruppenForm.FilterKeinMitZurueck}"
		actionClose="#{NavigationForm.Reload}" center="true"
		title="#{msgs.benutzergruppen}" immediate="true">
		<x:updateActionListener property="#{BenutzergruppenForm.zurueck}"
			value="BerechtigungBenutzergruppenPopup" />
		<h:outputText style="border-bottom: #a24033 dashed 1px;"
			value="#{msgs.benutzergruppenHinzufuegen}" />
	</jp:popupFrame>
</h:panelGroup>
