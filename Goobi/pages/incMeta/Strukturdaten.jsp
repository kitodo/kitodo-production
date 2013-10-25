<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/dynamic" prefix="jd"%>
<%@ taglib uri="http://myfaces.apache.org/sandbox" prefix="s"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
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
<h:panelGroup rendered="#{not Metadaten.modusStrukturelementVerschieben}">

	<%-- ++++++++++++++++     Neues Strukturelement      ++++++++++++++++ --%>
	<htm:table cellpadding="3" cellspacing="0" width="350px" styleClass="eingabeBoxen" style="margin-top:0px">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1">
				<h:outputText value="#{msgs.neuesStrukturelement}" />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2">
				<h:panelGrid id="mygrid1" columns="1">

					<%-- onclick="addableTypenAnzeigen(this)" --%>
					<x:selectOneRadio id="auswahlElementWohin" forceId="true" layout="pageDirection" style="font-size:12px" onclick="setSecretElement(this.value)"
						value="#{Metadaten.neuesElementWohin}">
						<f:selectItem itemValue="1" itemLabel="#{msgs.vorDasAktuelleElement}" />
						<f:selectItem itemValue="2" itemLabel="#{msgs.hinterDasAktuelleElement}" />
						<f:selectItem itemValue="3" itemLabel="#{msgs.alsErstesKindDesAktuellenElements}" />
						<f:selectItem itemValue="4" itemLabel="#{msgs.alsLetztesKindDesAktuellenElements}" />
					</x:selectOneRadio>

					<x:inputText id="secretElement" forceId="true" value="#{Metadaten.neuesElementWohin}" style="display:none;" />

					<h:panelGroup>
						<x:selectOneMenu id="auswahlAddable1" forceId="true" style="width:315px;margin-left:8px;margin-bottom:4px"
							value="#{Metadaten.addDocStructType1}">
							<f:selectItems value="#{Metadaten.addableDocStructTypenAlsNachbar}" />
						</x:selectOneMenu>
						<x:selectOneMenu id="auswahlAddable2" forceId="true" style="width:315px;margin-left:8px;margin-bottom:4px"
							value="#{Metadaten.addDocStructType2}">
							<f:selectItems value="#{Metadaten.addableDocStructTypenAlsKind}" />
						</x:selectOneMenu>
					</h:panelGroup>
				</h:panelGrid>

				<h:panelGrid columns="3" width="100%" columnClasses="standardTable_Column,standardTable_Column">
					<h:outputText value="#{msgs.ersteSeite}: " />
					<h:panelGroup id="pageStartGroup">
						<x:inputText id="pagestart1" forceId="true" value="#{Metadaten.pagesStart}" />
						<rich:suggestionbox height="200" width="145" for="pagestart1" var="startpage" id="suggestion3" suggestionAction="#{Metadaten.autocomplete}">
							<h:column>
								<h:outputText value="#{startpage}" />
							</h:column>
						</rich:suggestionbox>
					</h:panelGroup>
				
				
					<a4j:commandLink action="#{Metadaten.CurrentStartpage}" reRender="pageStartGroup">
						<h:graphicImage value="/newpages/images/buttons/left_20px.gif" style="border: 0px;vertical-align:middle;" />
						<x:updateActionListener value="#{Metadaten.bildNummer}" property="#{Metadaten.pageNumber}"/>
					</a4j:commandLink>
				
					<h:outputText value="#{msgs.letzteSeite}: " />
					<h:panelGroup id="pageEndGroup">
						<x:inputText id="pageend1" forceId="true" value="#{Metadaten.pagesEnd}" />
						<rich:suggestionbox tokens=":" height="200" width="145" for="pageend1" var="endpage" id="suggestion4"
							suggestionAction="#{Metadaten.autocomplete}">
							<h:column>
								<h:outputText value="#{endpage}" />
							</h:column>
						</rich:suggestionbox>
					</h:panelGroup>
					<a4j:commandLink action="#{Metadaten.CurrentEndpage}" reRender="pageEndGroup">
						<h:graphicImage value="/newpages/images/buttons/left_20px.gif" style="border: 0px;vertical-align:middle;" />
						<x:updateActionListener value="#{Metadaten.bildNummer}" property="#{Metadaten.pageNumber}"/>
					</a4j:commandLink>
				</h:panelGrid>
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3" align="right">
				<h:commandLink action="#{Metadaten.KnotenAdd}" value="#{msgs.strukturelementHinzufuegen}" target="links" />
			</htm:td>
		</htm:tr>
	</htm:table>
	<%-- ++++++++++++++++     // Neues Strukturelement      ++++++++++++++++ --%>


	<%-- ++++++++++++++++     Strukturelement verschieben oder l�schen     ++++++++++++++++ --%>
	<htm:table cellpadding="3" cellspacing="0" width="350px" styleClass="eingabeBoxen" style="margin-top:20px">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1">
				<h:outputText value="#{msgs.ausgewaehltesStrukturelement} ..." />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2">
				<h:panelGrid id="mygrid2" columns="1">

					<%-- Knoten nach oben --%>
					<h:commandLink action="#{Metadaten.KnotenUp}" title="#{msgs.docstructNachObenSchieben}" target="links">
						<h:graphicImage value="/newpages/images/buttons/sort_up_20px.gif" style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<h:outputText value="#{msgs.docstructNachObenSchieben}" />
					</h:commandLink>

					<%-- Knoten nach unten --%>
					<h:commandLink action="#{Metadaten.KnotenDown}" title="#{msgs.docstructNachUntenSchieben}" target="links">
						<h:graphicImage value="/newpages/images/buttons/sort_down_20px.gif" style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<h:outputText value="#{msgs.docstructNachUntenSchieben}" />
					</h:commandLink>

					<%-- Knoten an andere Stelle schieben --%>
					<h:commandLink action="Metdaten2rechts" title="#{msgs.docstructAnAndereStelleSchieben}" target="rechts">
						<h:graphicImage value="/newpages/images/buttons/sort_left_20px.gif" style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<h:outputText value="#{msgs.docstructAnAndereStelleSchieben}" />
						<x:updateActionListener property="#{Metadaten.modusStrukturelementVerschieben}" value="true" />
					</h:commandLink>

					<%-- DocstructType �ndern --%>
					<h:panelGroup>
						<jd:hideableController for="changeDocStructType" title="#{msgs.docstructTypeAendern}">
							<h:graphicImage value="/newpages/images/buttons/sort_down_20px.gif" style="margin-left:4px;margin-right:6px;vertical-align:middle" />
							<h:outputText value="#{msgs.docstructTypeAendern}" />
						</jd:hideableController>

						<jd:hideableArea id="changeDocStructType" saveState="view">
							<h:panelGrid id="mygrid3" columns="1" style="margin-left:40px;" rowClasses="top" columnClasses="standardTable_ColumnRight">
								<%-- Auswahlliste --%>
								<x:selectOneMenu style="width:315px;" value="#{Metadaten.tempWert}">
									<f:selectItems value="#{Metadaten.addableDocStructTypenAlsNachbar}" />
								</x:selectOneMenu>
								<%-- Action --%>
								<h:commandLink action="#{Metadaten.ChangeCurrentDocstructType}" title="#{msgs.uebernehmen}" target="links"
									onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return">
									<h:outputText value="#{msgs.uebernehmen}" />
								</h:commandLink>
							</h:panelGrid>
						</jd:hideableArea>
					</h:panelGroup>

					<%-- zus�tzliche Strukturelemente als Kinder unter das ausgew�hlte Element --%>
					<h:panelGroup>
						<jd:hideableController for="addZusaetzlicheDocstructs" title="#{msgs.strukturelementeAusOpacHinzufuegen}">
							<h:graphicImage value="/newpages/images/buttons/sort_down_20px.gif" style="margin-left:4px;margin-right:6px;vertical-align:middle" />
							<h:outputText value="#{msgs.strukturelementeAusOpacHinzufuegen}" />
						</jd:hideableController>

						<jd:hideableArea id="addZusaetzlicheDocstructs" saveState="view">
							<h:panelGrid id="mygrid4" columns="2" style="margin-left:40px;" rowClasses="top" columnClasses="standardTable_ColumnRight">

								<h:outputText value="#{msgs.katalog}" />

								<%-- uses goobi_opac.xml --%>

								<h:selectOneMenu id="katalogauswahl" value="#{Metadaten.opacKatalog}" style="display:inline; margin-left:7px">
									<si:selectItems value="#{ProzesskopieForm.allOpacCatalogues}" var="step" itemLabel="#{step}" itemValue="#{step}" />
								</h:selectOneMenu>
								<h:outputText value="#{msgs.feld}" style="display:inline; margin-left:7px" />
								<h:selectOneMenu id="feldauswahl" value="#{Metadaten.opacSuchfeld}" style="display:inline; margin-left:10px">
									<f:selectItem itemLabel="PPN" itemValue="12" />
									<f:selectItem itemLabel="Barcode" itemValue="8535" />
									<f:selectItem itemLabel="Barcode 8200" itemValue="8200" />
									<f:selectItem itemLabel="ISBN" itemValue="7" />
									<f:selectItem itemLabel="ISSN" itemValue="8" />
								</h:selectOneMenu>


								<h:outputText value="#{msgs.suchbegriffe}" />
								<%-- Auswahlliste --%>
								<h:inputTextarea value="#{Metadaten.additionalOpacPpns}" />
								<%-- Action 
								onclick="parent.oben.document.getElementById('treeReload').value='reload'; if (!confirm('#{msgs.wirklichAusfuehren}?')) return"
								--%>
								<h:outputText value="" />


								<h:panelGroup>
									<h:commandLink target="rechts" immediate="false" style="margin-right:10px" action="#{Metadaten.AddMetadaFromOpacPpn}"
										title="#{msgs.uebernehmen}" onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return">
										<h:outputText value="#{msgs.nurMetadaten}" />
									</h:commandLink>

									<h:commandLink target="links" immediate="false" action="#{Metadaten.AddAdditionalOpacPpns}" title="#{msgs.uebernehmen}"
										onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return">
										<h:outputText value="#{msgs.alsUnterelemente}" />
									</h:commandLink>
								</h:panelGroup>
							</h:panelGrid>
						</jd:hideableArea>
					</h:panelGroup>

					<%-- OCR-Button --%>
					<h:outputLink rendered="#{Metadaten.showOcrButton}" target="_blank" value="#{Metadaten.ocrAcdress}" title="#{msgs.ocr}">
						<h:graphicImage value="/newpages/images/buttons/ocr.png" style="margin-left:4px;margin-right:7px;vertical-align:middle" />
						<h:outputText value="#{msgs.ocr}" />
					</h:outputLink>

					<%-- Neu-Schaltknopf 
					<h:panelGroup rendered="#{Metadaten.showOcrButton}">
						<jp:popupFrame scrolling="auto" height="380px" width="430px"
							topStyle="background: #1874CD;" bottomStyleClass="popup_unten"
							styleFrame="border-style: solid;border-color: #1874CD; border-width: 2px;"
							styleClass="standardlink"
							style="margin-top:2px;display:block; text-decoration:none"
							actionOpen="#{Metadaten.showOcrPopup}" center="true"
							title="#{msgs.ocr}" immediate="true">
							<h:graphicImage value="/newpages/images/buttons/ocr.png"
								style="margin-left:4px;margin-right:7px;vertical-align:middle" />
							<h:outputText style="border-bottom: #a24033 dashed 1px;"
								value="#{msgs.ocr}" />
						</jp:popupFrame>
					</h:panelGroup>--%>

					<%-- Knoten l�schen --%>
					<h:commandLink rendered="#{Metadaten.isNotRootElement}" action="#{Metadaten.KnotenDelete}" title="#{msgs.strukturelementLoeschen}" target="links">
						<h:graphicImage value="/newpages/images/buttons/waste1a_20px.gif" style="margin-left:4px;margin-right:7px;vertical-align:middle" />
						<h:outputText value="#{msgs.strukturelementLoeschen}" />
					</h:commandLink>

				</h:panelGrid>
			</htm:td>
		</htm:tr>
	</htm:table>
	<%-- ++++++++++++++++     // Strukturelement verschieben oder l�schen   ++++++++++++++++ --%>


	<%-- ########################################

                                    vorhandene Seiten

