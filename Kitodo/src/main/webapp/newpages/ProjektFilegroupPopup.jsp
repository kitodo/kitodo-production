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
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j" %>

<a4j:keepAlive beanName="ProjekteForm"/>
<html>
<f:view locale="#{SpracheForm.locale}">
    <%@include file="inc/head.jsp" %>
    <body style="margin: 0px; padding: 0px">
    <h:form id="filegroupform">

        <h:outputText id="id0" value="#{msgs.filegroup}"
                      style="position:absolute;left:10;top:2;color:white;font-weight:bold;font-size:12;z-index:3"/>

        <%-- ===================== // Popup-Rahmen ====================== --%>

        <htm:table style="margin-top:20px" align="center" width="90%"
                   border="0">
            <htm:tr>
                <htm:td>
                    <%-- globale Warn- und Fehlermeldungen --%>
                    <h:messages id="id1" globalOnly="true" errorClass="text_red"
                                infoClass="text_blue" showDetail="true" showSummary="true"
                                tooltip="true"/>

                    <%-- Box für die Bearbeitung der Details --%>
                    <htm:table cellpadding="3" cellspacing="0" width="100%"
                               styleClass="eingabeBoxen">

                        <htm:tr>
                            <htm:td styleClass="eingabeBoxen_row1" align="left">
                                <h:outputText id="id2" value="#{msgs.details}"/>
                            </htm:td>
                            <htm:td styleClass="eingabeBoxen_row1" align="right">
                                <h:commandLink id="id3" action="#{NavigationForm.Reload}">
                                    <h:graphicImage id="id4" value="/newpages/images/reload.gif"/>
                                </h:commandLink>
                            </htm:td>
                        </htm:tr>

                        <%-- Formular für die Bearbeitung der Texte --%>
                        <htm:tr>
                            <htm:td styleClass="eingabeBoxen_row2" colspan="2">

                                <h:panelGrid id="id5" columns="2" rowClasses="top">

                                    <%-- name --%>
                                    <h:outputLabel id="id6" for="name" value="#{msgs.name}"/>
                                    <h:panelGroup id="id7">
                                        <h:inputText id="name" style="width: 550px;margin-right:15px"
                                                     value="#{ProjekteForm.myFilegroup.name}" required="true"/>
                                        <x:message id="id8" for="name" style="color: red"
                                                   replaceIdWithLabel="true"/>
                                    </h:panelGroup>

                                    <%-- path --%>
                                    <h:outputLabel id="id9" for="path" value="#{msgs.path}"/>
                                    <h:panelGroup id="id10">
                                        <h:inputText id="path" style="width: 550px;margin-right:15px"
                                                     value="#{ProjekteForm.myFilegroup.path}" required="true"/>
                                        <x:message id="id11" for="path" style="color: red"
                                                   replaceIdWithLabel="true"/>
                                    </h:panelGroup>

                                    <%-- mimetype --%>
                                    <h:outputLabel id="id12" for="mimetype" value="#{msgs.mimetype}"/>
                                    <h:panelGroup id="id13">
                                        <h:inputText id="mimetype"
                                                     style="width: 550px;margin-right:15px"
                                                     value="#{ProjekteForm.myFilegroup.mimeType}" required="true"/>
                                        <x:message id="id14" for="mimetype" style="color: red"
                                                   replaceIdWithLabel="true"/>
                                    </h:panelGroup>

                                    <%-- suffix --%>
                                    <h:outputLabel id="id15" for="suffix" value="#{msgs.suffix}"/>
                                    <h:panelGroup id="id16">
                                        <h:inputText id="suffix"
                                                     style="width: 550px;margin-right:15px"
                                                     value="#{ProjekteForm.myFilegroup.suffix}" required="true"/>
                                        <x:message id="id17" for="suffix" style="color: red"
                                                   replaceIdWithLabel="true"/>
                                    </h:panelGroup>

                                    <%-- folder --%>
                                    <h:outputLabel for="folder" value="#{msgs.folder}"/>
                                    <h:panelGroup>
                                        <h:inputText id="folder"
                                                     style="width: 550px;margin-right:15px"
                                                     value="#{ProjekteForm.myFilegroup.folder}" required="false"/>
                                    </h:panelGroup>

                                    <%-- use for preview image --%>
                                    <h:panelGroup/>
                                    <h:panelGroup>
                                        <h:selectBooleanCheckbox id="pimage"
                                                                 style="margin-right: 6px;vertical-align: sub"
                                                                 value="#{ProjekteForm.myFilegroup.previewImage}"/>
                                        <h:outputLabel for="pimage" value="#{msgs.useForPreviewImage}"/>
                                    </h:panelGroup>

                                </h:panelGrid>
                            </htm:td>
                        </htm:tr>

                        <htm:tr>
                            <htm:td styleClass="eingabeBoxen_row3" align="left">

                                <%-- cancel-Schaltknopf --%>
                                <jp:closePopupFrame>
                                    <h:commandLink id="id18" value="#{msgs.abbrechen}" immediate="true"
                                                   action="#{NavigationForm.JeniaPopupCloseAction}"></h:commandLink>
                                </jp:closePopupFrame>

                            </htm:td>
                            <htm:td styleClass="eingabeBoxen_row3" align="right">
                                <%-- uebernehmen-Schaltknopf --%>
                                <jp:closePopupFrame>
                                    <h:commandLink id="id19" action="#{ProjekteForm.filegroupSave}"
                                                   title="#{msgs.uebernehmen}" value="#{msgs.uebernehmen}">
                                    </h:commandLink>
                                </jp:closePopupFrame>
                            </htm:td>
                        </htm:tr>
                    </htm:table>
                    <%-- // Box für die Bearbeitung der Details --%>


                </htm:td>
            </htm:tr>
        </htm:table>
    </h:form>
    </body>
</f:view>

</html>
