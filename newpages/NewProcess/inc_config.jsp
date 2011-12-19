<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi"
	prefix="si"%>

<%-- ######################################## 
				
	zusätzliche Daten aus der Konfiguration
					
	#########################################--%>
<h:panelGroup>
	<f:verbatim>
		<hr width="90%" />
	</f:verbatim>
</h:panelGroup>

<h:outputText value="#{msgs.zusaetzlicheDetails}"
	style="font-size:13;font-weight:bold;color:#00309C" />

<x:dataList var="intern" style="font-weight: normal;margin-left:30px"
	value="#{ProzesskopieForm.additionalFields}" layout="ordered list"
	rowCountVar="rowCount" rowIndexVar="rowIndex">
	<htm:table rendered="#{intern.showDependingOnDoctype}"
		style="margin-left:30px" cellspacing="0">
		<htm:tr>
			<htm:td width="150">
				<h:outputText value="#{intern.titel}:" />
			</htm:td>
			<htm:td>
				<h:inputText value="#{intern.wert}"
					styleClass="prozessKopieFeldbreite"
					rendered="#{intern.selectList==null}" />
				<h:selectOneMenu value="#{intern.wert}"
					styleClass="prozessKopieFeldbreite"
					rendered="#{intern.selectList!=null}">
					<f:selectItems value="#{intern.selectList}" />
				</h:selectOneMenu>
				<h:outputText value="*" rendered="#{intern.required}" />
			</htm:td>
		</htm:tr>
	</htm:table>
</x:dataList>
