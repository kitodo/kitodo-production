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

<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="main_statistikboxen">

    <htm:tr>
        <htm:td styleClass="main_statistikboxen_row1" colspan="2">
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
                        <h:outputText value="#{item.titelLokalisiert}" />
                    </htm:td>
                </htm:tr>
                <htm:tr>
                    <htm:td width="150">
                        <h:outputText value="#{msgs.prioritaet}:" />
                    </htm:td>
                    <htm:td>
                        <h:outputText value="#{item.prioritaet}" />
                    </htm:td>
                </htm:tr>
                <htm:tr rendered="#{item.bearbeitungsbeginn !=null && !HelperForm.anonymized}">
                    <htm:td width="150">
                        <h:outputText value="#{msgs.bearbeitungsbeginn}:" />
                    </htm:td>
                    <htm:td>
                        <h:outputText value="#{item.bearbeitungsbeginnAsFormattedString}" />
                    </htm:td>
                </htm:tr>
                <htm:tr rendered="#{item.bearbeitungszeitpunkt !=null && !HelperForm.anonymized}">
                    <htm:td width="150">
                        <h:outputText value="#{msgs.zuletztBearbeitet}:" />
                    </htm:td>
                    <htm:td>
                        <h:outputText value="#{item.bearbeitungszeitpunktAsFormattedString}" />
                    </htm:td>
                </htm:tr>
                <htm:tr rendered="#{item.bearbeitungsende !=null && !HelperForm.anonymized}">
                    <htm:td width="150">
                        <h:outputText value="#{msgs.bearbeitungsende}:" />
                    </htm:td>
                    <htm:td>
                        <h:outputText value="#{item.bearbeitungsendeAsFormattedString}" />
                    </htm:td>
                </htm:tr>
                <htm:tr rendered="#{item.bearbeitungsbenutzer !=null && item.bearbeitungsbenutzer.id !=0 && !HelperForm.anonymized}">
                    <htm:td width="150">
                        <h:outputText value="#{msgs.letzteAktualisierungDurch}:" />
                    </htm:td>
                    <htm:td>
                        <h:outputText value="#{item.bearbeitungsbenutzer.nachVorname}" />
                    </htm:td>
                </htm:tr>
                <htm:tr rendered="#{item.editTypeEnum !=null}">
                    <htm:td width="150">
                        <h:outputText value="#{msgs.aktualisierungstyp}:" />
                    </htm:td>
                    <htm:td>
                        <h:outputText value="#{item.editTypeEnum.title}" />
                    </htm:td>
                </htm:tr>

                <htm:tr rendered="#{item.typAutomatischScriptpfad != null && item.typAutomatischScriptpfad != ''}">
                    <htm:td width="150">
                        <h:outputText value="#{msgs.automatischerSchritt}:" />
                    </htm:td>
                    <htm:td>
                        <h:outputText value="#{item.typAutomatischScriptpfad}" />
                    </htm:td>
                </htm:tr>

                <htm:tr rendered="#{item.typModulName != null && item.typModulName != ''}">
                    <htm:td width="150">
                        <h:outputText value="#{msgs.module}:" />
                    </htm:td>
                    <htm:td>
                        <h:outputText value="#{item.typModulName}" />
                    </htm:td>
                </htm:tr>

            </htm:table>
        </htm:td>
    </htm:tr>

</htm:table>
