<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp" prefix="c"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi"
	prefix="si"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%-- ++++++++++++++++     old jfree-Statistic-Charts   ++++++++++++++++ --%>

<htm:h4
	rendered="#{ProzessverwaltungForm.statisticsManager != null && ProzessverwaltungForm.showStatistics}"
	style="margin-top:20px">
	<h:outputText value="#{msgs.statistischeAuswertung}: " />
	<h:outputText
		value="#{ProzessverwaltungForm.statisticsManager.statisticMode.title}" />
</htm:h4>

<c:chart id="myPie2"
	rendered="#{ProzessverwaltungForm.statisticsManager != null  && ProzessverwaltungForm.showStatistics && ProzessverwaltungForm.statisticsManager.statisticMode.isSimple && (ProzessverwaltungForm.statisticsManager.statisticMode=='STATUS_VOLUMES' || ProzessverwaltungForm.statisticsManager.statisticMode=='USERGROUPS'  || ProzessverwaltungForm.statisticsManager.statisticMode=='PROJECTS')}"
	datasource="#{ProzessverwaltungForm.statisticsManager.jfreeDataset}"
	is3d="true" width="700" height="500" type="pie" depth="9"
	startAngle="240" alpha="70" antialias="true" background="#fafcfe" />

<c:chart id="myStack"
	rendered="#{ProzessverwaltungForm.statisticsManager != null  && ProzessverwaltungForm.showStatistics && ProzessverwaltungForm.statisticsManager.statisticMode.isSimple && (ProzessverwaltungForm.statisticsManager.statisticMode=='SIMPLE_RUNTIME_STEPS')}"
	datasource="#{ProzessverwaltungForm.statisticsManager.jfreeDataset}"
	type="stackedbar" width="700"
	height="#{ProzessverwaltungForm.myDatasetHoeheInt}" is3d="false"
	orientation="horizontal" xlabel="#{msgs.prozesse}"
	ylabel="#{msgs.days}" />

<%-- ++++++++++++++++     // old jfree-Statistic-Charts   ++++++++++++++++ --%>


<%-- ++++++++++++++++     Action      ++++++++++++++++ --%>
<htm:table cellpadding="3" cellspacing="0" width="100%"
	styleClass="eingabeBoxen"
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

				<h:outputLabel for="from" value="#{msgs.zeitraum} #{msgs.von}"
					style="width:150px" />
				<h:panelGroup>
					<x:inputCalendar id="from" style="width:110px"
						value="#{ProzessverwaltungForm.statisticsManager.sourceDateFrom}"
						renderAsPopup="true" renderPopupButtonAsImage="true"
						popupTodayString="#{msgs.heute}" popupWeekString="#{msgs.kw}" />
				</h:panelGroup>

				<h:outputLabel for="to" value="#{msgs.bis}" />
				<h:panelGroup>
					<x:inputCalendar id="to" style="width:110px"
						value="#{ProzessverwaltungForm.statisticsManager.sourceDateTo}"
						renderAsPopup="true" renderPopupButtonAsImage="true"
						popupTodayString="#{msgs.heute}" popupWeekString="#{msgs.kw}" />
				</h:panelGroup>

				<h:outputText style="font-weight:bold" value=" - #{msgs.orLast} - " />

				<h:inputText style="width:130px;text-align:right" required="false"
					value="#{ProzessverwaltungForm.statisticsManager.sourceNumberOfTimeUnitsAsString}" />

				<h:selectOneMenu style="width:130px"
					value="#{ProzessverwaltungForm.statisticsManager.sourceTimeUnit}"
					converter="StatisticsTimeUnitConverter">
					<si:selectItems
						value="#{ProzessverwaltungForm.statisticsManager.allTimeUnits}"
						var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
				</h:selectOneMenu>

				<h:panelGroup style="margin-bottom:0px">
					<h:selectBooleanCheckbox
						value="#{ProzessverwaltungForm.statisticsManager.showAverage}"
						title="#{msgs.showAverage}">
					</h:selectBooleanCheckbox>
					<h:outputLabel value="#{msgs.showAverage}" />
				</h:panelGroup>

				<h:outputText value="#{msgs.einheit}" style="width:150px" />

				<h:selectOneMenu style="width:130px"
					value="#{ProzessverwaltungForm.statisticsManager.targetTimeUnit}"
					converter="StatisticsTimeUnitConverter">
					<si:selectItems
						value="#{ProzessverwaltungForm.statisticsManager.allTimeUnits}"
						var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
				</h:selectOneMenu>

				<h:outputText value="#{msgs.anzeige}" />

				<h:selectOneMenu style="width:130px"
					value="#{ProzessverwaltungForm.statisticsManager.targetCalculationUnit}"
					converter="StatisticsCalculationUnitConverter">
					<si:selectItems
						value="#{ProzessverwaltungForm.statisticsManager.allCalculationUnits}"
						var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
				</h:selectOneMenu>

				<h:outputText value="#{msgs.ausgabe}" />

				<h:selectOneMenu style="width:130px"
					value="#{ProzessverwaltungForm.statisticsManager.targetResultOutput}"
					converter="StatisticsResultOutputConverter">
					<si:selectItems
						value="#{ProzessverwaltungForm.statisticsManager.allResultOutputs}"
						var="out" itemLabel="#{out.title}" itemValue="#{out}" />
				</h:selectOneMenu>

				<h:panelGroup style="margin-bottom:0px">
				</h:panelGroup>

				<h:panelGroup style="margin-bottom:0px"
					rendered="#{ProzessverwaltungForm.statisticsManager.renderLoopOption}">
					<h:selectBooleanCheckbox
						value="#{ProzessverwaltungForm.statisticsManager.includeLoops}"
						title="#{msgs.includeLoops}">
					</h:selectBooleanCheckbox>
					<h:outputLabel value="#{msgs.includeLoops}" />
				</h:panelGroup>

			</h:panelGrid>

			<x:commandButton id="myStatisticButton" forceId="true"
				style="margin:5px" title="#{msgs.calculateStatistics}"
				value="#{msgs.calculateStatistics}"
				action="#{ProzessverwaltungForm.statisticsManager.calculate}">
				<x:updateActionListener
					property="#{ProzessverwaltungForm.showStatistics}" value="true" />
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
	<x:dataList var="element"
		rendered="#{ProzessverwaltungForm.statisticsManager.renderingElements!=null}"
		value="#{ProzessverwaltungForm.statisticsManager.renderingElements}"
		layout="ordered list" rowCountVar="rowCount" rowIndexVar="rowIndex">

		<htm:h4 style="margin-top:20px">
			<h:outputText value="#{element.title}" />
		</htm:h4>

