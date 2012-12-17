<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
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