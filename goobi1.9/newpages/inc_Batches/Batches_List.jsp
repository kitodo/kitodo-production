<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/dynamic" prefix="jd"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<a4j:loadBundle basename="Messages.messages" var="msgs" />

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

	<%-- workflow status --%>
	<x:column style="text-align:center">
		<f:facet name="header">
			<h:outputText value="#{msgs.status}" />
		</f:facet>
		<h:panelGroup>
			<x:dataTable var="batchDisplayItem" value="#{item.stepList}">
				<x:column>
					<h:outputText value="#{batchDisplayItem.stepOrder}" />
				</x:column>
				<x:column>
					<h:outputText value="#{batchDisplayItem.stepTitle}" />
				</x:column>
				<x:column>
					<h:outputText value="#{batchDisplayItem.stepStatus}" />
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
		<h:outputText value="#{item.currentStep.stepTitle} #{item.currentStep.stepOrder} #{item.currentStep.stepStatus}" />
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
			<x:updateActionListener property="#{BatchForm.batch}"
				value="#{item}" />
		</h:commandLink>

		
		<h:commandLink action="BatchesEdit" id="view2"
			rendered="#{item.currentStep.stepStatus == 'INWORK' && item.user.id != LoginForm.myBenutzer.id}"
			title="#{msgs.inBearbeitungDurch}: #{item.user!=null && item.user.id!=0 ? item.user.nachVorname:''}">
			<h:graphicImage value="/newpages/images/buttons/admin3b.gif" />
			<x:updateActionListener property="#{BatchForm.batch}"
				value="#{item}" />
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
