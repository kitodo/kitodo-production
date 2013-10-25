<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
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
<f:view locale="#{SpracheForm.locale}">
	<%@include file="/newpages/inc/head.jsp"%>
	<body>
	
	<h:form id="formularOben">
		<htm:table width="100%" style="margin-top:3px" align="center"
			border="0px">

			<htm:tr valign="top">
				<htm:td colspan="2">
					<htm:table width="100%" styleClass="layoutKopf"
						style="#{HelperForm.applicationHeaderBackground}" cellpadding="0"
						cellspacing="0" border="0">
						<htm:tr valign="top">
							<htm:td width="20%" height="48">
								<h:commandLink action="#{Metadaten.goMain}" target="_parent">
									<h:graphicImage value="#{HelperForm.applicationLogo}" />
								</h:commandLink>
							</htm:td>
							<htm:td valign="middle" align="center">
								<h:outputText style="#{HelperForm.applicationTitleStyle}"
									value="#{Metadaten.myProzess.titel}" />
								<htm:noscript>
									<h:outputText style="color: red;font-weight: bold;"
										value="#{msgs.keinJavascript}" />
								</htm:noscript>
							</htm:td>
							<htm:td valign="middle" align="right">
								<h:panelGrid id="grid1" columns="2" cellpadding="0" cellspacing="0">
									<h:commandLink value="#{msgs.treelevel} " action="Metadaten3links"
										target="links" styleClass="metadataHeaderLinks">
										<x:updateActionListener
											value="#{Metadaten.treeProperties.showtreelevel?false:true}"
											property="#{Metadaten.treeProperties.showtreelevel}" />
									</h:commandLink>
									<h:commandLink value="#{msgs.treeTitle} " action="Metadaten3links" style="padding-left: 5px;"
										target="links" styleClass="metadataHeaderLinks">
										<x:updateActionListener
											value="#{Metadaten.treeProperties.showtitle?false:true}"
											property="#{Metadaten.treeProperties.showtitle}" />
									</h:commandLink>
									<h:commandLink value="#{msgs.treePageNumber} "
										action="Metadaten3links" target="links" styleClass="metadataHeaderLinks">
										<x:updateActionListener
											value="#{Metadaten.treeProperties.showfirstpagenumber?false:true}"
											property="#{Metadaten.treeProperties.showfirstpagenumber}" />
									</h:commandLink>
									<h:commandLink value="#{msgs.treeExpand} " style="padding-left: 5px;"
										action="#{Metadaten.TreeExpand}" target="links"
										styleClass="metadataHeaderLinks">
										<x:updateActionListener
											value="#{Metadaten.treeProperties.fullexpanded?false:true}"
											property="#{Metadaten.treeProperties.fullexpanded}" />
									</h:commandLink>
									<h:commandLink value="#{msgs.stickyImage} "
										action="Metadaten2rechts" target="rechts"
										styleClass="metadataHeaderLinks">
										<x:updateActionListener
											value="#{Metadaten.treeProperties.imageSticky?false:true}"
											property="#{Metadaten.treeProperties.imageSticky}" />
									</h:commandLink>
									<h:outputText />
								</h:panelGrid>
							</htm:td>													
						</htm:tr>
					</htm:table>
				</htm:td>
			</htm:tr>

		</htm:table>
	</h:form>



	</body>
</f:view>
</html>
