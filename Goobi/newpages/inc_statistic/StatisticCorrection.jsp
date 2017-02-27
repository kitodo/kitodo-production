<%--
 *
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 *
--%>

<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

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
                    <x:inputCalendar id="from" style="width:110px" value="#{Form.statisticsManager3.sourceDateFrom}" renderAsPopup="true"
                        renderPopupButtonAsImage="true" popupTodayString="#{msgs.heute}" popupWeekString="#{msgs.kw}"
                        styleClass="projekteBearbeiten"
                        imageLocation="/newpages/images/calendarImages"
                        popupButtonImageUrl="/newpages/images/calendarImages/calendar.gif" />
                </h:panelGroup>

                <h:outputLabel id="uaid6" for="to" value="#{msgs.to}" />
                <h:panelGroup id="uaid7">
                    <x:inputCalendar id="to" style="width:110px" value="#{Form.statisticsManager3.sourceDateTo}" renderAsPopup="true" renderPopupButtonAsImage="true"
                        popupTodayString="#{msgs.heute}" popupWeekString="#{msgs.kw}" imageLocation="/newpages/images/calendarImages" styleClass="projekteBearbeiten"
                        popupButtonImageUrl="/newpages/images/calendarImages/calendar.gif" />
                </h:panelGroup>

                <h:outputText id="uaid8" style="font-weight:bold" value=" - #{msgs.orLast} - " />

                <h:inputText id="uaid9" style="width:130px;text-align:right" required="false" value="#{Form.statisticsManager3.sourceNumberOfTimeUnitsAsString}" />

                <h:selectOneMenu id="uaid10" style="width:130px" value="#{Form.statisticsManager3.sourceTimeUnit}" converter="StatisticsTimeUnitConverter">
                    <si:selectItems id="uaid11" value="#{Form.statisticsManager3.allTimeUnits}" var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
                </h:selectOneMenu>

                <h:panelGroup id="uaid12" style="margin-bottom:0px">
                    <h:selectBooleanCheckbox id="uaid13" value="#{Form.statisticsManager3.showAverage}" title="#{msgs.showAverage}">
                    </h:selectBooleanCheckbox>
                    <h:outputLabel for="uaid13" id="uaid14" value="#{msgs.showAverage}" />
                </h:panelGroup>

                <h:outputText id="uaid15" value="#{msgs.einheit}" style="width:150px" />

                <h:selectOneMenu id="uaid16" style="width:130px" value="#{Form.statisticsManager3.targetTimeUnit}" converter="StatisticsTimeUnitConverter">
                    <si:selectItems id="uaid17" value="#{Form.statisticsManager3.allTimeUnits}" var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
                </h:selectOneMenu>

                <h:outputText id="uaid18" value="#{msgs.anzeige}" />

                <h:selectOneMenu id="uaid19" style="width:130px" value="#{Form.statisticsManager3.targetCalculationUnit}"
                    converter="StatisticsCalculationUnitConverter">
                    <si:selectItems id="uaid20" value="#{Form.statisticsManager3.allCalculationUnits}" var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
                </h:selectOneMenu>

                <h:outputText id="uaid21" value="#{msgs.ausgabe}" />

                <h:selectOneMenu id="uaid22" style="width:130px" value="#{Form.statisticsManager3.targetResultOutput}" converter="StatisticsResultOutputConverter">
                    <si:selectItems id="uaid23" value="#{Form.statisticsManager3.allResultOutputs}" var="out" itemLabel="#{out.title}" itemValue="#{out}" />
                </h:selectOneMenu>

                <h:panelGroup id="uaid241" style="margin-bottom:0px">
                    <h:outputText>
                    </h:outputText>
                </h:panelGroup>

                <%-- the following is an alternating couple to make the columns match --%>
                <h:panelGroup id="uaid242" style="margin-bottom:0px" rendered="#{!Form.statisticsManager3.renderLoopOption}">
                    <h:outputText>
                    </h:outputText>
                </h:panelGroup>

                <h:panelGroup id="uaid25" style="margin-bottom:0px" rendered="#{Form.statisticsManager3.renderLoopOption}">
                    <h:selectBooleanCheckbox id="uaid26_2" value="#{Form.statisticsManager3.includeLoops}" title="#{msgs.includeLoops}">
                    </h:selectBooleanCheckbox>
                    <h:outputLabel for="uaid26_2" id="uaid27_2" value="#{msgs.includeLoops}" />
                </h:panelGroup>

            </h:panelGrid>

            <x:commandButton id="myStatisticButton" style="margin:5px" title="#{msgs.calculateStatistics}" value="#{msgs.calculateStatistics} "
                action="#{Form.statisticsManager3.calculate}">
                <x:updateActionListener property="#{Form.showStatistics}" value="true" />
            </x:commandButton>


            <br />

        </htm:td>
    </htm:tr>

</htm:table>


<h:panelGroup rendered="#{Form.statisticsManager3 != null && !Form.statisticsManager3.statisticMode.isSimple && Form.showStatistics}">
    <x:dataList id="uaid29" var="element" rendered="#{Form.statisticsManager3.renderingElements!=null}"
        value="#{Form.statisticsManager3.renderingElements}" layout="ordered list" rowCountVar="rowCount" rowIndexVar="rowIndex">

        <htm:h4 style="margin-top:20px">
            <h:outputText id="uaid30" value="#{element.title}" />
        </htm:h4>

        <h:panelGroup id="uaid33">

            <h:graphicImage style="margin-bottom:15px" url="#{HelperForm.servletPathWithHostAsUrl}/pages/imagesTemp/#{element.imageUrl}" alt="#{element.title}"
                title="#{element.title}"
                rendered="#{element.dataTable.showableInChart && (Form.statisticsManager3.targetResultOutput=='chart' || Form.statisticsManager3.targetResultOutput=='chartAndTable')}" />

            <h:outputText id="uaid34" value="#{element.htmlTableRenderer.rendering}" escape="false"
                rendered="#{element.dataTable.showableInTable && (Form.statisticsManager3.targetResultOutput== 'table' || Form.statisticsManager3.targetResultOutput=='chartAndTable')}" />

            <h:commandLink action="#{Form.CreateExcel}" title="#{msgs.createExcel}"
                rendered="#{element.dataTable.showableInTable && (Form.statisticsManager3.targetResultOutput== 'table' || Form.statisticsManager3.targetResultOutput=='chartAndTable')}">
                <h:graphicImage value="/newpages/images/buttons/excel20.png" />
                <h:outputText value="  #{msgs.createExcel}" />
                <x:updateActionListener value="#{element}" property="#{Form.myCurrentTable}" />
            </h:commandLink>
        </h:panelGroup>
    </x:dataList>

</h:panelGroup>


