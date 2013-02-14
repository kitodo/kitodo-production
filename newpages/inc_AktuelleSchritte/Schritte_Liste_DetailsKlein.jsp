<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

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
				<htm:tr rendered="#{item.bearbeitungsbeginn !=null}">
					<htm:td width="150">
						<h:outputText value="#{msgs.bearbeitungsbeginn}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.bearbeitungsbeginn}" />
					</htm:td>
				</htm:tr>
				<htm:tr rendered="#{item.bearbeitungszeitpunkt !=null}">
					<htm:td width="150">
						<h:outputText value="#{msgs.zuletztBearbeitet}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.bearbeitungszeitpunkt}" />
					</htm:td>
				</htm:tr>
				<htm:tr rendered="#{item.bearbeitungsbenutzer.id !=0 && item.bearbeitungsbenutzer !=null}">
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
