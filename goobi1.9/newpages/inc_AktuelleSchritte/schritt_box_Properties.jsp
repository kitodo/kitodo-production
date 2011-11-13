<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>


<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- ++++++++++++++++     Properties      ++++++++++++++++ --%>
<%-- ++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>

<h:form id="propform2">
	<%-- Box für die Bearbeitung der Details --%>
	<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen">

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1" colspan="2">
				<h:outputText value="#{msgs.erweiterteEigenschaften}" />
			</htm:td>
		</htm:tr>

		<%-- Formular für die Bearbeitung der Texte --%>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2" colspan="3">

			
			<x:dataTable id="eigenschaften" var="prop" value="#{AktuelleSchritteForm.processProperties}" style="border-bottom: 1px solid #F4BBA5;">

				<h:column>
					<h:outputText value="#{prop.name}"/>
				</h:column>
				
				<h:column>
					<h:outputText value="#{prop.type}"/>
				</h:column>
				
				<h:column>
					<h:inputText value="#{prop.value}"/>
				</h:column>
				
				<h:column>
					<h:outputText value="#{prop.possibleValues}"/>
				</h:column>

			</x:dataTable>
		
			</htm:td>
		</htm:tr>

		<htm:tr>
			<htm:td colspan="2" styleClass="eingabeBoxen_row3" align="right">
				<h:commandButton value="#{msgs.speichern}" action="#{AktuelleSchritteForm.saveProcessProperties}" />
			</htm:td>
		</htm:tr>
	</htm:table>
	<%-- // Box für die Bearbeitung der Details --%>

</h:form>