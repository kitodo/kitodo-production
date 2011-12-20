<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%--
  ~ This file is part of the Goobi Application - a Workflow tool for the support of
  ~ mass digitization.
  ~
  ~ Visit the websites for more information.
  ~     - http://gdz.sub.uni-goettingen.de
  ~     - http://www.goobi.org
  ~     - http://launchpad.net/goobi-production
  ~
  ~ This program is free software; you can redistribute it and/or modify it under
  ~ the terms of the GNU General Public License as published by the Free Software
  ~ Foundation; either version 2 of the License, or (at your option) any later
  ~ version.
  ~
  ~ This program is distributed in the hope that it will be useful, but WITHOUT ANY
  ~ WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  ~ PARTICULAR PURPOSE. See the GNU General Public License for more details. You
  ~ should have received a copy of the GNU General Public License along with this
  ~ program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  ~ Suite 330, Boston, MA 02111-1307 USA
  --%>

<h:panelGrid id="pcid3" columns="4" columnClasses="standardTable_ColumnLeft,standardTable_Column,standardTable_ColumnRight" rowClasses="rowTop">
	<%-- SelectManyMenu Workflow --%>

	<h:selectManyListbox id="input5" value="#{form.selectedSteps}">
		<si:selectItems value="#{form.selectableSteps}" var="item" itemLabel="#{item}" itemValue="#{item}"/>
	</h:selectManyListbox>

	<h:selectOneMenu id="pcid10" style="height:20px" value="#{form.timeUnit}" converter="StatisticsTimeUnitConverter">
		<si:selectItems id="pcid11" value="#{form.selectableTimeUnits}" var="unit" itemLabel="#{unit.title}" itemValue="#{unit}" />
	</h:selectOneMenu>

	<h:panelGroup id="pcid12" style="height:20px">
		<h:selectBooleanCheckbox id="pcid13" value="#{form.referenceCurve}" title="#{msgs.refCurve}">
		</h:selectBooleanCheckbox>
		<h:outputLabel id="pcid14" value="#{msgs.refCurve}" for="pcid13" />
	</h:panelGroup>

	<h:commandButton id="pcid20" value="#{msgs.rerender}" >
			<a4j:support id="vwid13" event="onclick" reRender="vzid36" />
	</h:commandButton>
	
</h:panelGrid>

<h:panelGroup id="pcid16">
	<x:graphicImage forceId="true" id="vzid36" rendered="#{ProjekteForm.projectProgressImage != ''}"
		value="#{HelperForm.servletPathWithHostAsUrl}/pages/imagesTemp/#{ProjekteForm.projectProgressImage}" />

</h:panelGroup>

<%--
<h:panelGroup id="propErrorDisplay">
	<x:aliasBean alias="#{goobiObject}" value="#{form}">
		<f:subview id="pcid18">
			<%@include file="../inc/prop_errors.jsp"%>
		</f:subview>
	</x:aliasBean>
</h:panelGroup>
 --%>