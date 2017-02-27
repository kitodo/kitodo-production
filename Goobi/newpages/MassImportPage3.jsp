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
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>

<%-- ########################################

                            Add mass import

    #########################################--%>
<a4j:keepAlive beanName="MassImportForm" />
<html>
<f:view locale="#{SpracheForm.locale}">
    <%@include file="inc/head.jsp"%>
    <body>
        <htm:table styleClass="headTable" cellspacing="0" cellpadding="0" style="padding-left:5px;padding-right:5px;margin-top:5px;">
            <%@include file="inc/tbl_Kopf.jsp"%>
        </htm:table>
        <htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable" align="center">
            <link href="../css/tabbedPane.css" rel="stylesheet" type="text/css" />
            <htm:tr>
                <%@include file="inc/tbl_Navigation.jsp"%>
                <htm:td valign="top" styleClass="layoutInhalt">

                    <%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
                    <h:form enctype="multipart/form-data" id="formupload">
                        <%-- Breadcrumb --%>
                        <h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf" id="projgrid112">
                            <h:panelGroup id="id1">
                                <h:commandLink value="#{msgs.startseite}" action="newMain" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:commandLink value="#{msgs.prozessverwaltung}" action="ProzessverwaltungAlle" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:outputText value="#{msgs.MassImport}" />
                            </h:panelGroup>
                        </h:panelGrid>


                        <htm:table border="0" align="center" width="100%" cellpadding="15">
                            <htm:tr>
                                <htm:td>
                                    <htm:h3>
                                        <h:outputText value="#{msgs.MassImport}" />
                                    </htm:h3>

                                    <%-- globale Warn- und Fehlermeldungen --%>
                                    <h:messages id="id8" globalOnly="true" errorClass="text_red" infoClass="text_blue" showDetail="true" showSummary="true" tooltip="true" />

                                    <%-- Box für die Bearbeitung der Details --%>
                                    <htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen">

                                        <htm:tr>
                                            <htm:td styleClass="eingabeBoxen_row1" colspan="2">
                                                <h:outputText id="utid38" value="#{msgs.nextStep}" />
                                            </htm:td>
                                        </htm:tr>

                                        <htm:tr>
                                            <htm:td styleClass="eingabeBoxen_row2" colspan="2">

                                                <h:panelGrid columns="1" cellpadding="4">

                                                    <h:commandLink id="utid21" action="#{MassImportForm.downloadDocket}">
                                                        <h:graphicImage id="utid24" alt="/newpages/images/buttons/laufzettel_wide.png" value="/newpages/images/buttons/laufzettel_wide.png"
                                                            style="vertical-align:middle" />
                                                        <h:outputText value="#{msgs.laufzettelDrucken}" />
                                                    </h:commandLink>

                                                    <h:commandLink id="utid20" action="#{MassImportForm.prepare}">
                                                        <h:graphicImage id="utid25" alt="/newpages/images/buttons/star_blue.gif"
                                                        value="/newpages/images/buttons/star_blue.gif"
                                                            style="vertical-align:middle" />
                                                        <h:outputText value="#{msgs.weiterenVorgangAnlegen}" />
                                                    </h:commandLink>
                                                </h:panelGrid>
                                            </htm:td>

                                        </htm:tr>
                                    </htm:table>

                                    <%-- // Box für die Bearbeitung der Details --%>

                                </htm:td>
                            </htm:tr>
                        </htm:table>
                    </h:form>

                </htm:td>
            </htm:tr>
            <%@include file="inc/tbl_Fuss.jsp"%>
        </htm:table>



    </body>
</f:view>

</html>
