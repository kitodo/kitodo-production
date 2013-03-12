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

<%-- ######################################## 

	Kopie einer Prozessvorlage anlegen mit Berücksichtigung komplexer Projekteinstellungen

	#########################################--%>

<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="/newpages/inc/head.jsp"%>
	<body>

		<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable" align="center">
			<%@include file="/newpages/inc/tbl_Kopf.jsp"%>
			<htm:tr>
				<%@include file="/newpages/inc/tbl_Navigation.jsp"%>
				<htm:td valign="top" styleClass="layoutInhalt">

					<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
					<h:form id="pageform1" onkeypress="return ignoreEnterKey(event);">
						<%-- Breadcrumb --%>
						<h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
							<h:panelGroup>
								<h:commandLink value="#{msgs.startseite}" action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:commandLink value="#{msgs.prozessverwaltung}" action="ProzessverwaltungAlle" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText value="#{msgs.einenNeuenProzessAnlegen}" />
							</h:panelGroup>
						</h:panelGrid>

						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>
									<htm:h3>
										<h:outputText value="#{msgs.einenNeuenProzessAnlegen}" />
									</htm:h3>

									<%-- globale Warn- und Fehlermeldungen --%>
									<h:messages globalOnly="true" errorClass="text_red" infoClass="text_blue" showDetail="true" showSummary="true" tooltip="true" />

									<%-- ===================== Eingabe der Details ====================== --%>
									<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen">

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row1" colspan="2">
												<h:outputText value="#{msgs.details}" />
											</htm:td>
										</htm:tr>

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row2" colspan="2">
												<h:panelGrid columns="1" width="100%">
													<%-- Formular für Eingabe der Prozess-Metadaten --%>
													<%@include file="inc_process.jsp"%>
													<%@include file="inc_config.jsp"%>
												</h:panelGrid>
											</htm:td>
										</htm:tr>

										<htm:tr>
											<htm:td styleClass="eingabeBoxen_row3" align="left">
												<h:commandButton value="#{msgs.abbrechen}" immediate="true" action="ProzessverwaltungAlle">
												</h:commandButton>
											</htm:td>
											<htm:td styleClass="eingabeBoxen_row3" align="right">
												<h:commandButton value="#{msgs.weiter}" rendered="#{ProzesskopieForm.prozessKopie.eigenschaftenSize>0}"
													action="#{ProzesskopieForm.GoToSeite2}">
												</h:commandButton>
												<h:commandButton value="#{msgs.speichern}" rendered="#{ProzesskopieForm.prozessKopie.eigenschaftenSize==0}"
													action="#{ProzesskopieForm.NeuenProzessAnlegen}">
												</h:commandButton>
											</htm:td>
										</htm:tr>
									</htm:table>

									<%-- ===================== // Eingabe der Details ====================== --%>

								</htm:td>
							</htm:tr>
						</htm:table>
					</h:form>
					<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

				</htm:td>
			</htm:tr>
			<%@include file="/newpages/inc/tbl_Fuss.jsp"%>
		</htm:table>

<script type="text/javascript">
	function getKeyCode(e) {
		var keycode;
		
		keycode = e.keyCode ? e.keyCode : e.charCode;
		//alert('keycode ' + keycode);

		return keycode;
	}

	function checkOpac(commandId, e) {
		var keycode;

		keycode = getKeyCode(e);

		e.stopPropagation();
		if (keycode == 36) {
			return false;
		} else if ((keycode == 13) && (commandId == 'OpacRequest')) {
			element = document.getElementById('pageform1:performOpacQuery');
			if (element) {
				element.click();

				return false;
			}
		} else {
			return true;
		}

		return true;

	}

	function ignoreEnterKey(e) {
		var keycode;
		keycode = getKeyCode(e);
		if (keycode == 13) {
			return false;
		}
		return true;
	}
</script>
	</body>
</f:view>
</html>
