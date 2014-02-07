<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp" prefix="c"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
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
<%-- ++++++++++++++++     old jfree-Statistic-Charts   ++++++++++++++++ --%>

<htm:h4 rendered="#{ProzessverwaltungForm.statisticsManager != null && ProzessverwaltungForm.showStatistics}" style="margin-top:20px">
	<h:outputText value="#{msgs.statistischeAuswertung}: " />
	<h:outputText value="#{ProzessverwaltungForm.statisticsManager.statisticMode.title}" />
</htm:h4>

<c:chart id="myPie2"
	rendered="#{ProzessverwaltungForm.statisticsManager != null  && ProzessverwaltungForm.showStatistics && ProzessverwaltungForm.statisticsManager.statisticMode.isSimple && (ProzessverwaltungForm.statisticsManager.statisticMode=='STATUS_VOLUMES' || ProzessverwaltungForm.statisticsManager.statisticMode=='USERGROUPS'  || ProzessverwaltungForm.statisticsManager.statisticMode=='PROJECTS')}"
	datasource="#{ProzessverwaltungForm.statisticsManager.jfreeDataset}" is3d="true" width="700" height="500" type="pie" depth="9" startAngle="240"
	alpha="70" antialias="true" background="#fafcfe" />

<c:chart id="myStack"
	rendered="#{ProzessverwaltungForm.statisticsManager != null  && ProzessverwaltungForm.showStatistics && ProzessverwaltungForm.statisticsManager.statisticMode.isSimple && (ProzessverwaltungForm.statisticsManager.statisticMode=='SIMPLE_RUNTIME_STEPS')}"
	datasource="#{ProzessverwaltungForm.statisticsManager.jfreeDataset}" type="stackedbar" width="700"
	height="#{ProzessverwaltungForm.myDatasetHoeheInt}" is3d="false" orientation="horizontal" xlabel="#{msgs.prozesse}" ylabel="#{msgs.days}" />



<%-- ++++++++++++++++     // old jfree-Statistic-Charts   ++++++++++++++++ --%>


<%-- ++++++++++++++++     Action      ++++++++++++++++ --%>
<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen"
	rendered="#{ProzessverwaltungForm.statisticsManager != null && ProzessverwaltungForm.statisticsManager.statisticMode.restrictedDate && ProzessverwaltungForm.showStatistics}">
	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row1">
			<h:outputText value="#{msgs.defineStatisticalQuestion}" />
		</htm:td>
	</htm:tr>
	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row2">

			<h:panelGrid columns="8"
				columnClasses="standardTable_ColumnRight,standardTable_Column,standardTable_ColumnRight,standardTable_Column,standardTable_ColumnRight,standardTable_Column, standardTable_Column">

				<h:outputLabel for="from" value="#{msgs.zeitraum} #{msgs.von}" style="width:150px" />
				<h:panelGroup>
					<x:inputCalendar id="from" style="width:110px" value="#{ProzessverwaltungForm.statisticsManager.sourceDateFrom}" renderAsPopup="true"
						renderPopupButtonAsImage="true" popupTodayString="#{msgs.heute}" popupWeekString="#{msgs.kw}" 
						imageLocation="/newpages/images/calendarImages"
						popupButtonImageUrl="/newpages/images/calendarImages/calendar.gif" 
						styleClass="projekteBearbeiten"
						/>
				</h:panelGroup>

				<h:outputLabel for="to" value="#{msgs.to}" />
				<h:panelGroup>
					<x:inputCalendar id="to" style="width:110px" value="#{ProzessverwaltungForm.statisticsManager.sourceDateTo}" renderAsPopup="true"
						renderPopupButtonAsImage="true" popupTodayString="#{msgs.heute}" popupWeekString="#{msgs.kw}" 
						imageLocation="/newpages/images/calendarImages"
						popupButtonImageUrl="/newpages/images/calendarImages/calendar.gif" 
						styleClass="projekteBearbeiten"
						/>
				</h:panelGroup>

				<h:outputText style="font-weight:bold" value=" - #{msgs.orLast} - " />

				<h:inputText style="width:130px;text-align:right" required="false"
					value="#{ProzessverwaltungForm.statisticsManager.sourceNumberOfTimeUnitsAsString}" />

				<h:selectOneMenu style="width:130px" value="#{ProzessverwaltungForm.statisticsManager.sourceTimeUnit}" converter="StatisticsTimeUnitConverter">
					<si:selectItems value="#{ProzessverwaltungForm.statisticsManager.allTimeUnits}" var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
				</h:selectOneMenu>

				<h:panelGroup style="margin-bottom:0px">
					<h:selectBooleanCheckbox value="#{ProzessverwaltungForm.statisticsManager.showAverage}" title="#{msgs.showAverage}">
					</h:selectBooleanCheckbox>
					<h:outputLabel value="#{msgs.showAverage}" />
				</h:panelGroup>

				<h:outputText value="#{msgs.einheit}" style="width:150px" />

				<h:selectOneMenu style="width:130px" value="#{ProzessverwaltungForm.statisticsManager.targetTimeUnit}" converter="StatisticsTimeUnitConverter">
					<si:selectItems value="#{ProzessverwaltungForm.statisticsManager.allTimeUnits}" var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
				</h:selectOneMenu>

				<h:outputText value="#{msgs.anzeige}" />

				<h:selectOneMenu style="width:130px" value="#{ProzessverwaltungForm.statisticsManager.targetCalculationUnit}"
					converter="StatisticsCalculationUnitConverter">
					<si:selectItems value="#{ProzessverwaltungForm.statisticsManager.allCalculationUnits}" var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
				</h:selectOneMenu>

				<h:outputText value="#{msgs.ausgabe}" />

				<h:selectOneMenu style="width:130px" value="#{ProzessverwaltungForm.statisticsManager.targetResultOutput}"
					converter="StatisticsResultOutputConverter">
					<si:selectItems value="#{ProzessverwaltungForm.statisticsManager.allResultOutputs}" var="out" itemLabel="#{out.title}" itemValue="#{out}" />
				</h:selectOneMenu>

				<h:panelGroup style="margin-bottom:0px">
				</h:panelGroup>

				<h:panelGroup style="margin-bottom:0px" rendered="#{ProzessverwaltungForm.statisticsManager.renderLoopOption}">
					<h:selectBooleanCheckbox value="#{ProzessverwaltungForm.statisticsManager.includeLoops}" title="#{msgs.includeLoops}">
					</h:selectBooleanCheckbox>
					<h:outputLabel value="#{msgs.includeLoops}" />
				</h:panelGroup>

			</h:panelGrid>

			<x:commandButton id="myStatisticButton" forceId="true" style="margin:5px" title="#{msgs.calculateStatistics}" value="#{msgs.calculateStatistics}"
				action="#{ProzessverwaltungForm.statisticsManager.calculate}">
				<x:updateActionListener property="#{ProzessverwaltungForm.showStatistics}" value="true" />
			</x:commandButton>
			<x:message for="myStatisticButton" style="color: red" />

			<htm:br />

		</htm:td>
	</htm:tr>

