<%--
 *
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 *
--%>

<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/chart" prefix="jc"%>

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
