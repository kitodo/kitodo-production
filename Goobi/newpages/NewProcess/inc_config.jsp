<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
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
<%-- ######################################## 
				
	zusaetzliche Daten aus der Konfiguration
					
	#########################################--%>
<h:panelGroup>
	<f:verbatim>
		<hr width="90%" />
	</f:verbatim>
</h:panelGroup>

<h:outputText value="#{msgs.zusaetzlicheDetails}" style="font-size:13;font-weight:bold;color:#00309C" />

<htm:table style="margin-left:30px" cellspacing="0">
	<x:dataList var="intern" style="font-weight: normal;margin-left:30px" value="#{ProzesskopieForm.additionalFields}" layout="ordered list"
		rowCountVar="rowCount" rowIndexVar="rowIndex">
		<htm:tr rendered="#{intern.showDependingOnDoctype}">
			<htm:td width="150">
				<h:outputText value="#{intern.titel}:" />
			</htm:td>
			<htm:td>
				<h:inputText value="#{intern.wert}" styleClass="prozessKopieFeldbreite" rendered="#{intern.selectList==null}" />
		<%--  		<h:selectOneMenu  value="#{intern.wert}" styleClass="prozessKopieFeldbreite" rendered="#{intern.selectList!=null}">
					<f:selectItems value="#{intern.selectList}" />
				</h:selectOneMenu> --%>
				<h:selectOneListbox  value="#{intern.wert}" styleClass="prozessKopieFeldbreite" rendered="#{intern.selectList!=null}">
					<f:selectItems value="#{intern.selectList}" />
				</h:selectOneListbox>
				
				
				<h:outputText value="*" rendered="#{intern.required}" />
			</htm:td>
		</htm:tr>

	</x:dataList>

	<htm:tr styleClass="rowTop">
		<htm:td width="150" style="">
			<h:outputText value="#{msgs.addToProcessLog}:" />
		</htm:td>
		<htm:td>
			<h:inputTextarea value="#{ProzesskopieForm.addToWikiField}" styleClass="prozessKopieFeldbreite" />
		</htm:td>
	</htm:tr>

</htm:table>
<h:outputText value="#{msgs.requiredField}" />
