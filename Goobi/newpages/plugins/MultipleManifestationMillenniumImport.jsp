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

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<h:dataTable var="docstruct" id="docstructs" value="#{MassImportForm.docstructs}" headerClass="Tabelle2">

    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.manifestationType}" />
        </f:facet>
        <h:selectOneMenu value="#{docstruct.docStruct}">
            <si:selectItems value="#{MassImportForm.plugin.possibleDocstructs}" itemLabel="#{test}" itemValue="#{test}" var="test" />
        </h:selectOneMenu>
    </h:column>


    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.reihenfolge}" />
        </f:facet>
        <h:inputText value="#{docstruct.order}" />
    </h:column>

    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.volume}" />
        </f:facet>
        <h:selectOneMenu value="#{docstruct.volumeProperty.value}" id="prpvw15_2">
            <si:selectItems id="prpvw15_2_2" value="#{docstruct.volumeProperty.possibleValues}" var="props" itemLabel="#{props}" itemValue="#{props}" />
        </h:selectOneMenu>

    </h:column>

    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.copy}" />
        </f:facet>
        <h:selectOneMenu value="#{docstruct.copyProperty.value}" id="prpvw15_1">
            <si:selectItems id="prpvw15_1_2" value="#{docstruct.copyProperty.possibleValues}" var="props" itemLabel="#{props}" itemValue="#{props}" />
        </h:selectOneMenu>

    </h:column>

    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.auswahl}" />
        </f:facet>
        <h:commandLink action="#{MassImportForm.plugin.addDocstruct}">
            <h:graphicImage value="/newpages/images/plus.gif" style="margin-right:4px" />

        </h:commandLink>

        <h:commandLink action="#{MassImportForm.plugin.deleteDocstruct}" rendered="#{MassImportForm.docstructssize > 1}">
            <h:graphicImage value="/newpages/images/minus.gif" style="margin-right:4px" />
            <x:updateActionListener property="#{MassImportForm.plugin.docstruct}" value="#{docstruct}" />
        </h:commandLink>
    </h:column>

</h:dataTable>
