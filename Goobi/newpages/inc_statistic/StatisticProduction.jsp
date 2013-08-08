<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
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

<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen">
	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row1">
			<h:outputText id="uaid2" value="#{msgs.defineStatisticalQuestion}" />
		</htm:td>
	</htm:tr>
	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row2">

			<h:panelGrid id="uaid3" columns="8"
				columnClasses="standardTable_ColumnRight,standardTable_Column,standardTable_ColumnRight,standardTable_Column,standardTable_ColumnRight,standardTable_Column, standardTable_Column">

				<h:outputLabel id="uaid4" for="from" value="#{msgs.zeitraum} #{msgs.from}" style="width:150px" />
				<h:panelGroup id="uaid5">
					<x:inputCalendar id="from" style="width:110px" value="#{Form.statisticsManager1.sourceDateFrom}" renderAsPopup="true"
						renderPopupButtonAsImage="true" popupTodayString="#{msgs.heute}" popupWeekString="#{msgs.kw}" styleClass="projekteBearbeiten"
						popupButtonImageUrl="/newpages/images/calendarImages/calendar.gif" imageLocation="/newpages/images/calendarImages" />
				</h:panelGroup>

				<h:outputLabel id="uaid6" for="to" value="#{msgs.to}" />
				<h:panelGroup id="uaid7">
					<x:inputCalendar id="to" style="width:110px" value="#{Form.statisticsManager1.sourceDateTo}" renderAsPopup="true" renderPopupButtonAsImage="true"
						popupTodayString="#{msgs.heute}" popupWeekString="#{msgs.kw}" imageLocation="/newpages/images/calendarImages" styleClass="projekteBearbeiten"
						popupButtonImageUrl="/newpages/images/calendarImages/calendar.gif" />
				</h:panelGroup>

				<h:outputText id="uaid8" style="font-weight:bold" value=" - #{msgs.orLast} - " />

				<h:inputText id="uaid9" style="width:130px;text-align:right" required="false" value="#{Form.statisticsManager1.sourceNumberOfTimeUnitsAsString}" />

				<h:selectOneMenu id="uaid10" style="width:130px" value="#{Form.statisticsManager1.sourceTimeUnit}" converter="StatisticsTimeUnitConverter">
					<si:selectItems id="uaid11" value="#{Form.statisticsManager1.allTimeUnits}" var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
				</h:selectOneMenu>

				<h:panelGroup id="uaid12" style="margin-bottom:0px">
					<h:selectBooleanCheckbox id="uaid13" value="#{Form.statisticsManager1.showAverage}" title="#{msgs.showAverage}">
					</h:selectBooleanCheckbox>
					<h:outputLabel for="uaid13" id="uaid14" value="#{msgs.showAverage}" />
				</h:panelGroup>

				<h:outputText id="uaid15" value="#{msgs.einheit}" style="width:150px" />

				<h:selectOneMenu id="uaid16" style="width:130px" value="#{Form.statisticsManager1.targetTimeUnit}" converter="StatisticsTimeUnitConverter">
					<si:selectItems id="uaid17" value="#{Form.statisticsManager1.allTimeUnits}" var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
				</h:selectOneMenu>

				<h:outputText id="uaid18" value="#{msgs.anzeige}" />

				<h:selectOneMenu id="uaid19" style="width:130px" value="#{Form.statisticsManager1.targetCalculationUnit}"
					converter="StatisticsCalculationUnitConverter">
					<si:selectItems id="uaid20" value="#{Form.statisticsManager1.allCalculationUnits}" var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
				</h:selectOneMenu>

				<h:outputText id="uaid21" value="#{msgs.ausgabe}" />

				<h:selectOneMenu id="uaid22" style="width:130px" value="#{Form.statisticsManager1.targetResultOutput}" converter="StatisticsResultOutputConverter">
					<si:selectItems id="uaid23" value="#{Form.statisticsManager1.allResultOutputs}" var="out" itemLabel="#{out.title}" itemValue="#{out}" />
				</h:selectOneMenu>

				<h:panelGroup id="uaid241" style="margin-bottom:0px">
					<h:outputText>
					</h:outputText>
				</h:panelGroup>

				<%-- the following is an alternating couple to make the columns match --%>
				<h:panelGroup id="uaid242" style="margin-bottom:0px" rendered="#{!Form.statisticsManager1.renderLoopOption}">
					<h:outputText>
					</h:outputText>
				</h:panelGroup>

				<h:panelGroup id="uaid25" style="margin-bottom:0px" rendered="#{Form.statisticsManager1.renderLoopOption}">
					<h:selectBooleanCheckbox id="uaid26_2" value="#{Form.statisticsManager1.includeLoops}" title="#{msgs.includeLoops}">
					</h:selectBooleanCheckbox>
					<h:outputLabel for="uaid26_2" id="uaid27_2" value="#{msgs.includeLoops}" />
				</h:panelGroup>

			</h:panelGrid>

			<x:commandButton id="myStatisticButton" style="margin:5px" title="#{msgs.calculateStatistics}" value="#{msgs.calculateStatistics} "
				action="#{Form.statisticsManager1.calculate}">
				<x:updateActionListener property="#{Form.showStatistics}" value="true" />
			</x:commandButton>


			<br />

		</htm:td>
	</htm:tr>

</htm:table>


<h:panelGroup rendered="#{Form.statisticsManager1 != null && !Form.statisticsManager1.statisticMode.isSimple && Form.showStatistics}">
	<x:dataList id="uaid29" var="element" rendered="#{Form.statisticsManager1.renderingElements!=null}"
		value="#{Form.statisticsManager1.renderingElements}" layout="ordered list" rowCountVar="rowCount" rowIndexVar="rowIndex">

		<htm:h4 style="margin-top:20px">
			<h:outputText id="uaid30" value="#{element.title}" />
		</htm:h4>

		<h:panelGroup id="uaid33">

			<h:graphicImage style="margin-bottom:15px" url="#{HelperForm.servletPathWithHostAsUrl}/pages/imagesTemp/#{element.imageUrl}" alt="#{element.title}"
				title="#{element.title}"
				rendered="#{element.dataTable.showableInChart && (Form.statisticsManager1.targetResultOutput=='chart' || Form.statisticsManager1.targetResultOutput=='chartAndTable')}" />

			<h:outputText id="uaid34" value="#{element.htmlTableRenderer.rendering}" escape="false"
				rendered="#{element.dataTable.showableInTable && (Form.statisticsManager1.targetResultOutput== 'table' || Form.statisticsManager1.targetResultOutput=='chartAndTable')}" />
			
			<h:commandLink action="#{Form.CreateExcel}" title="#{msgs.createExcel}" rendered="#{element.dataTable.showableInTable && (Form.statisticsManager1.targetResultOutput== 'table' || Form.statisticsManager1.targetResultOutput=='chartAndTable')}" >
				<h:graphicImage value="/newpages/images/buttons/excel20.png" />
				<h:outputText value="  #{msgs.createExcel}" />
				<x:updateActionListener value="#{element}" property="#{Form.myCurrentTable}" />
			</h:commandLink>
			
		</h:panelGroup>
	</x:dataList>

</h:panelGroup>


