<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi"
	prefix="si"%>
<%-- 
	This file is part of the Goobi Application - a Workflow tool for the support
	of mass digitization.
	
	(c) 2013 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
	
	Visit the websites for more information.
	    		- http://www.goobi.org/en/
	    		- https://github.com/goobi
	
	This program is free software; you can redistribute it and/or modify it under
	the terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later
	version.
	
	This program is distributed in the hope that it will be useful, but WITHOUT
	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
	FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
	details.
	
	You should have received a copy of the GNU General Public License along with
	this program; if not, write to the Free Software Foundation, Inc., 59 Temple
	Place, Suite 330, Boston, MA 02111-1307 USA
	
	Linking this library statically or dynamically with other modules is making a
	combined work based on this library. Thus, the terms and conditions of the
	GNU General Public License cover the whole combination. As a special
	exception, the copyright holders of this library give you permission to link
	this library with independent modules to produce an executable, regardless of
	the license terms of these independent modules, and to copy and distribute
	the resulting executable under terms of your choice, provided that you also
	meet, for each linked independent module, the terms and conditions of the
	license of that module. An independent module is a module which is not
	derived from or based on this library. If you modify this library, you may
	extend this exception to your version of the library, but you are not obliged
	to do so. If you do not wish to do so, delete this exception statement from
	your version.
