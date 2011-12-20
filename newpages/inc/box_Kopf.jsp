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

<%-- =====================
 ====================== --%>
<h:form style="width:100%;margin:0px;#{HelperForm.applicationHeaderBackground}" id="headform">
	<h:graphicImage value="#{HelperForm.applicationLogo}" />
	
	<h:commandLink action="#{SpracheForm.SpracheUmschalten}" id="lang1"
		style="position:absolute;top:15px;right:100px;margin:0px;"
		title="deutsche Version">
		<h:graphicImage value="/newpages/images/flag_de_ganzklein.gif" />
		<f:param name="locale" value="de" />
	</h:commandLink>
	<h:commandLink action="#{SpracheForm.SpracheUmschalten}" id="lang2"
		style="position:absolute;top:15px;right:65px;margin:0px"
		title="english version">
		<h:graphicImage value="/newpages/images/flag_en_ganzklein.gif" />
		<f:param name="locale" value="en" />
	</h:commandLink>
	<h:commandLink action="#{SpracheForm.SpracheUmschalten}" id="lang3"
		style="position:absolute;top:15px;right:30px;margin:0px" rendered="true"
		title="russian version">
		<h:graphicImage value="/newpages/images/flag_ru_ganzklein.gif" />
		<f:param name="locale" value="ru" />
	</h:commandLink>

</h:form>


<%-- ===================== 

<htm:table width="100%" styleClass="layoutKopf"
	style="#{HelperForm.applicationHeaderBackground}" cellpadding="0"
	cellspacing="0" border="0">
	<htm:tr valign="top">
		<htm:td width="20%" >
			<h:graphicImage value="#{HelperForm.applicationLogo}" />
		</htm:td>
		<htm:td valign="middle" align="center">

			<h:outputText style="#{HelperForm.applicationTitleStyle}"
				value="#{HelperForm.applicationTitle}" />

			<htm:noscript>
				<h:outputText style="color: red;font-weight: bold;"
					value="#{msgs.keinJavascript}" />
			</htm:noscript>

		</htm:td>
		<htm:td valign="middle" width="20%" align="right" style="padding:3px">
			<h:form style="margin:0px">
				<h:panelGrid columns="3">
					<h:commandLink action="#{SpracheForm.SpracheUmschalten}"
						title="deutsche Version">
						<h:graphicImage value="/newpages/images/flag_de_ganzklein.gif" />
						<f:param name="locale" value="de" />
					</h:commandLink>
					<h:commandLink action="#{SpracheForm.SpracheUmschalten}"
						title="english version">
						<h:graphicImage value="/newpages/images/flag_en_ganzklein.gif" />
						<f:param name="locale" value="en" />
					</h:commandLink>
					<h:commandLink action="#{SpracheForm.SpracheUmschalten}"
						rendered="true" title="russian version">
						<h:graphicImage value="/newpages/images/flag_ru_ganzklein.gif" />
						<f:param name="locale" value="ru" />
					</h:commandLink>
				</h:panelGrid>
			</h:form>
		</htm:td>
	</htm:tr>
</htm:table>
 ====================== --%>

