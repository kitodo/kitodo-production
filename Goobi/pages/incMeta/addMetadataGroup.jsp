<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<h:panelGroup rendered="#{Metadaten.addMetadataGroupMode}">
	<htm:h3 style="margin-top:10px">
		<h:outputText value="#{msgs.editMetadataGroup}" />
	</htm:h3>
	<htm:table cellpadding="3" cellspacing="0" style="width:540px" styleClass="eingabeBoxen">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1">
				<h:outputText value="#{msgs.editMetadataGroup}" />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2">
				<h:selectOneMenu value="#{Metadaten.newMetadataGroup.type}" onchange="submit()">
					<f:selectItems value="#{Metadaten.newMetadataGroup.possibleTypes}" />
				</h:selectOneMenu>
				<x:dataTable var="member" value="#{Metadaten.newMetadataGroup.members}">
					<h:column>
						<h:outputText value="#{item.label}" />
					</h:column>
					<h:column>
						<h:inputTextarea value="#{member.value}"
							rendered="#{member.class.simpleName == 'RenderableTextbox'}" />
						<h:inputText value="#{member.value}"
							rendered="#{member.class.simpleName == 'RenderableEdit'}" />
						<h:selectManyListbox value="#{member.value}"
							rendered="#{member.class.simpleName == 'RenderableListbox' && member.multiselect == true}">
							<f:selectItems value="#{member.items}" />
						</h:selectManyListbox>
						<h:selectOneMenu value="#{member.value}"
							rendered="#{member.class.simpleName == 'RenderableDropDownList'}">
							<f:selectItems value="#{member.items}" />
						</h:selectOneMenu>
						<h:outputText id="myOutput" value="#{member.value}"
							rendered="#{member.class.simpleName == 'RenderableBevel'}" />
					</h:column>
				</x:dataTable>
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3">
				<h:commandButton action="#{Metadaten.showMetadata}" value="#{msgs.abbrechen}"/>
				<h:commandButton action="#{Metadaten.addMetadataGroup}" value="#{msgs.dieAenderungenSpeichern}"/>
			</htm:td>
		</htm:tr>
	</htm:table>
	
</h:panelGroup>