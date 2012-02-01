<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<h:panelGrid id="myFilterGrid" width="100%" columnClasses="standardTable_ColumnRight" rowClasses="standardTable_Row" columns="1">
	<h:panelGroup id="myFilter">
		<h:outputText value="#{msgs.selectFilter}: " />
		<%-- Filter Liste --%>
		<x:selectOneMenu forceId="true" id="select" style="width:18%;" value="#{Form.filter}" onclick="setFilter();">
			<si:selectItems id="pcid11" value="#{Form.user.filters}" var="item" itemLabel="#{item}" itemValue="#{item}" />
		</x:selectOneMenu>

		<%--Filter zur User-Liste hinzufuegen --%>
		<h:commandLink id="newFilterubid11" action="#{Form.addFilterToUser}" title="#{msgs.addFilter}" style="margin-left:5px">
			<h:graphicImage id="newFilterubid12" alt="Filter" value="/newpages/images/buttons/save1.gif" />
		</h:commandLink>

		<%-- remove filter from list --%>
		<h:commandLink id="removeFilterubid11" action="#{Form.removeFilterFromUser}" title="#{msgs.removeFilter}" style="margin-left:5px">
			<h:graphicImage id="removeFilterubid12" alt="Filter" value="/newpages/images/buttons/waste1_20px.gif" />
		</h:commandLink>
	</h:panelGroup>
</h:panelGrid>
<h:panelGrid id="aslsid2" width="100%" columnClasses="standardTable_Column,standardTable_ColumnRight" rowClasses="standardTable_Row"
	columns="2">
	<h:outputText id="aslsid3" value="#{msgs.treffer}: #{Form.page.totalResults}" rendered="#{showHits}" />
	<h:outputText id="aslsid3_alt" value="" rendered="#{!showHits}" />

	<h:panelGroup id="aslsid4">
		
		<h:outputText value="#{msgs.showAutomaticTasks}:"  rendered="#{showUserRelatedFilter}" />
		<x:selectBooleanCheckbox id="check3" forceId="true" value="#{Form.showAutomaticTasks}" rendered="#{showUserRelatedFilter}" onchange="document.getElementById('check2').checked=false; document.getElementById('FilterAlle').click()" style="margin-right:40px" />
		

		<h:outputText id="aslsid5" value="#{msgs.nurEigeneAufgabenAnzeigen}:"  rendered="#{showUserRelatedFilter}" />

		<x:selectBooleanCheckbox id="check1" forceId="true" value="#{Form.nurEigeneSchritte}" rendered="#{showUserRelatedFilter}" onchange="document.getElementById('check2').checked=false; document.getElementById('FilterAlle').click()" style="margin-right:40px" />

		<h:outputText id="aslsid6" value="#{msgs.nurOffeneAufgabenAnzeigen}:"  rendered="#{showUserRelatedFilter}" />
		<x:selectBooleanCheckbox id="check2" forceId="true" value="#{Form.nurOffeneSchritte}" rendered="#{showUserRelatedFilter}" onchange="document.getElementById('check1').checked=false; document.getElementById('FilterAlle').click()" style="margin-right:40px" />

		<h:outputText id="aslsid7" value="#{msgs.filter}: " />

		<x:inputText style="width:20%" forceId="true" id="filterfield" value="#{Form.filter}"
			onkeypress="return submitEnter('FilterAlle',event)" />
		<x:commandButton type="submit" id="FilterAlle" forceId="true" style="display:none" action="#{Form.FilterAlleStart}" />

		<h:commandLink id="aslsid9" action="#{Form.FilterAlleStart}" title="#{msgs.filterAnwenden}" style="margin-left:5px">
			<h:graphicImage id="aslsid10" alt="reload" value="/newpages/images/buttons/reload.gif" />
		</h:commandLink>

		<h:outputLink id="aslsid11" target="_blank" value="http://wiki.goobi.org/index.php/Filter_f%C3%BCr_Vorg%C3%A4nge">
			<h:graphicImage id="aslsid12" alt="help" title="#{msgs.hilfeZumFilter}" value="/newpages/images/buttons/help.png" style="margin-left:5px" />
		</h:outputLink>
		

	</h:panelGroup>

</h:panelGrid>
<script type="text/javascript"><!--
function setFilter() {
	var myFilter = document.getElementById('select').value;
	document.getElementById('filterfield').value = myFilter;
}
-->
</script>
<%-- 
<h:panelGrid id="myFilterGrid" width="100%" columnClasses="standardTable_ColumnRight" rowClasses="standardTable_Row_bottom" columns="1">
	<h:panelGroup id="myFilter">
		<h:outputText value="#{msgs.selectFilter}" />
	
		<x:selectOneMenu forceId="true" id="select" style="width:15%;height:20px" value="#{Form.user.filter}" onclick="setFilter();">
			<si:selectItems id="pcid11" value="#{Form.user.filters}" var="item" itemLabel="#{item.value}" itemValue="#{item}" />
		</x:selectOneMenu>

		<h:commandLink id="newFilterubid11" action="#{Form.user.addFilter}" title="#{msgs.addFilter}" style="margin-left:5px">
			<h:graphicImage id="newFilterubid12" alt="Filter" value="/images/buttons/reload.gif" />
		</h:commandLink>

		<h:commandLink id="removeFilterubid11" action="#{Form.user.removeFilter}" title="#{msgs.removeFilter}" style="margin-left:5px">
			<h:graphicImage id="removeFilterubid12" alt="Filter" value="/images/buttons/reload.gif" />
		</h:commandLink>
	</h:panelGroup>
</h:panelGrid>
--%>
