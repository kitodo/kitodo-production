<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
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
<f:view locale="#{SpracheForm.locale}">
	<%@include file="/newpages/inc/head.jsp"%>
	
	<body style="margin:2px 2px 2px 2px;background-color:#CBE5FC;" onload="reloadRightFrame();">

	<%-- ========================  neuer Baum style="color: #434387; border-style: solid; border-width: 1px; border-color: #e3c240; background-color: #faf3da;width: 100%;height: 99%;" ============================== --%>
	<h:outputText rendered="false" value="up"
		style="position: fixed;top: 20px;left: 170px" onclick="move(-50)"
		onmousemove="move(-2)" />
	<h:outputText rendered="false" value="down"
		style="position: fixed;top: 40px;left: 170px" onclick="move(50)"
		onmouseover="move(2)" />

	<htm:table id="myTree" styleClass="layoutTreeView2"
		style="width: 100%;height: 100%;">
		<htm:tr>
			<htm:td valign="top" height="100%">

				<%-- globale Warn- und Fehlermeldungen --%>
				<h:messages globalOnly="true" errorClass="text_red"
					infoClass="text_blue" showDetail="true" showSummary="true"
					tooltip="true" />

				<h:form rendered="true" id="treeform">

					<x:commandButton id="reloadMyTree" forceId="true" value="reload"
						action="Metadaten3links" style="color:#E7C342;display:none">
					</x:commandButton>

					<h:graphicImage id="veid3" alt="print" url="/newpages/images/print.png" onclick="self.window.print()" styleClass="action" style="position:absolute;right:5px"></h:graphicImage>

					<x:dataTable id="tabelle" forceId="true"
						value="#{Metadaten.strukturBaum3}" var="item" cellpadding="0"
						cellspacing="0">

						<h:column>
							<h:commandLink action="#{NavigationForm.Reload}" target="links">
								<x:updateActionListener property="#{item.node.expanded}"
									value="#{not item.node.expanded}" />
								<h:graphicImage value="/newpages/images/plus.gif"
									rendered="#{item.node.hasChildren && not item.node.expanded}"
									style="border: 0px none;margin-top:0px;margin-right:3px;margin-left:#{item.niveau * 10 + 5}px;" />
								<h:graphicImage value="/newpages/images/minus.gif"
									rendered="#{item.node.hasChildren && item.node.expanded}"
									style="border: 0px none;margin-top:0px;margin-right:3px;margin-left:#{item.niveau * 10 + 5}px;" />
							</h:commandLink>

							<%-- Popup --%>
							<x:popup id="popup" closePopupOnExitingElement="true"
								closePopupOnExitingPopup="true" displayAtDistanceX="17"
								displayAtDistanceY="-40">

								<f:facet name="popup">
									<htm:div>
										<h:panelGrid columns="1"
											style="background-color:white; font-size:11px; border: 1px solid #e3c240; padding: 1px;"
											rendered="#{item.node.mainTitle != '' || item.node.zblNummer != '' || item.node.firstImage != '' || item.node.zblSeiten != '' || item.node.ppnDigital != ''}">
											<h:panelGrid columns="1" style="font-size: 10"
												cellspacing="0" cellpadding="0" width="110">

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
									style="margin-right:2px;vertical-align:middle;margin-left:#{item.niveau * 10 + 17}px;" />

							</x:popup>

							<h:outputText value="#{item.niveau}"
								style="color:grey;margin-right:4px"
								rendered="#{Metadaten.treeProperties.showtreelevel}" />

							<h:commandLink id="link" immediate="true" target="rechts"
								styleClass="#{item.node.selected ? 'documentSelected':'document'}"
								action="#{Metadaten.loadRightFrame}"
								onclick="if (!styleAnpassen2(this)) return">
								<h:outputText value="#{item.node.description}"
									rendered="#{not Metadaten.treeProperties.showtitle || (Metadaten.treeProperties.showtitle && item.node.mainTitle == '')}" />
								<h:outputText value="#{item.node.mainTitle}"
									rendered="#{item.node.mainTitle != '' && Metadaten.treeProperties.showtitle}" />
								<x:updateActionListener
									property="#{Metadaten.myStrukturelement}"
									value="#{item.node.struct}" />
							</h:commandLink>

							<h:outputText value="(#{item.node.firstImage}"
								style="color:grey;margin-left:4px"
								rendered="#{Metadaten.treeProperties.showfirstpagenumber && item.node.firstImage!=''}" />

							<h:outputText value="-#{item.node.lastImage})"
								style="color:grey;margin-left:0px"
								rendered="#{Metadaten.treeProperties.showfirstpagenumber && item.node.lastImage!=''}" />

						</h:column>

					</x:dataTable>

				</h:form>

				<%-- ======================== // neuer Baum  ============================== --%>

				<h:form id="formWarn" style="display:none">
					<h:inputHidden id="Warnmeldung"
						value="#{msgs.esGibtUngespeicherteDaten}" />
				</h:form>

			</htm:td>
		</htm:tr>
	</htm:table>

	</body>
</f:view>
<script type="text/javascript"><!--
	// Funktion, die dynamisch das Stylesheet zuweist
	function styleAnpassen2(element){
		try{
		//	if (parent.rechts.document.getElementById("formular4:DatenGeaendert").value == "1"){
		//		// Daten sind ungespeichert, also Warnung ausgeben und abbrechen
		//		alert(document.getElementById("formWarn:Warnmeldung").value);
		//		//return false;
		//	}else{
				// Daten sind gespeichert, also den Klick weitergeben
		 		var galleryLinks;
				galleryLinks = document.getElementsByTagName('a');
				for (var i=0;i<galleryLinks.length;i++)
					galleryLinks[i].className = "document";
				element.className = "documentSelected";
				return true;
		//	}
		}catch(err){		
			parent.rechts.location.href="Metadaten2rechts.jsf";
		}
	}

		
	function reloadRightFrame(){
		var myelement = parent.rechts.document.getElementById("formular2:docStructVerschiebenAbbrechen");
		if (myelement!=null){
			myelement.click();
		}
		var myelement1 = parent.rechts.document.getElementById("formular2:docStructReload");
		if (myelement1!=null){
			myelement1.click();
		}
	}
	
	var DHTML = (document.getElementById || document.all || document.layers);
	var texttop = 10;
	
	function getObj(name){
		  if (document.getElementById)
		  {
		  	this.obj = document.getElementById(name);
			this.style = document.getElementById(name).style;
		  }
		  else if (document.all)
		  {
			this.obj = document.all[name];
			this.style = document.all[name].style;
		  }
		  else if (document.layers)
		  {
		   	this.obj = document.layers[name];
		   	this.style = document.layers[name];
		  }
		}
		
		function move(amount){
			if (!DHTML) return;
			var x = new getObj('treeform:tabelle');
			texttop += amount;
			x.style.top = texttop;
		}
 //-->
 </script>
</html>