--%>

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
		 * The function addClickQuery() checks whether adding a title block can be
		 * performed without unexpected side effects. In the rare case that there
		 * could be confusion the user will be prompted with an explainatory message
		 * and has an option to continue or not.
		 * 
		 * @return whether the add request shall be processed
		 --%>
			function addClickQuery() {
				if (!titleDataIsValid()) {
					return false;
				}
				if (document.getElementById("form1:applyLink").style.display == "none") {
					return true;
				} else {
					return confirm("${msgs['calendar.title.add.query']}");
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
		 * The function removeClickQuery() checks whether a title block shall or
		 * shall not be deleted. The user is presented with a query whether it wants
		 * to delete the block. This is to prevent misclicks. If there is only one
		 * block left, instead, the user is presented with an information that this
		 * isn’t allowed.
		 * 
		 * @return whether a title block can be deleted
		 --%>
			function removeClickQuery() {
				return confirm("${msgs['calendar.title.remove.query']}");
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
				var select = document.getElementById('form1:titlePicker');
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
		 * The function startEditTitle() is called whenever the data of the title
		 * block is being edited by the user. The button “apply changes” is shown
		 * except for the first title block (because there isn’t anything yet that
		 * changes can be “applied on” in the sense of meaning).
		 * 
		 * @return always true
		 --%>
			function startEditTitle() {
				if (document.getElementById("form1:titlePicker").options.length > 0){
					document.getElementById("form1:applyLinkPlaceholder").style.display = "none";
					document.getElementById("form1:applyLink").style.display = "inline";
				}
				return true;
			}
		<%--
		 * The function titleDataIsValid() validates the title data typed in by the
		 * user.
		 * 
		 * The following requirements must be met:
		 * 		• The dates must be well-formed.
		 * 
		 * @return whether the title data is valid
		 --%>
			function titleDataIsValid() {
				if (!document.getElementById("form1:firstAppearance").value
						.match(/^\D*\d+\D+\d+\D+\d+\D*$/)) {
					alert("${msgs['calendar.title.firstAppearance.invalid']}");
					document.getElementById("form1:firstAppearance").focus();
					return false;
				}
				if (!document.getElementById("form1:lastAppearance").value
						.match(/^\D*\d+\D+\d+\D+\d+\D*$/)) {
					alert("${msgs['calendar.title.lastAppearance.invalid']}");
					document.getElementById("form1:lastAppearance").focus();
					return false;
				}
				return true;
			}
		<%--
		 * The function titlePickerChangeQuery() checks whether silently changing
		 * the title block is possible. In the rare case that there are unsubmitted
		 * changes to the title block, the user is presented with a query whether it
		 * wants to continue, which implies that the changes will be lost. In case
		 * that the user decides not to continue the selected option in the title
		 * picker is restored so that a subsequent form submission results in the
		 * correct behaviour.
		 * 
		 * @param originValue
		 *            form value of the option that was selected in the title picker
		 *            on page load
		 * @return whether the change request shall be processed
		 --%>
			function titlePickerChangeQuery(originValue) {
				if (document.getElementById("form1:applyLink").style.display == "none"
						|| confirm("${msgs['calendar.title.alter.query']}"))
					return true;
				setSelectSelectedByValue("form1:titlePicker", originValue);
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
						onsubmit="return setIgnoreInvalidValue(uploadWindow) || titleDataIsValid()">

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
											action="#{CalendarForm.downloadClick}" styleClass="rightText" />
										<h:commandLink value="#{msgs['calendar.upload']}"
											action="#{CalendarForm.showUploadClick}"
											onclick="uploadWindow=true" styleClass="rightText"
											style="padding-right: 10px; " />
										<h:commandLink value="#{msgs['calendar.applyChanges']}"
											id="applyLink" styleClass="rightText"
											style="padding-right: 10px; display: none; " />
										<h:outputText value="#{msgs['calendar.applyChanges']}"
											title="#{msgs['calendar.applyChanges.placeholder']}"
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
										style="margin-bottom: 14px; ">
										<htm:legend>
											<h:outputText value="#{msgs['calendar.title.caption']}" />
										</htm:legend>

										<%-- Input elements for base data --%>
										<htm:div styleClass="formRow">
											<%-- Select box to switch between already defined titles --%>
											<h:outputLabel for="titlePicker" styleClass="leftText"
												value="#{msgs['calendar.title.select']}"
												style="margin-top: 10px;" />

											<h:selectOneMenu value="#{CalendarForm.titlePickerSelected}"
												onchange="if(titlePickerChangeQuery('#{CalendarForm.titlePickerSelected}')){submit();}"
												id="titlePicker" style="margin-top: 5px; min-width: 162px; ">
												<si:selectItems value="#{CalendarForm.titlePickerOptions}"
													var="item" itemLabel="#{item.label}"
													itemValue="#{item.value}" />
											</h:selectOneMenu>


											<htm:div styleClass="keepTogether">
												<h:outputText
													value="#{msgs['calendar.title.firstAppearance']}" />
												<h:inputText value="#{CalendarForm.firstAppearance}"
													onkeydown="startEditTitle()" onchange="startEditTitle()"
													id="firstAppearance" />
											</htm:div>

											<htm:div styleClass="keepTogether">
												<h:outputText
													value="#{msgs['calendar.title.lastAppearance']}" />
												<h:inputText value="#{CalendarForm.lastAppearance}"
													onkeydown="startEditTitle()" onchange="startEditTitle()"
													id="lastAppearance" />
											</htm:div>

											<%-- Buttons to add and remove titles --%>
											<h:commandLink title="#{msgs['calendar.title.addFirst']}"
												rendered="#{CalendarForm.blank}" styleClass="actionLink">
												<h:graphicImage style="vertical-align: text-bottom;"
													value="/newpages/images/buttons/edit_20.gif"
													rendered="#{CalendarForm.blank}" />
											</h:commandLink>
											<h:commandLink title="#{msgs['calendar.title.add']}"
												action="#{CalendarForm.addTitleClick}"
												onclick="if(!addClickQuery()){return false;}"
												rendered="#{not CalendarForm.blank}" styleClass="actionLink">
												<h:graphicImage style="vertical-align: text-bottom;"
													value="/newpages/images/buttons/star_blue.gif"
													rendered="#{not CalendarForm.blank}" />
											</h:commandLink>
											<h:commandLink title="#{msgs['calendar.title.remove']}"
												style="vertical-align: text-bottom;"
												action="#{CalendarForm.removeTitleClick}"
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

									<%-- Calender sheet --%>

									<htm:table styleClass="calendarSheet">
										<htm:caption>
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
