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

<htm:table cellpadding="3" cellspacing="0" style="width:100%">
    <htm:tr style="vertical-align:top">
        <htm:td>

            <htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="main_statistikboxen">

                <htm:tr>
                    <htm:td styleClass="main_statistikboxen_row1">
                        <h:outputText value="#{msgs.eigenschaften}" />
                    </htm:td>
                </htm:tr>


                <htm:tr>
                    <htm:td styleClass="main_statistikboxen_row2">

                        <htm:table border="0" width="90%" cellpadding="2">
                            <htm:tr>
                                <htm:td width="150">
                                    <h:outputText value="#{msgs.titel}:" />
                                </htm:td>
                                <htm:td>
                                    <h:outputText value="#{AktuelleSchritteForm.mySchritt.titelLokalisiert}" />
                                </htm:td>
                            </htm:tr>

                            <htm:tr>
                                <htm:td width="150">
                                    <h:outputText value="#{msgs.prozessTitel}:" />
                                </htm:td>
                                <htm:td>
                                    <h:outputText value="#{AktuelleSchritteForm.mySchritt.prozess.titel}" />
                                </htm:td>
                            </htm:tr>
                            <htm:tr>
                                <htm:td width="150">
                                    <h:outputText value="#{msgs.reihenfolge}:" />
                                </htm:td>
                                <htm:td>
                                    <h:outputText value="#{AktuelleSchritteForm.mySchritt.reihenfolge}" />
                                </htm:td>
                            </htm:tr>
                            <htm:tr>
                                <htm:td width="150">
                                    <h:outputText value="#{msgs.prioritaet}:" />
                                </htm:td>
                                <htm:td>
                                    <h:outputText value="#{AktuelleSchritteForm.mySchritt.prioritaet}" rendered="#{AktuelleSchritteForm.mySchritt.prioritaet!=10}" />
                                    <h:outputText value="#{msgs.korrektur}" rendered="#{AktuelleSchritteForm.mySchritt.prioritaet==10}" />
                                </htm:td>
                            </htm:tr>
                            <htm:tr>
                                <htm:td width="150">
                                    <h:outputText value="#{msgs.status}:" />
                                </htm:td>
                                <htm:td>
                                    <h:outputText value="#{AktuelleSchritteForm.mySchritt.bearbeitungsstatusEnum.title}" />
                                </htm:td>
                            </htm:tr>

                            <htm:tr rendered="#{AktuelleSchritteForm.mySchritt.bearbeitungsbeginn !=null && !HelperForm.anonymized}">
                                <htm:td width="150">
                                    <h:outputText value="#{msgs.bearbeitungsbeginn}:" />
                                </htm:td>
                                <htm:td>
                                    <h:outputText value="#{AktuelleSchritteForm.mySchritt.bearbeitungsbeginnAsFormattedString}" />
                                </htm:td>
                            </htm:tr>
                            <htm:tr rendered="#{AktuelleSchritteForm.mySchritt.bearbeitungszeitpunkt !=null && !HelperForm.anonymized}">
                                <htm:td width="150">
                                    <h:outputText value="#{msgs.zuletztBearbeitet}:" />
                                </htm:td>
                                <htm:td>
                                    <h:outputText value="#{AktuelleSchritteForm.mySchritt.bearbeitungszeitpunktAsFormattedString}" />
                                </htm:td>
                            </htm:tr>

                            <htm:tr rendered="#{AktuelleSchritteForm.mySchritt.bearbeitungszeitpunkt !=null && !HelperForm.anonymized}">
                                <htm:td width="150">
                                    <h:outputText value="#{msgs.aktualisierungstyp}:" />
                                </htm:td>
                                <htm:td>
                                    <h:outputText value="#{AktuelleSchritteForm.mySchritt.editTypeEnum.title}" />
                                </htm:td>
                            </htm:tr>

                        </htm:table>
                    </htm:td>
                </htm:tr>
            </htm:table>

        </htm:td>
        <htm:td>
            <htm:table>
                <htm:tr>
                    <htm:td style="border: 1px solid lightgray;">
                        <h:outputText escape="false" value="#{AktuelleSchritteForm.wikiField}" />
                    </htm:td>
                </htm:tr>
                <htm:tr>
                    <htm:td>
                        <h:form id="addToWikiForm">
                            <h:inputText id="addToTextArea" value="#{AktuelleSchritteForm.addToWikiField}" style="width: 60%" />
                            <h:commandButton value="#{msgs.nachrichtHinzufuegen}" action="#{AktuelleSchritteForm.addToWikiField}" />
                        </h:form>
                    </htm:td>
                </htm:tr>
            </htm:table>

        </htm:td>
    </htm:tr>
</htm:table>
