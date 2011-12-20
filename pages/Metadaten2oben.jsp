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

<html>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

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
									<h:commandLink value="Treelevel 0/1" action="Metadaten3links"
										target="links" styleClass="metadataHeaderLinks">
										<x:updateActionListener
											value="#{Metadaten.treeProperties.showtreelevel?false:true}"
											property="#{Metadaten.treeProperties.showtreelevel}" />
									</h:commandLink>
									<h:commandLink value="TreeTitel 0/1" action="Metadaten3links"
										target="links" styleClass="metadataHeaderLinks">
										<x:updateActionListener
											value="#{Metadaten.treeProperties.showtitle?false:true}"
											property="#{Metadaten.treeProperties.showtitle}" />
									</h:commandLink>
									<h:commandLink value="TreePageNumber 0/1"
										action="Metadaten3links" target="links" styleClass="metadataHeaderLinks">
										<x:updateActionListener
											value="#{Metadaten.treeProperties.showfirstpagenumber?false:true}"
											property="#{Metadaten.treeProperties.showfirstpagenumber}" />
									</h:commandLink>
									<h:commandLink value="TreeExpand 0/1"
										action="#{Metadaten.TreeExpand}" target="links"
										styleClass="metadataHeaderLinks">
										<x:updateActionListener
											value="#{Metadaten.treeProperties.fullexpanded?false:true}"
											property="#{Metadaten.treeProperties.fullexpanded}" />
									</h:commandLink>
									<h:commandLink value="stickyimage 0/1"
										action="Metadaten2rechts" target="rechts"
										styleClass="metadataHeaderLinks">
										<x:updateActionListener
											value="#{Metadaten.treeProperties.imageSticky?false:true}"
											property="#{Metadaten.treeProperties.imageSticky}" />
									</h:commandLink>
									<h:outputText />
								</h:panelGrid>
							</htm:td>
							<htm:td width="105px" align="right" valign="middle"
								style="padding:3px">
								<x:inputText rendered="false" id="treeReload" forceId="true" />
								<h:commandLink rendered="false" id="treeReloadButton"
									value="TreeReloaden" action="Metadaten3links" target="links" />
								<h:commandLink action="#{SpracheForm.SpracheUmschalten}"
									title="deutsche Version" target="rechts">
									<h:graphicImage value="/newpages/images/flag_de_ganzklein.gif" />
									<f:param name="locale" value="de" />
									<f:param name="ziel" value="Metadaten2rechts" />
								</h:commandLink>
								<h:commandLink action="#{SpracheForm.SpracheUmschalten}"
									title="english version" target="rechts">
									<h:graphicImage value="/newpages/images/flag_en_ganzklein.gif" />
									<f:param name="locale" value="en" />
									<f:param name="ziel" value="Metadaten2rechts" />
								</h:commandLink>
								<h:commandLink action="#{SpracheForm.SpracheUmschalten}"
									title="spanish version" target="rechts">
									<h:graphicImage value="/newpages/images/flag_es_ganzklein.gif" />
									<f:param name="locale" value="es" />
									<f:param name="ziel" value="Metadaten2rechts" />
								</h:commandLink>
								<h:commandLink action="#{SpracheForm.SpracheUmschalten}"
									title="english version" target="rechts" rendered="false">
									<h:graphicImage value="/newpages/images/flag_ru_ganzklein.gif" />
									<f:param name="locale" value="ru" />
									<f:param name="ziel" value="Metadaten2rechts" />
								</h:commandLink>
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
