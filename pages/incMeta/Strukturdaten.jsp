<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/dynamic" prefix="jd"%>
<%@ taglib uri="http://myfaces.apache.org/sandbox" prefix="s"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi"
	prefix="si"%>

<h:panelGroup
	rendered="#{not Metadaten.modusStrukturelementVerschieben}">

	<%-- ++++++++++++++++     Neues Strukturelement      ++++++++++++++++ --%>
	<htm:table cellpadding="3" cellspacing="0" width="350px"
		styleClass="eingabeBoxen" style="margin-top:0px">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1">
				<h:outputText value="#{msgs.neuesStrukturelement}" />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2">
				<h:panelGrid id="mygrid1" columns="1">

					<%-- onclick="addableTypenAnzeigen(this)" --%>
					<x:selectOneRadio id="auswahlElementWohin" forceId="true"
						layout="pageDirection" style="font-size:12px"
						onclick="setSecretElement(this.value)"
						value="#{Metadaten.neuesElementWohin}">
						<f:selectItem itemValue="1"
							itemLabel="#{msgs.vorDasAktuelleElement}" />
						<f:selectItem itemValue="2"
							itemLabel="#{msgs.hinterDasAktuelleElement}" />
						<f:selectItem itemValue="3"
							itemLabel="#{msgs.alsErstesKindDesAktuellenElements}" />
						<f:selectItem itemValue="4"
							itemLabel="#{msgs.alsLetztesKindDesAktuellenElements}" />
					</x:selectOneRadio>

					<x:inputText id="secretElement" forceId="true"
						value="#{Metadaten.neuesElementWohin}" style="display:none;" />

					<h:panelGroup>
						<x:selectOneMenu id="auswahlAddable1" forceId="true"
							style="width:315px;margin-left:8px;margin-bottom:4px"
							value="#{Metadaten.addDocStructType1}">
							<f:selectItems
								value="#{Metadaten.addableDocStructTypenAlsNachbar}" />
						</x:selectOneMenu>
						<x:selectOneMenu id="auswahlAddable2" forceId="true"
							style="width:315px;margin-left:8px;margin-bottom:4px"
							value="#{Metadaten.addDocStructType2}">
							<f:selectItems value="#{Metadaten.addableDocStructTypenAlsKind}" />
						</x:selectOneMenu>
					</h:panelGroup>
				</h:panelGrid>
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3" align="right">
				<h:commandLink action="#{Metadaten.KnotenAdd}"
					value="#{msgs.strukturelementHinzufuegen}" target="links" />

				<a4j:commandLink action="#{Metadaten.KnotenAdd}"
					reRender="metadatenRechts" rendered="false"
					value="#{msgs.strukturelementHinzufuegen}" target="rechts">
					<a4j:support event="onmouseup" oncomplete="TreeReloaden()" />
				</a4j:commandLink>
			</htm:td>
		</htm:tr>
	</htm:table>
	<%-- ++++++++++++++++     // Neues Strukturelement      ++++++++++++++++ --%>


	<%-- ++++++++++++++++     Strukturelement verschieben oder l�schen     ++++++++++++++++ --%>
	<htm:table cellpadding="3" cellspacing="0" width="350px"
		styleClass="eingabeBoxen" style="margin-top:20px">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1">
				<h:outputText value="#{msgs.ausgewaehltesStrukturelement} ..." />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2">
				<h:panelGrid id="mygrid2" columns="1">

					<%-- Knoten nach oben --%>
					<h:commandLink action="#{Metadaten.KnotenUp}"
						title="#{msgs.docstructNachObenSchieben}" target="links">
						<h:graphicImage value="/newpages/images/buttons/sort_up_20px.gif"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<h:outputText value="#{msgs.docstructNachObenSchieben}" />
					</h:commandLink>

					<%-- Knoten nach unten --%>
					<h:commandLink action="#{Metadaten.KnotenDown}"
						title="#{msgs.docstructNachUntenSchieben}" target="links">
						<h:graphicImage
							value="/newpages/images/buttons/sort_down_20px.gif"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<h:outputText value="#{msgs.docstructNachUntenSchieben}" />
					</h:commandLink>

					<%-- Knoten an andere Stelle schieben --%>
					<h:commandLink action="Metdaten2rechts"
						title="#{msgs.docstructAnAndereStelleSchieben}" target="rechts">
						<h:graphicImage
							value="/newpages/images/buttons/sort_left_20px.gif"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<h:outputText value="#{msgs.docstructAnAndereStelleSchieben}" />
						<x:updateActionListener
							property="#{Metadaten.modusStrukturelementVerschieben}"
							value="true" />
					</h:commandLink>

					<%-- DocstructType �ndern --%>
					<h:panelGroup>
						<jd:hideableController for="changeDocStructType"
							title="#{msgs.docstructTypeAendern}">
							<h:graphicImage
								value="/newpages/images/buttons/sort_down_20px.gif"
								style="margin-left:4px;margin-right:6px;vertical-align:middle" />
							<h:outputText value="#{msgs.docstructTypeAendern}" />
						</jd:hideableController>

						<jd:hideableArea id="changeDocStructType" saveState="view">
							<h:panelGrid id="mygrid3" columns="1" style="margin-left:40px;"
								rowClasses="top" columnClasses="standardTable_ColumnRight">
								<%-- Auswahlliste --%>
								<x:selectOneMenu style="width:315px;"
									value="#{Metadaten.tempWert}">
									<f:selectItems
										value="#{Metadaten.addableDocStructTypenAlsNachbar}" />
								</x:selectOneMenu>
								<%-- Action --%>
								<h:commandLink action="#{Metadaten.ChangeCurrentDocstructType}"
									title="#{msgs.uebernehmen}" target="links"
									onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return">
									<h:outputText value="#{msgs.uebernehmen}" />
								</h:commandLink>
							</h:panelGrid>
						</jd:hideableArea>
					</h:panelGroup>

					<%-- zus�tzliche Strukturelemente als Kinder unter das ausgew�hlte Element --%>
					<h:panelGroup>
						<jd:hideableController for="addZusaetzlicheDocstructs"
							title="#{msgs.strukturelementeAusOpacHinzufuegen}">
							<h:graphicImage
								value="/newpages/images/buttons/sort_down_20px.gif"
								style="margin-left:4px;margin-right:6px;vertical-align:middle" />
							<h:outputText value="#{msgs.strukturelementeAusOpacHinzufuegen}" />
						</jd:hideableController>

						<jd:hideableArea id="addZusaetzlicheDocstructs" saveState="view">
							<h:panelGrid id="mygrid4" columns="2" style="margin-left:40px;"
								rowClasses="top" columnClasses="standardTable_ColumnRight">

								<h:outputText value="#{msgs.katalog}" />

								<%-- uses opac.xml --%>	
	
								<h:selectOneMenu id="katalogauswahl"
									value="#{Metadaten.opacKatalog}"
									style="display:inline; margin-left:7px">
								  <si:selectItems value="#{ProzesskopieForm.allOpacCatalogues}"
						            var="step" itemLabel="#{step}" itemValue="#{step}" />
						    	</h:selectOneMenu>
								<h:outputText value="#{msgs.feld}"
									style="display:inline; margin-left:7px" />
								<h:selectOneMenu id="feldauswahl"
									value="#{Metadaten.opacSuchfeld}"
									style="display:inline; margin-left:10px">
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
									<h:commandLink target="rechts" immediate="false"
										style="margin-right:10px"
										action="#{Metadaten.AddMetadaFromOpacPpn}"
										title="#{msgs.uebernehmen}"
										onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return">
										<h:outputText value="#{msgs.nurMetadaten}" />
									</h:commandLink>

									<h:commandLink target="links" immediate="false"
										action="#{Metadaten.AddAdditionalOpacPpns}"
										title="#{msgs.uebernehmen}"
										onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return">
										<h:outputText value="#{msgs.alsUnterelemente}" />
									</h:commandLink>
								</h:panelGroup>
							</h:panelGrid>
						</jd:hideableArea>
					</h:panelGroup>

					<%-- OCR-Button --%>
					<h:outputLink rendered="#{Metadaten.showOcrButton}" target="_blank"
						value="#{Metadaten.ocrAcdress}" title="#{msgs.ocr}">
						<h:graphicImage value="/newpages/images/buttons/ocr.png"
							style="margin-left:4px;margin-right:7px;vertical-align:middle" />
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
					<h:commandLink action="#{Metadaten.KnotenDelete}"
						title="#{msgs.strukturelementLoeschen}" target="links">
						<h:graphicImage value="/newpages/images/buttons/waste1a_20px.gif"
							style="margin-left:4px;margin-right:7px;vertical-align:middle" />
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
		<htm:table cellpadding="3" cellspacing="0" width="350px"
			styleClass="eingabeBoxen" style="margin-top:20px">
			<htm:tr>
				<htm:td styleClass="eingabeBoxen_row1">
					<h:outputText value="#{msgs.seitenzuordnung} " />
					<h:commandLink>
						<h:outputText value="(normal)"
							rendered="#{Metadaten.treeProperties.showpagesasajax==true}" />
						<h:outputText value="(ajax)"
							rendered="#{Metadaten.treeProperties.showpagesasajax==false}" />
						<x:updateActionListener
							value="#{Metadaten.treeProperties.showpagesasajax?false:true}"
							property="#{Metadaten.treeProperties.showpagesasajax}" />
					</h:commandLink>
				</htm:td>
			</htm:tr>
			<htm:tr>
				<htm:td styleClass="eingabeBoxen_row2">
					<h:panelGrid id="mygrid5" columns="2" width="100%"
						columnClasses="standardTable_Column,standardTable_ColumnRight">

						<a4j:commandLink action="#{Metadaten.BildErsteSeiteAnzeigen}"
							reRender="BildArea">
							<h:outputText value="#{msgs.ersteSeite}" />
						</a4j:commandLink>

						<s:inputSuggestAjax style="width: 200px"
							rendered="#{Metadaten.treeProperties.showpagesasajax==true}"
							suggestedItemsMethod="#{Metadaten.getAjaxAlleSeiten}"
							
							value="#{Metadaten.ajaxSeiteStart}" />

						<h:selectOneMenu style="width: 200px"
							rendered="#{Metadaten.treeProperties.showpagesasajax==false}"
							value="#{Metadaten.alleSeitenAuswahl_ersteSeite}">
							<f:selectItems value="#{Metadaten.alleSeiten}" />
						</h:selectOneMenu>

						<a4j:commandLink action="#{Metadaten.BildLetzteSeiteAnzeigen}"
							reRender="BildArea">
							<h:outputText value="#{msgs.letzteSeite}" />
						</a4j:commandLink>

						<s:inputSuggestAjax style="width: 200px"
							rendered="#{Metadaten.treeProperties.showpagesasajax==true}"
							suggestedItemsMethod="#{Metadaten.getAjaxAlleSeiten}"
							value="#{Metadaten.ajaxSeiteEnde}" />

						<h:selectOneMenu style="width: 200px"
							rendered="#{Metadaten.treeProperties.showpagesasajax==false}"
							value="#{Metadaten.alleSeitenAuswahl_letzteSeite}">
							<f:selectItems value="#{Metadaten.alleSeiten}" />
						</h:selectOneMenu>
					</h:panelGrid>
				</htm:td>
			</htm:tr>

			<htm:tr>
				<htm:td styleClass="eingabeBoxen_row3">
					<h:panelGrid id="mygrid6" columns="2" width="100%"
						columnClasses="standardTable_Column,standardTable_ColumnRight">
						<a4j:commandLink
							action="#{Metadaten.SeitenVonChildrenUebernehmen}"
							reRender="menuZugehoerigeSeiten">
							<h:outputText value="#{msgs.seitenVonUnterelementenZuweisen}" />
						</a4j:commandLink>
						<a4j:commandLink action="#{Metadaten.SeitenStartUndEndeSetzen}"
							reRender="menuZugehoerigeSeiten,BildArea"
							rendered="#{Metadaten.treeProperties.showpagesasajax==false}">
							<h:outputText value="#{msgs.seitenZuweisen}" />
						</a4j:commandLink>
						<a4j:commandLink
							action="#{Metadaten.AjaxSeitenStartUndEndeSetzen}"
							reRender="menuZugehoerigeSeiten,BildArea"
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

				<x:selectManyListbox style="font-size:12px;height:300px;width:150px"
					value="#{Metadaten.alleSeitenAuswahl}">
					<f:selectItems value="#{Metadaten.alleSeiten}" />
				</x:selectManyListbox>

			</h:panelGroup>

			<%-- Pfeile zum Verschieben der Seiten --%>
			<h:panelGroup>
				<%-- nach rechts --%>
				<a4j:commandLink id="s1" action="#{Metadaten.SeitenHinzu}"
					reRender="menuZugehoerigeSeiten,BildArea"
					style="margin-top:175px;margin-left:10px;margin-right:10px;display:block">
					<h:graphicImage value="/newpages/images/buttons/order_right.gif" />
				</a4j:commandLink>
				<%-- nach links --%>
				<a4j:commandLink id="s2" action="#{Metadaten.SeitenWeg}"
					reRender="menuZugehoerigeSeiten,BildArea"
					style="margin-top:7px;margin-left:10px;margin-right:10px;display:block">
					<h:graphicImage value="/newpages/images/buttons/order_left.gif" />
				</a4j:commandLink>
			</h:panelGroup>

			<%-- zugewiesene Seiten --%>
			<h:panelGroup>
				<htm:h3 style="margin-top:20px">
					<h:outputText value="#{msgs.zugehoerigeSeiten}" />
				</htm:h3>
				<x:selectManyListbox id="menuZugehoerigeSeiten"
					style="font-size:12px;height:300px;width:150px"
					value="#{Metadaten.structSeitenAuswahl}">
					<f:selectItems value="#{Metadaten.structSeiten}" />
				</x:selectManyListbox>
			</h:panelGroup>
		</h:panelGrid>

	</h:panelGroup>
</h:panelGroup>

<h:panelGroup rendered="#{Metadaten.modusStrukturelementVerschieben}">
	<%@include file="BaumZumVerschieben.jsp"%>
</h:panelGroup>
