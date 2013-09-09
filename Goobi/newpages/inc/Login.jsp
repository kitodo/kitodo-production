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
<h:panelGrid columns="1" width="100%" align="center"
	rendered="#{LoginForm.myBenutzer != null}" style="margin-bottom:0px;">

	<h:commandLink action="#{LoginForm.Ausloggen}" styleClass="th_menu" id="logout">
		<h:outputText style="text-align:right" value="#{msgs.logout}:" />
		<htm:br />

		<%-- Mouse-Over für Benutzergruppenmitgliedschaft --%>
		<x:popup
			styleClass="popup"			
			closePopupOnExitingElement="true" closePopupOnExitingPopup="true"
			displayAtDistanceX="10" displayAtDistanceY="-10">

			<h:outputText rendered="#{LoginForm.myBenutzer != null}"
				style="font-weight: normal"
				value="#{LoginForm.myBenutzer.nachname}, #{LoginForm.myBenutzer.vorname}" />

			<f:facet name="popup">
				<h:panelGroup>
					<h:panelGrid columns="1" width="200">
						<h:outputText style="font-weight:bold"
							value="#{msgs.benutzergruppen}" />

						<x:dataList var="intern" style="font-weight: normal"
							rendered="#{LoginForm.myBenutzer.benutzergruppenSize != 0}"
							value="#{LoginForm.myBenutzer.benutzergruppenList}"
							layout="ordered list" rowCountVar="rowCount"
							rowIndexVar="rowIndex">
							<h:outputText value="#{intern.titel}" />
							<h:outputText value=";" rendered="#{rowIndex + 1 < rowCount}" />
						</x:dataList>

					</h:panelGrid>
				</h:panelGroup>
			</f:facet>
		</x:popup>

	</h:commandLink>
</h:panelGrid>

<h:panelGrid rendered="#{LoginForm.myBenutzer == null}" columns="1" width="90%" align="center"
	styleClass="loginBorder">
	<h:panelGroup id="logintable"
		rendered="#{LoginForm.myBenutzer == null && !LoginForm.schonEingeloggt}">
		<h:panelGrid columns="2" style="font-size: 9px" align="center">

			<h:outputText value="#{msgs.login}" />
			<h:panelGroup>
				<h:message id="messlogin" for="login" style="color: red" />
				<x:inputText id="login" forceId="true" style="width: 80px"
					value="#{LoginForm.login}" />
			</h:panelGroup>

			<h:outputText value="#{msgs.passwort}" />
			<h:panelGroup>
				<h:message id="messpasswort" for="passwort" style="color: red" />
				<x:inputSecret id="passwort" forceId="true" style="width: 80px"
					value="#{LoginForm.passwort}"
					onkeypress="return submitEnter('LoginAbsenden2',event)" />
			</h:panelGroup>

		</h:panelGrid>
		<h:commandLink action="#{LoginForm.Einloggen}">
			<h:outputText value="#{msgs.einloggen}"
				rendered="#{!LoginForm.schonEingeloggt}" />
			<h:outputText value="#{msgs.dennochEinloggen}"
				rendered="#{LoginForm.schonEingeloggt}" />
		</h:commandLink>
		<x:commandButton id="LoginAbsenden2" forceId="true"
			style="display:none" type="submit" action="#{LoginForm.Einloggen}"
			value="#{msgs.einloggen}" />
	</h:panelGroup>
	
	<h:panelGroup
		rendered="#{LoginForm.myBenutzer == null && LoginForm.schonEingeloggt}">

		<h:outputText value="#{msgs.sieSindBereitsEingeloggt}" />
		<htm:br />
		<htm:br />
		<h:commandLink value="#{msgs.abbrechen}" id="login-cancel"
			action="#{LoginForm.Ausloggen}" />
		<htm:br />
		<h:commandLink value="#{msgs.alteSessionsAufraeumen}" id="login-clean"
			action="#{LoginForm.EigeneAlteSessionsAufraeumen}" />
		<htm:br />
		<h:commandLink value="#{msgs.dennochEinloggen}" id="login-go"
			action="#{LoginForm.NochmalEinloggen}" />

	</h:panelGroup>
</h:panelGrid>
