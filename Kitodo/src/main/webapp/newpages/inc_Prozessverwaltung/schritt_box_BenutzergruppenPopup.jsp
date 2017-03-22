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

<%@ page session="false" contentType="text/html;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x" %>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp" %>

<html>
<f:view locale="#{SpracheForm.locale}">
    <%@include file="/newpages/inc/head.jsp" %>
    <body style="margin:0px;padding:0px">
    <h:form id="groupform">

        <h:outputText value="#{msgs.benutzergruppenHinzufuegen}"
                      style="position:absolute;left:10;top:2;color:white;font-weight:bold;font-size:12;z-index:3"/>

        <%-- ===================== // Popup-Rahmen ====================== --%>

        <htm:table style="margin-top:20px" align="center" width="90%"
                   border="0">
            <htm:tr>
                <htm:td>
                    <%-- globale Warn- und Fehlermeldungen --%>
                    <h:messages globalOnly="true" errorClass="text_red"
                                infoClass="text_blue" showDetail="true" showSummary="true"
                                tooltip="true"/>

                    <%-- Datentabelle --%>
                    <x:dataTable styleClass="standardTable" width="100%"
                                 cellspacing="1px" cellpadding="1px"
                                 headerClass="standardTable_Header"
                                 rowClasses="standardTable_Row1,standardTable_Row2"
                                 columnClasses="standardTable_Column,standardTable_ColumnCentered"
                                 var="item" value="#{BenutzergruppenForm.page.listReload}">

                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="#{msgs.benutzergruppe}"/>
                            </f:facet>
                            <h:outputText value="#{item.title}"/>
                        </h:column>

                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="#{msgs.auswahl}"/>
                            </f:facet>
                            <%-- Hinzufügen-Schaltknopf --%>
                            <h:commandLink
                                    action="#{ProzessverwaltungForm.BenutzergruppeHinzufuegen}"
                                    title="#{msgs.uebernehmen}">
                                <h:graphicImage value="/newpages/images/buttons/addUser.gif"/>
                                <x:updateActionListener
                                        property="#{ProzessverwaltungForm.myBenutzergruppe}"
                                        value="#{item}"/>
                            </h:commandLink>

                        </h:column>

                    </x:dataTable>

                    <htm:table width="100%" border="0">
                        <htm:tr valign="top">
                            <htm:td align="left">
                                <%-- ===================== Datascroller für die Ergebnisse ====================== --%>
                                <x:aliasBean alias="#{mypage}"
                                             value="#{BenutzergruppenForm.page}">
                                    <jsp:include page="/newpages/inc/datascroller.jsp"/>
                                </x:aliasBean>
                                <%-- ===================== // Datascroller für die Ergebnisse ====================== --%>
                            </htm:td>
                            <htm:td align="center">
                                <%-- Schliessen-Schaltknopf --%>
                                <jp:closePopupFrame>
                                    <h:commandLink value="#{msgs.close}"
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