</htm:table>

<%-- ++++++++++++++++     // Action      ++++++++++++++++ --%>

<%-- ++++++++++++++++     Presentation of Data      ++++++++++++++++ --%>
<h:panelGroup
	rendered="#{ProzessverwaltungForm.statisticsManager != null && !ProzessverwaltungForm.statisticsManager.statisticMode.isSimple && ProzessverwaltungForm.showStatistics}">
	<x:dataList var="element" rendered="#{ProzessverwaltungForm.statisticsManager.renderingElements!=null}"
		value="#{ProzessverwaltungForm.statisticsManager.renderingElements}" layout="ordered list" rowCountVar="rowCount" rowIndexVar="rowIndex">

		<h:panelGroup rendered="#{element.dataTable.showableInChart && ProzessverwaltungForm.statisticsManager.targetResultOutput=='chart'}">
			<htm:h4 style="margin-top:20px">
				<h:outputText value="#{element.title}" />
			</htm:h4>
			<htm:img style="margin-bottom:15px" src="#{HelperForm.servletPathWithHostAsUrl}/pages/imagesTemp/#{element.imageUrl}" title="#{element.title}"
				rendered="#{element.dataTable.showableInChart && ProzessverwaltungForm.statisticsManager.targetResultOutput=='chart'}" />
		</h:panelGroup>


		<h:panelGroup rendered="#{element.dataTable.showableInTable && ProzessverwaltungForm.statisticsManager.targetResultOutput== 'table'}">
			<htm:h4 style="margin-top:20px">
				<h:outputText value="#{element.title}" />
			</htm:h4>
			<h:outputText value="#{element.htmlTableRenderer.rendering}" escape="false" />
			<h:commandLink action="#{ProzessverwaltungForm.CreateExcel}" title="#{msgs.createExcel}">
				<h:graphicImage value="/newpages/images/buttons/excel20.png" />
				<h:outputText value="  #{msgs.createExcel}" />
				<x:updateActionListener value="#{element}" property="#{ProzessverwaltungForm.myCurrentTable}" />
			</h:commandLink>
		</h:panelGroup>

		<h:panelGroup rendered="#{ProzessverwaltungForm.statisticsManager.targetResultOutput=='chartAndTable'}">
			<htm:h4 style="margin-top:20px">
				<h:outputText value="#{element.title}" />
			</htm:h4>
			<h:panelGroup>
				<htm:img style="margin-bottom:15px" src="#{HelperForm.servletPathWithHostAsUrl}/pages/imagesTemp/#{element.imageUrl}" title="#{element.title}"
					rendered="#{element.dataTable.showableInChart}" />
				<h:outputText value="#{element.htmlTableRenderer.rendering}" escape="false" rendered="#{element.dataTable.showableInTable}" />
				<h:commandLink action="#{ProzessverwaltungForm.CreateExcel}" title="#{msgs.createExcel}" rendered="#{element.dataTable.showableInTable}">
					<h:graphicImage value="/newpages/images/buttons/excel20.png" />
					<h:outputText value="  #{msgs.createExcel}" />
					<x:updateActionListener value="#{element}" property="#{ProzessverwaltungForm.myCurrentTable}" />
				</h:commandLink>
			</h:panelGroup>
		</h:panelGroup>
	</x:dataList>





</h:panelGroup>

<%-- ++++++++++++++++     // Presentation of Data      ++++++++++++++++ --%>

<%-- hide statistics, if any action happens
for old simple statistics: hide immediately
for the new enhanced statistics: hide it only, when some data where calculated




<h:outputText value="#{ProzessverwaltungForm.resetStatistic}"
    rendered="#{ProzessverwaltungForm.statisticsManager != null && (ProzessverwaltungForm.statisticsManager.statisticMode.isSimple || (!ProzessverwaltungForm.statisticsManager.statisticMode.isSimple && ProzessverwaltungForm.statisticsManager.myDataTables!=null))}" />
    --%>


<h:outputText value="#{ProzessverwaltungForm.resetStatistic}" rendered="#{ProzessverwaltungForm.statisticsManager.statisticMode.isSimple}" />