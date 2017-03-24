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
<%@ taglib uri="http://www.jenia.org/jsf/dynamic" prefix="jd" %>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi"
           prefix="si" %>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j" %>

<%-- ######################################## 

								Administrationsaufgaben

	#########################################--%>
<a4j:keepAlive beanName="AdministrationForm"/>
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
                <h:form id="myform">
                    <%-- Breadcrumb --%>
                    <h:panelGrid id="id0" columns="1" styleClass="layoutInhaltKopf">
                        <h:panelGroup id="id1">
                            <h:commandLink id="id2" value="#{msgs.startseite}"
                                           action="newMain"/>
                            <f:verbatim> &#8250;&#8250; </f:verbatim>
                            <h:outputText id="id3" value="#{msgs.administrationsaufgaben}"/>
                        </h:panelGroup>
                    </h:panelGrid>

                    <htm:table border="0" align="center" width="100%" cellpadding="15">
                        <htm:tr>
                            <htm:td>
                                <htm:h3>
                                    <h:outputText id="id4" value="#{msgs.administrationsaufgaben}"/>
                                </htm:h3>

                                <%-- globale Warn- und Fehlermeldungen --%>
                                <htm:span style="text-align: right;">
                                    <h:messages id="id5" globalOnly="true" errorClass="text_red"
                                                infoClass="text_blue" showDetail="true" showSummary="true"
                                                tooltip="true" layout="table"
                                                style="margin-bottom:15px;display:block"/>
                                </htm:span>
                                <%-- ++++++++++++++++     Administrationsaufgaben      ++++++++++++++++ --%>
                                <htm:table cellpadding="3" cellspacing="0" width="100%"
                                           styleClass="eingabeBoxen">
                                    <htm:tr>
                                        <htm:td styleClass="eingabeBoxen_row1">
                                            <h:outputText id="id6"
                                                          value="#{msgs.administrationsaufgaben}"/>
                                        </htm:td>
                                    </htm:tr>
                                    <htm:tr>
                                        <htm:td styleClass="eingabeBoxen_row2">

                                            <%-- Administration nur nach zweitem Login --%>
                                            <h:panelGroup
                                                    rendered="#{not AdministrationForm.istPasswortRichtig && (LoginForm.maximaleBerechtigung == 1)}">
                                                <h:panelGrid id="panel1" columns="2">
                                                    <h:outputText id="id7" value="Passwort"/>
                                                    <h:panelGroup id="id8">
                                                        <h:inputSecret id="pw"
                                                                       value="#{AdministrationForm.passwort}"/>
                                                        <h:message id="mess1" for="pw" style="color: red"/>
                                                    </h:panelGroup>
                                                </h:panelGrid>
                                                <h:commandButton id="button2" value="Weiter"
                                                                 action="#{AdministrationForm.Weiter}"/>
                                            </h:panelGroup>

                                            <%-- Administrationsaufgaben --%>
                                            <h:panelGroup
                                                    rendered="#{AdministrationForm.istPasswortRichtig && (LoginForm.maximaleBerechtigung == 1)}">

                                                <h:commandLink id="id9" value="Prozesse durchlaufen"
                                                               rendered="false"
                                                               onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                               action="#{AdministrationForm.prozesseDurchlaufen}"/>
                                                <htm:br/>

                                                <%-- Schritt zurückgeben an vorherige Station für Korrekturzwecke --%>
                                                <h:panelGroup id="id10">
                                                    <jd:hideableController for="bitteAusloggen"
                                                                           title="#{msgs.benutzerZumAusloggenAuffordern}">
                                                        <h:outputText
                                                                value="#{msgs.benutzerZumAusloggenAuffordern}"/>
                                                    </jd:hideableController>
                                                    <jd:hideableArea id="bitteAusloggen" saveState="view">
                                                        <h:panelGrid id="id11" columns="2"
                                                                     style="margin-left:40px;" rowClasses="top"
                                                                     columnClasses="standardTable_Column,standardTable_ColumnRight">
                                                            <h:outputText id="id12" value="#{msgs.bemerkung}"/>
                                                            <h:inputTextarea id="id13"
                                                                             style="width:350px;height:80px"
                                                                             value="#{SessionForm.bitteAusloggen}"/>

                                                            <h:outputText id="id14" value=""/>
                                                            <h:commandLink id="id15"
                                                                           action="#{NavigationForm.Reload}"
                                                                           title="#{msgs.uebernehmen}"
                                                                           onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return">
                                                                <h:outputText id="id16" value="#{msgs.uebernehmen}"/>
                                                            </h:commandLink>
                                                        </h:panelGrid>
                                                    </jd:hideableArea>
                                                </h:panelGroup>
                                                <htm:br/>


                                                <a4j:commandLink id="renderlink" reRender="myform"
                                                                 action="#{AdministrationForm.startStorageCalculationForAllProcessesNow}"
                                                                 immediate="true" onclick="changeToText()"
                                                                 oncomplete="changeToLink()">
                                                    <h:outputText id="id17"
                                                                  value="Run complete history analyser job now"/>
                                                </a4j:commandLink>
                                                <htm:br/>
                                                <h:commandLink id="adminid23"
                                                               value="Lucene Index neu erzeugen"
                                                               onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                               action="#{AdministrationForm.createIndex}"/>
                                                <htm:br/>


                                                <x:outputText id="rendertext" forceId="true"
                                                              value="Run complete history analyser job now"
                                                              style="display:none"/>

                                                <htm:br/>

                                                <h:commandLink
                                                        action="#{AdministrationForm.restartStorageCalculationScheduler}">
                                                    <h:outputText id="id18"
                                                                  value="Restart automatic job manager"/>
                                                </h:commandLink>
                                                <htm:br/>
                                                <htm:br/>

                                                <htm:hr/>
                                                <htm:br/>


                                                <%--
                                            <h:commandLink
                                                action="#{AdministrationForm.startStorageCalculationForAllProcessesNow}">
                                                <h:outputText id="id19" value="storage calculator now" />
                                            </h:commandLink>
                                            <htm:br />
                                              --%>
                                                <%--
                                            <h:commandLink
                                                action="#{AdministrationForm.OlmsOnlineBaendeAnlegen}">
                                                <h:outputText id="id20" value="Olms Online - Baende anlegen" />
                                            </h:commandLink>
                                            <htm:br />
                                             --%>

                                                <h:commandLink id="id21" action="#{NavigationForm.Reload}"
                                                               title="#{AdministrationForm.rusFullExport}">
                                                    <x:updateActionListener
                                                            value="#{AdministrationForm.rusFullExport?false:true}"
                                                            property="#{AdministrationForm.rusFullExport}"/>
                                                    <h:outputText
                                                            value="#{msgs.russischeMetadatenExportieren}: "/>
                                                    <h:outputText id="id22"
                                                                  value="#{AdministrationForm.rusFullExport}"/>
                                                </h:commandLink>
                                                <htm:br/>
                                                <%--
                                            <htm:br />
                                            <h:commandLink id="id23" value="Groovy Test"
                                                onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                action="#{AdministrationForm.GroovyTest}" />
                                            <htm:br />
                                            --%>
                                                <h:commandLink id="id24" value="Sici korrigieren"
                                                               rendered="false"
                                                               onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                               action="#{AdministrationForm.siciKorr}"/>
                                                <htm:br/>

                                                <h:commandLink id="id25" value="ProzesseDatumSetzen"
                                                               rendered="true"
                                                               onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                               action="#{AdministrationForm.prozesseDatumSetzen}"/>
                                                <htm:br/>

                                                <h:commandLink id="id26" value="RusDmlBaendeTiffPruefen"
                                                               rendered="true"
                                                               onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                               action="#{AdministrationForm.RusDmlBaendeTiffPruefen}"/>
                                                <htm:br/>

                                                <h:commandLink id="id27" value="Imagepfad korrigieren"
                                                               rendered="true"
                                                               onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                               action="#{AdministrationForm.imagepfadKorrigieren}"/>
                                                <htm:br/>

                                                <h:commandLink id="id28"
                                                               value="MesskatalogeOrigOrdner erstellen" rendered="true"
                                                               onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                               action="#{AdministrationForm.MesskatalogeOrigOrdnerErstellen}"/>
                                                <htm:br/>

                                                <h:commandLink id="id29" value="PPNs korrigieren"
                                                               rendered="true"
                                                               onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                               action="#{AdministrationForm.ppnsKorrigieren}"/>
                                                <htm:br/>

                                                <h:commandLink
                                                        value="PPNs für Statistisches Jahrbuch korrigieren"
                                                        rendered="true"
                                                        onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                        action="#{AdministrationForm.ppnsFuerStatistischesJahrbuchKorrigieren}"/>
                                                <htm:br/>

                                                <h:commandLink
                                                        value="PPNs für Statistisches Jahrbuch korrigieren2"
                                                        rendered="true"
                                                        onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                        action="#{AdministrationForm.ppnsFuerStatistischesJahrbuchKorrigierenTwo}"/>
                                                <htm:br/>

                                                <h:commandLink id="id30" value="Anzahlen ermitteln"
                                                               rendered="true"
                                                               onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                               action="#{AdministrationForm.anzahlenErmitteln}"/>
                                                <htm:br/>

                                                <h:commandLink id="id31" value="LDAP testen"
                                                               rendered="false"
                                                               onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                               action="#{AdministrationForm.LDAPtest}"/>
                                                <htm:br/>

                                                <h:commandLink id="id32" value="Regelsatz festlegen"
                                                               rendered="false"
                                                               onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                               action="#{AdministrationForm.standardRegelsatzSetzen}"/>
                                                <htm:br/>
                                                <htm:br/>

                                                <%-- Classloader-Plugin --%>
                                                <%--
                                            <h:outputLabel id="id33" for="plugins" value="#{msgs.Plugins}" />
                                            <h:selectOneMenu id="plugins"
                                                value="#{AdministrationForm.myPlugin}">
                                                <si:selectItems id="id34" value="#{AdministrationForm.myPluginList}"
                                                    var="step" itemLabel="#{step}" itemValue="#{step}" />
                                            </h:selectOneMenu>
                                            <h:commandLink id="id35" value="Plugin ausführen"
                                                onclick="if (!confirm('#{msgs.sicher}?')) return"
                                                action="#{AdministrationForm.startPlugin}" />

                                            <htm:br />
                                            --%>

                                            </h:panelGroup>

                                        </htm:td>
                                    </htm:tr>
                                </htm:table>

                                <%-- ++++++++++++++++     // Administrationsaufgaben      ++++++++++++++++ --%>

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
<script type="text/javascript">
    function changeToText() {
        document.getElementById("myform:renderlink").style.display = "none";
        document.getElementById("rendertext").style.display = "inline";
    }

    function changeToLink() {
        document.getElementById("myform:renderlink").style.display = "inline";
        document.getElementById("rendertext").style.display = "none";

    }
</script>
</html>


