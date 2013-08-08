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
<htm:table width="580">
	<htm:tr>
		<htm:td colspan="2">
			<h:outputText value="#{msgs.strukturelementVerschiebenErlaeuterung}" />
			<htm:br />
			<htm:br />
		</htm:td>
	</htm:tr>
	<htm:tr>
		<htm:td>

			<h:dataTable value="#{Metadaten.strukturBaum3Alle}" var="item"
				cellpadding="0" cellspacing="0" border="0">
				<h:column>

					<%-- Popup --%>
					<x:popup id="z" closePopupOnExitingElement="true"
						closePopupOnExitingPopup="true" displayAtDistanceX="15"
						displayAtDistanceY="-40">

						<h:graphicImage value="/newpages/images/spacer.gif"
							rendered="#{item.node.hasChildren}"
							style="border: 0px none;margin-top:1px;margin-left:#{item.niveau * 15 + 5};" />

						<f:facet name="popup">
							<htm:div>
								<h:panelGrid columns="1"
									style="background-color:#FFFFEA; font-size:11px; border: 1px solid #CCCCCC; padding: 1px;"
									rendered="#{item.node.mainTitle != '' || item.node.zblNummer != '' || item.node.firstImage != '' || item.node.zblSeiten != '' || item.node.ppnDigital != ''}">
									<h:panelGrid columns="1" style="font-size: 11" cellspacing="0"
										cellpadding="0" width="110">

										<h:outputText value="Maintitle:"
											rendered="#{item.node.mainTitle != ''}"
											style="font-size: 10; text-decoration: underline;font-weight: bold; color: black;" />
										<h:outputText value="#{item.node.mainTitle}"
											rendered="#{item.node.mainTitle != ''}" />

										<h:outputText value="Startimage:"
											rendered="#{node.firstImage!= ''}"
											style="font-size: 10; text-decoration: underline;font-weight: bold; color: black;" />
										<h:outputText value="#{item.node.firstImage}"
											rendered="#{item.node.firstImage != ''}" />

										<h:outputText value="ZBL-Seiten:"
											rendered="#{item.node.zblSeiten != ''}"
											style="font-size: 10; text-decoration: underline;font-weight: bold; color: black;" />
										<h:outputText value="#{item.node.zblSeiten}"
											rendered="#{item.node.zblSeiten != ''}" />

										<h:outputText value="ZBL-ID:"
											rendered="#{item.node.zblNummer != ''}"
											style="font-size: 10; text-decoration: underline;font-weight: bold; color: black;" />
										<h:outputText value="#{item.node.zblNummer}"
											rendered="#{item.node.zblNummer != ''}" />

										<h:outputText value="PPN-Digital"
											rendered="#{item.node.ppnDigital != ''}"
											style="font-size: 10; text-decoration: underline;font-weight: bold; color: black;" />
										<h:outputText value="#{item.node.ppnDigital}"
											rendered="#{item.node.ppnDigital != ''}" />
									</h:panelGrid>
								</h:panelGrid>
							</htm:div>
						</f:facet>

						<h:graphicImage value="/newpages/images/document.png"
							rendered="#{item.node.hasChildren}"
							style="margin-right:2px;vertical-align:middle" />
						<h:graphicImage value="/newpages/images/document.png"
							rendered="#{!item.node.hasChildren}"
							style="margin-right:2px;vertical-align:middle;margin-left:#{item.niveau * 10 + 17};" />

						<h:commandLink target="links" styleClass="document"
							action="#{Metadaten.KnotenVerschieben}"
							rendered="#{item.node.einfuegenErlaubt && not item.node.selected}">
							<h:outputText value="#{item.node.description}" />
							<x:updateActionListener
								property="#{Metadaten.modusStrukturelementVerschieben}"
								value="false" />
							<x:updateActionListener
								property="#{Metadaten.tempStrukturelement}"
								value="#{item.node.struct}" />
						</h:commandLink>

						<h:panelGroup
							rendered="#{not item.node.einfuegenErlaubt || item.node.selected}">
							<h:outputText value="#{item.node.description}"
								style="font-size: 12px;#{item.node.selected?'color:green;':'color:#999999;'}" />
						</h:panelGroup>

					</x:popup>

				</h:column>

			</h:dataTable>

		</htm:td>
		<htm:td width="1%" valign="top" align="right" nowrap="">
			<htm:div style="border: 2px dashed silver">
				<h:commandLink target="_self" action="#{NavigationForm.Reload}"
					style="margin:5px" title="#{msgs.verschiebenAbbrechen}">
					<h:graphicImage value="/newpages/images/buttons/cancel1.gif"
						style="border: 0px;vertical-align:middle;" />
					<h:outputText value="#{msgs.abbrechen}"
						title="#{msgs.verschiebenAbbrechen}" />
					<x:updateActionListener
						property="#{Metadaten.modusStrukturelementVerschieben}"
						value="false" />
				</h:commandLink>

				<h:commandButton id="docStructVerschiebenAbbrechen"
					action="#{NavigationForm.Reload}" style="display:none">
					<x:updateActionListener
						property="#{Metadaten.modusStrukturelementVerschieben}"
						value="false" />
				</h:commandButton>
			</htm:div>
		</htm:td>
	</htm:tr>
</htm:table>
