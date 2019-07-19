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

<%-- ########################################

    Kopie einer Prozessvorlage anlegen mit Berücksichtigung komplexer Projekteinstellungen

    #########################################--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<html>
<f:view locale="#{SpracheForm.locale}">
    <%@include file="/newpages/inc/head.jsp"%>
    <body>

        <htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable" align="center">
            <%@include file="/newpages/inc/tbl_Kopf.jsp"%>
            <htm:tr>
                <%@include file="/newpages/inc/tbl_Navigation.jsp"%>
                <htm:td valign="top" styleClass="layoutInhalt">

                    <%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
                    <h:form id="pageform1" onkeypress="return ignoreEnterKey(event);" enctype="multipart/form-data">
                        <%-- Breadcrumb --%>
                        <h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
                            <h:panelGroup>
                                <h:commandLink value="#{msgs.startseite}" action="newMain" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:commandLink value="#{msgs.prozessverwaltung}" action="ProzessverwaltungAlle" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:outputText value="#{msgs.einenNeuenProzessAnlegen}" />
                            </h:panelGroup>
                        </h:panelGrid>

                        <htm:table border="0" align="center" width="100%" cellpadding="15">
                            <htm:tr>
                                <htm:td>
                                    <htm:h3>
                                        <h:outputText value="#{msgs.einenNeuenProzessAnlegen}" />
                                    </htm:h3>

                                    <%-- globale Warn- und Fehlermeldungen --%>
                                    <h:messages globalOnly="true" errorClass="text_red" infoClass="text_blue" showDetail="true" showSummary="true" tooltip="true" />

                                    <%-- Show hit list if several results were found --%>

                                    <htm:div styleClass="modalBackground"
                                        rendered="#{ProzesskopieForm.hitlistShowing}" />
                                    <htm:div styleClass="hitlistBoxWrapper"
                                        rendered="#{ProzesskopieForm.hitlistShowing}">
                                        <htm:div styleClass="hitlistBox">
                                            <htm:h3>
                                                <h:outputText
                                                    value="#{msgs['newProcess.catalogueSearch.heading']}" />
                                            </htm:h3>
                                            <htm:p>
                                                <h:outputFormat
                                                    value="#{msgs['newProcess.catalogueSearch.results']}">
                                                    <f:param value="#{ProzesskopieForm.numberOfHits}" />
                                                </h:outputFormat>
                                            </htm:p>
                                            <x:dataList layout="unorderedList" var="hit"
                                                value="#{ProzesskopieForm.hitlist}">
                                                <h:commandLink action="#{hit.selectClick}" rendered="#{not hit.error}">
                                                    <h:outputText value="#{hit.bibliographicCitation}" escape="false" />
                                                </h:commandLink>
                                                <h:outputText value="#{msgs['newProcess.catalogueSearch.failed']} "
                                                    rendered="#{hit.error}" styleClass="text_red" />
                                                <h:outputText value="#{hit.errorMessage}"
                                                    rendered="#{hit.error}" styleClass="text_red" />
                                            </x:dataList>
                                            <h:commandLink
                                                value="#{msgs['newProcess.catalogueSearch.previousPage']}"
                                                styleClass="leftText"
                                                action="#{ProzesskopieForm.previousPageClick}"
                                                rendered="#{!ProzesskopieForm.firstPage}" />
                                            <h:commandLink
                                                value="#{msgs['newProcess.catalogueSearch.leaveDisplay']}"
                                                styleClass="leftText"
                                                action="#{ProzesskopieForm.previousPageClick}"
                                                rendered="#{ProzesskopieForm.firstPage}" />
                                            <h:commandLink
                                                value="#{msgs['newProcess.catalogueSearch.nextPage']}"
                                                styleClass="rightText"
                                                action="#{ProzesskopieForm.nextPageClick}"
                                                rendered="#{not ProzesskopieForm.lastPage}" />
                                        </htm:div>
                                    </htm:div>

                                    <%-- ===================== Eingabe der Details ====================== --%>
                                    <htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen">

                                        <htm:tr>
                                            <htm:td styleClass="eingabeBoxen_row1" colspan="2">
                                                <h:outputText value="#{msgs.details}" />
                                            </htm:td>
                                        </htm:tr>

                                        <htm:tr>
                                            <htm:td styleClass="eingabeBoxen_row2" colspan="2">
                                                <h:panelGrid columns="1" width="100%">
                                                    <%-- Formular für Eingabe der Prozess-Metadaten --%>
                                                    <%@include file="inc_process.jsp"%>
                                                    <%@include file="inc_config.jsp"%>
                                                </h:panelGrid>
                                            </htm:td>
                                        </htm:tr>

                                        <htm:tr>
                                            <htm:td styleClass="eingabeBoxen_row3" align="left">
                                                <h:commandButton value="#{msgs.abbrechen}" immediate="true" action="ProzessverwaltungAlle">
                                                </h:commandButton>
                                            </htm:td>
                                            <htm:td styleClass="eingabeBoxen_row3" align="right">
                                                <h:commandButton value="#{msgs.weiter}" rendered="#{ProzesskopieForm.prozessKopie.eigenschaftenSize>0}"
                                                    action="#{ProzesskopieForm.GoToSeite2}">
                                                </h:commandButton>
                                                <h:commandButton value="#{msgs.speichern}" rendered="#{ProzesskopieForm.prozessKopie.eigenschaftenSize==0}"
                                                    action="#{ProzesskopieForm.NeuenProzessAnlegen}">
                                                </h:commandButton>
                                                <h:commandButton value="#{msgs.weiter}" rendered="#{ProzesskopieForm.calendarButtonShowing}"
                                                    action="ShowCalendarEditor">
                                                </h:commandButton>
                                            </htm:td>
                                        </htm:tr>
                                    </htm:table>

                                    <%-- ===================== // Eingabe der Details ====================== --%>

                                </htm:td>
                            </htm:tr>
                        </htm:table>
                    </h:form>
                    <%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

                </htm:td>
            </htm:tr>
            <%@include file="/newpages/inc/tbl_Fuss.jsp"%>
        </htm:table>

<script type="text/javascript">
    function getKeyCode(e) {
        var keycode;

        keycode = e.keyCode ? e.keyCode : e.charCode;
        //alert('keycode ' + keycode);

        return keycode;
    }

    function checkOpac(commandId, e) {
        var keycode;

        keycode = getKeyCode(e);

        e.stopPropagation();
        if (keycode == 36) {
            return false;
        } else if ((keycode == 13) && (commandId == 'OpacRequest')) {
            element = document.getElementById('pageform1:performOpacQuery');
            if (element) {
                element.click();

                return false;
            }
        } else {
            return true;
        }

        return true;

    }

    function ignoreEnterKey(e) {
        var keycode;
        keycode = getKeyCode(e);
        if (keycode == 13) {
            return false;
        }
        return true;
    }
</script>
    </body>
</f:view>
</html>
