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
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j" %>

<%-- ######################################## 

							Alle Benutzer in der Übersicht

	#########################################--%>
<a4j:keepAlive beanName="BenutzerverwaltungForm"/>
<html>
<f:view locale="#{SpracheForm.locale}">
    <%@include file="inc/head.jsp" %>
    <body>
    <htm:table styleClass="headTable" cellspacing="0" cellpadding="0"
               style="padding-left:5px;padding-right:5px;margin-top:5px;">
        <%@include file="inc/tbl_Kopf.jsp" %>
    </htm:table>
    <htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable"
               align="center">

        <htm:tr>
            <%@include file="inc/tbl_Navigation.jsp" %>
            <htm:td valign="top" styleClass="layoutInhalt">

                <%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
                <h:form id="userform">
                    <%-- Breadcrumb --%>
                    <h:panelGrid id="id0" width="100%" columns="1"
                                 styleClass="layoutInhaltKopf">
                        <h:panelGroup id="id1">
                            <h:commandLink id="id2" value="#{msgs.startseite}"
                                           action="newMain"/>
                            <f:verbatim> &#8250;&#8250; </f:verbatim>
                            <h:outputText id="id3" value="#{msgs.benutzerverwaltung}"/>
                        </h:panelGroup>
                    </h:panelGrid>

                    <htm:table border="0" align="center" width="100%" cellpadding="15">
                        <htm:tr>
                            <htm:td>

                                <%-- Caption Administration / Users --%>
                                <htm:h3>
                                    <h:outputText id="id4" value="#{msgs.users}"/>
                                </htm:h3>
                                <h:commandLink id="id5" action="#{BenutzerverwaltungForm.newUser}"
                                               immediate="true"
                                               rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
                                    <h:outputText id="id6" value="#{msgs.neuenBenutzerAnlegen}"/>
                                </h:commandLink>
                                <%-- globale Warn- und Fehlermeldungen --%>
                                <htm:span style="text-align: right;">
                                    <h:messages id="id7" globalOnly="true" errorClass="text_red"
                                                infoClass="text_blue" showDetail="true" showSummary="true"
                                                tooltip="true"/>
                                </htm:span>


                                <%-- +++++++++++++++++  Anzeigefilter ++++++++++++++++++++++++ --%>
                                <h:panelGrid id="id8" width="100%"
                                             columnClasses="standardTable_Column,standardTable_ColumnRight"
                                             rowClasses="standardTable_Row" columns="2" style="margin-top: 10px;">
                                    <h:outputText style="vertical-align: conter;"
                                                  value="#{msgs.treffer}: #{BenutzerverwaltungForm.page.totalResults}"/>
                                    <h:panelGroup id="id9">
                                        <h:outputText id="id10"
                                                      value="#{msgs.nurAktiveNutzerZeigen}:"/>
                                        <x:selectBooleanCheckbox id="check1" forceId="true"
                                                                 value="#{BenutzerverwaltungForm.hideInactiveUsers}"
                                                                 onchange="document.getElementById('FilterAlle').click()"
                                                                 style="margin-right:40px"/>

                                        <h:outputText id="id11" value="#{msgs.filter}: "/>

                                        <h:inputText id="id12"
                                                     value="#{BenutzerverwaltungForm.filter}"
                                                     onkeypress="return submitEnter('FilterAlle',event)"/>
                                        <x:commandButton type="submit" id="FilterAlle" forceId="true"
                                                         style="display:none"
                                                         action="#{BenutzerverwaltungForm.filterAlleStart}"/>

                                        <h:commandLink
                                                action="#{BenutzerverwaltungForm.filterAlleStart}"
                                                title="#{msgs.filterAnwenden}" style="margin-left:5px">
                                            <h:graphicImage id="id13"
                                                            value="/newpages/images/buttons/reload.gif"/>
                                        </h:commandLink>


                                    </h:panelGroup>
                                </h:panelGrid>

                                <%-- +++++++++++++++++  // Anzeigefilter ++++++++++++++++++++++++ --%>


                                <%-- Datentabelle --%>
                                <x:dataTable id="id14" styleClass="standardTable" width="100%"
                                             cellspacing="1px" cellpadding="1px"
                                             headerClass="standardTable_Header"
                                             rowClasses="standardTable_Row1,standardTable_Row2"
                                             columnClasses="standardTable_Column,standardTable_Column,standardTable_Column,standardTable_Column, standardTable_ColumnCentered"
                                             var="item" value="#{BenutzerverwaltungForm.page.listReload}">

                                    <h:column id="id15">
                                        <f:facet name="header">
                                            <h:outputText id="id16" value="#{msgs.benutzer}"/>
                                        </f:facet>
                                        <h:outputText id="id17"
                                                      value="#{item.surname}, #{item.name}"
                                                      styleClass="#{not item.active?'text_light':''}"/>
                                    </h:column>

                                    <h:column id="id18">
                                        <f:facet name="header">
                                            <h:outputText id="id19" value="#{msgs.standort}"/>
                                        </f:facet>
                                        <h:outputText id="id20" value="#{item.location}"
                                                      styleClass="#{not item.active?'text_light':''}"/>
                                    </h:column>

                                    <h:column id="id21">
                                        <f:facet name="header">
                                            <h:outputText id="id22" value="#{msgs.benutzergruppen}"/>
                                        </f:facet>
                                        <x:dataList id="id23" var="intern"
                                                    styleClass="#{not item.active?'text_light':''}"
                                                    rendered="#{item.userGroupSize != 0}"
                                                    value="#{item.userGroups}" layout="ordered list"
                                                    rowCountVar="rowCount" rowIndexVar="rowIndex">
                                            <h:outputText id="id24" value="#{intern.title}"/>
                                            <h:outputText id="id25" value=","
                                                          rendered="#{rowIndex + 1 < rowCount}"/>
                                        </x:dataList>
                                    </h:column>

                                    <h:column id="id26">
                                        <f:facet name="header">
                                            <h:outputText id="id27" value="#{msgs.projekte}"/>
                                        </f:facet>
                                        <x:dataList id="id28" var="intern"
                                                    styleClass="#{not item.active?'text_light':''}"
                                                    rendered="#{item.projectsSize != 0}"
                                                    value="#{item.projects}" layout="ordered list"
                                                    rowCountVar="rowCount" rowIndexVar="rowIndex">
                                            <h:outputText id="id29" value="#{intern.title}"/>
                                            <h:outputText id="id30" value=","
                                                          rendered="#{rowIndex + 1 < rowCount}"/>
                                        </x:dataList>
                                    </h:column>

                                    <h:column
                                            rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
                                        <f:facet name="header">
                                            <h:outputText id="id31" value="#{msgs.auswahl}"/>
                                        </f:facet>
                                        <%-- Bearbeiten-Schaltknopf --%>
                                        <h:commandLink id="id32" action="BenutzerBearbeiten"
                                                       title="#{msgs.benutzerBearbeiten}">
                                            <h:graphicImage id="id33"
                                                            value="/newpages/images/buttons/edit.gif"/>
                                            <x:updateActionListener
                                                    property="#{BenutzerverwaltungForm.myClass}"
                                                    value="#{item}"/>
                                        </h:commandLink>

                                        <%-- LdapKonfiguration schreiben-Schaltknopf --%>
                                        <%-- 										<h:commandLink id="id34" title="#{msgs.ldapKonfigurationSchreiben}"
                                        action="#{BenutzerverwaltungForm.LdapKonfigurationSchreiben}">
                                        <h:graphicImage id="id35" value="/newpages/images/buttons/key3.gif" />
                                        <x:updateActionListener
                                            property="#{BenutzerverwaltungForm.myClass}" value="#{item}" />
                                    </h:commandLink>
