<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp"%>
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
<a4j:keepAlive beanName="BenutzerverwaltungForm"/>
<a4j:keepAlive beanName="ProjekteForm"/>
<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body style="margin:0px;padding:0px">
	<h:form id="usereditpopupform">
		<%-- ===================== Popup-Rahmen ====================== 
	<h:graphicImage id="id0" value="/newpages/images/popup/oben.gif" width="430"
		style="position:absolute;left:0;top:0;z-index:1" />
	<h:graphicImage id="id1" value="/newpages/images/popup/links.gif" height="360"
		style="position:absolute;left:0;top:19" />
	<h:graphicImage id="id2" value="/newpages/images/popup/rechts.gif" height="360"
		style="position:absolute;left:426;top:19" />
	<h:graphicImage id="id3" value="/newpages/images/popup/unten.gif" width="430"
		style="position:absolute;left:0;top:377" />

	<h:form id="hmmmForm1">
		<jp:closePopupFrame>
			<h:commandLink id="id4" action="#{NavigationForm.JeniaPopupCloseAction}">
				<h:graphicImage id="id5" value="/newpages/images/popup/close.gif"
					style="position:absolute;left:410;top:2;z-index:2" />
			</h:commandLink>
		</jp:closePopupFrame>
	</h:form>--%>

		<h:outputText id="id6" value="#{msgs.projektHinzufuegen}"
			style="position:absolute;left:10;top:2;color:white;font-weight:bold;font-size:12;z-index:3" />

		<%-- ===================== // Popup-Rahmen ====================== --%>

		<htm:table style="margin-top:20px" align="center" width="90%"
			border="0">
			<htm:tr>
				<htm:td>
					<%-- globale Warn- und Fehlermeldungen --%>
					<h:messages id="id7" globalOnly="true" errorClass="text_red"
						infoClass="text_blue" showDetail="true" showSummary="true"
						tooltip="true" />

					<%-- Datentabelle --%>
					<x:dataTable id="id8" styleClass="standardTable" width="100%"
						cellspacing="1px" cellpadding="1px"
						headerClass="standardTable_Header"
						rowClasses="standardTable_Row1,standardTable_Row2"
						columnClasses="standardTable_Column,standardTable_ColumnCentered"
						var="item" value="#{ProjekteForm.page.listReload}">

						<h:column id="id9">
							<f:facet name="header">
								<h:outputText id="id10" value="#{msgs.projekt}" />
							</f:facet>
							<h:outputText id="id11" value="#{item.titel}" />
						</h:column>

						<h:column id="id12">
							<f:facet name="header">
								<h:outputText id="id13" value="#{msgs.auswahl}" />
							</f:facet>
							<%-- Hinzufügen-Schaltknopf --%>
							<h:commandLink
								action="#{BenutzerverwaltungForm.ZuProjektHinzufuegen}"
								title="#{msgs.uebernehmen}">
								<h:graphicImage id="id14" value="/newpages/images/buttons/addUser.gif" />
								<f:param id="id15" name="ID" value="#{item.id}" />
							</h:commandLink>

						</h:column>

					</x:dataTable>

					<htm:table width="100%" border="0">
						<htm:tr valign="top">
							<htm:td align="left">
								<%-- ===================== Datascroller für die Ergebnisse ====================== --%>
								<x:aliasBean alias="#{mypage}" value="#{ProjekteForm.page}">
									<jsp:include page="/newpages/inc/datascroller.jsp" />
								</x:aliasBean>
								<%-- ===================== // Datascroller für die Ergebnisse ====================== --%>
							</htm:td>
							<htm:td align="center">
								<%-- Schliessen-Schaltknopf --%>
								<jp:closePopupFrame>
								<%-- TODO: Use massage files here --%>
									<h:commandLink id="id17" value="#{msgs.close}"
										action="#{NavigationForm.JeniaPopupCloseAction}"></h:commandLink>
								</jp:closePopupFrame>
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
