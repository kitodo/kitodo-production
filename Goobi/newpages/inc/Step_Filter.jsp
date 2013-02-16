<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
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
