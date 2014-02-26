<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
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

<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="/newpages/inc/head.jsp"%>
	<body>
		<script type="text/javascript">
		<%--
		 * The function startEditTitle() is called whenever the data of the title
		 * block is being edited by the user. It disables all other form elements
		 * whose contents depend on this data and need to be recomposed after it was
		 * altered. The form elements must be re-enabled if the user clicks “apply
		 * changes” before the form is submitted, otherwise the browser—by
		 * specification—doesn’t submit them which will cause JSF to fail.
		 * 
		 * @return always true
		 --%>
			function startEditTitle() {
				// TODO
				return true;
			}
		<%--
		 * The function endEditTitle() is called after successful validation of the
		 * modified title block data when the user clicks “apply changes” to
		 * re-enable any form elements previously disabled by startEditTitle(). This
		 * is necessary because otherwise the browser—by specification—doesn’t
		 * submit them, which will cause JSF to fail.
		 * 
		 * @return always true
		 --%>
			function endEditTitle() {
				// TODO
				return true;
			}
		<%--
		 * The function titleDataIsValid() validates the title data typed in by the
		 * user.
		 * 
		 * The following requirements must be met:
		 * 		• The title must not be empty.
		 * 		• The dates must be well-formed.
		 * 
		 * @return whether the title data is valid
		 --%>
			function titleDataIsValid() {
				// TODO
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

					<h:form>

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
								<h:outputText value="#{msgs.enterCourse}" />
							</h:panelGroup>
						</h:panelGrid>

						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>
									<htm:h3>
										<h:outputText value="#{msgs.enterCourse}" />
									</htm:h3>

									<%-- Global warnings and error messages --%>

									<h:messages globalOnly="true" errorClass="text_red"
										infoClass="text_blue" showDetail="true" showSummary="true"
										tooltip="true" />

									<%-- ===================== Page main content ====================== --%>

									<htm:div styleClass="blocksAndIssues">
										<htm:div styleClass="titleManagement">

											<%-- Select box to switch between already defined titles --%>
											<h:selectOneListbox styleClass="titlePicker" size="7"
												value="#{CalendarForm.titlePickerSelected}"
												onchange="submit()">
												<si:selectItems value="#{CalendarForm.titlePickerOptions}"
													var="item" itemLabel="#{item.label}"
													itemValue="#{item.value}" />
											</h:selectOneListbox>

											<%-- Buttons to add and remove titles --%>
											<h:commandLink value="#{msgs.addTitle}"
												action="#{CalendarForm.addTitleClick}" />
											<h:commandLink value="#{msgs.removeTitle}"
												action="#{CalendarForm.removeTitleClick}" />
										</htm:div>

										<%-- Input elements for base data --%>
										<htm:div styleClass="titleData">
											<htm:div styleClass="keepTogether">
												<h:outputText value="#{msgs.titleHeading}" />
												<h:inputText value="#{CalendarForm.titleHeading}"
													onchange="startEditTitle()" />
											</htm:div>

											<htm:div styleClass="keepTogether">
												<h:outputText value="#{msgs.firstAppearance}" />
												<h:inputText value="#{CalendarForm.firstAppearance}"
													onchange="startEditTitle()" />
											</htm:div>

											<htm:div styleClass="keepTogether">
												<h:outputText value="#{msgs.lastAppearance}" />
												<h:inputText value="#{CalendarForm.lastAppearance}"
													onchange="startEditTitle()" />
											</htm:div>

											<h:commandLink value="#{msgs.applyChanges}"
												onclick="if(titleDataIsValid()){endEditTitle()}" />
										</htm:div>

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
