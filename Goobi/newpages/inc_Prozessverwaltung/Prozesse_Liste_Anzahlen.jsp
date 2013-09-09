<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/chart" prefix="jc"%>
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
<htm:h4 rendered="#{ProzessverwaltungForm.myAnzahlList != null}" style="margin-top:20px">
	<h:outputText value="#{msgs.anzahlDerArtikelUndImages}" />
</htm:h4>

<%-- Datentabelle --%>
<x:dataTable styleClass="standardTable" width="100%" cellspacing="1px"
	cellpadding="1px" headerClass="standardTable_Header" style="margin-top:5px"
	rowClasses="standardTable_Row1,standardTable_Row2"
	columnClasses="standardTable_Column"
	rendered="#{ProzessverwaltungForm.myAnzahlList != null}" var="item"
	value="#{ProzessverwaltungForm.myAnzahlList}">

	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.titel}" />
		</f:facet>
		<h:outputText value="#{item.title}" />
	</h:column>

	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.images}" />
		</f:facet>
		<h:outputText value="#{item.images}" />
	</h:column>
	
	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.relativeAnzahl}" />
		</f:facet>
		<h:graphicImage value="/newpages/images/fortschritt/rt.gif"
			style="width:#{item.relImages * 1}%;height:10px" title="#{item.images} #{msgs.images}"/>
	</h:column>
	
    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.docstructs}" />
        </f:facet>
        <h:outputText value="#{item.docstructs}" />
    </h:column>
    
    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.relativeAnzahl}" />
        </f:facet>
        <h:graphicImage value="/newpages/images/fortschritt/ge.gif"
            style="width:#{item.relDocstructs * 1}%;height:10px" title="#{item.docstructs} #{msgs.docstructs}"/>
    </h:column>
    
    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.metadata}" />
        </f:facet>
        <h:outputText value="#{item.metadata}" />
    </h:column>
    
    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.relativeAnzahl}" />
        </f:facet>
        <h:graphicImage value="/newpages/images/fortschritt/gr.gif"
            style="width:#{item.relMetadata * 1}%;height:10px" title="#{item.metadata} #{msgs.metadata}"/>
    </h:column>

	
</x:dataTable>

<htm:table cellpadding="3" cellspacing="0" style="margin-top:10px; width:250px"
	styleClass="main_statistikboxen" rendered="#{ProzessverwaltungForm.myAnzahlList != null}">
	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row1">
			<h:outputText value="#{msgs.zusammenfassung}" />
		</htm:td>
	</htm:tr>
	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row2">
			<h:panelGrid width="100%" columns="2" columnClasses="columnLinks,columnRechts">
				<h:outputText value="#{msgs.baendeGesamt}:" />
				<h:outputText value="#{ProzessverwaltungForm.myAnzahlSummary.sumProcesses}" />
				<h:outputText value="#{msgs.imagesGesamt}:" />
                <h:outputText value="#{ProzessverwaltungForm.myAnzahlSummary.sumImages}" />
                <h:outputText value="#{msgs.imagesDurchschnitt}:" />
                <h:outputText value="#{ProzessverwaltungForm.myAnzahlSummary.averageImages}" />
                <h:outputText value="#{msgs.docstructsGesamt}:" />
                <h:outputText value="#{ProzessverwaltungForm.myAnzahlSummary.sumDocstructs}" />
                <h:outputText value="#{msgs.docstructsDurchschnitt}:" />
                <h:outputText value="#{ProzessverwaltungForm.myAnzahlSummary.averageDocstructs}" />
                <h:outputText value="#{msgs.metadataGesamt}:" />
				<h:outputText value="#{ProzessverwaltungForm.myAnzahlSummary.sumMetadata}" />
				<h:outputText value="#{msgs.metadataDurchschnitt}:" />
				<h:outputText value="#{ProzessverwaltungForm.myAnzahlSummary.averageMetadata}" />
			</h:panelGrid>
		</htm:td>
	</htm:tr>
</htm:table>

<%-- ++++++++++++++++     // Action      ++++++++++++++++ --%>
