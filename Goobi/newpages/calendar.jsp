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
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi"
    prefix="si"%>

<%--  Calendar editor for newspapers --%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<f:view locale="#{SpracheForm.locale}">
    <%@include file="/newpages/inc/head.jsp"%>
    <body>
        <script type="text/javascript">

        <%--
         * The variable uploadWindow indicates whether the form validation must be skipped.
         * This is the case if the upload button is clicked because the user should
         * be allowed to upload a file without being forced to manually enter the
         * data he wants to upload beforehand.
         --%>
            var uploadWindow = false;
        <%--
         * The function addClickQuery() checks whether adding a block can be
         * performed without unexpected side effects. In the rare case that there
         * could be confusion the user will be prompted with an explainatory message
         * and has an option to continue or not.
         *
         * @return whether the add request shall be processed
         --%>
            function addClickQuery() {
                if (!blockDataIsValid()) {
                    return false;
                }
                if (document.getElementById("form1:applyLink").style.display == "none") {
                    return true;
                } else {
                    return confirm("${msgs['calendar.block.add.query']}");
                }
            }
        <%--
         * The function deleteClickQuery() checks whether an issue shall or shall
         * not be deleted. The user is presented with a query whether it wants to
         * delete the block. This is to prevent misclicks.
         *
         * @return whether an issue can be deleted
         --%>
            function deleteClickQuery() {
                return confirm("${msgs['calendar.issue.delete.query']}");
            }
        <%--
         * The function removeClickQuery() checks whether a block shall or
         * shall not be deleted. The user is presented with a query whether it wants
         * to delete the block. This is to prevent misclicks. If there is only one
         * block left, instead, the user is presented with an information that this
         * isn’t allowed.
         *
         * @return whether a block can be deleted
         --%>
            function removeClickQuery() {
                return confirm("${msgs['calendar.block.remove.query']}");
            }
        <%--
         * The function setIgnoreInvalidValue() inserts an alternate white space
         * into the date input boxes in case the upload window is shown. This
         * is later used in the application to notice that the user chose the
         * link to show the upload window even before the link action is executed
         * to prevent error messages concerning an invalid input to show. This is
         * necessary because JSF evaluates the field values before executing the
         * action link.
         *
         * @param uploadWindow
         *            whether the upload window shall show
         * @return whether the upload window shall show
         --%>
            function setIgnoreInvalidValue(uploadWindow) {
                if(uploadWindow == true){
                    document.getElementById("form1:firstAppearance").value += "\xA0";
                    document.getElementById("form1:lastAppearance").value += "\xA0";
                }
                return uploadWindow;
            }
        <%--
         * The function setSelectSelectedByValue() sets the selected element of a
         * select box to the first option whose submit value is given.
         *
         * @param id
         *            id of the select box to set
         * @param value
         *            form value of the option to select
         * @throws NoSuchElementException
         *             if no option with the given value was found
         --%>
            function setSelectSelectedByValue(id, value) {
                var select = document.getElementById('form1:blockChanger');
                for (var i = 0; i < select.options.length; i++) {
                    if (select.options[i].value == value) {
                        select.selectedIndex = i;
                        return;
                    }
                }
                throw "NoSuchElementException";
            }
        <%--
         * The function showApplyLink() makes the apply changes link for an issue
         * name box show.
         *
         * @return always true
         --%>
            function showApplyLink() {
                document.getElementById("form1:applyLinkPlaceholder").style.display = "none";
                document.getElementById("form1:applyLink").style.display = "inline";
                return true;
            }
        <%--
         * The function startEditBlock() is called whenever the data of the
         * block is being edited by the user. The button “apply changes” is shown
         * except for the first block (because there isn’t anything yet that
         * changes can be “applied on” in the sense of meaning).
         *
         * @return always true
         --%>
            function startEditBlock() {
                if (document.getElementById("form1:blockChanger").options.length > 0){
                    document.getElementById("form1:applyLinkPlaceholder").style.display = "none";
                    document.getElementById("form1:applyLink").style.display = "inline";
                }
                return true;
            }
        <%--
         * The function blockDataIsValid() validates the block data typed in by the
         * user.
         *
         * The following requirements must be met:
         *      • The dates must be well-formed.
         *
         * @return whether the block data is valid
         --%>
            function blockDataIsValid() {
                if (!document.getElementById("form1:firstAppearance").value
                        .match(/^\D*\d+\D+\d+\D+\d+\D*$/)) {
                    alert("${msgs['calendar.block.firstAppearance.invalid']}");
                    document.getElementById("form1:firstAppearance").focus();
                    return false;
                }
                if (!document.getElementById("form1:lastAppearance").value
                        .match(/^\D*\d+\D+\d+\D+\d+\D*$/)) {
                    alert("${msgs['calendar.block.lastAppearance.invalid']}");
                    document.getElementById("form1:lastAppearance").focus();
                    return false;
                }
                return true;
            }
        <%--
         * The function blockChangerChangeQuery() checks whether silently changing
         * the block is possible. In the rare case that there are unsubmitted
         * changes to the block, the user is presented with a query whether he or she
         * wants to continue, which implies that the changes will be lost. In case
         * that the user decides not to continue the selected option in the block
         * changer is restored so that a subsequent form submission results in the
         * correct behaviour.
         *
         * @param originValue
         *            form value of the option that was selected in the block changer
         *            on page load
         * @return whether the change request shall be processed
         --%>
            function blockChangerChangeQuery(originValue) {
                if (document.getElementById("form1:applyLink").style.display == "none"
                        || confirm("${msgs['calendar.block.alter.query']}"))
                    return true;
                setSelectSelectedByValue("form1:blockChanger", originValue);
                return false;
            }
        </script>
        <htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable"
            align="center">
            <%@include file="/newpages/inc/tbl_Kopf.jsp"%>
            <htm:tr>
                <%@include file="/newpages/inc/tbl_Navigation.jsp"%>
                <htm:td valign="top" styleClass="layoutInhalt">

                    <%-- ===================== Page main frame ===================== --%>

                    <h:form id="form1" enctype="multipart/form-data"
                        onsubmit="return setIgnoreInvalidValue(uploadWindow) || blockDataIsValid()">

                        <%-- Bread crumbs --%>

                        <h:panelGrid width="100%" columns="1"
                            styleClass="layoutInhaltKopf">
                            <h:panelGroup>
                                <h:commandLink value="#{msgs.startseite}" action="newMain"
                                    onclick="uploadWindow=true" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:commandLink value="#{msgs.prozessverwaltung}"
                                    action="ProzessverwaltungAlle" onclick="uploadWindow=true" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:commandLink value="#{msgs.einenNeuenProzessAnlegen}"
                                    action="#{ProzesskopieForm.GoToSeite1}"
                                    onclick="uploadWindow=true" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:outputText value="#{msgs['calendar.header']}" />
                            </h:panelGroup>
                        </h:panelGrid>

                        <htm:table border="0" align="center" width="100%" cellpadding="15">
                            <htm:tr>
                                <htm:td>
                                    <htm:div styleClass="formRow">
                                        <htm:h3 styleClass="calendarCaption">
                                            <h:outputText value="#{msgs['calendar.header']}" />
                                        </htm:h3>
                                        <h:commandLink value="#{msgs['granularity.download']}"
                                            action="#{CalendarForm.downloadClick}" rendered="#{not CalendarForm.editingYear}"
                                            styleClass="rightText" />
                                        <h:commandLink value="#{msgs['calendar.upload']}"
                                            action="#{CalendarForm.showUploadClick}"
                                            onclick="uploadWindow=true" styleClass="rightText"
                                            rendered="#{not CalendarForm.editingYear}"
                                            style="padding-right: 10px; " />
                                        <h:commandLink value="#{msgs['calendar.applyChanges']}"
                                            id="applyLink" styleClass="rightText"
                                            rendered="#{not CalendarForm.editingYear}"
                                            style="padding-right: 10px; display: none; " />
                                        <h:outputText value="#{msgs['calendar.applyChanges']}"
                                            title="#{msgs['calendar.applyChanges.placeholder']}"
                                            rendered="#{not CalendarForm.editingYear}"
                                            onclick="alert('#{msgs['calendar.applyChanges.placeholder']}'); return false;"
                                            id="applyLinkPlaceholder" styleClass="rightText"
                                            style="padding-right: 10px; color: gray; cursor: help;" />
                                    </htm:div>

                                    <%-- Global warnings and error messages --%>

                                    <htm:div style="clear: both; ">
                                        <h:messages globalOnly="true" errorClass="text_red"
                                            infoClass="text_blue" showDetail="true" showSummary="true"
                                            tooltip="true" />
                                    </htm:div>

                                    <%-- ===================== Page main content ====================== --%>

                                    <htm:fieldset styleClass="calendarTitleMgmt"
                                            rendered="#{not CalendarForm.editingYear}"
                                            style="margin-bottom: 14px; ">
                                        <htm:legend>
                                            <h:outputText value="#{msgs['calendar.block.caption']}" />
                                        </htm:legend>

                                        <%-- Input elements for base data --%>
                                        <htm:div styleClass="formRow">
                                            <%-- Drop down list to switch between already defined blocks --%>
                                            <h:outputLabel for="blockChanger" styleClass="leftText"
                                                value="#{msgs['calendar.block.select']}"
                                                style="margin-top: 10px;" />

                                            <h:selectOneMenu value="#{CalendarForm.blockChangerSelected}"
                                                onchange="if(blockChangerChangeQuery('#{CalendarForm.blockChangerSelected}')){submit();}"
                                                id="blockChanger" style="margin-top: 5px; min-width: 162px; ">
                                                <si:selectItems value="#{CalendarForm.blockChangerOptions}"
                                                    var="item" itemLabel="#{item.label}"
                                                    itemValue="#{item.value}" />
                                            </h:selectOneMenu>


                                            <htm:div styleClass="keepTogether">
                                                <h:outputText
                                                    value="#{msgs['calendar.block.firstAppearance']}" />
                                                <h:inputText value="#{CalendarForm.firstAppearance}"
                                                    onkeydown="startEditTitle()" onchange="startEditTitle()"
                                                    id="firstAppearance" />
                                            </htm:div>

                                            <htm:div styleClass="keepTogether">
                                                <h:outputText
                                                    value="#{msgs['calendar.block.lastAppearance']}" />
                                                <h:inputText value="#{CalendarForm.lastAppearance}"
                                                    onkeydown="startEditTitle()" onchange="startEditTitle()"
                                                    id="lastAppearance" />
                                            </htm:div>

                                            <%-- Buttons to copy and remove blocks --%>
                                            <h:commandLink title="#{msgs['calendar.block.addFirst']}"
                                                rendered="#{CalendarForm.blank}" styleClass="actionLink">
                                                <h:graphicImage style="vertical-align: text-bottom;"
                                                    value="/newpages/images/buttons/edit_20.gif"
                                                    rendered="#{CalendarForm.blank}" />
                                            </h:commandLink>
                                            <h:commandLink title="#{msgs['calendar.block.add']}"
                                                action="#{CalendarForm.copyBlockClick}"
                                                onclick="if(!addClickQuery()){return false;}"
                                                rendered="#{not CalendarForm.blank}" styleClass="actionLink">
                                                <h:graphicImage style="vertical-align: text-bottom;"
                                                    value="/newpages/images/buttons/star_blue.gif"
                                                    rendered="#{not CalendarForm.blank}" />
                                            </h:commandLink>
                                            <h:commandLink title="#{msgs['calendar.block.remove']}"
                                                style="vertical-align: text-bottom;"
                                                action="#{CalendarForm.removeBlockClick}"
                                                onclick="if(!removeClickQuery()){return false;}"
                                                styleClass="actionLink">
                                                <h:graphicImage
                                                    value="/newpages/images/buttons/waste1_20px.gif" />
                                            </h:commandLink>
                                        </htm:div>


                                        <htm:div styleClass="calendarTitleContent">
                                            <t:dataList layout="simple" var="issue"
                                                value="#{CalendarForm.issues}">
                                                <htm:div styleClass="filling formRow">
                                                    <htm:span styleClass="leftText">
                                                        <%-- bubble --%>
                                                        <htm:span styleClass="bubble"
                                                            style="color: #{issue.colour}">
                                                            <h:outputText value="●" />
                                                        </htm:span>

                                                        <%-- Prefix text --%>
                                                        <h:outputLabel value="#{msgs['calendar.issue']}"
                                                            for="issueHeading" />
                                                    </htm:span>

                                                    <%-- Delete button --%>
                                                    <h:commandLink title="#{msgs['calendar.issue.delete']}"
                                                        action="#{issue.deleteClick}"
                                                        onclick="if(!deleteClickQuery()){return false;}"
                                                        styleClass="rightText"
                                                        style="margin-left: 8px; margin-top: -2px;">
                                                        <h:graphicImage
                                                            value="/newpages/images/buttons/waste1_20px.gif" />
                                                    </h:commandLink>


                                                    <%-- Days of week --%>
                                                    <htm:div styleClass="rightText">

                                                        <htm:div styleClass="keepTogether">
                                                            <h:selectBooleanCheckbox id="monday"
                                                                value="#{issue.monday}" onchange="submit()" />
                                                            <h:outputLabel value="#{msgs['calendar.issue.monday']}"
                                                                for="monday" />
                                                        </htm:div>

                                                        <htm:div styleClass="keepTogether">
                                                            <h:selectBooleanCheckbox id="tuesday"
                                                                value="#{issue.tuesday}" onchange="submit()" />
                                                            <h:outputLabel value="#{msgs['calendar.issue.tuesday']}"
                                                                for="tuesday" />
                                                        </htm:div>

                                                        <htm:div styleClass="keepTogether">
                                                            <h:selectBooleanCheckbox id="wednesday"
                                                                value="#{issue.wednesday}" onchange="submit()" />
                                                            <h:outputLabel
                                                                value="#{msgs['calendar.issue.wednesday']}"
                                                                for="wednesday" />
                                                        </htm:div>

                                                        <htm:div styleClass="keepTogether">
                                                            <h:selectBooleanCheckbox id="thursday"
                                                                value="#{issue.thursday}" onchange="submit()" />
                                                            <h:outputLabel value="#{msgs['calendar.issue.thursday']}"
                                                                for="thursday" />
                                                        </htm:div>

                                                        <htm:div styleClass="keepTogether">
                                                            <h:selectBooleanCheckbox id="friday"
                                                                value="#{issue.friday}" onchange="submit()" />
                                                            <h:outputLabel value="#{msgs['calendar.issue.friday']}"
                                                                for="friday" />
                                                        </htm:div>

                                                        <htm:div styleClass="keepTogether">
                                                            <h:selectBooleanCheckbox id="saturday"
                                                                value="#{issue.saturday}" onchange="submit()" />
                                                            <h:outputLabel value="#{msgs['calendar.issue.saturday']}"
                                                                for="saturday" />
                                                        </htm:div>

                                                        <htm:div styleClass="keepTogether">
                                                            <h:selectBooleanCheckbox id="sunday"
                                                                value="#{issue.sunday}" onchange="submit()" />
                                                            <h:outputLabel value="#{msgs['calendar.issue.sunday']}"
                                                                for="sunday" />
                                                        </htm:div>
                                                    </htm:div>

                                                    <%-- Issue name box --%>
                                                    <htm:span styleClass="fillWrapper">
                                                        <h:inputText value="#{issue.heading}" id="issueHeading"
                                                            onkeydown="showApplyLink();" onchange="showApplyLink();"
                                                            styleClass="filling" />
                                                    </htm:span>
                                                </htm:div>
                                            </t:dataList>
                                            <%-- Add button --%>
                                            <h:commandLink title="#{msgs['calendar.issue.add']}"
                                                action="#{CalendarForm.addIssueClick}">
                                                <h:graphicImage style="margin-left: -5px;"
                                                    value="/newpages/images/buttons/star_blue.gif" />
                                            </h:commandLink>

                                        </htm:div>
                                    </htm:fieldset>

                                    <%-- Metadata input box --%>

                                    <htm:fieldset styleClass="calendarTitleMgmt" style="margin-bottom: 14px; "
                                        rendered="#{not CalendarForm.editingYear and not empty CalendarForm.metadataOptions}">
                                        <htm:legend>
                                            <h:outputText value="#{msgs['calendar.metadata.caption']}" />
                                        </htm:legend>

                                        <htm:div styleClass="formRow">
                                            <%-- Drop down list to switch between issues --%>
                                            <h:outputLabel for="issueChanger" styleClass="leftText"
                                                value="#{msgs['calendar.metadata.select']}"
                                                style="margin-top: 10px;" />

                                            <h:selectOneMenu value="#{CalendarForm.metadataSelected}" id="issueChanger"
                                                onchange="submit();" style="margin-top: 5px; min-width: 162px; ">
                                                <si:selectItems var="item" value="#{CalendarForm.metadataOptions}"
                                                    itemValue="#{item.value}" itemLabel="#{item.label}" />
                                            </h:selectOneMenu>
                                        </htm:div>
                                        
                                        <htm:div styleClass="calendarTitleContent">
                                            <t:dataList layout="simple" var="counter" value="#{CalendarForm.metadata}">
                                                <htm:div styleClass="filling formRow">

                                                    <%-- Metadata key --%>
                                                    <h:selectOneMenu value="#{counter.metadataKeySelected}"
                                                        onchange="submit();" style="margin-top: 5px; min-width: 162px; "
                                                        styleClass="leftText">
                                                        <si:selectItems var="item" value="#{CalendarForm.metadataKeyOptions}"
                                                            itemValue="#{item.value}" itemLabel="#{item.label}" />
                                                    </h:selectOneMenu>

                                                    <%-- Counter edit mode --%>
                                                    <h:selectOneRadio value="#{counter.editModeSelected}"
                                                        styleClass="leftText" layout="pageDirection" onchange="submit();">
                                                        <f:selectItem itemValue="CONTINUE"
                                                            itemLabel="#{msgs['calendar.metadata.continue']}"
                                                            itemDisabled="#{not counter.editModeChangeable}" />
                                                        <f:selectItem itemValue="DEFINE"
                                                            itemLabel="#{msgs['calendar.metadata.define']}" />
                                                        <f:selectItem itemValue="DELETE"
                                                            itemLabel="#{msgs['calendar.metadata.delete']}"
                                                            itemDisabled="#{not counter.editModeChangeable}" />
                                                    </h:selectOneRadio>

                                                    <%-- Delete button --%>
                                                    <h:commandLink title="#{msgs['calendar.metadata.delete']}"
                                                        action="#{counter.deleteClick}"
                                                        styleClass="rightText"
                                                        style="margin-left: 8px; margin-top: -2px;">
                                                        <h:graphicImage
                                                            value="/newpages/images/buttons/waste1_20px.gif" />
                                                    </h:commandLink>
                                                    
                                                    <%-- Counter mode --%>
                                                    <h:selectOneMenu value="#{counter.modeSelected}"
                                                        style="margin-top: 5px; min-width: 162px; "
                                                        styleClass="rightText" readonly="#{counter.editModeSelected != 'DEFINE'}">
                                                        <f:selectItem itemValue=""
                                                            itemLabel="#{msgs['calendar.metadata.mode.off']}" />
                                                        <f:selectItem itemValue="ISSUES"
                                                            itemLabel="#{msgs['calendar.metadata.mode.issues']}" />
                                                        <f:selectItem itemValue="DAYS"
                                                            itemLabel="#{msgs['calendar.metadata.mode.days']}" />
                                                        <f:selectItem itemValue="WEEKS"
                                                            itemLabel="#{msgs['calendar.metadata.mode.weeks']}" />
                                                        <f:selectItem itemValue="MONTHS"
                                                            itemLabel="#{msgs['calendar.metadata.mode.months']}" />
                                                        <f:selectItem itemValue="QUARTERS"
                                                            itemLabel="#{msgs['calendar.metadata.mode.quarters']}" />
                                                        <f:selectItem itemValue="YEARS"
                                                            itemLabel="#{msgs['calendar.metadata.mode.years']}" />
                                                    </h:selectOneMenu>

                                                    <%-- Counter value --%>
                                                    <htm:span styleClass="fillWrapper">
                                                        <h:inputText value="#{counter.value}" id="metadataValue"
                                                            styleClass="filling" readonly="#{counter.editModeSelected != 'DEFINE'}" />
                                                    </htm:span>
                                                </htm:div>
                                            </t:dataList>

                                            <%-- Add button --%>
                                            <htm:div style="clear: both; ">
                                                <h:commandLink title="#{msgs['calendar.metadata.add']}"
                                                    action="#{CalendarForm.addMetadataClick}">
                                                    <h:graphicImage style="margin-left: -5px;"
                                                        value="/newpages/images/buttons/star_blue.gif" />
                                                </h:commandLink>
                                            </htm:div>
                                        </htm:div>
                                    </htm:fieldset>
                                    
                                    <%-- Year properties box --%>

                                    <htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen"
                                        rendered="#{CalendarForm.editingYear}" style="margin-bottom: 14px; ">
 
                                        <htm:tr>
                                            <htm:td styleClass="eingabeBoxen_row1" colspan="2">
                                                <h:outputText value="#{msgs['calendar.yearSettings.caption']}" />
                                            </htm:td>
                                        </htm:tr>
                                        <htm:tr>
                                            <htm:td styleClass="eingabeBoxen_row2" colspan="2">
                                                <h:panelGrid columns="2">
                                                    <h:outputLabel for="yearName1"
                                                        value="#{msgs['calendar.yearSettings.yearName']}" />
                                                    <h:panelGroup>
                                                        <h:inputText id="yearName1"
                                                            style="width: 300px;margin-right:15px"
                                                            value="#{CalendarForm.yearName}" />
                                                    </h:panelGroup>

                                                    <h:outputLabel for="yearStart"
                                                        value="#{msgs['calendar.yearSettings.yearStart']}" />
                                                    <h:panelGroup>
                                                        <h:inputText id="yearName"
                                                            style="width: 300px;margin-right:15px"
                                                            value="#{CalendarForm.yearStart}" />
                                                    </h:panelGroup>
                                                </h:panelGrid>
                                            </htm:td>
                                        </htm:tr>
                                        <htm:tr>
                                            <htm:td styleClass="eingabeBoxen_row3" align="right" colspan="2">
                                                <h:commandButton value="#{msgs.speichern}"
                                                    action="#{CalendarForm.onSaveYearPropertiesClick}" />
                                            </htm:td>
                                        </htm:tr>
                                    </htm:table>

                                    <%-- File upload dialogue --%>

                                    <htm:div styleClass="modalBackground"
                                        rendered="#{CalendarForm.uploadShowing}" />
                                    <htm:div styleClass="calendarUploadBox"
                                        rendered="#{CalendarForm.uploadShowing}">
                                        <htm:h3>
                                            <h:outputText value="#{msgs['calendar.upload']}" />
                                        </htm:h3>
                                        <htm:div styleClass="formRow">
                                            <t:inputFileUpload value="#{CalendarForm.uploadedFile}" />
                                        </htm:div>
                                        <htm:div styleClass="formRow">
                                            <h:commandLink value="#{msgs['calendar.upload.submit']}"
                                                action="#{CalendarForm.uploadClick}"
                                                onclick="uploadWindow=true" styleClass="actionLink" />
                                            <h:commandLink value="#{msgs.abbrechen}"
                                                action="#{CalendarForm.hideUploadClick}"
                                                onclick="uploadWindow=true" styleClass="actionLink" />
                                        </htm:div>
                                    </htm:div>

                                    <%-- Calendar sheet --%>

                                    <htm:table styleClass="calendarSheet">
                                        <htm:caption>
                                            <h:graphicImage
                                                    value="/newpages/images/buttons/edit_20.gif"
                                                    style="float:left; visibility: hidden;"
                                                    rendered="#{not CalendarForm.editingYear}" />
                                            <h:commandLink title="#{msgs['calendar.yearSettings.edit']}"
                                                    action="#{CalendarForm.onEditYearClick}" style="float: right;"
                                                    rendered="#{not CalendarForm.editingYear}"
                                                    onclick="uploadWindow=true" >
                                                <h:graphicImage
                                                    value="/newpages/images/buttons/edit_20.gif" />
                                            </h:commandLink>
                                            <h:commandLink value="←"
                                                action="#{CalendarForm.backwardClick}" styleClass="backward" />
                                            <h:outputText value="#{CalendarForm.year}" />
                                            <h:commandLink value="→"
                                                action="#{CalendarForm.forwardClick}" styleClass="forward" />
                                        </htm:caption>
                                        <htm:tr>
                                            <htm:th>
                                                <h:outputText
                                                    value="#{msgs['calendar.sheet.column.january']}" />
                                            </htm:th>
                                            <htm:th>
                                                <h:outputText
                                                    value="#{msgs['calendar.sheet.column.february']}" />
                                            </htm:th>
                                            <htm:th>
                                                <h:outputText value="#{msgs['calendar.sheet.column.march']}" />
                                            </htm:th>
                                            <htm:th>
                                                <h:outputText value="#{msgs['calendar.sheet.column.april']}" />
                                            </htm:th>
                                            <htm:th>
                                                <h:outputText value="#{msgs['calendar.sheet.column.may']}" />
                                            </htm:th>
                                            <htm:th>
                                                <h:outputText value="#{msgs['calendar.sheet.column.june']}" />
                                            </htm:th>
                                            <htm:th>
                                                <h:outputText value="#{msgs['calendar.sheet.column.july']}" />
                                            </htm:th>
                                            <htm:th>
                                                <h:outputText
                                                    value="#{msgs['calendar.sheet.column.august']}" />
                                            </htm:th>
                                            <htm:th>
                                                <h:outputText
                                                    value="#{msgs['calendar.sheet.column.september']}" />
                                            </htm:th>
                                            <htm:th>
                                                <h:outputText
                                                    value="#{msgs['calendar.sheet.column.october']}" />
                                            </htm:th>
                                            <htm:th>
                                                <h:outputText
                                                    value="#{msgs['calendar.sheet.column.november']}" />
                                            </htm:th>
                                            <htm:th>
                                                <h:outputText
                                                    value="#{msgs['calendar.sheet.column.december']}" />
                                            </htm:th>
                                        </htm:tr>
                                        <t:dataList layout="simple" var="row"
                                            value="#{CalendarForm.calendarSheet}">
                                            <htm:tr>
                                                <t:dataList layout="simple" var="cell" value="#{row}">
                                                    <htm:td styleClass="#{cell.styleClass}">
                                                        <h:outputText value="#{cell.day}" />
                                                        <htm:span styleClass="issueOptions">
                                                            <t:dataList layout="simple" var="issueOption"
                                                                value="#{cell.issues}">
                                                                <h:commandLink value="●​"
                                                                    style="color: #{issueOption.colour};"
                                                                    title="#{issueOption.issue} #{msgs['calendar.sheet.issue.selected']}"
                                                                    styleClass="issueOption"
                                                                    action="#{issueOption.unselectClick}"
                                                                    rendered="#{issueOption.selected}" />
                                                                <h:commandLink value="○​"
                                                                    style="color: #{issueOption.colour};"
                                                                    title="#{issueOption.issue} #{msgs['calendar.sheet.issue.notSelected']}"
                                                                    styleClass="issueOption"
                                                                    action="#{issueOption.selectClick}"
                                                                    rendered="#{not issueOption.selected}" />
                                                            </t:dataList>
                                                        </htm:span>
                                                    </htm:td>
                                                </t:dataList>
                                            </htm:tr>
                                        </t:dataList>
                                    </htm:table>

                                    <htm:div styleClass="leftText">
                                        <h:commandButton value="#{msgs['goBack']}"
                                            action="#{ProzesskopieForm.GoToSeite1}"
                                            onclick="uploadWindow=true" />
                                    </htm:div>
                                    <htm:div styleClass="rightText">
                                        <h:commandButton value="#{msgs['weiter']}"
                                            action="#{CalendarForm.nextClick}" />
                                    </htm:div>

                                    <%-- ===================== End page main content ====================== --%>

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
