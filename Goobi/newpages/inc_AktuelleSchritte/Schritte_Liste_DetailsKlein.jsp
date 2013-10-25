<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
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
<htm:table cellpadding="3" cellspacing="0" width="100%"
	styleClass="main_statistikboxen">

	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row1">
			<h:outputText value="#{msgs.eigenschaften}" />
		</htm:td>
	</htm:tr>

	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row2">

			<htm:table border="0" width="90%" cellpadding="2">
				<htm:tr valign="top">
					<htm:td width="150">
						<h:outputText value="#{msgs.titel}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.titelLokalisiert}" />
					</htm:td>
				</htm:tr>
				<htm:tr rendered="false">
					<htm:td width="150">
						<h:outputText value="#{msgs.id}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.id}" />
					</htm:td>
				</htm:tr>
				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.prioritaet}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.prioritaet}" />
					</htm:td>
				</htm:tr>
				<htm:tr rendered="#{item.bearbeitungsbeginn !=null && !HelperForm.anonymized}">
					<htm:td width="150">
						<h:outputText value="#{msgs.bearbeitungsbeginn}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.bearbeitungsbeginnAsFormattedString}" />
					</htm:td>
				</htm:tr>
				<htm:tr rendered="#{item.bearbeitungszeitpunkt !=null && !HelperForm.anonymized}">
					<htm:td width="150">
						<h:outputText value="#{msgs.zuletztBearbeitet}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.bearbeitungszeitpunktAsFormattedString}" />
					</htm:td>
				</htm:tr>
				<htm:tr rendered="#{item.bearbeitungsbenutzer.id !=0 && item.bearbeitungsbenutzer !=null && !HelperForm.anonymized}">
					<htm:td width="150">
						<h:outputText value="#{msgs.letzteAktualisierungDurch}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.bearbeitungsbenutzer.nachVorname}" />
					</htm:td>
				</htm:tr>
				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.aktualisierungstyp}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.editTypeEnum.title}" />
					</htm:td>
				</htm:tr>
				<x:dataList var="intern" style="font-weight: normal"
					rendered="#{item.eigenschaftenSize!=0}"
					value="#{item.eigenschaftenList}" layout="ordered list"
					rowCountVar="rowCount" rowIndexVar="rowIndex">
					<htm:tr rendered="#{item.bearbeitungsbenutzer.id !=0}">
						<htm:td width="150">
							<h:outputText value="#{intern.titel}:" />
						</htm:td>
						<htm:td>
							<h:outputText value="#{intern.wert}" />
						</htm:td>
					</htm:tr>
				</x:dataList>
			</htm:table>
		</htm:td>
	</htm:tr>

</htm:table>
