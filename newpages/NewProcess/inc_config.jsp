<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi"
	prefix="si"%>

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
