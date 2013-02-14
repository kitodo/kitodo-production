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
	styleClass="main_statistikboxen"
	rendered="#{ProzessverwaltungForm.modusBearbeiten!='vorlage'}">

	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row1">
			<h:outputText value="#{msgs.vorlage}" />
		</htm:td>
		<htm:td styleClass="main_statistikboxen_row1" align="right">
			<h:commandLink action="#{ProzessverwaltungForm.Reload}">
				<h:graphicImage value="/newpages/images/reload.gif" />
			</h:commandLink>
		</htm:td>
	</htm:tr>

		<htm:tr>
			<htm:td styleClass="main_statistikboxen_row2" colspan="2">

				<htm:table border="0" width="90%" cellpadding="2">
					<htm:tr>
						<htm:td width="150">
							<h:outputText value="#{msgs.herkunft}:" />
						</htm:td>
						<htm:td>
							<h:outputText value="#{ProzessverwaltungForm.myVorlage.herkunft}" />
						</htm:td>
						<htm:td rowspan="2" align="right">
							<h:commandLink title="#{msgs.bearbeiten}"
								action="#{NavigationForm.Reload}">
								<h:graphicImage value="/newpages/images/buttons/edit_20.gif" />
								<x:updateActionListener
									property="#{ProzessverwaltungForm.modusBearbeiten}"
									value="vorlage" />
							</h:commandLink>

							<h:commandLink title="#{msgs.loeschen}"
								action="#{ProzessverwaltungForm.VorlageLoeschen}"
								style="margin-right:20px">
								<h:graphicImage value="/newpages/images/buttons/waste1a_20px.gif" />
							</h:commandLink>
						</htm:td>
					</htm:tr>

				</htm:table>

			</htm:td>
		</htm:tr>
</htm:table>


<%-- Box für die Bearbeitung der Details --%>
<htm:table cellpadding="3" cellspacing="0" width="100%"
	styleClass="eingabeBoxen"
	rendered="#{ProzessverwaltungForm.modusBearbeiten=='vorlage'}">

	<htm:tr>
		<htm:td styleClass="eingabeBoxen_row1" colspan="2">
			<h:outputText value="#{msgs.vorlage}" />
		</htm:td>
	</htm:tr>

	<h:form id="vorform" onkeypress="ifEnterClick(event, 'vorform2:vorform:absenden');">
		<%-- Formular für die Bearbeitung der Vorlage --%>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2" colspan="2">
				<h:panelGrid columns="2">

					<%-- Felder --%>
					<h:outputLabel for="titel2" value="#{msgs.herkunft}" />
					<h:panelGroup>
						<h:inputText id="titel2" style="width: 300px;margin-right:15px"
							value="#{ProzessverwaltungForm.myVorlage.herkunft}"
							required="true" />
						<x:message for="titel2" style="color: red"
							replaceIdWithLabel="true" />
					</h:panelGroup>

				</h:panelGrid>

			</htm:td>
		</htm:tr>

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3" align="left">
				<h:commandButton value="#{msgs.abbrechen}" immediate="true"
					action="#{NavigationForm.Reload}">
					<x:updateActionListener
						property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
				</h:commandButton>
			</htm:td>
			<htm:td styleClass="eingabeBoxen_row3" align="right">
				<h:commandButton value="#{msgs.uebernehmen}" id="absenden"
					action="#{ProzessverwaltungForm.VorlageUebernehmen}">
					<x:updateActionListener
						property="#{ProzessverwaltungForm.modusBearbeiten}" value="" />
				</h:commandButton>
			</htm:td>
		</htm:tr>
	</h:form>
</htm:table>
<%-- // Box für die Bearbeitung der Details --%>
