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
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<%--  Granularity selector for multiple process generation --%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<f:view locale="#{SpracheForm.locale}">
    <%@include file="/newpages/inc/head.jsp"%>
    <body>
        <script type="text/javascript">

        <%--
         * The function numberOfPagesValid() validates content of the form field
         * numberOfPages to make sure it consists of digits only.
         *
         * @return whether the title data is valid
         --%>
            function numberOfPagesValid() {
                if (!document.getElementById("form1:numberOfPages").value
                        .match(/^[<h:outputText value="#{SpracheForm.groupingSeparator}"/>0-9]*$/)) {
                    alert("${msgs['granularity.numberOfPages.invalid']}");
                    document.getElementById("form1:numberOfPages").focus();
                    return false;
                }
                return true;
            }

        <%--
         * If a message is provided, the function shows a message box to the user
         * with the message provided an prevents the button from executing. If the
         * message is empty, the function will have no effect.
         *
         * @param message
         *            message to show
         * @return true to cancel the operation
         --%>
            function locked(message) {
                if (message == ""){
                    return false;
                }
                alert(message);
                return true;
            }
        </script>
        <htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable"
            align="center">
            <%@include file="/newpages/inc/tbl_Kopf.jsp"%>
            <htm:tr>
                <%@include file="/newpages/inc/tbl_Navigation.jsp"%>
                <htm:td valign="top" styleClass="layoutInhalt">

                    <%-- ===================== Page main frame ===================== --%>

                    <h:form id="form1" onsubmit="return numberOfPagesValid()">

                        <%-- Bread crumbs --%>

                        <h:panelGrid width="100%" columns="1"
                            styleClass="layoutInhaltKopf">
                            <h:panelGroup>
                                <h:commandLink value="#{msgs.startseite}" action="newMain" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:commandLink value="#{msgs.prozessverwaltung}"
                                    action="ProzessverwaltungAlle" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:commandLink value="#{msgs.einenNeuenProzessAnlegen}"
                                    action="#{ProzesskopieForm.GoToSeite1}" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:commandLink value="#{msgs['calendar.header']}"
                                    action="ShowCalendarEditor" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:outputText value="#{msgs['granularity.header']}" />
                            </h:panelGroup>
                        </h:panelGrid>

                        <htm:table border="0" align="center" width="100%" cellpadding="15">
                            <htm:tr>
                                <htm:td>
                                    <htm:h3>
                                        <h:outputText value="#{msgs['granularity.header']}" />
                                    </htm:h3>

                                    <%-- Global warnings and error messages --%>

                                    <h:messages globalOnly="true" errorClass="text_red"
                                        infoClass="text_blue" showDetail="true" showSummary="true"
                                        tooltip="true" />

                                    <%-- ===================== Page main content ====================== --%>

                                    <htm:div styleClass="leftBox granularityMainCtlWrapper">
                                        <htm:fieldset styleClass="granularityMainCtl">
                                            <htm:legend>
                                                <h:outputText value="#{msgs['granularity.title']} " />
                                            </htm:legend>

                                            <%-- Information on issues & enter pages --%>
                                            <htm:div styleClass="formRow">
                                                <h:outputText value="#{msgs['granularity.issueCount']} " />
                                                <h:outputText value="#{GranularityForm.issueCount}">
                                                    <f:convertNumber />
                                                </h:outputText>
                                            </htm:div>
                                            <htm:div styleClass="formRow">
                                                <h:outputLabel for="numberOfPages"
                                                    value="#{msgs['granularity.numberOfPages']}"
                                                    styleClass="leftText" />
                                                <h:commandLink value="#{msgs['granularity.apply']}"
                                                    id="applyLink" styleClass="rightText" />
                                                <htm:span styleClass="fillWrapper">
                                                    <h:inputText
                                                        value="#{GranularityForm.numberOfPagesPerIssue}"
                                                        id="numberOfPages" onkeydown="showApplyLink();"
                                                        onchange="showApplyLink();" styleClass="filling">
                                                        <f:convertNumber />
                                                    </h:inputText>
                                                </htm:span>
                                            </htm:div>

                                            <%-- Buttons to choose granularity --%>
                                            <h:outputText value="#{msgs['granularity.pick']} " />
                                            <htm:div styleClass="formRow centerRow">
                                                <h:commandButton value="#{msgs['granularity.issues']}"
                                                    action="#{GranularityForm.issuesClick}"
                                                    styleClass="granularityButton #{GranularityForm.granularity=='issues'?'granularityButtonSelected':''}" />
                                                <h:commandButton value="#{msgs['granularity.days']}"
                                                    action="#{GranularityForm.daysClick}"
                                                    styleClass="granularityButton #{GranularityForm.granularity=='days'?'granularityButtonSelected':''}" />
                                                <h:commandButton value="#{msgs['granularity.weeks']}"
                                                    action="#{GranularityForm.weeksClick}"
                                                    styleClass="granularityButton #{GranularityForm.granularity=='weeks'?'granularityButtonSelected':''}" />
                                                <h:commandButton value="#{msgs['granularity.months']}"
                                                    action="#{GranularityForm.monthsClick}"
                                                    styleClass="granularityButton #{GranularityForm.granularity=='months'?'granularityButtonSelected':''}" />
                                                <h:commandButton value="#{msgs['granularity.quarters']}"
                                                    action="#{GranularityForm.quartersClick}"
                                                    styleClass="granularityButton #{GranularityForm.granularity=='quarters'?'granularityButtonSelected':''}" />
                                                <h:commandButton value="#{msgs['granularity.years']}"
                                                    action="#{GranularityForm.yearsClick}"
                                                    styleClass="granularityButton #{GranularityForm.granularity=='years'?'granularityButtonSelected':''}" />
                                            </htm:div>
                                        </htm:fieldset>

                                        <htm:div styleClass="granularityMainCtl">
                                            <h:outputLabel for="batches"
                                                value="#{msgs['granularity.batches.label']}" />
                                            <t:selectOneMenu id="batches"
                                                value="#{GranularityForm.selectedBatchOption}"
                                                styleClass="granularityBatchOption" onchange="submit();">
                                                <f:selectItems value="#{GranularityForm.batchOptions}" />
                                            </t:selectOneMenu>
                                        </htm:div>

                                        <%-- Button to download course of appearance as XML --%>
                                        <h:commandButton value="#{msgs['granularity.download']}"
                                            action="#{GranularityForm.downloadClick}"
                                            onclick="if(locked('#{GranularityForm.lockMessage}')) return false;" />

                                        <%-- Button to create a long running task to create processes --%>
                                        <h:commandButton value="#{msgs['granularity.create']}"
                                            action="#{GranularityForm.createProcessesClick}"
                                            onclick="if(locked('#{GranularityForm.lockMessage}')) return false;" />
                                    </htm:div>

                                    <htm:fieldset>
                                        <htm:legend>
                                            <h:outputText value="#{msgs['granularity.info.legend']} " />
                                        </htm:legend>
                                        <h:outputText
                                            value="granularity.#{GranularityForm.granularity}"
                                            binding="#{requestScope.granularityChoiceKey}"
                                            rendered="false" />
                                        <h:outputText
                                            value="#{msgs[requestScope.granularityChoiceKey.value]}"
                                            styleClass="granularityInfoChoice" />

                                        <h:outputText value="#{GranularityForm.numberOfProcesses}"
                                            binding="#{requestScope.processesFormatted}" rendered="false">
                                            <f:convertNumber />
                                        </h:outputText>
                                        <htm:div styleClass="granularityInfoTextbox"
                                            rendered="#{GranularityForm.numberOfProcesses>0}"
                                            title="#{msgs['granularity.info.textbox.mouseOver']}">
                                            <h:outputText
                                                value="#{msgs['granularity.info.textbox.glyph']}"
                                                styleClass="granularityInfoGlyph redI leftText"
                                                rendered="#{GranularityForm.numberOfPagesOptionallyGuessed/GranularityForm.numberOfProcesses>1000}" />
                                            <h:outputText
                                                value="#{msgs['granularity.info.textbox.glyph']}"
                                                styleClass="granularityInfoGlyph yellowI leftText"
                                                rendered="#{GranularityForm.granularity!='days' and GranularityForm.numberOfPagesOptionallyGuessed/GranularityForm.numberOfProcesses<=1000}" />
                                            <h:outputText
                                                value="#{msgs['granularity.info.textbox.glyph']}"
                                                styleClass="granularityInfoGlyph greenI leftText"
                                                rendered="#{GranularityForm.granularity=='days' and GranularityForm.numberOfPagesOptionallyGuessed/GranularityForm.numberOfProcesses<=1000}" />
                                            <htm:div styleClass="fillWrapper">
                                                <h:outputText
                                                    value="#{msgs['granularity.info.textbox.caption']}"
                                                    styleClass="granularityInfoCaption filling" />
                                                <h:outputFormat
                                                    value="#{msgs['granularity.info.noNumberOfPages']}"
                                                    rendered="#{GranularityForm.numberOfPages == null}"
                                                    styleClass="filling">
                                                    <f:param value="#{requestScope.processesFormatted.value}" />
                                                </h:outputFormat>
                                                <h:outputFormat value="#{msgs['granularity.info.full']}"
                                                    rendered="#{GranularityForm.numberOfPages != null}"
                                                    styleClass="filling">
                                                    <f:param value="#{requestScope.processesFormatted.value}" />
                                                    <f:param value="#{GranularityForm.pagesPerProcessRounded}" />
                                                </h:outputFormat>
                                                <h:outputText
                                                    rendered="#{GranularityForm.numberOfPagesOptionallyGuessed/GranularityForm.numberOfProcesses>1000}"
                                                    value="#{msgs['granularity.info.largeNumberOfImagesWarning']}"
                                                    styleClass="granularityWarning filling" />
                                                <h:outputText
                                                    value="granularity.info.textbox.#{GranularityForm.granularity}"
                                                    binding="#{requestScope.granularityInfoKey}"
                                                    rendered="false" styleClass="filling" />
                                                <h:outputText
                                                    value="#{msgs[requestScope.granularityInfoKey.value]}" />
                                                <h:outputText
                                                    value="granularity.#{GranularityForm.selectedBatchOption}"
                                                    binding="#{requestScope.selectedBatchOptionLabel}"
                                                    rendered="false" />
                                                <h:outputFormat
                                                    value="#{msgs['granularity.info.textbox.batches']}"
                                                    rendered="#{GranularityForm.selectedBatchOption ne 'null'}"
                                                    styleClass="filling">
                                                    <f:param
                                                        value="#{msgs[requestScope.selectedBatchOptionLabel.value]}" />
                                                </h:outputFormat>
                                            </htm:div>
                                        </htm:div>
                                    </htm:fieldset>

                                    <%-- ===================== End page main content ====================== --%>

                                </htm:td>
                            </htm:tr>
                            <htm:tr>
                                <htm:td>
                                    <h:commandButton value="#{msgs.goBack}"
                                        rendered="#{ProzesskopieForm.calendarButtonShowing}"
                                        action="ShowCalendarEditor">
                                    </h:commandButton>
                                </htm:td>
                            </htm:tr>
                        </htm:table>
                    </h:form>

                    <%-- ===================== End page main frame ===================== --%>

                </htm:td>
            </htm:tr>
            <%@include file="/newpages/inc/tbl_Fuss.jsp"%>
        </htm:table>
    </body>
</f:view>
</html>