<%--
		<h:outputText value="#{msgs.noStatisticalResultsFound}"
			style="color:red" rendered="#{datatable.dataRowsSize==0}" />
			
			rendered="#{datatable.dataRowsSize>0}"

<a4j:mediaOutput element="img" cacheable="false" value="#{rowIndex}" style="margin-bottom:10px"
                rendered="#{ProzessverwaltungForm.statisticsManager.myHtmlRenderer[rowIndex].dataTable.showableInChart && (ProzessverwaltungForm.statisticsManager.targetResultOutput=='chart' || ProzessverwaltungForm.statisticsManager.targetResultOutput=='chartAndTable')}"
                createContent="#{ProzessverwaltungForm.statisticsManager.renderAsChart}"
                mimeType="image/png" />

--%>
		<h:panelGroup>

			<htm:img style="margin-bottom:15px"
				src="#{HelperForm.servletPathWithHostAsUrl}/pages/imagesTemp/#{element.imageUrl}"
				title="#{element.title}"
				rendered="#{element.dataTable.showableInChart && (ProzessverwaltungForm.statisticsManager.targetResultOutput=='chart' || ProzessverwaltungForm.statisticsManager.targetResultOutput=='chartAndTable')}" />

			<h:outputText value="#{element.htmlTableRenderer.rendering}"
				escape="false"
				rendered="#{element.dataTable.showableInTable && (ProzessverwaltungForm.statisticsManager.targetResultOutput== 'table' || ProzessverwaltungForm.statisticsManager.targetResultOutput=='chartAndTable')}" />
		</h:panelGroup>
	</x:dataList>

</h:panelGroup>

<%-- ++++++++++++++++     // Presentation of Data      ++++++++++++++++ --%>

<%-- hide statistics, if any action happens
for old simple statistics: hide immediatly
for the new enhanced statistics: hide it only, when some data where calculated




<h:outputText value="#{ProzessverwaltungForm.resetStatistic}"
    rendered="#{ProzessverwaltungForm.statisticsManager != null && (ProzessverwaltungForm.statisticsManager.statisticMode.isSimple || (!ProzessverwaltungForm.statisticsManager.statisticMode.isSimple && ProzessverwaltungForm.statisticsManager.myDataTables!=null))}" />
    --%>


<h:outputText value="#{ProzessverwaltungForm.resetStatistic}"
	rendered="#{ProzessverwaltungForm.statisticsManager.statisticMode.isSimple}" />