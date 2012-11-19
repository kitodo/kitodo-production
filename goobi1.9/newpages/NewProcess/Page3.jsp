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

							Kopie einer Prozessvorlage anlegen

	#########################################--%>

<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="../inc/head.jsp"%>
	<body>

	<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable"
		align="center">
		<%@include file="../inc/tbl_Kopf.jsp"%>
		<htm:tr>
			<%@include file="../inc/tbl_Navigation.jsp"%>
			<htm:td valign="top" styleClass="layoutInhalt">

				<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
				<h:form id="utid30">
					<%-- Breadcrumb --%>
					<h:panelGrid id="utid31" width="100%" columns="1"
						styleClass="layoutInhaltKopf">
						<h:panelGroup id="utid32">
							<h:commandLink id="utid33" value="#{msgs.startseite}"
								action="newMain" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:commandLink id="utid34" value="#{msgs.prozessverwaltung}"
								action="ProzessverwaltungAlle" />
							<f:verbatim> &#8250;&#8250; </f:verbatim>
							<h:outputText id="utid35"
								value="#{msgs.einenNeuenProzessAnlegen}" />
						</h:panelGroup>
					</h:panelGrid>

					<htm:table border="0" align="center" width="100%" cellpadding="15">
						<htm:tr>
							<htm:td>
								<htm:h3>
									<h:outputText id="utid36"
										value="#{msgs.einenNeuenProzessAnlegen}" />
								</htm:h3>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages id="utid37" globalOnly="true" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" />

								<%-- ===================== Eingabe der Details ====================== --%>
								<htm:table cellpadding="3" cellspacing="0" width="100%"
									styleClass="eingabeBoxen">

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row1" colspan="2">
											<h:outputText id="utid38" value="#{msgs.nextStep}" />
										</htm:td>
									</htm:tr>

									<htm:tr>
										<htm:td styleClass="eingabeBoxen_row2" colspan="2">

											<h:panelGrid columns="1" cellpadding="4">

												<h:commandLink id="utid21"
													action="#{ProzesskopieForm.downloadDocket}">
													<h:graphicImage id="utid24" alt="/newpages/images/buttons/laufzettel_wide.png"
														value="/newpages/images/buttons/laufzettel_wide.png"
														style="vertical-align:middle" />
													<h:outputText value="#{msgs.laufzettelDrucken}" />
												</h:commandLink>

												<h:commandLink id="utid20"
													action="#{ProzesskopieForm.Prepare}">
													<h:graphicImage id="utid25"
														alt="/newpages/images/buttons/star_blue.gif"
														value="/newpages/images/buttons/star_blue.gif"
														style="vertical-align:middle" />
													<h:outputText value="#{msgs.weiterenVorgangAnlegen}" />
												</h:commandLink>

												<h:commandLink id="utid22"
													action="ProzessverwaltungBearbeiten">
													<x:updateActionListener
														property="#{ProzessverwaltungForm.myProzess}"
														value="#{ProzesskopieForm.prozessKopie}" />
														<x:updateActionListener value="" property="#{ProzessverwaltungForm.modusBearbeiten}"/>
													<h:graphicImage id="utid23"
														alt="/newpages/images/buttons/edit_20.gif"
														value="/newpages/images/buttons/edit_20.gif"
														style="vertical-align:middle" />

													<h:outputText value="#{msgs.denErzeugtenBandOeffnen}" />
												</h:commandLink>

											</h:panelGrid>
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
		<%@include file="../inc/tbl_Fuss.jsp"%>
	</htm:table>

	</body>
</f:view>

</html>