--%>
                                        <%-- Benutzerprofil laden-Schaltknopf --%>
                                        <h:commandLink id="id36" title="#{msgs.benutzerprofilLaden}"
                                                       action="#{LoginForm.EinloggenAls}" style="margin-left:15px">
                                            <h:graphicImage
                                                    value="/newpages/images/buttons/change_user3_20px.gif"/>
                                            <f:param id="id37" name="ID" value="#{item.id}"/>
                                        </h:commandLink>

                                    </h:column>

                                </x:dataTable>
                                <h:commandLink id="id52" action="#{BenutzerverwaltungForm.newUser}"
                                               immediate="true"
                                               rendered="#{((LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)) && (BenutzerverwaltungForm.page.totalResults > LoginForm.myBenutzer.tableSize)}"
                                >
                                    <h:outputText id="id62" value="#{msgs.neuenBenutzerAnlegen}"/>
                                </h:commandLink>
                                <htm:table width="100%" border="0">
                                    <htm:tr valign="top">
                                        <htm:td align="left">
                                            <%-- newUser-Schaltknopf --%>
                                            <%-- 				<h:commandLink id="id38" action="#{BenutzerverwaltungForm.newUser}"
                                            immediate="true"
                                            rendered="#{(LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2)}">
                                            <h:outputText id="id39" value="#{msgs.neuenBenutzerAnlegen}" />
                                        </h:commandLink> --%>
                                        </htm:td>
                                        <htm:td align="center">
                                            <%-- ===================== Datascroller für die Ergebnisse ====================== --%>
                                            <x:aliasBean alias="#{mypage}"
                                                         value="#{BenutzerverwaltungForm.page}">
                                                <jsp:include page="/newpages/inc/datascroller.jsp"/>
                                            </x:aliasBean>
                                            <%-- ===================== // Datascroller für die Ergebnisse ====================== --%>
                                        </htm:td>
                                    </htm:tr>
                                </htm:table>
                            </htm:td>
                        </htm:tr>
                    </htm:table>
                </h:form>
                <%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

            </htm:td>
        </htm:tr>
        <%@include file="inc/tbl_Fuss.jsp" %>
    </htm:table>

    </body>
</f:view>

</html>
