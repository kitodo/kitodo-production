<%--
 *
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 *
--%>

<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>

<%-- ########################################

                                  Paginierungssequenzen

#########################################--%>

<%-- <h:form> --%>
<a4j:commandLink action="#{Metadaten.createPagination}"
	reRender="mygrid10,myMessages,myBild" value="#{msgs.paginierungEinlesen}" />
<%-- </h:form> --%>
<h:panelGrid id="mygrid10" columns="2" style="margin:0px;">

	<%-- ++++++++++++++++     Auswahl der Seiten      ++++++++++++++++ --%>
	<htm:table cellpadding="3" cellspacing="0" width="200px"
		rendered="#{Metadaten.alleSeiten !=null}" styleClass="eingabeBoxen"
		style="margin-top:20px">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1">
				<h:outputText value="#{msgs.auswahlDerSeiten}" />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td id="PaginierungAlleImages" styleClass="eingabeBoxen_row2">
				<h:selectManyCheckbox layout="pageDirection" id="myCheckboxes"
					value="#{Metadaten.alleSeitenAuswahl}">
					<f:selectItems value="#{Metadaten.alleSeiten}" id="myCheckbox" />
				</h:selectManyCheckbox>
			</htm:td>
		</htm:tr>
	</htm:table>
	<%-- ++++++++++++++++     // Auswahl der Seiten      ++++++++++++++++ --%>

	<%-- ++++++++++++++++     Spacer for valid HTML     ++++++++++++++++ --%>
	<htm:table rendered="#{Metadaten.alleSeiten ==null}"
		style="display:none"/>
	
	<%-- ++++++++++++++++     // Auswahl der Seiten      ++++++++++++++++ --%>

	<h:panelGrid id="mygrid11" columns="1" width="270px" style="margin:0px;">
		<%-- ++++++++++++++++     Paginierung festlegen      ++++++++++++++++ --%>
		<h:panelGroup rendered="#{Metadaten.alleSeiten !=null}" style="position: fixed; top: 122px; left: 250px;">
			<htm:table cellpadding="3" cellspacing="0" id="PaginierungActionBox" rendered="#{Metadaten.alleSeiten !=null}" styleClass="eingabeBoxen"
				style="width: 320px;">
			<htm:tr>
				<htm:td styleClass="eingabeBoxen_row1">
					<h:outputText value="#{msgs.paginierungFestlegen}" />
				</htm:td>
			</htm:tr>
			<htm:tr>
				<htm:td styleClass="eingabeBoxen_row2">
					<h:selectOneMenu value="#{Metadaten.paginierungArt}" rendered="#{not Metadaten.advancedPaginationEnabled}"
						style="width: 270px;margin-top:10px;margin-left:5px" onchange="paginierungWertAnzeigen(this);">
						<f:selectItem itemValue="1" itemLabel="#{msgs.arabisch}" />
						<f:selectItem itemValue="2" itemLabel="#{msgs.roemisch}" />
						<f:selectItem itemValue="3" itemLabel="#{msgs.unnummeriert}" />
						<f:selectItem itemValue="6" itemLabel="#{msgs.paginationFreetext}" />
					</h:selectOneMenu>
					<h:selectOneMenu value="#{Metadaten.paginierungArt}" rendered="#{Metadaten.advancedPaginationEnabled}"
						style="width: 270px;margin-top:10px;margin-left:5px" onchange="paginierungWertAnzeigen(this);">
						<f:selectItem itemValue="1" itemLabel="#{msgs.arabisch}" />
						<f:selectItem itemValue="2" itemLabel="#{msgs.roemisch}" />
						<f:selectItem itemValue="3" itemLabel="#{msgs.unnummeriert}" />
						<f:selectItem itemValue="6" itemLabel="#{msgs.paginationFreetext}" />
						<f:selectItem itemValue="99" itemLabel="#{msgs.paginationAdvanced}" />
					</h:selectOneMenu>
					<htm:br />
					<x:inputText id="paginierungWert" forceId="true"
						value="#{Metadaten.paginierungWert}"
						style="width: 270px;margin-top:10px;margin-bottom:5px;margin-left:5px" />
					<htm:div id="traditionalPagination" style="display:#{(Metadaten.paginierungArt eq '99')?'none':'block'}">

					<htm:div style="margin-top: 5px;margin-bottom:10px">
						<h:selectBooleanCheckbox id="checkbox-fictitious" value="#{Metadaten.fictitious}" />
						<h:outputLabel for="checkbox-fictitious" value="#{msgs.paginationFictitious}" />
					</htm:div>

					<a4j:commandLink rendered="#{Metadaten.paginierungSeitenProImage!=1}"
						title="#{msgs.seitenzaehlung}" reRender="PaginierungActionBox,myMessages,mygrid10">
						<h:graphicImage
							value="/newpages/images/buttons/paginierung_seite_inactive.svg"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<x:updateActionListener value="1"
							property="#{Metadaten.paginierungSeitenProImage}" />
					</a4j:commandLink>
					<h:graphicImage
						rendered="#{Metadaten.paginierungSeitenProImage==1}"
						value="/newpages/images/buttons/paginierung_seite.svg"
						style="margin-left:4px;margin-right:6px;vertical-align:middle"
						title="#{msgs.seitenzaehlung}" />

					<a4j:commandLink rendered="#{Metadaten.paginierungSeitenProImage!=2}"
						title="#{msgs.spaltenzaehlung}" reRender="PaginierungActionBox,myMessages,mygrid10">
						<h:graphicImage
							value="/newpages/images/buttons/paginierung_spalte_inactive.svg"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<x:updateActionListener value="2"
							property="#{Metadaten.paginierungSeitenProImage}" />
					</a4j:commandLink>
					<h:graphicImage
						rendered="#{Metadaten.paginierungSeitenProImage==2}"
						value="/newpages/images/buttons/paginierung_spalte.svg"
						style="margin-left:4px;margin-right:6px;vertical-align:middle"
						title="#{msgs.spaltenzaehlung}" />

					<a4j:commandLink rendered="#{Metadaten.paginierungSeitenProImage!=3}"
						title="#{msgs.blattzaehlung}" reRender="PaginierungActionBox,myMessages,mygrid10">
						<h:graphicImage
							value="/newpages/images/buttons/paginierung_blatt_inactive.svg"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<x:updateActionListener value="3"
							property="#{Metadaten.paginierungSeitenProImage}" />
					</a4j:commandLink>
					<h:graphicImage
						rendered="#{Metadaten.paginierungSeitenProImage==3}"
						value="/newpages/images/buttons/paginierung_blatt.svg"
						style="margin-left:4px;margin-right:6px;vertical-align:middle"
						title="#{msgs.blattzaehlung}" />
					
					<a4j:commandLink
						rendered="#{Metadaten.paginierungSeitenProImage!=4}"
						title="#{msgs.blattzaehlungrectoverso}"
						reRender="PaginierungActionBox,myMessages,mygrid10">
						<h:graphicImage
							value="/newpages/images/buttons/paginierung_blatt_rectoverso_inactive.svg"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<x:updateActionListener value="4"
							property="#{Metadaten.paginierungSeitenProImage}" />
					</a4j:commandLink>
					<h:graphicImage
						rendered="#{Metadaten.paginierungSeitenProImage==4}"
						value="/newpages/images/buttons/paginierung_blatt_rectoverso.svg"
						style="margin-left:4px;margin-right:6px;vertical-align:middle"
						title="#{msgs.blattzaehlungrectoverso}" />
								
					<a4j:commandLink
						rendered="#{Metadaten.paginierungSeitenProImage!=5}"
						title="#{msgs.seitenzaehlungrectoverso}"
						reRender="PaginierungActionBox,myMessages,mygrid10">
						<h:graphicImage
							value="/newpages/images/buttons/paginierung_seite_rectoverso_inactive.svg"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<x:updateActionListener value="5"
							property="#{Metadaten.paginierungSeitenProImage}" />
					</a4j:commandLink>
					<h:graphicImage
						rendered="#{Metadaten.paginierungSeitenProImage==5}"
						value="/newpages/images/buttons/paginierung_seite_rectoverso.svg"
						style="margin-left:4px;margin-right:6px;vertical-align:middle"
						title="#{msgs.seitenzaehlungrectoverso}" />
						
					<a4j:commandLink
						rendered="#{Metadaten.paginierungSeitenProImage!=6}"
						title="#{msgs.seitenzaehlungdoppelseiten}"
						reRender="PaginierungActionBox,myMessages,mygrid10">
						<h:graphicImage
							value="/newpages/images/buttons/paginierung_doppelseite_inactive.svg"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<x:updateActionListener value="6"
							property="#{Metadaten.paginierungSeitenProImage}" />
					</a4j:commandLink>
					<h:graphicImage
						rendered="#{Metadaten.paginierungSeitenProImage==6}"
						value="/newpages/images/buttons/paginierung_doppelseite.svg"
						style="margin-left:4px;margin-right:6px;vertical-align:middle"
						title="#{msgs.seitenzaehlungdoppelseiten}" />
						
					<htm:div rendered="#{Metadaten.paginierungSeitenProImage>=5}"
							style=" margin-left: 6px; margin-top: 8px;">
						<h:outputText value="#{msgs.pageSeparator}: "/>
						<h:selectOneMenu value="#{Metadaten.paginierungSeparator}">
						    <si:selectItems var="ps" value="#{Metadaten.paginierungSeparators}"
						    		itemLabel="#{ps.label}" itemValue="#{ps.id}" />
						</h:selectOneMenu>
					</htm:div>
				</htm:div>
				<htm:div id="advancedPagination"
					style="display:#{(Metadaten.paginierungArt eq '99')?'block':'none'}">
					<h:commandButton value="#{msgs.paginationAdvancedText}"
						title="#{msgs.paginationAdvancedTextDesc}"
						onclick="return writeToPaginierungWert('`','`')" />
					<h:commandButton value="#{msgs.paginationAdvancedNoIncrement}"
						title="#{msgs.paginationAdvancedNoIncrementDesc}"
						onclick="return writeToPaginierungWert('','°')" />
					<h:commandButton value="#{msgs.paginationAdvancedPlusOneHalf}"
						title="#{msgs.paginationAdvancedPlusOneHalfDesc}"
						onclick="return writeToPaginierungWert('','½')" />
					<h:commandButton value="#{msgs.paginationAdvancedPlusOne}"
						title="#{msgs.paginationAdvancedPlusOneDesc}"
						onclick="return writeToPaginierungWert('','¹')" />
					<h:commandButton value="#{msgs.paginationAdvancedPlusTwo}"
						title="#{msgs.paginationAdvancedPlusTwoDesc}"
						onclick="return writeToPaginierungWert('','²')" />
					<h:commandButton value="#{msgs.paginationAdvancedPlusThree}"
						title="#{msgs.paginationAdvancedPlusThreeDesc}"
						onclick="return writeToPaginierungWert('','³')" />
					<h:commandButton value="#{msgs.paginationAdvancedFullPage}"
						title="#{msgs.paginationAdvancedFullPageDesc}"
						onclick="return writeToPaginierungWert('¡','')" />
					<h:commandButton value="#{msgs.paginationAdvancedHalfPage}"
						title="#{msgs.paginationAdvancedHalfPageDesc}"
						onclick="return writeToPaginierungWert('¿','')" />
				</htm:div>
					<htm:br />

						<a4j:commandLink id="s4" action="#{Metadaten.Paginierung}" style="margin-top:15px" reRender="PaginierungAlleImages,myMessages,mygrid10">
							<h:outputText value="#{msgs.nurDieMarkiertenSeiten}" />
							<x:updateActionListener property="#{Metadaten.paginierungAbSeiteOderMarkierung}" value="2" />
						</a4j:commandLink>
						<htm:br style="margin-top:15px" />
						<a4j:commandLink id="s5" action="#{Metadaten.Paginierung}" style="margin-top:15px" reRender="PaginierungAlleImages,myMessages,mygrid10">
							<h:outputText value="#{msgs.abDerErstenMarkiertenSeite}" />
							<x:updateActionListener property="#{Metadaten.paginierungAbSeiteOderMarkierung}" value="1" />
						</a4j:commandLink>
					</htm:td>
				</htm:tr>
			</htm:table>
			<%-- ++++++++++++++++     // Paginierung festlegen      ++++++++++++++++ --%>

			<htm:table cellpadding="3" cellspacing="0" id="pageOrder" styleClass="eingabeBoxen" style="width: 320px;margin-top:10px;" rendered="#{Metadaten.displayFileManipulation}">
				<htm:tr>
					<htm:td styleClass="eingabeBoxen_row1">
						<h:outputText value="#{msgs.pageOrder}" />
					</htm:td>
				</htm:tr>
				<htm:tr>
					<htm:td styleClass="eingabeBoxen_row2">
						<h:commandLink action="#{Metadaten.moveSeltectedPagesUp}">
							<h:graphicImage value="/newpages/images/buttons/up_20px.gif" style="margin-left:4px;margin-right:6px;vertical-align:middle" />
							<h:outputText value="#{msgs.moveSeltectedPagesUp}" />
						</h:commandLink>
					</htm:td>
				</htm:tr>
				<htm:tr>
					<htm:td styleClass="eingabeBoxen_row2">
						<h:commandLink action="#{Metadaten.moveSeltectedPagesDown}">
							<h:graphicImage value="/newpages/images/buttons/down_20px.gif" style="margin-left:4px;margin-right:6px;vertical-align:middle" />
							<h:outputText value="#{msgs.moveSeltectedPagesDown}" />
						</h:commandLink>
					</htm:td>
				</htm:tr>
				<htm:tr>
					<htm:td styleClass="eingabeBoxen_row2">
						<h:commandLink action="#{Metadaten.deleteSeltectedPages}" onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return false">
							<h:graphicImage value="/newpages/images/buttons/waste1a_20px.gif" style="margin-left:4px;margin-right:6px;vertical-align:middle" />
							<h:outputText value="#{msgs.deleteSeltectedPages}" />
						</h:commandLink>
					</htm:td>
				</htm:tr>
				<htm:tr>
					<htm:td styleClass="eingabeBoxen_row2">
						<h:commandLink action="#{Metadaten.reOrderPagination}">
							<h:graphicImage value="/newpages/images/buttons/reload.gif" style="margin-left:4px;margin-right:6px;vertical-align:middle" />
							<h:outputText value="#{msgs.reOrder}" />
						</h:commandLink>
					</htm:td>
				</htm:tr>

			</htm:table>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>
