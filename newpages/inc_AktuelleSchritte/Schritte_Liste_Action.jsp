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

<%-- ++++++++++++++++     Action      ++++++++++++++++ --%>
<htm:table cellpadding="3" cellspacing="0" width="100%"
	styleClass="eingabeBoxen" style="margin-top:20px"
	rendered="#{AktuelleSchritteForm.page.totalResults > 0}">
	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row1" colspan="2">
			<h:outputText value="#{msgs.moeglicheAktionen}" />
		</htm:td>
	</htm:tr>
	<htm:tr valign="top"
		rendered="#{LoginForm.myBenutzer.mitMassendownload}">
		<htm:td styleClass="eingabeBoxen_row2">
			<h:panelGrid columns="1">

				<%-- Upload-Schaltknopf --%>
				<h:commandLink action="#{AktuelleSchritteForm.UploadFromHomeAlle}"
					title="#{msgs.verzeichnisFertigHochladen}"
					onclick="if (!confirm('#{msgs.upload}?')) return">
					<h:graphicImage value="/newpages/images/buttons/load_up1.gif"
						style="margin-right:3px;vertical-align:middle" />
					<h:outputText value="#{msgs.verzeichnisFertigHochladen}" />
				</h:commandLink>

				<%-- Download-Schaltknopf für Page--%>
				<h:commandLink action="#{AktuelleSchritteForm.DownloadToHomePage}"
					title="#{msgs.alleTrefferDieserSeiteInMeinHomeverzeichnis}"
					onclick="if (!confirm('#{msgs.download}?')) return">
					<h:graphicImage value="/newpages/images/buttons/load_down1.gif"
						style="margin-right:3px;vertical-align:middle" />
					<h:outputText
						value="#{msgs.alleTrefferDieserSeiteInMeinHomeverzeichnis}" />
				</h:commandLink>

				<%-- Download-Schaltknopf für gesamtes Trefferset --%>
				<h:commandLink action="#{AktuelleSchritteForm.DownloadToHomeHits}"
					title="#{msgs.gesamtesTreffersetInMeinHomeverzeichnis}"
					onclick="if (!confirm('#{msgs.download}?')) return">
					<h:graphicImage value="/newpages/images/buttons/load_down1.gif"
						style="margin-right:3px;vertical-align:middle" />
					<h:outputText
						value="#{msgs.gesamtesTreffersetInMeinHomeverzeichnis}" />
				</h:commandLink>

			</h:panelGrid>
		</htm:td>
		<htm:td styleClass="eingabeBoxen_row2" align="right">
			<h:panelGrid columns="3" styleClass="text_light">
				<%-- Anzahl der Images aller Treffer --%>
				<h:outputText value="#{msgs.anzahlDerImagesAllerTreffer}:" />
				<h:outputText id="calcNumber" value="#{AktuelleSchritteForm.allImages}" />

				<a4j:commandLink reRender="calcNumber" action="#{AktuelleSchritteForm.calcHomeImages}">
					<h:graphicImage value="/newpages/images/reload.gif"
						style="margin-right:4px" rendered="#{item.selected}" />
				</a4j:commandLink>

			</h:panelGrid>
		</htm:td>
	</htm:tr>

	<htm:tr valign="top">
		<htm:td styleClass="eingabeBoxen_row2" colspan="2">

			<h:panelGroup id="viewgroup">
				<jd:hideableController for="changeView" id="viewswitcher"
					title="#{msgs.anzeigeAnpassen}">
					<h:graphicImage value="/newpages/images/buttons/view3.gif"
						style="margin-left:5px;margin-right:8px;vertical-align:middle" />
					<h:outputText value="#{msgs.anzeigeAnpassen}" />
				</jd:hideableController>

				<jd:hideableArea id="changeView" saveState="view">
					<h:panelGrid columns="2" style="margin-left:40px">
						<h:outputText value="#{msgs.auswahlboxen}" rendered="#{false}" />
						<h:selectBooleanCheckbox rendered="#{false}"
							value="#{AktuelleSchritteForm.anzeigeAnpassen['selectionBoxes']}" />
						<h:outputText value="#{msgs.id}" />
						<h:selectBooleanCheckbox
							value="#{AktuelleSchritteForm.anzeigeAnpassen['processId']}" />

						<h:outputText value="#{msgs.module}" />
						<h:selectBooleanCheckbox
							value="#{AktuelleSchritteForm.anzeigeAnpassen['modules']}" />

						<h:outputText value="#{msgs.vorgangsdatum}" />
						<h:selectBooleanCheckbox
							value="#{AktuelleSchritteForm.anzeigeAnpassen['processDate']}" />

						<h:outputText value="#{msgs.sperrungen}" />
						<h:selectBooleanCheckbox
							value="#{AktuelleSchritteForm.anzeigeAnpassen['lockings']}" />
					</h:panelGrid>
					<h:commandLink action="#{NavigationForm.Reload}"
						style="margin-left:44px" title="#{msgs.uebernehmen}">
						<h:outputText value="#{msgs.uebernehmen}" />
					</h:commandLink>
				</jd:hideableArea>
			</h:panelGroup>

		</htm:td>
	</htm:tr>


</htm:table>

<%-- ++++++++++++++++     // Action      ++++++++++++++++ --%>
