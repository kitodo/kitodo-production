<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/dynamic" prefix="jd"%>
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
<a4j:loadBundle basename="messages.messages" var="msgs" />

<htm:h4>
	<h:outputText value="#{msgs.batches}" />
</htm:h4>




<%-- +++++++++++++++++  Anzeigefilter ++++++++++++++++++++++++ 

	<x:aliasBeansScope>
		<x:aliasBean alias="#{Form}" value="#{BatchForm}" />
		<x:aliasBean alias="#{showUserRelatedFilter}" value="#{true}" />
		<x:aliasBean alias="#{showHits}" value="#{true}" />
		<f:subview id="sub1">
			<jsp:include page="/newpages/inc/Process_Filter.jsp" />
		</f:subview>
	</x:aliasBeansScope>

--%>



<%-- Datentabelle --%>
<x:dataTable id="auflistung" styleClass="standardTable" width="100%"
	cellspacing="1px" cellpadding="1px" headerClass="standardTable_Header"
	rowClasses="standardTable_Row1,standardTable_Row2,standardTable_Row1,standardTable_Row2"
	var="item" value="#{BatchForm.page.listReload}">

	<%-- batch id --%>
	<x:column style="text-align:center">
		<f:facet name="header">
			<h:outputText value="#{msgs.id}" />
		</f:facet>
		<h:outputText value="#{item.id}" />
	</x:column>

	<%-- batch size --%>
	<x:column style="text-align:center">
		<f:facet name="header">
			<h:outputText value="#{msgs.size}" />
		</f:facet>
		<h:outputText value="#{item.listSize}" />
	</x:column>

	<%-- process list --%>
	<x:column style="text-align:center">
		<f:facet name="header">
			<h:outputText value="#{msgs.processes}" />
		</f:facet>
		<h:panelGroup>
			<x:dataTable var="process" value="#{item.batchList}">
				<x:column>
					<h:outputText value="#{process.titel}" />
				</x:column>
			</x:dataTable>
		</h:panelGroup>
	</x:column>






	<x:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.status}" />
		</f:facet>
		<a4j:commandLink reRender="auflistungIntern,myself" id="myself" style="color:black">
			<h:graphicImage value="/newpages/images/plus.gif" style="margin-right:4px" rendered="#{!item.batchDisplayHelper.panelOpen}" />
			<h:graphicImage value="/newpages/images/minus.gif" style="margin-right:4px" rendered="#{item.batchDisplayHelper.panelOpen}" />
			<x:updateActionListener value="#{item.batchDisplayHelper.panelOpen?false:true}" property="#{item.batchDisplayHelper.panelOpen}" />
			<h:graphicImage value="/newpages/images/fortschritt/ende_links.gif"
			rendered="true" />
		<h:graphicImage value="/newpages/images/fortschritt/gr.gif"
			style="width:#{item.batchDisplayHelper.fortschritt3 * 0.8}px;height:10px" />
		<h:graphicImage value="/newpages/images/fortschritt/ge.gif"
			style="width:#{item.batchDisplayHelper.fortschritt2 * 0.8}px;height:10px" />
		<h:graphicImage value="/newpages/images/fortschritt/rt.gif"
			style="width:#{item.batchDisplayHelper.fortschritt1 * 0.8}px;height:10px" />
		<h:graphicImage value="/newpages/images/fortschritt/ende_rechts.gif"
			rendered="true" />
			<a4j:ajaxListener type="org.ajax4jsf.ajax.ForceRender" />
		</a4j:commandLink>
		
		<h:panelGroup id="auflistungIntern">
		<x:dataTable var="batchDisplayItem" value="#{item.stepList}" rendered="#{item.batchDisplayHelper.panelOpen}">
				<x:column>
					<h:outputText value="#{batchDisplayItem.stepOrder}" />
				</x:column>
				<x:column>
					<h:outputText value="#{batchDisplayItem.stepTitle}" />
				</x:column>
				<x:column>
					<%-- 		<h:outputText value="#{batchDisplayItem.stepStatus}" />--%>
					<h:graphicImage
						value="#{batchDisplayItem.stepStatus.smallImagePath}"
						title="#{batchDisplayItem.stepTitle}"
						rendered="#{batchDisplayItem.stepStatus == 'OPEN' || batchDisplayItem.stepStatus == 'LOCKED'}" />
					<h:graphicImage
						value="#{batchDisplayItem.stepStatus.smallImagePath}"
						title="#{batchDisplayItem.stepStatus.title}: #{step.bearbeitungsbenutzer!=null && step.bearbeitungsbenutzer.id!=0?step.bearbeitungsbenutzer.nachVorname:''} (#{step.bearbeitungszeitpunkt !=null?step.bearbeitungszeitpunktAsFormattedString:''})  - #{step.editTypeEnum.title}"
						rendered="#{(batchDisplayItem.stepStatus == 'DONE' || batchDisplayItem.stepStatus == 'INWORK') && !HelperForm.anonymized}" />
					<h:graphicImage
						value="#{batchDisplayItem.stepStatus.smallImagePath}"
						title="#{batchDisplayItem.stepStatus.title}: #{step.editTypeEnum.title}"
						rendered="#{(batchDisplayItem.stepStatus == 'DONE' || batchDisplayItem.stepStatus == 'INWORK') && HelperForm.anonymized}" />
				</x:column>


			</x:dataTable>
		
		</h:panelGroup>
	

	</x:column>
	<%-- batch project --%>
	<x:column style="text-align:center">
		<f:facet name="header">
			<h:outputText value="#{msgs.project}" />
		</f:facet>
		<h:outputText value="#{item.project.titel}" />
	</x:column>

	<%-- batch currentStep --%>
	<x:column style="text-align:center">
		<f:facet name="header">
			<h:outputText value="#{msgs.currentStep}" />
		</f:facet>
		<h:outputText
			value="#{item.currentStep.stepTitle} #{item.currentStep.stepOrder} #{item.currentStep.stepStatus}" />
	</x:column>


	<%--actions --%>
	<x:column style="text-align:center">
		<f:facet name="header">
			<h:outputText value="#{msgs.auswahl}" />
		</f:facet>

		<h:commandLink id="take"
			rendered="#{item.currentStep.stepStatus == 'OPEN'}"
			action="#{BatchForm.BatchDurchBenutzerUebernehmen}"
			title="#{msgs.bearbeitungDiesesSchrittsUebernehmen}">
			<h:graphicImage value="/newpages/images/buttons/admin2a.gif" />
			<x:updateActionListener property="#{BatchForm.batch}" value="#{item}" />
		</h:commandLink>

		<h:commandLink action="BatchesEdit" id="view1"
			rendered="#{item.currentStep.stepStatus == 'INWORK' && item.user.id == LoginForm.myBenutzer.id}"
			title="#{msgs.inBearbeitungDurch}: #{item.user!=null && item.user.id!=0 ? item.user.nachVorname:''}">
			<h:graphicImage value="/newpages/images/buttons/admin1b.gif" />
			<x:updateActionListener property="#{BatchForm.batch}" value="#{item}" />
		</h:commandLink>


		<h:commandLink action="BatchesEdit" id="view2"
			rendered="#{item.currentStep.stepStatus == 'INWORK' && item.user.id != LoginForm.myBenutzer.id}"
			title="#{msgs.inBearbeitungDurch}: #{item.user!=null && item.user.id!=0 ? item.user.nachVorname:''}">
			<h:graphicImage value="/newpages/images/buttons/admin3b.gif" />
			<x:updateActionListener property="#{BatchForm.batch}" value="#{item}" />
		</h:commandLink>


	</x:column>


</x:dataTable>

<%-- ===================== Datascroller für die Ergebnisse ====================== --%>
<x:aliasBean alias="#{mypage}" value="#{BatchForm.page}">
	<jsp:include page="/newpages/inc/datascroller.jsp" />
</x:aliasBean>
<%-- ===================== // Datascroller für die Ergebnisse ====================== --%>

<%-- Schritte auflisten 
<%@include file="Schritte_Liste_Action.jsp"%>--%>
