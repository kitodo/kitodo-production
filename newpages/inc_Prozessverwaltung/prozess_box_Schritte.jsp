<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/dynamic" prefix="jd"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<a4j:loadBundle basename="Messages.messages" var="msgs" />

<%-- ===================== GROSSE SCHRITT BOX IM GELADENEN PROZESS ====================== --%>

<htm:h4 style="margin-top:15">
	<h:outputText value="#{msgs.arbeitsschritte}" />
</htm:h4>

<x:dataTable id="vorgaenge" styleClass="standardTable" width="100%"
	cellspacing="1px" cellpadding="1px" headerClass="standardTable_Header"
	rowClasses="standardTable_Row1,standardTable_Row2"
	columnClasses="standardTable_ColumnCentered,standardTable_Column,standardTable_ColumnCentered,standardTable_ColumnCentered"
	var="item" value="#{ProzessverwaltungForm.myProzess.schritteList}">

	<%-- ===================== Reihenfolge ====================== --%>
	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.nr}" />
		</f:facet>
		<h:outputText value="#{item.reihenfolge}" />
		<%-- Schaltknopf: Reihenfolge nach oben --%>
		<a4j:commandLink action="#{ProzessverwaltungForm.reihenfolgeUp}" reRender="vorgaenge"
			rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
			<h:graphicImage value="/newpages/images/buttons/order_up_klein.gif"
				style="margin-left:5px;vertical-align:middle" />
			<x:updateActionListener property="#{ProzessverwaltungForm.mySchritt}"
				value="#{item}" />
		</a4j:commandLink>
		<%-- Schaltknopf: Reihenfolge nach unten --%>
		<a4j:commandLink action="#{ProzessverwaltungForm.reihenfolgeDown}" reRender="vorgaenge"
			rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
			<h:graphicImage value="/newpages/images/buttons/order_down_klein.gif"
				style="vertical-align:middle" />
			<x:updateActionListener property="#{ProzessverwaltungForm.mySchritt}"
				value="#{item}" />
		</a4j:commandLink>
	</h:column>
	<%-- ===================== // Reihenfolge ====================== --%>

	<h:column rendered="#{true}">
		<f:facet name="header">
			<h:outputText value="#{msgs.titel}" />
		</f:facet>

		<a4j:commandLink reRender="auflistungIntern,myself" id="myself"
			style="color:black">
			<h:graphicImage value="/newpages/images/plus.gif"
				style="margin-right:4px" rendered="#{!item.panelAusgeklappt}" />
			<h:graphicImage value="/newpages/images/minus.gif"
				style="margin-right:4px" rendered="#{item.panelAusgeklappt}" />
			<x:updateActionListener value="#{item.panelAusgeklappt?false:true}"
				property="#{item.panelAusgeklappt}" />
			<h:outputText value="#{item.titel}" />
			<a4j:ajaxListener type="org.ajax4jsf.ajax.ForceRender" />
		</a4j:commandLink>

		<h:panelGroup id="auflistungIntern">
			<x:div style="width:90%;margin-top;margin-left:12px;margin-top:5px"
				rendered="#{item.panelAusgeklappt}">
				<%-- Schrittdetails --%>
				<%@include file="prozess_box_Schritte_box_DetailsKlein.jsp"%>
			</x:div>
		</h:panelGroup>
	</h:column>

	<%-- +++++++++++++++++  alle Eigenschaften auflisten - mit CSS ++++++++++++++++++++++++ --%>
	<h:column rendered="#{false}">
		<f:facet name="header">
			<x:div>
				<x:headerLink immediate="true">
					<h:graphicImage value="/newpages/images/plus.gif"
						style="margin-right:4px;" rendered="#{isCollapsed}" />
					<h:graphicImage value="/newpages/images/minus.gif"
						style="margin-right:4px;" rendered="#{!isCollapsed}" />
				</x:headerLink>
				<h:outputText value="#{item.titelLokalisiert}"
					rendered="#{isCollapsed}" />
			</x:div>
		</f:facet>

		<h:panelGroup>
			<jd:hideableController for="tab">
				<f:facet name="show">
					<h:panelGroup>
						<h:graphicImage value="/newpages/images/minus.gif"
							style="margin-right:5px" />
						<h:outputText value="#{item.titelLokalisiert}" />
					</h:panelGroup>
				</f:facet>
				<f:facet name="hide">
					<h:panelGroup>
						<h:graphicImage value="/newpages/images/plus.gif"
							style="margin-right:5px" />
						<h:outputText value="#{item.titelLokalisiert}" />
					</h:panelGroup>
				</f:facet>
			</jd:hideableController>

			<jd:hideableArea id="tab">
				<h:panelGrid columns="1" style="margin:10px">
					<%@include file="prozess_box_Schritte_box_DetailsKlein.jsp"%>
				</h:panelGrid>
			</jd:hideableArea>
		</h:panelGroup>
	</h:column>
	<%-- +++++++++++++++++  // alle Eigenschaften auflisten mit CSS ++++++++++++++++++++++++ --%>

	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.status}" />
		</f:facet>
		<h:panelGrid columns="2" align="center" id="statuscolumn">
			
			<h:graphicImage value="#{item.bearbeitungsstatusEnum.bigImagePath}"
                title="#{item.bearbeitungsstatusEnum.title}"/>

			<h:panelGrid columns="1" cellpadding="0" cellspacing="0"
				rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
				<%-- Bearbeitungsstatus hoch und runter --%>
				<a4j:commandLink action="#{ProzessverwaltungForm.SchrittStatusUp}"
					title="#{msgs.statusHoeherSetzen}" reRender="statuscolumn">
					<h:graphicImage
						value="/newpages/images/buttons/order_right_klein.gif" />
					<x:updateActionListener
						property="#{ProzessverwaltungForm.mySchrittReload}"
						value="#{item}" />
					<a4j:ajaxListener type="org.ajax4jsf.ajax.ForceRender" />
				</a4j:commandLink>
				<%-- Bearbeitungsstatus hoch und runter --%>
				<a4j:commandLink action="#{ProzessverwaltungForm.SchrittStatusDown}"
					title="#{msgs.statusRunterSetzen}" reRender="statuscolumn">
					<h:graphicImage
						value="/newpages/images/buttons/order_left_klein.gif" />
					<x:updateActionListener
						property="#{ProzessverwaltungForm.mySchrittReload}"
						value="#{item}" />
					<a4j:ajaxListener type="org.ajax4jsf.ajax.ForceRender" />
				</a4j:commandLink>
			</h:panelGrid>

		</h:panelGrid>
	</h:column>

	<h:column
		rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
		<f:facet name="header">
			<h:outputText value="#{msgs.auswahl}" />
		</f:facet>
		<%-- Bearbeiten-Schaltknopf --%>
		<h:commandLink action="ProzessverwaltungBearbeitenSchritt"
			title="#{msgs.detailsDesSchritts}">
			<h:graphicImage value="/newpages/images/buttons/goInto.gif" />
			<x:updateActionListener
				property="#{ProzessverwaltungForm.mySchrittReload}" value="#{item}" />
			<x:updateActionListener
				property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
		</h:commandLink>
	</h:column>

</x:dataTable>

<%-- Neu-Schaltknopf --%>
<h:commandLink action="#{ProzessverwaltungForm.SchrittNeu}"
	value="#{msgs.schrittHinzufuegen}" title="#{msgs.schrittHinzufuegen}"
	rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
</h:commandLink>
