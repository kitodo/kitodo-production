<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
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
<%-- ######################################## 

			alle lang laufenden Aufgaben 

	#########################################--%>

<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body>
		<htm:table styleClass="headTable" cellspacing="0" cellpadding="0" style="padding-left:5px;padding-right:5px;margin-top:5px;">
			<%@include file="inc/tbl_Kopf.jsp"%>
		</htm:table>
		<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable"
			align="center">
			<htm:tr>
				<%@include file="inc/tbl_Navigation.jsp"%>
				<htm:td valign="top" styleClass="layoutInhalt">

					<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
					<h:form id="taskmanform">
						<%-- Breadcrumb --%>
						<h:panelGrid id="id0" width="100%" columns="1"
							styleClass="layoutInhaltKopf">
							<h:panelGroup id="id1">
								<h:commandLink id="id2" value="#{msgs.startseite}"
									action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText id="id3" value="#{msgs.langLaufendeAufgaben}" />
							</h:panelGroup>
						</h:panelGrid>

						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>

									<h:panelGrid id="id4" columns="2"
										columnClasses="standardTable_Column,standardTable_ColumnRight"
										width="100%">
										<%-- �?berschrift --%>
										<htm:h3>
											<h:outputText id="id5" value="#{msgs.langLaufendeAufgaben}" />
										</htm:h3>
										<%-- TaskManager start - stop --%>
										<h:panelGroup id="id6">

											<h:commandLink id="id7" value="add SampleTask"
												action="#{LongRunningTasksForm.addNewMasterTask}" />

											<h:commandLink id="id8" action="#{NavigationForm.Reload}"
												style="margin-right:15px">
												<h:graphicImage id="id9"
													value="/newpages/images/icons/reload.png"
													style="margin-right:3px;margin-left:15px;vertical-align:bottom" />
												<h:outputText id="id10" value="#{msgs.listeAktualisieren}" />
											</h:commandLink>

											<h:commandLink
												action="#{LongRunningTasksForm.clearFinishedTasks}"
												style="margin-right:15px">
												<h:graphicImage
													value="/newpages/images/icons/progress_remAll.gif"
													style="margin-right:3px;vertical-align:bottom" />
												<h:outputText
													value="#{msgs.abgeschlosseneTasksAusListeEntfernen}" />
											</h:commandLink>

											<h:commandLink id="id11"
												action="#{LongRunningTasksForm.clearAllTasks}"
												style="margin-right:15px">
												<h:graphicImage
													value="/newpages/images/icons/progress_remAll2.gif"
													style="margin-right:3px;vertical-align:bottom" />
												<h:outputText id="id12"
													value="#{msgs.alleTasksAusListeEntfernen}" />
											</h:commandLink>

											<h:commandLink id="id13"
												action="#{LongRunningTasksForm.toggleRunning}">
												<h:graphicImage id="id14"
													rendered="#{!LongRunningTasksForm.running}"
													value="/newpages/images/icons/start_task.gif"
													style="margin-right:3px;vertical-align:bottom" />
												<h:graphicImage id="id15"
													rendered="#{LongRunningTasksForm.running}"
													value="/newpages/images/icons/stop_task.gif"
													style="margin-right:3px;vertical-align:bottom" />
												<h:outputText id="id16" value="#{msgs.taskManagerIsRunning}"
													rendered="#{LongRunningTasksForm.running}" />
												<h:outputText id="id17" value="#{msgs.taskManagerIsStopped}"
													rendered="#{!LongRunningTasksForm.running}" />
											</h:commandLink>


										</h:panelGroup>
									</h:panelGrid>

									<%-- globale Warn- und Fehlermeldungen --%>
									<h:messages id="id18" globalOnly="true" errorClass="text_red"
										title="Meldungen" layout="table" infoClass="text_blue"
										showDetail="true" showSummary="true" tooltip="true" />

									<a4j:poll id="id19" interval="5001" reRender="taskliste"
										ajaxSingle="true" />

									<%-- Datentabelle --%>
									<x:dataTable id="taskliste" styleClass="standardTable"
										width="100%" cellspacing="1px" cellpadding="1px"
										headerClass="standardTable_Header"
										rowClasses="standardTable_Row1" var="item"
										value="#{LongRunningTasksForm.tasks}">

										<h:column id="id20">
											<f:facet name="header">
												<h:outputText id="id21" value="#{msgs.titel}" />
											</f:facet>
											<h:outputText id="id22" value="#{item.title}" />
										</h:column>

										<x:column id="id23" style="text-align:center">
											<f:facet name="header">
												<h:outputText id="id24" value="#{msgs.fortschritt}" />
											</f:facet>

											<h:graphicImage
												value="/newpages/images/fortschritt/ende_links.gif"
												rendered="true" />
											<h:graphicImage id="id25"
												value="/newpages/images/fortschritt/gr.gif"
												style="width:#{item.statusProgress * 0.8}px;height:10px"
												rendered="#{item.statusProgress!=-1}" />
											<h:graphicImage id="id26"
												value="/newpages/images/fortschritt/ge.gif"
												style="width:#{(100 - item.statusProgress) * 0.8}px;height:10px"
												rendered="#{item.statusProgress!=-1}" />
											<h:graphicImage id="id27"
												value="/newpages/images/fortschritt/rt.gif"
												style="width:#{100 * 0.8}px;height:10px"
												rendered="#{item.statusProgress==-1}" />
											<h:graphicImage
												value="/newpages/images/fortschritt/ende_rechts.gif"
												rendered="true" />
										</x:column>

										<h:column id="id28">
											<f:facet name="header">
												<h:outputText id="id29" value="#{msgs.status}" />
											</f:facet>
											<%-- Popup für lange Fehlermeldungen --%>
											<x:popup id="popup" closePopupOnExitingElement="true"
												closePopupOnExitingPopup="true" displayAtDistanceX="-400"
												displayAtDistanceY="5"
												rendered="#{item.longMessage != null && item.longMessage != ''}">
												<f:facet name="popup">
													<htm:div>
														<h:panelGrid id="id30" columns="1" width="400"
															style="background-color:white; font-size:11px; border: 1px solid red; padding: 1px;">
															<h:outputText id="id31" value="#{item.longMessage}" />
														</h:panelGrid>
													</htm:div>
												</f:facet>
												<h:graphicImage id="id32" style="margin-right:5px"
													value="/newpages/images/icons/exclamation.png" />
											</x:popup>

											<h:outputText id="id33" value="#{item.statusMessage}" />
										</h:column>

										<%-- Action --%>
										<x:column id="id34" style="width:50px">
											<f:facet name="header">
												<h:outputText id="id35" value="#{msgs.auswahl}" />
											</f:facet>

											<%-- nach oben --%>
											<h:commandLink id="id36"
												action="#{LongRunningTasksForm.moveTaskUp}"
												title="#{msgs.start}">
												<h:graphicImage
													value="/newpages/images/buttons/order_up_klein.gif" />
												<x:updateActionListener value="#{item}"
													property="#{LongRunningTasksForm.task}" />
											</h:commandLink>
											<%-- nach unten --%>
											<h:commandLink id="id37"
												action="#{LongRunningTasksForm.moveTaskDown}"
												style="margin-right:5px" title="#{msgs.start}">
												<h:graphicImage
													value="/newpages/images/buttons/order_down_klein.gif" />
												<x:updateActionListener value="#{item}"
													property="#{LongRunningTasksForm.task}" />
											</h:commandLink>

											<h:panelGroup id="id38"
												rendered="#{LongRunningTasksForm.running}">
												<%-- start --%>
												<h:commandLink id="id39"
													action="#{LongRunningTasksForm.executeTask}"
													title="#{msgs.start}" rendered="#{item.statusProgress<=0}">
													<h:graphicImage
														value="/newpages/images/icons/start_task.gif" />
													<x:updateActionListener value="#{item}"
														property="#{LongRunningTasksForm.task}" />
												</h:commandLink>

												<%-- stop --%>
												<h:commandLink id="id40"
													action="#{LongRunningTasksForm.cancelTask}"
													title="#{msgs.stop}"
													rendered="#{item.statusProgress>0 && item.statusProgress<100}">
													<h:graphicImage id="id41"
														value="/newpages/images/icons/stop_task.gif" />
													<x:updateActionListener value="#{item}"
														property="#{LongRunningTasksForm.task}" />
												</h:commandLink>


											</h:panelGroup>
											<%-- löschen --%>
											<h:commandLink id="id42"
												action="#{LongRunningTasksForm.removeTask}"
												title="#{msgs.loeschen}"
												rendered="#{item.statusProgress==100 || item.statusProgress<1}">
												<h:graphicImage
													value="/newpages/images/icons/progress_rem.gif" />
												<x:updateActionListener value="#{item}"
													property="#{LongRunningTasksForm.task}" />
											</h:commandLink>
										</x:column>
										<%-- // Action --%>
									</x:dataTable>

								</htm:td>
							</htm:tr>
						</htm:table>
					</h:form>
					<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

				</htm:td>
			</htm:tr>
			<%@include file="inc/tbl_Fuss.jsp"%>
		</htm:table>
	</body>
</f:view>

</html>