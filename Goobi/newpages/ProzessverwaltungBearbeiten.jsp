<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

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

							Prozess bearbeiten

	#########################################--%>
<a4j:keepAlive beanName="ProzessverwaltungForm" />
<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body>
		<htm:table styleClass="headTable" cellspacing="0" cellpadding="0" style="padding-left:5px;padding-right:5px;margin-top:5px;">
			<%@include file="inc/tbl_Kopf.jsp"%>
		</htm:table>
		<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable" align="center">
			<htm:tr>
				<%@include file="inc/tbl_Navigation.jsp"%>
				<htm:td valign="top" styleClass="layoutInhalt">

					<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
					<h:form id="id0" style="margin:0px">
						<%-- Breadcrumb --%>
						<h:panelGrid id="id0" width="100%" columns="1" styleClass="layoutInhaltKopf">
							<h:panelGroup id="id1">
								<h:commandLink id="id2" value="#{msgs.startseite}" action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:commandLink id="id3" value="#{msgs.prozessverwaltung}" action="ProzessverwaltungAlle" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText id="id4" value="#{msgs.einenNeuenProzessAnlegen}" rendered="#{ProzessverwaltungForm.myProzess.id == null}" />
								<h:outputText id="id5" value="#{msgs.prozessDetails}" rendered="#{ProzessverwaltungForm.myProzess.id != null}" />
							</h:panelGroup>
						</h:panelGrid>
					</h:form>
					
					<htm:table border="0" align="center" width="100%" cellpadding="15">
						<htm:tr>
							<htm:td>
								<htm:h3>
									<h:outputText id="id6" value="#{msgs.einenNeuenProzessAnlegen}" rendered="#{ProzessverwaltungForm.myProzess.id == null}" />
									<h:outputText id="id7" value="#{msgs.prozessDetails}" rendered="#{ProzessverwaltungForm.myProzess.id != null}" />
								</htm:h3>

								<%-- globale Warn- und Fehlermeldungen --%>
								<h:messages id="id8" globalOnly="true" errorClass="text_red" infoClass="text_blue" showDetail="true" showSummary="true" tooltip="true" />

								<%-- Prozessdetails --%>
								<%@include file="inc_Prozessverwaltung/prozess_box_Prozessdetails.jsp"%>

								<h:form id="proceditform" onkeypress="ifEnterClick(event, 'proceditform:absenden');">

									<f:subview id="sub001" rendered="#{ProzessverwaltungForm.myProzess.id != null}">
										<%-- Schritte --%>
										<%@include file="inc_Prozessverwaltung/prozess_box_Schritte.jsp"%>

										<%-- Vorlagen --%>
										<f:subview id="subVorlage" rendered="#{ProzessverwaltungForm.myProzess.vorlagenSize != 0}">
											<%@include file="inc_Prozessverwaltung/prozess_box_Vorlagen.jsp"%>
										</f:subview>

										<%-- Werkstuecke --%>
										<f:subview id="subWerk" rendered="#{ProzessverwaltungForm.myProzess.werkstueckeSize != 0}">
											<%@include file="inc_Prozessverwaltung/prozess_box_Werkstuecke.jsp"%>
										</f:subview>

										<%-- Prozesseigenschaften --%>
										<%@include file="inc_Prozessverwaltung/prozess_box_Properties.jsp"%>
									</f:subview>

								</h:form>
							</htm:td>
						</htm:tr>
					</htm:table>
					<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

				</htm:td>
			</htm:tr>
			<%@include file="inc/tbl_Fuss.jsp"%>
		</htm:table>



	</body>
</f:view>

</html>
