<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/dynamic" prefix="jd"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
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
<%-- ++++++++++++++++     Action      ++++++++++++++++ --%>

<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen" style="margin-top:20px">
	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row1" colspan="2">
			<h:outputText value="#{msgs.moeglicheAktionen}" />
		</htm:td>
	</htm:tr>
	<htm:tr valign="top" rendered="#{LoginForm.myBenutzer.mitMassendownload && AktuelleSchritteForm.page.totalResults > 0}">
		<htm:td styleClass="eingabeBoxen_row2">
			<h:panelGrid columns="1">

				<%-- Upload-Schaltknopf --%>
				<h:commandLink action="#{AktuelleSchritteForm.UploadFromHomeAlle}" title="#{msgs.verzeichnisFertigHochladen}"
					onclick="if (!confirm('#{msgs.upload}?')) return">
					<h:graphicImage value="/newpages/images/buttons/load_up1.gif" style="margin-right:3px;vertical-align:middle" />
					<h:outputText value="#{msgs.verzeichnisFertigHochladen}" />
				</h:commandLink>

				<%-- Download-Schaltknopf für Page--%>
				<h:commandLink action="#{AktuelleSchritteForm.DownloadToHomePage}" title="#{msgs.alleTrefferDieserSeiteInMeinHomeverzeichnis}"
					onclick="if (!confirm('#{msgs.download}?')) return">
					<h:graphicImage value="/newpages/images/buttons/load_down1.gif" style="margin-right:3px;vertical-align:middle" />
					<h:outputText value="#{msgs.alleTrefferDieserSeiteInMeinHomeverzeichnis}" />
				</h:commandLink>

				<%-- Download-Schaltknopf für gesamtes Trefferset --%>
				<h:commandLink action="#{AktuelleSchritteForm.DownloadToHomeHits}" title="#{msgs.gesamtesTreffersetInMeinHomeverzeichnis}"
					onclick="if (!confirm('#{msgs.download}?')) return">
					<h:graphicImage value="/newpages/images/buttons/load_down1.gif" style="margin-right:3px;vertical-align:middle" />
					<h:outputText value="#{msgs.gesamtesTreffersetInMeinHomeverzeichnis}" />
				</h:commandLink>

			</h:panelGrid>
		</htm:td>
		<htm:td styleClass="eingabeBoxen_row2" align="right">
			<h:panelGrid columns="3" styleClass="text_light">
				<%-- Anzahl der Images aller Treffer --%>
				<h:outputText value="#{msgs.anzahlDerImagesAllerTreffer}:" />
				<h:outputText id="calcNumber" value="#{AktuelleSchritteForm.allImages}" />

				<a4j:commandLink reRender="calcNumber" action="#{AktuelleSchritteForm.calcHomeImages}">
					<h:graphicImage value="/newpages/images/reload.gif" style="margin-right:4px" rendered="#{item.selected}" />
				</a4j:commandLink>

			</h:panelGrid>
		</htm:td>
	</htm:tr>

	<htm:tr valign="top">
		<htm:td styleClass="eingabeBoxen_row2" colspan="2">
			<h:panelGrid columns="1">
				<h:panelGroup>
					<jd:hideableController for="changeSearch" id="viewswitcher2" title="#{msgs.filterAnpassen}">
						<h:graphicImage value="/newpages/images/buttons/view3.gif" style="margin-left:5px;margin-right:8px;vertical-align:middle" />
						<h:outputText value="#{msgs.filterAnpassen}" />
					</jd:hideableController>

					<jd:hideableArea id="changeSearch" saveState="view">
						<h:panelGrid columns="2" style="margin-left:40px">


							<h:outputText value="#{msgs.showAutomaticTasks}:" />
							<h:selectBooleanCheckbox id="check3" value="#{AktuelleSchritteForm.showAutomaticTasks}" style="margin-right:40px" />


							<h:outputText value="#{msgs.hideCorrectionTasks}:" />
							<h:selectBooleanCheckbox id="check4" value="#{AktuelleSchritteForm.hideCorrectionTasks}" style="margin-right:40px" />


							<h:outputText id="aslsid5" value="#{msgs.nurEigeneAufgabenAnzeigen}:" />
							<h:selectBooleanCheckbox id="check1" value="#{AktuelleSchritteForm.nurEigeneSchritte}" style="margin-right:40px" />

							<h:outputText id="aslsid6" value="#{msgs.nurOffeneAufgabenAnzeigen}:" />
							<h:selectBooleanCheckbox id="check2" value="#{AktuelleSchritteForm.nurOffeneSchritte}" style="margin-right:40px" />




						</h:panelGrid>
						<h:commandLink action="#{AktuelleSchritteForm.FilterAlleStart}" style="margin-left:44px" title="#{msgs.uebernehmen}">
							<h:outputText value="#{msgs.uebernehmen}" />
						</h:commandLink>
					</jd:hideableArea>
				</h:panelGroup>

				<h:panelGroup id="viewgroup">
					<jd:hideableController for="changeView" id="viewswitcher" title="#{msgs.anzeigeAnpassen}">
						<h:graphicImage value="/newpages/images/buttons/view3.gif" style="margin-left:5px;margin-right:8px;vertical-align:middle" />
						<h:outputText value="#{msgs.anzeigeAnpassen}" />
					</jd:hideableController>

					<jd:hideableArea id="changeView" saveState="view">
						<h:panelGrid columns="2" style="margin-left:40px">
							<h:outputText value="#{msgs.auswahlboxen}" rendered="#{false}" />
							<h:selectBooleanCheckbox rendered="#{false}" value="#{AktuelleSchritteForm.anzeigeAnpassen['selectionBoxes']}" />
							<h:outputText value="#{msgs.id}" />
							<h:selectBooleanCheckbox value="#{AktuelleSchritteForm.anzeigeAnpassen['processId']}" />
							<h:outputText value="#{msgs.batch}" />
							<h:selectBooleanCheckbox value="#{AktuelleSchritteForm.anzeigeAnpassen['batchId']}" />

							<h:outputText value="#{msgs.module}" />
							<h:selectBooleanCheckbox value="#{AktuelleSchritteForm.anzeigeAnpassen['modules']}" />

							<h:outputText value="#{msgs.vorgangsdatum}" />
							<h:selectBooleanCheckbox value="#{AktuelleSchritteForm.anzeigeAnpassen['processDate']}" />

							<h:outputText value="#{msgs.sperrungen}" />
							<h:selectBooleanCheckbox value="#{AktuelleSchritteForm.anzeigeAnpassen['lockings']}" />
						</h:panelGrid>
						<h:commandLink action="#{NavigationForm.Reload}" style="margin-left:44px" title="#{msgs.uebernehmen}">
							<h:outputText value="#{msgs.uebernehmen}" />
						</h:commandLink>
					</jd:hideableArea>
				</h:panelGroup>
			</h:panelGrid>
		</htm:td>
	</htm:tr>


</htm:table>

<%-- ++++++++++++++++     // Action      ++++++++++++++++ --%>
