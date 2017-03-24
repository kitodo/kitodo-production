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

<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x" %>

<htm:table width="580">
    <htm:tr>
        <htm:td colspan="2">
            <h:outputText value="#{msgs.strukturelementVerschiebenErlaeuterung}"/>
            <htm:br/>
            <htm:br/>
        </htm:td>
    </htm:tr>
    <htm:tr>
        <htm:td>

            <h:dataTable value="#{Metadaten.strukturBaum3Alle}" var="item"
                         cellpadding="0" cellspacing="0" border="0">
                <h:column>

                    <%-- Popup --%>
                    <x:popup id="z" closePopupOnExitingElement="true"
                             closePopupOnExitingPopup="true" displayAtDistanceX="15"
                             displayAtDistanceY="-40">

                        <h:graphicImage value="/newpages/images/spacer.gif"
                                        rendered="#{item.node.hasChildren}"
                                        style="border: 0px none;margin-top:1px;margin-left:#{item.niveau * 15 + 5};"/>

                        <f:facet name="popup">
                            <htm:div>
                                <h:panelGrid columns="1"
                                             style="background-color:#FFFFEA; font-size:11px; border: 1px solid #CCCCCC; padding: 1px;"
                                             rendered="#{item.node.mainTitle != '' || item.node.zblNummer != '' || item.node.firstImage != '' || item.node.zblSeiten != '' || item.node.ppnDigital != ''}">
                                    <h:panelGrid columns="1" style="font-size: 11" cellspacing="0"
                                                 cellpadding="0" width="110">

                                        <h:outputText value="Maintitle:"
                                                      rendered="#{item.node.mainTitle != ''}"
                                                      style="font-size: 10; text-decoration: underline;font-weight: bold; color: black;"/>
                                        <h:outputText value="#{item.node.mainTitle}"
                                                      rendered="#{item.node.mainTitle != ''}"/>

                                        <h:outputText value="Startimage:"
                                                      rendered="#{node.firstImage!= ''}"
                                                      style="font-size: 10; text-decoration: underline;font-weight: bold; color: black;"/>
                                        <h:outputText value="#{item.node.firstImage}"
                                                      rendered="#{item.node.firstImage != ''}"/>

                                        <h:outputText value="ZBL-Seiten:"
                                                      rendered="#{item.node.zblSeiten != ''}"
                                                      style="font-size: 10; text-decoration: underline;font-weight: bold; color: black;"/>
                                        <h:outputText value="#{item.node.zblSeiten}"
                                                      rendered="#{item.node.zblSeiten != ''}"/>

                                        <h:outputText value="ZBL-ID:"
                                                      rendered="#{item.node.zblNummer != ''}"
                                                      style="font-size: 10; text-decoration: underline;font-weight: bold; color: black;"/>
                                        <h:outputText value="#{item.node.zblNummer}"
                                                      rendered="#{item.node.zblNummer != ''}"/>

                                        <h:outputText value="PPN-Digital"
                                                      rendered="#{item.node.ppnDigital != ''}"
                                                      style="font-size: 10; text-decoration: underline;font-weight: bold; color: black;"/>
                                        <h:outputText value="#{item.node.ppnDigital}"
                                                      rendered="#{item.node.ppnDigital != ''}"/>
                                    </h:panelGrid>
                                </h:panelGrid>
                            </htm:div>
                        </f:facet>

                        <h:graphicImage value="/newpages/images/document.png"
                                        rendered="#{item.node.hasChildren}"
                                        style="margin-right:2px;vertical-align:middle"/>
                        <h:graphicImage value="/newpages/images/document.png"
                                        rendered="#{!item.node.hasChildren}"
                                        style="margin-right:2px;vertical-align:middle;margin-left:#{item.niveau * 10 + 17};"/>

                        <h:commandLink target="links" styleClass="document"
                                       action="#{Metadaten.KnotenVerschieben}"
                                       rendered="#{item.node.einfuegenErlaubt && not item.node.selected}">
                            <h:outputText value="#{item.node.description}"/>
                            <x:updateActionListener
                                    property="#{Metadaten.modusStrukturelementVerschieben}"
                                    value="false"/>
                            <x:updateActionListener
                                    property="#{Metadaten.tempStrukturelement}"
                                    value="#{item.node.struct}"/>
                        </h:commandLink>

                        <h:panelGroup
                                rendered="#{not item.node.einfuegenErlaubt || item.node.selected}">
                            <h:outputText value="#{item.node.description}"
                                          style="font-size: 12px;#{item.node.selected?'color:green;':'color:#999999;'}"/>
                        </h:panelGroup>

                    </x:popup>

                </h:column>

            </h:dataTable>

        </htm:td>
        <htm:td width="1%" valign="top" align="right" nowrap="">
            <htm:div style="border: 2px dashed silver">
                <h:commandLink target="_self" action="#{NavigationForm.Reload}"
                               style="margin:5px" title="#{msgs.verschiebenAbbrechen}">
                    <h:graphicImage value="/newpages/images/buttons/cancel1.gif"
                                    style="border: 0px;vertical-align:middle;"/>
                    <h:outputText value="#{msgs.abbrechen}"
                                  title="#{msgs.verschiebenAbbrechen}"/>
                    <x:updateActionListener
                            property="#{Metadaten.modusStrukturelementVerschieben}"
                            value="false"/>
                </h:commandLink>

                <h:commandButton id="docStructVerschiebenAbbrechen"
                                 action="#{NavigationForm.Reload}" style="display:none">
                    <x:updateActionListener
                            property="#{Metadaten.modusStrukturelementVerschieben}"
                            value="false"/>
                </h:commandButton>
            </htm:div>
        </htm:td>
    </htm:tr>
</htm:table>