#########################################--%>

	<h:panelGroup rendered="#{Metadaten.alleSeiten !=null}">

		<%-- ++++++++++++++++ Zuweisung der ersten und letzten Seite     ++++++++++++++++ --%>
		<htm:table cellpadding="3" cellspacing="0" width="350px" styleClass="eingabeBoxen" style="margin-top:20px">
			<htm:tr>
				<htm:td styleClass="eingabeBoxen_row1">
					<h:outputText value="#{msgs.seitenzuordnung} " />
					<h:commandLink>
						<h:outputText value="(normal)" rendered="#{Metadaten.treeProperties.showpagesasajax==true}" />
						<h:outputText value="(ajax)" rendered="#{Metadaten.treeProperties.showpagesasajax==false}" />
						<x:updateActionListener value="#{Metadaten.treeProperties.showpagesasajax?false:true}" property="#{Metadaten.treeProperties.showpagesasajax}" />
					</h:commandLink>
				</htm:td>
			</htm:tr>
			<htm:tr>
				<htm:td styleClass="eingabeBoxen_row2">
					<h:panelGrid id="mygrid5" columns="2" width="100%" columnClasses="standardTable_Column,standardTable_ColumnRight">

						<a4j:commandLink action="#{Metadaten.BildErsteSeiteAnzeigen}" reRender="BildArea,myBild">
							<h:outputText value="#{msgs.ersteSeite}" />
						</a4j:commandLink>
						<h:panelGroup rendered="#{Metadaten.treeProperties.showpagesasajax==true}">
							<x:inputText id="pagestart" forceId="true" value="#{Metadaten.ajaxSeiteStart}" />
							<%-- 
  <h:graphicImage value="/newpages/images/calendarImages/drop1.gif"
                    onclick="document.getElementById('formular2:suggestion').callSuggestion(true);"
                    alt="" />
		--%>

							<rich:suggestionbox height="200" width="145" for="pagestart" var="startpage" id="suggestion" suggestionAction="#{Metadaten.autocomplete}">
								<h:column>
									<h:outputText value="#{startpage}" />
								</h:column>
							</rich:suggestionbox>
						</h:panelGroup>

						<h:selectOneMenu style="width: 200px" rendered="#{Metadaten.treeProperties.showpagesasajax==false}"
							value="#{Metadaten.alleSeitenAuswahl_ersteSeite}">
							<f:selectItems value="#{Metadaten.alleSeiten}" />
						</h:selectOneMenu>




						<a4j:commandLink action="#{Metadaten.BildLetzteSeiteAnzeigen}" reRender="BildArea,myBild">
							<h:outputText value="#{msgs.letzteSeite}" />
						</a4j:commandLink>
						<h:panelGroup rendered="#{Metadaten.treeProperties.showpagesasajax==true}">
							<x:inputText id="pageend" forceId="true" value="#{Metadaten.ajaxSeiteEnde}" />

							<rich:suggestionbox tokens=":" height="200" width="200" for="pageend" var="endpage" id="suggestion2"
								suggestionAction="#{Metadaten.autocomplete}">
								<h:column>
									<h:outputText value="#{endpage}" />
								</h:column>
							</rich:suggestionbox>
						</h:panelGroup>
						<h:selectOneMenu style="width: 200px" rendered="#{Metadaten.treeProperties.showpagesasajax==false}"
							value="#{Metadaten.alleSeitenAuswahl_letzteSeite}">
							<f:selectItems value="#{Metadaten.alleSeiten}" />
						</h:selectOneMenu>
					</h:panelGrid>
				</htm:td>
			</htm:tr>

			<htm:tr>
				<htm:td styleClass="eingabeBoxen_row3">
					<h:panelGrid id="mygrid6" columns="2" width="100%" columnClasses="standardTable_Column,standardTable_ColumnRight">
						<a4j:commandLink action="#{Metadaten.SeitenVonChildrenUebernehmen}" reRender="menuZugehoerigeSeiten">
							<h:outputText value="#{msgs.seitenVonUnterelementenZuweisen}" />
						</a4j:commandLink>
						<a4j:commandLink action="#{Metadaten.SeitenStartUndEndeSetzen}" reRender="menuZugehoerigeSeiten,BildArea,myBild"
							rendered="#{Metadaten.treeProperties.showpagesasajax==false}">
							<h:outputText value="#{msgs.seitenZuweisen}" />
						</a4j:commandLink>
						<a4j:commandLink action="#{Metadaten.AjaxSeitenStartUndEndeSetzen}" reRender="menuZugehoerigeSeiten,BildArea,myBild"
							rendered="#{Metadaten.treeProperties.showpagesasajax==true}">
							<h:outputText value="#{msgs.seitenZuweisen}" />
						</a4j:commandLink>
					</h:panelGrid>

				</htm:td>

			</htm:tr>
		</htm:table>
		<%-- ++++++++++++++++     // Zuweisung der ersten und letzten Seite   ++++++++++++++++ --%>

		<h:panelGrid id="mygrid7" columns="3" columnClasses="top,top,top">
			<%-- alle Seiten --%>
			<h:panelGroup style="margin-left:0px">
				<htm:h3 style="margin-top:20px">
					<h:outputText value="#{msgs.alleSeiten}" />
				</htm:h3>

				<x:selectManyListbox style="font-size:12px;height:300px;width:150px" value="#{Metadaten.alleSeitenAuswahl}">
					<f:selectItems value="#{Metadaten.alleSeiten}" />
				</x:selectManyListbox>

			</h:panelGroup>

			<%-- Pfeile zum Verschieben der Seiten --%>
			<h:panelGroup>
				<%-- nach rechts --%>
				<a4j:commandLink id="s1" action="#{Metadaten.SeitenHinzu}" reRender="menuZugehoerigeSeiten,BildArea,myBild"
					style="margin-top:175px;margin-left:10px;margin-right:10px;display:block">
					<h:graphicImage value="/newpages/images/buttons/order_right.gif" />
				</a4j:commandLink>
				<%-- nach links --%>
				<a4j:commandLink id="s2" action="#{Metadaten.SeitenWeg}" reRender="menuZugehoerigeSeiten,BildArea,myBild"
					style="margin-top:7px;margin-left:10px;margin-right:10px;display:block">
					<h:graphicImage value="/newpages/images/buttons/order_left.gif" />
				</a4j:commandLink>
			</h:panelGroup>

			<%-- zugewiesene Seiten --%>
			<h:panelGroup>
				<htm:h3 style="margin-top:20px">
					<h:outputText value="#{msgs.zugehoerigeSeiten}" />
				</htm:h3>
				<x:selectManyListbox id="menuZugehoerigeSeiten" style="font-size:12px;height:300px;width:150px" value="#{Metadaten.structSeitenAuswahl}">
					<f:selectItems value="#{Metadaten.structSeiten}" />
				</x:selectManyListbox>
			</h:panelGroup>
		</h:panelGrid>

	</h:panelGroup>
	<h:commandButton id="docStructReload" immediate="true"
					action="#{NavigationForm.Reload}" style="display:none">
	</h:commandButton>
</h:panelGroup>

<h:panelGroup rendered="#{Metadaten.modusStrukturelementVerschieben}">
	<%@include file="BaumZumVerschieben.jsp"%>
</h:panelGroup>
