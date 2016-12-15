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

<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>

<%--  Form part to create metadata groups --%>

<h:panelGroup rendered="#{Metadaten.addMetadataGroupMode}">
	<htm:h3 style="margin-top:10px">
		<h:outputText value="#{msgs.editMetadataGroup}" />
	</htm:h3>
	<htm:table cellpadding="3" cellspacing="0" style="width:540px"
		styleClass="eingabeBoxen">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1" colspan="3">
				<h:outputText value="#{msgs.editMetadataGroup}" />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2 mdgroup">
				<h:outputLabel for="grouptype" value="#{msgs.typ}:" />
			</htm:td>
			<htm:td colspan="2" styleClass="eingabeBoxen_row2 mdgroup">
				<h:selectOneMenu id="grouptype"
					value="#{Metadaten.newMetadataGroup.type}" onchange="submit()">
					<f:selectItems value="#{Metadaten.newMetadataGroup.possibleTypes}" />
				</h:selectOneMenu>
			</htm:td>
		</htm:tr>
		<x:dataList var="member" value="#{Metadaten.newMetadataGroup.members}"
			layout="simple">
			<htm:tr rendered="#{member.class.simpleName != 'RenderablePersonMetadataGroup'}">
				<htm:td styleClass="eingabeBoxen_row2 mdgroup">
					<h:outputText value="#{member.label}:" />
				</htm:td>
				<htm:td colspan="2" styleClass="eingabeBoxen_row2 mdgroup">
					<h:inputTextarea value="#{member.value}"
						rendered="#{member.class.simpleName == 'RenderableLineEdit'}" />
					<h:inputText value="#{member.value}"
						rendered="#{member.class.simpleName == 'RenderableEdit' && not member.readonly}" />
					<h:selectManyListbox value="#{member.selectedItems}"
						rendered="#{member.class.simpleName == 'RenderableListBox'}">
						<f:selectItems value="#{member.items}" />
					</h:selectManyListbox>
					<h:selectOneMenu value="#{member.value}"
						rendered="#{member.class.simpleName == 'RenderableDropDownList'}">
						<f:selectItems value="#{member.items}" />
					</h:selectOneMenu>
					<h:outputText id="myOutput" value="#{member.value}"
						rendered="#{member.class.simpleName == 'RenderableEdit' && member.readonly}" />
				</htm:td>
			</htm:tr>
			<x:dataList var="innerMember" value="#{member.members}" rendered="#{member.class.simpleName == 'RenderablePersonMetadataGroup'}">
				<htm:tr>
					<htm:td rowspan="#{member.rowspan}" rendered="#{innerMember.first}" styleClass="eingabeBoxen_row2 mdgroup">
						<h:outputText value="#{member.label}:" />
					</htm:td>
					<htm:td styleClass="eingabeBoxen_row2 mdgroup">
						<h:outputText value="#{innerMember.label}:" />
					</htm:td>
					<htm:td styleClass="eingabeBoxen_row2 mdgroup">
						<h:inputTextarea value="#{innerMember.value}"
							rendered="#{innerMember.class.simpleName == 'RenderableLineEdit'}" />
						<h:inputText value="#{innerMember.value}"
							rendered="#{innerMember.class.simpleName == 'RenderableEdit' && not innerMember.readonly}" />
						<h:selectManyListbox value="#{innerMember.selectedItems}"
							rendered="#{innerMember.class.simpleName == 'RenderableListbox'}">
							<f:selectItems value="#{innerMember.items}" />
						</h:selectManyListbox>
						<h:selectOneMenu value="#{innerMember.value}"
							rendered="#{innerMember.class.simpleName == 'RenderableDropDownList'}">
							<f:selectItems value="#{innerMember.items}" />
						</h:selectOneMenu>
						<h:outputText id="myOutput" value="#{innerMember.value}"
							rendered="#{innerMember.class.simpleName == 'RenderableEdit' && innerMember.readonly}" />
					</htm:td>
				</htm:tr>
			</x:dataList>
		</x:dataList>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3" colspan="3">
				<h:commandButton action="#{Metadaten.showMetadata}"
					value="#{msgs.abbrechen}" />
				<h:commandButton action="#{Metadaten.addMetadataGroup}"
					value="#{msgs.dieAenderungenSpeichern}" />
			</htm:td>
		</htm:tr>
	</htm:table>

</h:panelGroup>
