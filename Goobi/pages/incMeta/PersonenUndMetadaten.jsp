<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
--%>
<h:panelGroup rendered="#{(not Metadaten.modusHinzufuegen) && (not Metadaten.modusHinzufuegenPerson) && (not Metadaten.addMetadataGroupMode)}">


	<%-- ########################################

        Speichern der Metadaten und Ueberschrift
        
 		#########################################--%>
	<htm:table width="540">
		<htm:tr>
			<htm:td>
				<%-- passende Ueberschrift --%>
				<htm:h3 style="margin-top:10px">
					<h:outputText value="#{msgs.metadatenBearbeiten}" rendered="#{not Metadaten.nurLesenModus}" />
					<h:outputText value="#{msgs.metadatenBetrachten}" rendered="#{Metadaten.nurLesenModus}" />
					<h:outputText value=" (#{msgs.schreibgeschuetzt})" rendered="#{Metadaten.nurLesenModus}" style="color: red" />
				</htm:h3>
			</htm:td>

		</htm:tr>
	</htm:table>



	<%-- ########################################

                                              Tabelle fuer die Personen

    #########################################--%>
	<h:panelGroup rendered="#{!empty Metadaten.myPersonen}">

		<htm:h4 style="margin-top:0px;margin-bottom:1px">
			<h:outputText value="#{msgs.personen}" />
		</htm:h4>

		<a4j:support event="onkeyup" requestDelay="1" />

		<%-- oeffnen der Tabelle --%>
		<h:dataTable value="#{Metadaten.myPersonen}" var="Item" style="background-color:#F0F0F0" styleClass="Tabelle"
			columnClasses="TabelleSpalteLinks,TabelleSpalteLinks">

			<%-- Metadaten --%>
			<h:column>
				<h:panelGrid columns="2">
					<h:outputText value="#{msgs.rolle}" style="font-size: 11px" />
					<h:selectOneMenu value="#{Item.rolle}" styleClass="metadatenInput" style="width: 350px;" disabled="#{Metadaten.nurLesenModus}"
						onchange="styleAnpassenPerson(this)" readonly="#{Metadaten.nurLesenModus}">
						<f:selectItems value="#{Item.addableRollen}" />
						<a4j:support event="onmouseup" requestDelay="1" />
					</h:selectOneMenu>
					<h:outputText value="#{msgs.normDataRecord}" style="font-size: 11px" />
					<h:inputText value="#{Item.record}" onchange="styleAnpassen(this)" styleClass="metadatenInput" style="width: 350px;"
						readonly="#{Metadaten.nurLesenModus}" id="record" />
					<h:outputText value="#{msgs.vorname}" style="font-size: 11px" />
					<h:inputText value="#{Item.vorname}" onchange="styleAnpassen(this)" styleClass="metadatenInput" style="width: 350px;"
						readonly="#{Metadaten.nurLesenModus}" id="firstname" />
					<h:outputText value="#{msgs.nachname}" style="font-size: 11px" />
					<h:inputText value="#{Item.nachname}" onkeyup="astyleAnpassenPerson(this)" onchange="styleAnpassen(this)" styleClass="metadatenInput"
						style="width: 350px;" readonly="#{Metadaten.nurLesenModus}" id="lastname" />
				</h:panelGrid>
			</h:column>

			<%-- Link fuer Details --%>
			<h:column rendered="#{not Metadaten.nurLesenModus}">
				<%-- Kopieren-Schaltknopf --%>
				<h:commandLink id="l7" action="#{Metadaten.KopierenPerson}" title="#{msgs.personendatenKopieren}">
					<h:graphicImage value="/newpages/images/buttons/copy.gif" />
					<f:param name="ID" value="#{Item.identifier}" />
					<x:updateActionListener property="#{Metadaten.curPerson}" value="#{Item}" />
				</h:commandLink>

				<%-- Loeschen-Schaltknopf --%>
				<h:commandLink id="l6" action="#{Metadaten.LoeschenPerson}" title="#{msgs.personendatenLoeschen}">
					<h:graphicImage value="/newpages/images/buttons/waste1a_20px.gif" style="margin-left:3px" />
					<f:param name="ID" value="#{Item.identifier}" />
					<x:updateActionListener property="#{Metadaten.curPerson}" value="#{Item}" />
				</h:commandLink>

				<h:commandLink
					onclick="getNormDataPersonenUndMetadaten(this); return false;"
					value="#{msgs.getNormDataRecord}" id="clicker" style="margin-left: 5px" />

			</h:column>
		</h:dataTable>
	</h:panelGroup>

	<%-- ########################################

            table to display metadata groups

    #########################################--%>
	<h:panelGroup rendered="#{!empty Metadaten.myGroups}">

		<htm:h4 style="margin-top:0px;margin-bottom:1px">
			<h:outputText value="#{msgs.metadataGroups}" />
		</htm:h4>
		
		<a4j:support event="onkeyup" requestDelay="1" />

		<htm:table style="#{Metadaten.nurLesenModus ? '' : 'min-width: 536px; '}">
			<x:dataList var="aGroup" value="#{Metadaten.myGroups}"
				layout="simple">
				<x:dataList var="member" value="#{aGroup.members}" layout="simple">
					<htm:tr
						rendered="#{member.class.simpleName != 'RenderablePersonMetadataGroup'}">
						<htm:td rowspan="#{aGroup.rowspan}" rendered="#{member.first}"
							styleClass="mdgroup">
							<h:outputText value="#{aGroup.label}" />
						</htm:td>
						<htm:td styleClass="mdgroup">
							<h:outputText value="#{member.label}" />
						</htm:td>
						<htm:td colspan="2" styleClass="mdgroup">
							<h:inputTextarea value="#{member.value}" readonly="#{Metadaten.nurLesenModus}"
								styleClass="metadatenInput"
								rendered="#{member.class.simpleName == 'RenderableLineEdit'}" />
							<h:inputText value="#{member.value}" styleClass="metadatenInput" readonly="#{Metadaten.nurLesenModus}"
								rendered="#{member.class.simpleName == 'RenderableEdit' && not member.readonly}" />
							<h:selectManyListbox value="#{member.selectedItems}" readonly="#{Metadaten.nurLesenModus}"
								styleClass="metadatenInput"
								rendered="#{member.class.simpleName == 'RenderableListBox'}">
								<f:selectItems value="#{member.items}" />
								<a4j:support event="onmouseup" requestDelay="1" />
							</h:selectManyListbox>
							<h:selectOneMenu value="#{member.value}" readonly="#{Metadaten.nurLesenModus}"
								styleClass="metadatenInput"
								rendered="#{member.class.simpleName == 'RenderableDropDownList'}">
								<f:selectItems value="#{member.items}" />
								<a4j:support event="onmouseup" requestDelay="1" />
							</h:selectOneMenu>
							<h:outputText id="myOutput" value="#{member.value}"
								rendered="#{member.class.simpleName == 'RenderableEdit' && member.readonly}" />
						</htm:td>
						<htm:td rowspan="#{aGroup.rowspan}" rendered="#{member.first}"
							styleClass="mdgroup">
							<h:commandLink action="#{aGroup.copy}" rendered="#{aGroup.copyable && not Metadaten.nurLesenModus}"
								title="#{msgs.metadatenKopieren}">
								<h:graphicImage value="/newpages/images/buttons/copy.gif" />
							</h:commandLink>
							<h:commandLink action="#{aGroup.delete}" rendered="#{not Metadaten.nurLesenModus}"
								title="#{msgs.metadatenLoeschen}">
								<h:graphicImage
									value="/newpages/images/buttons/waste1a_20px.gif"
									style="margin-left:3px" />
							</h:commandLink>
						</htm:td>
					</htm:tr>
					<x:dataList var="innerMember" value="#{member.members}"
						rendered="#{member.class.simpleName == 'RenderablePersonMetadataGroup'}">
						<htm:tr>
							<htm:td rowspan="#{aGroup.rowspan}" rendered="#{member.first && innerMember.first}"
								styleClass="mdgroup">
								<h:outputText value="#{aGroup.label}" />
							</htm:td>
							<htm:td rowspan="#{member.rowspan}"
								rendered="#{innerMember.first}" styleClass="mdgroup">
								<h:outputText value="#{member.label}" />
							</htm:td>
							<htm:td styleClass="mdgroup">
								<h:outputText value="#{innerMember.label}" />
							</htm:td>
							<htm:td styleClass="mdgroup">
								<h:inputTextarea value="#{innerMember.value}" readonly="#{Metadaten.nurLesenModus}"
									styleClass="metadatenInput"
									rendered="#{innerMember.class.simpleName == 'RenderableLineEdit'}" />
								<h:inputText value="#{innerMember.value}" readonly="#{Metadaten.nurLesenModus}"
									styleClass="metadatenInput"
									rendered="#{innerMember.class.simpleName == 'RenderableEdit' && not innerMember.readonly}" />
								<h:selectManyListbox value="#{innerMember.selectedItems}" readonly="#{Metadaten.nurLesenModus}"
									styleClass="metadatenInput"
									rendered="#{innerMember.class.simpleName == 'RenderableListbox'}">
									<f:selectItems value="#{innerMember.items}" />
									<a4j:support event="onmouseup" requestDelay="1" />
								</h:selectManyListbox>
								<h:selectOneMenu value="#{innerMember.value}" readonly="#{Metadaten.nurLesenModus}"
									styleClass="metadatenInput"
									rendered="#{innerMember.class.simpleName == 'RenderableDropDownList'}">
									<f:selectItems value="#{innerMember.items}" />
									<a4j:support event="onmouseup" requestDelay="1" />
								</h:selectOneMenu>
								<h:outputText id="myOutput" value="#{innerMember.value}"
									rendered="#{innerMember.class.simpleName == 'RenderableEdit' && innerMember.readonly}" />
							</htm:td>
							<htm:td rowspan="#{aGroup.rowspan}" rendered="#{member.first && innerMember.first}"
								styleClass="mdgroup">
								<h:commandLink action="#{aGroup.copy}" rendered="#{aGroup.copyable && not Metadaten.nurLesenModus}"
									title="#{msgs.metadatenKopieren}">
									<h:graphicImage value="/newpages/images/buttons/copy.gif" />
								</h:commandLink>
								<h:commandLink action="#{aGroup.delete}" rendered="#{not Metadaten.nurLesenModus}"
									title="#{msgs.metadatenLoeschen}">
									<h:graphicImage
										value="/newpages/images/buttons/waste1a_20px.gif"
										style="margin-left:3px" />
								</h:commandLink>
							</htm:td>
						</htm:tr>
					</x:dataList>
				</x:dataList>
				<htm:tr>
					<htm:td styleClass="mdgroupBorderBox" colspan="5">
						<h:outputText value="&nbsp;" escape="false" />
					</htm:td>
				</htm:tr>
			</x:dataList>
		</htm:table>
	</h:panelGroup>

	<%-- ########################################

                                                 Tabelle fuer die Metadaten

 		#########################################--%>
	<h:panelGroup rendered="#{!empty Metadaten.myMetadaten}" style="#{empty Metadaten.myPersonen ? 'margin-top:0px':'margin-top:15px;'}; display:block">
		<htm:h4 style="margin-top:0px; margin-bottom:1px">
			<h:outputText value="#{msgs.metadaten}" />
		</htm:h4>

		<a4j:support event="onkeyup" requestDelay="5" />

		<%-- oeffnen der Tabelle --%>
		<x:dataTable value="#{Metadaten.myMetadaten}" var="Item" rowClasses="metadatenGrauBackground" styleClass="Tabelle" headerClass="TabelleHeader"
			columnClasses="TabelleSpalteLinks,TabelleSpalteLinks,TabelleSpalteLinks">

			<%-- Metadaten --%>
			<x:column width="70px">
				<h:outputText value="#{Item.typ}" style="font-size: 11px" />
			</x:column>

			<%-- Metadaten --%>
			<h:column>
				<h:panelGrid columns="1" rendered="#{(Item.outputType == 'textarea')}">

					<h:inputTextarea value="#{Item.value}" immediate="true" readonly="#{Metadaten.nurLesenModus}" onchange="styleAnpassen(this)"
						styleClass="metadatenInput" style="width: 350px;height: 45px;">
						<a4j:support event="onmouseup" requestDelay="1" />
					</h:inputTextarea>

				</h:panelGrid>
				<h:panelGrid columns="1" rendered="#{(Item.outputType == 'input')}">
					<h:inputText value="#{Item.value}" onchange="styleAnpassen(this)" styleClass="metadatenInput" style="width: 350px;"
						readonly="#{Metadaten.nurLesenModus}" />
					<a4j:support event="onmouseup" requestDelay="1" />

				</h:panelGrid>


				<h:selectManyListbox onselect="styleAnpassen(this)" value="#{Item.selectedItems}" rendered="#{(Item.outputType == 'select')}"
					readonly="#{Metadaten.nurLesenModus}">
					<si:selectItems value="#{Item.items}" itemValue="#{element.label}" var="element" itemLabel="#{element.label}" />
					<a4j:support event="onchange" requestDelay="1" />
				</h:selectManyListbox>
				<h:selectOneMenu value="#{Item.selectedItem}" rendered="#{(Item.outputType == 'select1')}" readonly="#{Metadaten.nurLesenModus}">
					<si:selectItems value="#{Item.items}" itemValue="#{element.label}" var="element" itemLabel="#{element.label}" />
					<a4j:support event="onchange" requestDelay="1" />
				</h:selectOneMenu>
				<h:outputText id="myOutput" value="#{Item.value}"	
								rendered="#{(Item.outputType == 'readonly')}"
								styleClass="metadatenInput" style="width: 350px; border: 0 none;" />
			</h:column>





			<%-- Link fuer Details --%>
			<h:column rendered="#{not Metadaten.nurLesenModus}">
				<%-- Kopieren-Schaltknopf --%>
				<h:commandLink id="l5" action="#{Metadaten.Kopieren}" title="#{msgs.metadatenKopieren}">
					<h:graphicImage value="/newpages/images/buttons/copy.gif" />
					<f:param name="ID" value="#{Item.identifier}" />
					<x:updateActionListener property="#{Metadaten.curMetadatum}" value="#{Item}" />
				</h:commandLink>
				<%-- Transliterieren-Schaltknopf --%>
				<h:commandLink action="#{Metadaten.Transliterieren}" rendered="#{Item.typ=='russian Title'}" title="#{msgs.diesesFeldTransliterieren}">
					<h:graphicImage value="/newpages/images/buttons/translit.gif" style="margin-left:3px" />
					<f:param name="ID" value="#{Item.identifier}" />
					<x:updateActionListener property="#{Metadaten.curMetadatum}" value="#{Item}" />
				</h:commandLink>
				<%-- Loeschen-Schaltknopf --%>
				<h:commandLink id="l4" action="#{Metadaten.Loeschen}" title="#{msgs.metadatenLoeschen}">
					<h:graphicImage value="/newpages/images/buttons/waste1a_20px.gif" style="margin-left:3px" />
					<f:param name="ID" value="#{Item.identifier}" />
					<x:updateActionListener property="#{Metadaten.curMetadatum}" value="#{Item}" />
				</h:commandLink>

			</h:column>
		</x:dataTable>
	</h:panelGroup>

	<%-- ########################################

                          Speichern der Metadaten oder neue hinzufuegen

 		#########################################--%>
	<htm:table width="540">
		<htm:tr>
			<htm:td>
				<h:panelGroup rendered="#{not Metadaten.modusHinzufuegen && not Metadaten.modusHinzufuegenPerson && not Metadaten.addMetadataGroupMode && not Metadaten.nurLesenModus}"
					style="margin-top:10px">
					<%-- Hinzufuegen-Schaltknopf fuer Person--%>
					<h:commandLink id="l2" action="#{Metadaten.HinzufuegenPerson}" style="margin-left:2px" title="#{msgs.neuePersonAnlegen}"
						rendered="#{Metadaten.sizeOfRoles!=0}">


						<h:graphicImage value="/newpages/images/buttons/new.gif" style="border: 0px;vertical-align:middle" />
						<h:outputText value="#{msgs.neuePersonHinzufuegen}" />
					</h:commandLink>
					<htm:br />
					<%-- Hinzufuegen-Schaltknopf fuer Metadaten --%>
					<h:commandLink id="l1" action="#{Metadaten.Hinzufuegen}" style="margin-left:2px" title="#{msgs.neuesMetadatumAnlegen}"
						rendered="#{Metadaten.sizeOfMetadata!=0}">
						<h:graphicImage value="/newpages/images/buttons/new.gif" style="border: 0px;vertical-align:middle" />
						<h:outputText value="#{msgs.neueMetadatenHinzufuegen}" />
					</h:commandLink>
					<%-- Action link to add new metadata groups --%>
					<h:commandLink id="l3" action="#{Metadaten.showAddNewMetadataGroup}" style="margin-left:2px" title="#{msgs.addNewMetadataGroup}"
						rendered="#{Metadaten.addNewMetadataGroupLinkShowing}">
						<h:graphicImage value="/newpages/images/buttons/new.gif" style="border: 0px;vertical-align:middle" />
						<h:outputText value="#{msgs.addNewMetadataGroup}" />
					</h:commandLink>
				</h:panelGroup>
			</htm:td>
		</htm:tr>
	</htm:table>

</h:panelGroup>


