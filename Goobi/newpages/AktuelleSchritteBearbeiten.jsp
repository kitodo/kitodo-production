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
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>

<%-- ########################################

                            Alle Aktuellen Schritte in der Übersicht

    #########################################--%>
<a4j:keepAlive beanName="AktuelleSchritteForm" />
<html>
<f:view locale="#{SpracheForm.locale}">

    <%@include file="inc/head.jsp"%>
    <body>
        <htm:table styleClass="headTable" cellspacing="0" cellpadding="0" style="padding-left:5px;padding-right:5px;margin-top:5px;">
            <%@include file="inc/tbl_Kopf.jsp"%>
        </htm:table>
        <htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable"
            align="center">

            <htm:tr>
                <%@include file="inc/tbl_Navigation.jsp"%>
                <htm:td valign="top" styleClass="layoutInhalt">

                    <%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
                    <h:form id="id0" style="margin:0px">
                        <%-- Breadcrumb --%>
                        <h:panelGrid id="id1" width="100%" columns="1"
                            styleClass="layoutInhaltKopf">
                            <h:panelGroup id="id2">
                                <h:commandLink id="id3" value="#{msgs.startseite}"
                                    action="newMain" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:commandLink id="id4" value="#{msgs.aktuelleSchritte}"
                                    action="AktuelleSchritteAlle" />
                                <f:verbatim> &#8250;&#8250; </f:verbatim>
                                <h:outputText id="id5" value="#{msgs.detailsDesArbeitsschritts}" />
                            </h:panelGroup>
                        </h:panelGrid>
                    </h:form>
                    <htm:table border="0" align="center" width="100%" cellpadding="15"
                        rendered="#{LoginForm.myBenutzer!=null}">
                        <htm:tr>
                            <htm:td>

                                <%-- Überschrift --%>
                                <htm:h3>
                                    <h:outputText id="id6" value="#{msgs.aktuelleSchritte}" />
                                </htm:h3>

                                <%-- globale Warn- und Fehlermeldungen --%>
                                <h:messages id="id7" globalOnly="false" errorClass="text_red"
                                    infoClass="text_blue" showDetail="true" showSummary="true"
                                    tooltip="true" />

                                <%-- Schritt --%>
                                <%@include file="inc_AktuelleSchritte/schritt_box_Details.jsp"%>
                                <%@include
                                    file="inc_AktuelleSchritte/schritt_box_Properties.jsp"%>

                                <%@include file="inc_AktuelleSchritte/schritt_box_Action.jsp"%>

                            </htm:td>
                        </htm:tr>
                    </htm:table>
                    <%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

                </htm:td>
            </htm:tr>
            <%@include file="inc/tbl_Fuss.jsp"%>
        </htm:table>

    </body>
</f:view>

</html>
