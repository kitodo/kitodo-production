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
<%-- ########################################

        Formular fuer neue Person
        
  #########################################--%>
<h:panelGroup rendered="#{Metadaten.modusHinzufuegenPerson}">
	<htm:h3 style="margin-top:10px">
		<h:outputText value="#{msgs.personBearbeiten}" />
	</htm:h3>
	<htm:table cellpadding="3" cellspacing="0" style="width:540px"
		styleClass="eingabeBoxen">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1">
				<h:outputText value="#{msgs.personBearbeiten}" />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2">

				<h:panelGrid columns="2">
					<%-- vorname --%>
					<h:outputLabel for="vorname" value="#{msgs.vorname}" />
					<h:panelGroup>
						<h:inputText id="vorname" style="width: 400px;margin-right:15px"
							value="#{Metadaten.tempPersonVorname}" />
						<x:message for="vorname" style="color: red"
							replaceIdWithLabel="true" />
					</h:panelGroup>

					<%-- nachname --%>
					<h:outputLabel for="nachname" value="#{msgs.nachname}" />
					<h:panelGroup>
						<h:inputText id="nachname" style="width: 400px;margin-right:15px"
							value="#{Metadaten.tempPersonNachname}" />
						<x:message for="nachname" style="color: red"
							replaceIdWithLabel="true" />
					</h:panelGroup>

					<%-- Rolle --%>
					<h:outputLabel for="Rolle" value="#{msgs.rolle}" />
					<h:panelGroup>
						<h:selectOneMenu id="Rolle" value="#{Metadaten.tempPersonRolle}"
							style="width: 400px;margin-right:15px">
							<f:selectItems value="#{Metadaten.addableRollen}" />
						</h:selectOneMenu>
						<x:message for="Rolle" style="color: red"
							replaceIdWithLabel="true" />
					</h:panelGroup>

				</h:panelGrid>
			</htm:td>
		</htm:tr>

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3">
				<h:commandButton action="#{Metadaten.Abbrechen}"
					value="#{msgs.abbrechen}" immediate="true"></h:commandButton>
				<x:commandButton id="absenden2" forceId="true" type="submit"
					action="#{Metadaten.SpeichernPerson}"
					value="#{msgs.dieAenderungenSpeichern}"></x:commandButton>
			</htm:td>
		</htm:tr>

	</htm:table>
</h:panelGroup>
