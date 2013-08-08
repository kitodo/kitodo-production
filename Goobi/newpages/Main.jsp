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

							Startseite

	#########################################--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body>
		<htm:table styleClass="headTable" cellspacing="0" cellpadding="0" style="padding-left:5px;padding-right:5px;margin-top:5px;">
			<%@include file="inc/tbl_Kopf.jsp"%>
		</htm:table>		
		<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable"
			align="center">

			<htm:tr>
				<%@include file="inc/tbl_Navigation.jsp"%>
				<htm:td valign="top" styleClass="layoutInhalt">

					<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
					<h:form id="useform">
						<%-- Breadcrumb --%>
						<h:panelGrid id="id2" columns="1" styleClass="layoutInhaltKopf">
							<h:panelGroup id="id3">
								<h:outputText id="id4" value="#{msgs.startseite}" />
							</h:panelGroup>
						</h:panelGrid>
						<%-- // Breadcrumb --%>

						<%-- Inhalt --%>
						<%-- goobi logo for version 151 --%>
						<h:panelGrid id="id511" columns="2" width="100%" cellpadding="15"
							cellspacing="0" align="center" border="0" rowClasses="rowTop">

							<%-- Einführung --%>
							<x:panelGroup id="id6">

								<htm:noscript>
									<h:outputText
										style="color: red;font-weight: bold;margin-bottom:20px;display:block"
										value="#{msgs.keinJavascript}" />
								</htm:noscript>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages id="id5" globalOnly="true" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" />

								<htm:img src="#{HelperForm.logoUrl}"
									style="display: block; margin-top: 20px; margin-left: auto; margin-right: auto" />

								<htm:h3 style="margin-top:15px">
									<h:outputText id="id7" value="#{msgs.startseite}" />
								</htm:h3>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages id="id8" globalOnly="true" errorClass="text_red"
									infoClass="text_blue" showDetail="true" showSummary="true"
									tooltip="true" />

								<htm:p style="text-align: justify;">
									<h:outputText id="id9" style="text-align: justify;"
										value="#{HelperForm.applicationHomepageMsg}" escape="false"></h:outputText>
								</htm:p>
							</x:panelGroup>
							<%-- // Einführung --%>

							<%-- Kästen mit der Statistik --%>
							<h:panelGrid id="id10" columns="1" cellpadding="0px"
								width="200 px" style="margin-top: 10px" align="center">
								<%@include file="inc_Main/box1.jsp"%>
								<%-- <%@include file="inc_Main/box2.jsp"%> --%>
								<%-- 
							<%@include file="inc_Main/box3.jsp"%>
							--%>
							</h:panelGrid>
							<%-- // Kästen mit der Statistik --%>

						</h:panelGrid>
						<%-- // Inhalt --%>



					</h:form>
					<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

				</htm:td>
			</htm:tr>
			<%@include file="inc/tbl_Fuss.jsp"%>
		</htm:table>

	</body>
</f:view>

</html>