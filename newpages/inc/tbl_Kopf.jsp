<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
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
<htm:tr rendered="#{SessionForm.bitteAusloggen!=''}">
	<htm:td>
		<x:div
			style="border: 2px solid black; padding:7px; background-color: #ffd;position: fixed;top: 10px;left: 20px;">
			<h:outputText value="#{SessionForm.bitteAusloggen}"
				style="color: red;font-weight: bold;font-size:30px" />
		</x:div>
	</htm:td>
</htm:tr>
<htm:tr valign="top">

	<htm:td colspan="2">

		<htm:table width="100%" styleClass="layoutKopf"
			style="#{HelperForm.applicationHeaderBackground}" cellpadding="0"
			cellspacing="0" border="0">
			<htm:tr valign="top">
				<htm:td style="padding-left:1px;">
					<h:graphicImage value="#{HelperForm.applicationLogo}"/>
				</htm:td>
				<htm:td valign="middle" align="center">

					<h:outputText style="#{HelperForm.applicationTitleStyle}"
						value="#{HelperForm.applicationTitle}" />

					<a4j:status>
						<f:facet name="start">
							<h:graphicImage value="/newpages/images/ajaxload_small.gif"
								style="position: fixed;top: 85px;right: 15px;" />
						</f:facet>
					</a4j:status>

					<htm:noscript>
						<h:outputText style="color: red;font-weight: bold;"
							value="#{msgs.keinJavascript}" />
					</htm:noscript>

				</htm:td>

				<htm:td valign="middle" align="right" style="padding:3px"
					rendered="#{HelperForm.applicationIndividualHeader!=''}">
					<h:outputText value="#{HelperForm.applicationIndividualHeader}"
						escape="false" />
				</htm:td>


				   <htm:td valign="middle" align="right" style="padding-right: 3px">
					<h:form style="margin:0px" id="headform">
					<%-- 
						<h:commandLink action="#{SpracheForm.SpracheUmschalten}" id="lang1"
							title="deutsche Version">
							<h:graphicImage value="/newpages/images/flag_de_ganzklein.gif" />
							<f:param name="locale" value="de" />
						</h:commandLink>
						<h:commandLink action="#{SpracheForm.SpracheUmschalten}" id="lang2"
							title="english version" >
							<h:graphicImage value="/newpages/images/flag_en_ganzklein.gif" />
							<f:param name="locale" value="en" />
						</h:commandLink>			
						<h:commandLink action="#{SpracheForm.SpracheUmschalten}" id="lang4"
							title="spanish version" style="margin-left:0px;">
							<h:graphicImage value="/newpages/images/flag_es_ganzklein.gif" />
							<f:param name="locale" value="es" />
						</h:commandLink>
						<h:commandLink action="#{SpracheForm.SpracheUmschalten}" id="lang3"
							rendered="false" title="russian version">
							<h:graphicImage value="/newpages/images/flag_ru_ganzklein.gif" />
							<f:param name="locale" value="ru" />
						</h:commandLink>						
						<htm:br/>
					--%>

						<%-- First call to h:commandLink renders an <input type="hidden" name="autoScroll" />
                                                 element surrounded by new line characters which would result in additional white
                                                 space after the first link. This empty link will fix that problem: --%>
						<h:commandLink />

						<htm:div styleClass="languageSwitch">
							<x:dataList var="availableLanguage" value="#{SpracheForm.supportedLocales}">
								<htm:span styleClass="alterLanguage" rendered="#{not availableLanguage.selected}">
									<h:commandLink action="#{SpracheForm.SpracheUmschalten}" title="#{availableLanguage.displayLanguageTranslated}">
										<f:param name="locale" value="#{availableLanguage.id}" />
										<h:outputText value="#{availableLanguage.displayLanguageSelf}" />
									</h:commandLink>
								</htm:span>
								<htm:span styleClass="currentLanguage" rendered="#{availableLanguage.selected}" title="#{availableLanguage.displayLanguageTranslated}">
									<h:outputText value="#{availableLanguage.displayLanguageSelf}" />
								</htm:span>
							</x:dataList>
						</htm:div>
						<%-- logout --%>
							<h:commandLink action="#{LoginForm.Ausloggen}" id="logout2"
    							rendered="#{LoginForm.myBenutzer != null}"
								styleClass="text_head">

								<%-- Mouse-Over für Benutzergruppenmitgliedschaft --%>
								<x:popup
									style="background-color: white; color: #000000; border:1px solid #e3c240; font-size: smaller;"
									closePopupOnExitingElement="true"
									closePopupOnExitingPopup="true" displayAtDistanceX="-210"
									displayAtDistanceY="10">

								<h:outputText style="text-align:right;line-height:20px" value="#{msgs.logout}" />

									<f:facet name="popup">
										<h:panelGroup>
											<h:panelGrid columns="1" width="200">
												<h:outputText rendered="#{LoginForm.myBenutzer != null}"
										style="font-weight:bold"
										value="#{LoginForm.myBenutzer.nachname}, #{LoginForm.myBenutzer.vorname}" />
										
									

												<x:dataList var="intern" style="font-weight: normal"
													rendered="#{LoginForm.myBenutzer.benutzergruppenSize != 0}"
													value="#{LoginForm.myBenutzer.benutzergruppenList}"
													layout="ordered list" rowCountVar="rowCount"
													rowIndexVar="rowIndex">
													<h:outputText value="#{intern.titel}" />
													<h:outputText value=";"
														rendered="#{rowIndex + 1 < rowCount}" />
												</x:dataList>

											</h:panelGrid>
										</h:panelGroup>
									</f:facet>
								</x:popup>

							</h:commandLink>

					</h:form>




				</htm:td>
			</htm:tr>
		</htm:table>
	</htm:td>
</htm:tr>