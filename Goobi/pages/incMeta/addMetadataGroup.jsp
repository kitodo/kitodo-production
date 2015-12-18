<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%-- 
	This file is part of the Goobi Application - a Workflow tool for the support
	of mass digitization.
	
	(c) 2014 Goobi. Digitalisieren im Verein e. V. <contact@goobi.org>
	
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
	
	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
	
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
