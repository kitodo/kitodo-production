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

<h:panelGrid columns="1" width="100%" align="center"
    rendered="#{LoginForm.myBenutzer != null}" style="margin-bottom:0px;">

    <h:commandLink action="#{LoginForm.Ausloggen}" styleClass="th_menu" id="logout">
        <h:outputText style="text-align:right" value="#{msgs.logout}:" />
        <htm:br />

        <%-- Mouse-Over für Benutzergruppenmitgliedschaft --%>
        <x:popup
            styleClass="popup"
            closePopupOnExitingElement="true" closePopupOnExitingPopup="true"
            displayAtDistanceX="10" displayAtDistanceY="-10">

            <h:outputText rendered="#{LoginForm.myBenutzer != null}"
                style="font-weight: normal"
                value="#{LoginForm.myBenutzer.nachname}, #{LoginForm.myBenutzer.vorname}" />

            <f:facet name="popup">
                <h:panelGroup>
                    <h:panelGrid columns="1" width="200">
                        <h:outputText style="font-weight:bold"
                            value="#{msgs.benutzergruppen}" />

                        <x:dataList var="intern" style="font-weight: normal"
                            rendered="#{LoginForm.myBenutzer.benutzergruppenSize != 0}"
                            value="#{LoginForm.myBenutzer.benutzergruppenList}"
                            layout="ordered list" rowCountVar="rowCount"
                            rowIndexVar="rowIndex">
                            <h:outputText value="#{intern.titel}" />
                            <h:outputText value=";" rendered="#{rowIndex + 1 < rowCount}" />
                        </x:dataList>

                    </h:panelGrid>
                </h:panelGroup>
            </f:facet>
        </x:popup>

    </h:commandLink>
</h:panelGrid>

<h:panelGrid rendered="#{LoginForm.myBenutzer == null}" columns="1" width="90%" align="center"
    styleClass="loginBorder">
    <h:panelGroup id="logintable"
        rendered="#{LoginForm.myBenutzer == null && !LoginForm.schonEingeloggt}">
        <h:panelGrid columns="2" style="font-size: 9px" align="center">

            <h:outputText value="#{msgs.login}" />
            <h:panelGroup>
                <h:message id="messlogin" for="login" style="color: red" />
                <x:inputText id="login" forceId="true" style="width: 80px"
                    value="#{LoginForm.login}" />
            </h:panelGroup>

            <h:outputText value="#{msgs.passwort}" />
            <h:panelGroup>
                <h:message id="messpasswort" for="passwort" style="color: red" />
                <x:inputSecret id="passwort" forceId="true" style="width: 80px"
                    value="#{LoginForm.passwort}"
                    onkeypress="return submitEnter('LoginAbsenden2',event)" />
            </h:panelGroup>

        </h:panelGrid>
        <h:commandLink action="#{LoginForm.Einloggen}">
            <h:outputText value="#{msgs.einloggen}"
                rendered="#{!LoginForm.schonEingeloggt}" />
            <h:outputText value="#{msgs.dennochEinloggen}"
                rendered="#{LoginForm.schonEingeloggt}" />
        </h:commandLink>
        <x:commandButton id="LoginAbsenden2" forceId="true"
            style="display:none" type="submit" action="#{LoginForm.Einloggen}"
            value="#{msgs.einloggen}" />
    </h:panelGroup>

    <h:panelGroup
        rendered="#{LoginForm.myBenutzer == null && LoginForm.schonEingeloggt}">

        <h:outputText value="#{msgs.sieSindBereitsEingeloggt}" />
        <htm:br />
        <htm:br />
        <h:commandLink value="#{msgs.abbrechen}" id="login-cancel"
            action="#{LoginForm.Ausloggen}" />
        <htm:br />
        <h:commandLink value="#{msgs.alteSessionsAufraeumen}" id="login-clean"
            action="#{LoginForm.EigeneAlteSessionsAufraeumen}" />
        <htm:br />
        <h:commandLink value="#{msgs.dennochEinloggen}" id="login-go"
            action="#{LoginForm.NochmalEinloggen}" />

    </h:panelGroup>
</h:panelGrid>
