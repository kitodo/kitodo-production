<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/dynamic" prefix="jd"%>
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
<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen" style="margin-top:20px"
	rendered="#{ProzessverwaltungForm.modusAnzeige=='aktuell' && (LoginForm.maximaleBerechtigung == 1 || LoginForm.maximaleBerechtigung == 2)}">
	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row1">
			<h:outputText value="#{msgs.moeglicheAktionen}" />
		</htm:td>
	</htm:tr>
	<htm:tr >
		<htm:td styleClass="eingabeBoxen_row2">
			<h:panelGrid columns="1">

				<%-- Upload-Schaltknopf --%>
				<h:commandLink rendered="#{LoginForm.myBenutzer.mitMassendownload && ProzessverwaltungForm.page.totalResults > 0 }" id="action1" action="#{ProzessverwaltungForm.UploadFromHomeAlle}"
					title="#{msgs.verzeichnisFertigAusHomeverzeichnisEntfernen}" onclick="if (!confirm('#{msgs.upload}?')) return">
					<h:graphicImage value="/newpages/images/buttons/load_up_set_20px.gif" style="margin-left:0px;margin-right:0px;vertical-align:middle" />
					<h:outputText value="#{msgs.verzeichnisFertigAusHomeverzeichnisEntfernen}" />
				</h:commandLink>

				<h:panelGroup rendered="#{LoginForm.myBenutzer.mitMassendownload && ProzessverwaltungForm.modusAnzeige=='aktuell' && ProzessverwaltungForm.page.totalResults > 0 }">
					<jd:hideableController for="download" id="downloadswitcher" title="#{msgs.downloadInMeinHomeverzeichnis}">
						<h:graphicImage value="/newpages/images/buttons/load_down_set_20px.gif" style="margin-left:0px;margin-right:0px;vertical-align:middle" />
						<h:outputText value="#{msgs.imHomeVerzeichnisVerlinken}" />
					</jd:hideableController>

					<jd:hideableArea id="download" saveState="view" >
						<h:panelGrid columns="1" style="margin-left:40px">

							<%-- Download-Schaltknopf für Selection--%>
							<h:commandLink id="action2" action="#{ProzessverwaltungForm.DownloadToHomeSelection}"
								rendered="#{ProzessverwaltungForm.anzeigeAnpassen['selectionBoxes']}" title="#{msgs.auswahl2}"
								onclick="if (!confirm('#{msgs.download}?')) return">
								<h:outputText value="- #{msgs.auswahl2}" />
							</h:commandLink>

							<%-- Download-Schaltknopf für Page--%>
							<h:commandLink id="action3" action="#{ProzessverwaltungForm.DownloadToHomePage}" title="#{msgs.trefferDieserSeite}"
								onclick="if (!confirm('#{msgs.download}?')) return">
								<h:outputText value="- #{msgs.trefferDieserSeite}" />
							</h:commandLink>

							<%-- Download-Schaltknopf für gesamtes Trefferset --%>
							<h:commandLink id="action4" action="#{ProzessverwaltungForm.DownloadToHomeHits}" title="#{msgs.gesamtesTrefferset}"
								onclick="if (!confirm('#{msgs.download}?')) return">
								<h:outputText value="- #{msgs.gesamtesTrefferset}" />
							</h:commandLink>

						</h:panelGrid>
					</jd:hideableArea>
				</h:panelGroup>

				<h:panelGroup rendered="#{LoginForm.maximaleBerechtigung == 1 && ProzessverwaltungForm.modusAnzeige=='aktuell' && ProzessverwaltungForm.page.totalResults > 0 }">
					<jd:hideableController for="agoradownload" id="agoraswitcher" title="#{msgs.metadatenFuerDMSExportieren}">
						<h:graphicImage value="/newpages/images/buttons/dms.png" style="margin-left:0px;margin-right:0px;vertical-align:middle" />
						<h:outputText value="#{msgs.metadatenFuerDMSExportieren}" />
					</jd:hideableController>

					<jd:hideableArea id="agoradownload" saveState="view">
						<h:panelGrid columns="1" style="margin-left:40px">
							<%-- TODO: delete this warning once the root cause of the timeout problem is solved  --%>
							<h:outputText style="back-color:blue; color: red; font-weight: bold;" value="#{msgs.timeoutWarningDMS}" />

							<%-- Download-Schaltknopf für Selection--%>
							<h:commandLink id="action6" action="#{ProzessverwaltungForm.ExportDMSSelection}"
								rendered="#{ProzessverwaltungForm.anzeigeAnpassen['selectionBoxes']}" title="#{msgs.auswahl2}"
								onclick="if (!confirm('#{msgs.download}?')) return">
								<h:outputText value="- #{msgs.auswahl2}" />
							</h:commandLink>

							<%-- Download-Schaltknopf für Page--%>
							<h:commandLink action="#{ProzessverwaltungForm.ExportDMSPage}" title="#{msgs.trefferDieserSeite}" id="action7"
								onclick="if (!confirm('#{msgs.download}?')) return">
								<h:outputText value="- #{msgs.trefferDieserSeite}" />
							</h:commandLink>

							<%-- Download-Schaltknopf für gesamtes Trefferset --%>
							<h:commandLink action="#{ProzessverwaltungForm.ExportDMSHits}" title="#{msgs.gesamtesTrefferset}" id="action8"
								onclick="if (!confirm('#{msgs.download}?')) return">
								<h:outputText value="- #{msgs.gesamtesTrefferset}" />
							</h:commandLink>

						</h:panelGrid>
					</jd:hideableArea>
				</h:panelGroup>

				<%-- Bearbeitungsstatus hochsetzen--%>
				<h:panelGroup rendered="#{ProzessverwaltungForm.modusAnzeige=='aktuell'  && ProzessverwaltungForm.page.totalResults > 0 }">
					<jd:hideableController for="statusUp" id="statusswitcher" title="#{msgs.bearbeitungsstatusHochsetzen}">
						<h:graphicImage value="/newpages/images/buttons/step_for_20px.gif" style="margin-left:0px;margin-right:0px;vertical-align:middle" />
						<h:outputText value="#{msgs.bearbeitungsstatusHochsetzen}" />
					</jd:hideableController>

					<jd:hideableArea id="statusUp" saveState="view">
						<h:panelGrid columns="1" style="margin-left:40px">
							<%-- Download-Schaltknopf für Selection--%>
							<h:commandLink id="action9" action="#{ProzessverwaltungForm.BearbeitungsstatusHochsetzenSelection}"
								rendered="#{ProzessverwaltungForm.anzeigeAnpassen['selectionBoxes']}" title="#{msgs.auswahl2}"
								onclick="if (!confirm('#{msgs.bearbeitungsstatusHochsetzen}?')) return">
								<h:outputText value="- #{msgs.auswahl2}" />
							</h:commandLink>

							<%-- Download-Schaltknopf für Page--%>
							<h:commandLink action="#{ProzessverwaltungForm.BearbeitungsstatusHochsetzenPage}" title="#{msgs.trefferDieserSeite}" id="action10"
								onclick="if (!confirm('#{msgs.bearbeitungsstatusHochsetzen}?')) return">
								<h:outputText value="- #{msgs.trefferDieserSeite}" />
							</h:commandLink>

							<%-- Download-Schaltknopf für gesamtes Trefferset --%>
							<h:commandLink id="action11" action="#{ProzessverwaltungForm.BearbeitungsstatusHochsetzenHits}" title="#{msgs.gesamtesTrefferset}"
								onclick="if (!confirm('#{msgs.bearbeitungsstatusHochsetzen}?')) return">
								<h:outputText value="- #{msgs.gesamtesTrefferset}" />
							</h:commandLink>

						</h:panelGrid>
					</jd:hideableArea>
				</h:panelGroup>

				<h:panelGroup rendered="#{ProzessverwaltungForm.modusAnzeige=='aktuell' && ProzessverwaltungForm.page.totalResults > 0 }">
					<jd:hideableController for="statusDown" id="downswitcher" title="#{msgs.bearbeitungsstatusRuntersetzen}">
						<h:graphicImage value="/newpages/images/buttons/step_back_20px.gif" style="margin-left:0px;margin-right:0px;vertical-align:middle" />
						<h:outputText value="#{msgs.bearbeitungsstatusRuntersetzen}" />
					</jd:hideableController>

					<jd:hideableArea id="statusDown" saveState="view">
						<h:panelGrid columns="1" style="margin-left:40px">

							<%-- Download-Schaltknopf für Selection--%>
							<h:commandLink id="action12" action="#{ProzessverwaltungForm.BearbeitungsstatusRuntersetzenSelection}"
								rendered="#{ProzessverwaltungForm.anzeigeAnpassen['selectionBoxes']}" title="#{msgs.auswahl2}"
								onclick="if (!confirm('#{msgs.bearbeitungsstatusRuntersetzen}?')) return">
								<h:outputText value="- #{msgs.auswahl2}" />
							</h:commandLink>

							<%-- Download-Schaltknopf für Page--%>
							<h:commandLink id="action13" action="#{ProzessverwaltungForm.BearbeitungsstatusRuntersetzenPage}" title="#{msgs.trefferDieserSeite}"
								onclick="if (!confirm('#{msgs.bearbeitungsstatusRuntersetzen}?')) return">
								<h:outputText value="- #{msgs.trefferDieserSeite}" />
							</h:commandLink>

							<%-- Download-Schaltknopf für gesamtes Trefferset --%>
							<h:commandLink id="action14" action="#{ProzessverwaltungForm.BearbeitungsstatusRuntersetzenHits}" title="#{msgs.gesamtesTrefferset}"
								onclick="if (!confirm('#{msgs.bearbeitungsstatusRuntersetzen}?')) return">
								<h:outputText value="- #{msgs.gesamtesTrefferset}" />
							</h:commandLink>

						</h:panelGrid>
					</jd:hideableArea>
				</h:panelGroup>

				<h:panelGroup rendered="#{LoginForm.maximaleBerechtigung == 1 && ProzessverwaltungForm.modusAnzeige=='aktuell'  && ProzessverwaltungForm.page.totalResults > 0 }">
					<jd:hideableController for="goobiScript" id="scriptswitcher" title="#{msgs.goobiScriptAusfuehren}">
						<h:graphicImage value="/newpages/images/buttons/admin4b.gif" style="margin-left:0px;margin-right:0px;vertical-align:middle" />
						<h:outputText value="#{msgs.goobiScriptAusfuehren}" />
					</jd:hideableController>

					<jd:hideableArea id="goobiScript" saveState="view">
						<h:panelGrid columns="1" style="margin-left:40px">

							<h:message for="goobiScriptfield" errorClass="text_red" infoClass="text_blue" showDetail="true" showSummary="true" tooltip="true" />

							<htm:table>
								<htm:tr>
									<htm:td colspan="3">
										<h:outputText styleClass="goobiScriptLink" value="addUser"
											onclick="document.getElementById('goobiScriptfield').value='action:addUser \"steptitle:TITLE_STEP\" username:USER_NAME'" />
										<h:outputText styleClass="goobiScriptLink" value="addUserGroup"
											onclick="document.getElementById('goobiScriptfield').value='action:addUserGroup \"steptitle:TITLE_STEP\" group:GROUP_NAME'" />
										<h:outputText styleClass="goobiScriptLink" value="deleteTiffHeaderFile"
											onclick="document.getElementById('goobiScriptfield').value='action:deleteTiffHeaderFile'" />
										<h:outputText styleClass="goobiScriptLink" value="swapSteps"
											onclick="document.getElementById('goobiScriptfield').value='action:swapSteps swap1nr:ORDER_NUMBER_FIRST_STEP \"swap1title:TITLE_FIRST_STEP\"swap2nr:ORDER_NUMBER_SECOND_STEP \"swap2title:TITLE_SECOND_STEP\"'" />
										<h:outputText styleClass="goobiScriptLink" value="importFromFileSystem"
											onclick="document.getElementById('goobiScriptfield').value='action:importFromFileSystem sourcefolder:SOURCE_FOLDER'" />
										<h:outputText styleClass="goobiScriptLink" value="swapProzessesOut"
											onclick="document.getElementById('goobiScriptfield').value='action:swapProzessesOut'" />
										<h:outputText styleClass="goobiScriptLink" value="setRuleset"
											onclick="document.getElementById('goobiScriptfield').value='action:setRuleset \"ruleset:TITLE_RULESET\"'" />
									</htm:td>
								</htm:tr>
								<htm:tr>
									<htm:td colspan="3">
										<h:outputText styleClass="goobiScriptLink" value="swapProzessesIn"
											onclick="document.getElementById('goobiScriptfield').value='action:swapProzessesIn'" />
										<h:outputText styleClass="goobiScriptLink" value="deleteStep"
											onclick="document.getElementById('goobiScriptfield').value='action:deleteStep \"steptitle:TITLE_STEP\"'" />
										<h:outputText styleClass="goobiScriptLink" value="addStep"
											onclick="document.getElementById('goobiScriptfield').value='action:addStep \"steptitle:TITLE_STEP\" number:NUMBER_1_TO_?'" />
										<h:outputText styleClass="goobiScriptLink" value="setStepStatus"
											onclick="document.getElementById('goobiScriptfield').value='action:setStepStatus \"steptitle:TITLE_STEP\" status:NUMBER_0_TO_3'" />
										<h:outputText styleClass="goobiScriptLink" value="setStepNumber"
											onclick="document.getElementById('goobiScriptfield').value='action:setStepNumber \"steptitle:TITLE_STEP\" number:NUMBER_1_TO_?'" />
										<h:outputText styleClass="goobiScriptLink" value="addModuleToStep"
											onclick="document.getElementById('goobiScriptfield').value='action:addModuleToStep \"steptitle:TITLE_STEP\" \"module:MODULE_NAME\"'" />
										<h:outputText styleClass="goobiScriptLink" value="addShellScriptToStep"
											onclick="document.getElementById('goobiScriptfield').value='action:addShellScriptToStep \"steptitle:TITLE_STEP\" \"label:LABEL_FOR_SCRIPT\" \"script:PATH_TO_SCRIPT\"'" />
										<h:outputText styleClass="goobiScriptLink" value="setTaskProperty"
											onclick="document.getElementById('goobiScriptfield').value='action:setTaskProperty \"steptitle:TITLE_STEP\" property:metadata_readimages_writeimages_validate_exportdms_automatic_batch value:true_OR_false'" />
										<h:outputText styleClass="goobiScriptLink" value="tiffWriter"
											onclick="document.getElementById('goobiScriptfield').value='action:tiffWriter'" />
										 <h:outputText styleClass="goobiScriptLink" value="exportDms"
                                            onclick="document.getElementById('goobiScriptfield').value='action:exportDms exportImages:false'" />
									</htm:td>
								</htm:tr>
								<htm:tr>
									<htm:td colspan="3">
										<x:inputTextarea id="goobiScriptfield" forceId="true" style="width:450px;height:100px" value="#{ProzessverwaltungForm.goobiScript}" />
									</htm:td>
								</htm:tr>
								<htm:tr>
									<htm:td>
										<%-- GoobiScript für selektierte Treffer der Seite --%>
										<h:commandLink id="script1" rendered="#{ProzessverwaltungForm.anzeigeAnpassen['selectionBoxes']}"
											action="#{ProzessverwaltungForm.GoobiScriptSelection}" title="#{msgs.auswahl2}">
											<h:outputText value="#{msgs.auswahl2}" />
										</h:commandLink>
									</htm:td>

									<htm:td>
										<%-- GoobiScript für Treffer der Seite --%>
										<h:commandLink id="script2" action="#{ProzessverwaltungForm.GoobiScriptPage}" title="#{msgs.trefferDieserSeite}">
											<h:outputText value="#{msgs.trefferDieserSeite}" />
										</h:commandLink>
									</htm:td>

									<htm:td align="right">
										<%-- GoobiScript für alle Treffer --%>
										<h:commandLink id="script3" action="#{ProzessverwaltungForm.GoobiScriptHits}" title="#{msgs.gesamtesTrefferset}"
											onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return">
											<h:outputText value="#{msgs.gesamtesTrefferset}" />
										</h:commandLink>
									</htm:td>
								</htm:tr>
							</htm:table>
						</h:panelGrid>
					</jd:hideableArea>
				</h:panelGroup>

				<h:panelGroup rendered="#{ProzessverwaltungForm.modusAnzeige=='aktuell' && ProzessverwaltungForm.page.totalResults > 0 }">
					<h:graphicImage value="/newpages/images/buttons/excel20.png" style="margin-left:5px;margin-right:8px;vertical-align:middle" />
					<h:commandLink action="#{ProzessverwaltungForm.generateResult}" title="#{msgs.createExcel}">
						<h:outputText value="#{msgs.createExcel}" />
					</h:commandLink>
				</h:panelGroup>

				<h:panelGroup rendered="#{ProzessverwaltungForm.modusAnzeige=='aktuell' && ProzessverwaltungForm.page.totalResults > 0 }">
					<h:graphicImage value="/newpages/images/buttons/pdf.png" style="margin-left:0px;margin-right:0px;vertical-align:middle" />
					<h:commandLink action="#{ProzessverwaltungForm.generateResultAsPdf}" title="#{msgs.createPdf}">
						<h:outputText value="#{msgs.createPdf}" />
					</h:commandLink>
				</h:panelGroup>


				<h:panelGroup rendered="#{ProzessverwaltungForm.modusAnzeige=='aktuell' && ProzessverwaltungForm.page.totalResults > 0 }">
					<jd:hideableController for="anzahlen" id="countswitcher" title="#{msgs.anzahlMetadatenUndImagesErmitteln}">
						<h:graphicImage value="/newpages/images/buttons/statistik1_20px.gif" style="margin-left:0px;margin-right:0px;vertical-align:middle" />
						<h:outputText value="#{msgs.anzahlMetadatenUndImagesErmitteln}" />
					</jd:hideableController>

					<jd:hideableArea id="anzahlen" saveState="view">
						<h:panelGrid columns="1" style="margin-left:40px">
							<%-- Statistische Auswertung-Schaltknopf für Page--%>
							<h:commandLink id="action20" action="#{ProzessverwaltungForm.CalcMetadataAndImagesSelection}"
								rendered="#{ProzessverwaltungForm.anzeigeAnpassen['selectionBoxes']}" title="#{msgs.auswahl2}">
								<h:outputText value="- #{msgs.auswahl2}" />
							</h:commandLink>

							<%-- Statistische Auswertung-Schaltknopf für Page--%>
							<h:commandLink id="action21" action="#{ProzessverwaltungForm.CalcMetadataAndImagesPage}" title="#{msgs.trefferDieserSeite}">
								<h:outputText value="- #{msgs.trefferDieserSeite}" />
							</h:commandLink>

							<%-- Statistische Auswertung-Schaltknopf für gesamtes Trefferset --%>
							<h:commandLink id="action22" action="#{ProzessverwaltungForm.CalcMetadataAndImagesHits}" title="#{msgs.gesamtesTrefferset}"
								onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return">
								<h:outputText value="- #{msgs.gesamtesTrefferset}" />
							</h:commandLink>
						</h:panelGrid>
					</jd:hideableArea>
				</h:panelGroup>

				<h:panelGroup rendered="#{ProzessverwaltungForm.modusAnzeige=='aktuell' && ProzessverwaltungForm.page.totalResults > 0 }">
					<jd:hideableController for="statistik" id="statswitcher" title="#{msgs.statistischeAuswertung}">
						<h:graphicImage value="/newpages/images/buttons/statistik4_20px.gif" style="margin-left:0px;margin-right:0px;vertical-align:middle" />
						<h:outputText value="#{msgs.statistischeAuswertung}" />
					</jd:hideableController>

					<jd:hideableArea id="statistik" saveState="view">
						<h:panelGrid columns="1" style="margin-left:40px">
							<%-- StatisticsStatusVolumes--%>
							<h:commandLink id="action30" rendered="#{!HelperForm.anonymized}" action="#{ProzessverwaltungForm.StatisticsStatusVolumes}"
								title="#{msgs.statusOfVolumes}">
								<h:outputText value="- #{msgs.statusOfVolumes}" />
								<x:updateActionListener property="#{ProzessverwaltungForm.showStatistics}" value="true" />
							</h:commandLink>

							<%-- StatisticsUsergroups --%>
							<h:commandLink id="action31" rendered="#{!HelperForm.anonymized}" action="#{ProzessverwaltungForm.StatisticsUsergroups}"
								title="#{msgs.statusForUsers}">
								<h:outputText value="- #{msgs.statusForUsers}" />
								<x:updateActionListener property="#{ProzessverwaltungForm.showStatistics}" value="true" />
							</h:commandLink>

							<%-- StatisticsRuntimeSteps --%>
							<h:commandLink id="action32" rendered="#{!HelperForm.anonymized}" action="#{ProzessverwaltungForm.StatisticsRuntimeSteps}"
								title="#{msgs.runtimeOfSteps}">
								<h:outputText value="- #{msgs.runtimeOfSteps}" />
								<x:updateActionListener property="#{ProzessverwaltungForm.showStatistics}" value="true" />
							</h:commandLink>

							<%-- StatisticsProduction --%>
							<h:commandLink id="action33" rendered="#{!HelperForm.anonymized}" action="#{ProzessverwaltungForm.StatisticsProduction}"
								title="#{msgs.productionStatistics}">
								<h:outputText value="- #{msgs.productionStatistics}" />
								<x:updateActionListener property="#{ProzessverwaltungForm.showStatistics}" value="true" />
							</h:commandLink>

							<%-- StatisticsTroughput --%>
							<h:commandLink id="action34" rendered="#{!HelperForm.anonymized}" action="#{ProzessverwaltungForm.StatisticsTroughput}"
								title="#{msgs.productionThroughput}">
								<h:outputText value="- #{msgs.productionThroughput}" />
								<x:updateActionListener property="#{ProzessverwaltungForm.showStatistics}" value="true" />
							</h:commandLink>

							<%-- StatisticsStorage --%>
							<h:commandLink id="action35" action="#{ProzessverwaltungForm.StatisticsStorage}" title="#{msgs.storageCalculator}">
								<h:outputText value="- #{msgs.storageCalculator}" />
								<x:updateActionListener property="#{ProzessverwaltungForm.showStatistics}" value="true" />
							</h:commandLink>

							<%-- StatisticsCorrection --%>
							<h:commandLink id="action36" rendered="#{!HelperForm.anonymized}" action="#{ProzessverwaltungForm.StatisticsCorrection}"
								title="#{msgs.errorTracking}">
								<h:outputText value="- #{msgs.errorTracking}" />
								<x:updateActionListener property="#{ProzessverwaltungForm.showStatistics}" value="true" />
							</h:commandLink>

							<%-- ProjectAssociationss --%>
							<h:commandLink id="action37" action="#{ProzessverwaltungForm.StatisticsProject}" title="#{msgs.projectAssociation}">
								<h:outputText value="- #{msgs.projectAssociation}" />
								<x:updateActionListener property="#{ProzessverwaltungForm.showStatistics}" value="true" />
							</h:commandLink>

						</h:panelGrid>
					</jd:hideableArea>
				</h:panelGroup>


				<h:panelGroup>
					<jd:hideableController for="changeSearch" id="changeswitcher2" title="#{msgs.filterAnpassen}">
						<h:graphicImage value="/newpages/images/buttons/view3.gif" style="margin-left:5px;margin-right:8px;vertical-align:middle" />
						<h:outputText value="#{msgs.filterAnpassen}" />
					</jd:hideableController>

					<jd:hideableArea id="changeSearch" saveState="view">
						<h:panelGrid columns="2" style="margin-left:40px">

							<h:outputText value="#{msgs.showArchivedProjects}:" rendered="#{(LoginForm.maximaleBerechtigung == 1)}" />
							<h:selectBooleanCheckbox value="#{ProzessverwaltungForm.showArchivedProjects}" rendered="#{(LoginForm.maximaleBerechtigung == 1)}" />

							<h:outputText value="#{msgs.showClosedProcesses}:" />
							<h:selectBooleanCheckbox value="#{ProzessverwaltungForm.showClosedProcesses}" />
						</h:panelGrid>
						<h:commandLink action="#{ProzessverwaltungForm.FilterAlleStart}" style="margin-left:44px" title="#{msgs.uebernehmen}">
							<h:outputText value="#{msgs.uebernehmen}" />
						</h:commandLink>
					</jd:hideableArea>

				</h:panelGroup>


				<h:panelGroup>



					<jd:hideableController for="changeView" id="changeswitcher" title="#{msgs.anzeigeAnpassen}">
						<h:graphicImage value="/newpages/images/buttons/view3.gif" style="margin-left:5px;margin-right:8px;vertical-align:middle" />
						<h:outputText value="#{msgs.anzeigeAnpassen}" />
					</jd:hideableController>

					<jd:hideableArea id="changeView" saveState="view">
						<h:panelGrid columns="2" style="margin-left:40px">
							<h:outputText value="#{msgs.auswahlboxen}" />
							<h:selectBooleanCheckbox id="check1" value="#{ProzessverwaltungForm.anzeigeAnpassen['selectionBoxes']}" />
							<h:outputText value="#{msgs.id}" />
							<h:selectBooleanCheckbox id="check2" value="#{ProzessverwaltungForm.anzeigeAnpassen['processId']}" />
							<h:outputText value="#{msgs.batch}" />
							<h:selectBooleanCheckbox id="check2a" value="#{ProzessverwaltungForm.anzeigeAnpassen['batchId']}" />
							<h:outputText value="#{msgs.vorgangsdatum}" />
							<h:selectBooleanCheckbox id="check3" value="#{ProzessverwaltungForm.anzeigeAnpassen['processDate']}" />
							<h:outputText value="#{msgs.sperrungen}" />
							<h:selectBooleanCheckbox id="check4" value="#{ProzessverwaltungForm.anzeigeAnpassen['lockings']}" />
							<h:outputText value="#{msgs.ausgelagerung}" />
							<h:selectBooleanCheckbox id="check5" value="#{ProzessverwaltungForm.anzeigeAnpassen['swappedOut']}" />
						</h:panelGrid>
						<h:commandLink action="#{NavigationForm.Reload}" id="reloadcheck" style="margin-left:44px" title="#{msgs.uebernehmen}">
							<h:outputText value="#{msgs.uebernehmen}" />
						</h:commandLink>
					</jd:hideableArea>
				</h:panelGroup>




			</h:panelGrid>
		</htm:td>
	</htm:tr>

</htm:table>

<%-- ++++++++++++++++     // Action      ++++++++++++++++ --%>
