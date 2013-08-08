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
<%-- ########################################

                                  Paginierungssequenzen

#########################################--%>

<%-- <h:form> --%>
<a4j:commandLink action="#{Metadaten.createPagination}"
	reRender="mygrid10,myMessages" value="#{msgs.paginierungEinlesen}" />
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
					<h:selectOneMenu value="#{Metadaten.paginierungArt}"
						style="width: 270px;margin-top:10px;margin-left:5px" onchange="paginierungWertAnzeigen(this);">
						<f:selectItem itemValue="1" itemLabel="#{msgs.arabisch}" />
<%-- 						<f:selectItem itemValue="4" itemLabel="#{msgs.arabischBracket}" /> --%>
						<f:selectItem itemValue="2" itemLabel="#{msgs.roemisch}" />
<%-- 						<f:selectItem itemValue="5" itemLabel="#{msgs.roemischBracket}" /> --%>
						<f:selectItem itemValue="3" itemLabel="#{msgs.unnummeriert}" />
						<f:selectItem itemValue="6" itemLabel="#{msgs.paginationFreetext}" />
					</h:selectOneMenu>
					<htm:br />
					<x:inputText id="paginierungWert" forceId="true"
						value="#{Metadaten.paginierungWert}"
						style="width: 270px;margin-top:10px;margin-bottom:5px;margin-left:5px" />
					<htm:br />

					<htm:div style="margin-top: 5px;margin-bottom:10px">
						<h:selectBooleanCheckbox id="checkbox-fictitious" value="#{Metadaten.fictitious}" />
						<h:outputLabel for="checkbox-fictitious" value="#{msgs.paginationFictitious}" />
					</htm:div>

					<a4j:commandLink rendered="#{Metadaten.paginierungSeitenProImage!=1}"
						title="#{msgs.seitenzaehlung}" reRender="PaginierungActionBox,myMessages,mygrid10">
						<h:graphicImage
							value="/newpages/images/buttons/paginierung_seite_inactive.png"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<x:updateActionListener value="1"
							property="#{Metadaten.paginierungSeitenProImage}" />
					</a4j:commandLink>
					<h:graphicImage
						rendered="#{Metadaten.paginierungSeitenProImage==1}"
						value="/newpages/images/buttons/paginierung_seite.png"
						style="margin-left:4px;margin-right:6px;vertical-align:middle"
						title="#{msgs.seitenzaehlung}" />

					<a4j:commandLink rendered="#{Metadaten.paginierungSeitenProImage!=2}"
						title="#{msgs.spaltenzaehlung}" reRender="PaginierungActionBox,myMessages,mygrid10">
						<h:graphicImage
							value="/newpages/images/buttons/paginierung_spalte_inactive.png"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<x:updateActionListener value="2"
							property="#{Metadaten.paginierungSeitenProImage}" />
					</a4j:commandLink>
					<h:graphicImage
						rendered="#{Metadaten.paginierungSeitenProImage==2}"
						value="/newpages/images/buttons/paginierung_spalte.png"
						style="margin-left:4px;margin-right:6px;vertical-align:middle"
						title="#{msgs.spaltenzaehlung}" />

					<a4j:commandLink rendered="#{Metadaten.paginierungSeitenProImage!=3}"
						title="#{msgs.blattzaehlung}" reRender="PaginierungActionBox,myMessages,mygrid10">
						<h:graphicImage
							value="/newpages/images/buttons/paginierung_blatt_inactive.png"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<x:updateActionListener value="3"
							property="#{Metadaten.paginierungSeitenProImage}" />
					</a4j:commandLink>
					<h:graphicImage
						rendered="#{Metadaten.paginierungSeitenProImage==3}"
						value="/newpages/images/buttons/paginierung_blatt.png"
						style="margin-left:4px;margin-right:6px;vertical-align:middle"
						title="#{msgs.blattzaehlung}" />
					
					<a4j:commandLink
						rendered="#{Metadaten.paginierungSeitenProImage!=4}"
						title="#{msgs.blattzaehlungrectoverso}"
						reRender="PaginierungActionBox,myMessages,mygrid10">
						<h:graphicImage
							value="/newpages/images/buttons/paginierung_blatt_rectoverso_inactive.png"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<x:updateActionListener value="4"
							property="#{Metadaten.paginierungSeitenProImage}" />
					</a4j:commandLink>
					<h:graphicImage
						rendered="#{Metadaten.paginierungSeitenProImage==4}"
						value="/newpages/images/buttons/paginierung_blatt_rectoverso.png"
						style="margin-left:4px;margin-right:6px;vertical-align:middle"
						title="#{msgs.blattzaehlungrectoverso}" />
								
					<a4j:commandLink
						rendered="#{Metadaten.paginierungSeitenProImage!=5}"
						title="#{msgs.seitenzaehlungrectoverso}"
						reRender="PaginierungActionBox,myMessages,mygrid10">
						<h:graphicImage
							value="/newpages/images/buttons/paginierung_seite_rectoverso_inactive.png"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" />
						<x:updateActionListener value="5"
							property="#{Metadaten.paginierungSeitenProImage}" />
					</a4j:commandLink>
					<h:graphicImage
						rendered="#{Metadaten.paginierungSeitenProImage==5}"
						value="/newpages/images/buttons/paginierung_seite_rectoverso.png"
						style="margin-left:4px;margin-right:6px;vertical-align:middle"
						title="#{msgs.seitenzaehlungrectoverso}" />
						
					<htm:br />
					<htm:br />


						<a4j:commandLink rendered="#{Metadaten.paginierungSeitenProImage!=5}" title="#{msgs.seitenzaehlungrectoverso}"
							reRender="PaginierungActionBox,myMessages,mygrid10">
							<h:graphicImage value="/newpages/images/buttons/paginierung_seite_rectoverso_inactive.png"
								style="margin-left:4px;margin-right:6px;vertical-align:middle" />
							<x:updateActionListener value="5" property="#{Metadaten.paginierungSeitenProImage}" />
						</a4j:commandLink>
						<h:graphicImage rendered="#{Metadaten.paginierungSeitenProImage==5}" value="/newpages/images/buttons/paginierung_seite_rectoverso.png"
							style="margin-left:4px;margin-right:6px;vertical-align:middle" title="#{msgs.seitenzaehlungrectoverso}" />

						<htm:br />
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
