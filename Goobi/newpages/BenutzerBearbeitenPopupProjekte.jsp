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

<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

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
