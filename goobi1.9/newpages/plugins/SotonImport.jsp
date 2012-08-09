<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>



<h:dataTable var="docstruct" id="sotondocstructs" value="#{MassImportForm.docstructs}" headerClass="Tabelle2">


	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.orderNumber}" />
		</f:facet>
		<h:inputText value="#{docstruct.order}" >
			<f:validateLongRange minimum="1"/>
		</h:inputText>
	</h:column>

	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.volume}" />
		</f:facet>
		<h:inputText id="Volume" value="#{docstruct.volumeProperty.value}" required="true">
			<f:validateLength minimum="1"/>
			
		</h:inputText>
		<h:message for="Volume" >The value for volume is empty.</h:message>
		

	</h:column>

	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.part}" />
		</f:facet>
		<h:inputText value="#{docstruct.partProperty.value}" id="prpvw15_2"/>
	</h:column>


	<h:column>
		<f:facet name="header">
			<h:outputText value="#{msgs.year}" />
		</f:facet>
		<h:inputText value="#{docstruct.yearProperty.value}" id="prpvw15_3"/>
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