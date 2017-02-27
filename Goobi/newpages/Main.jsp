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

                            Startseite

    #########################################--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
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
                    <h:form id="useform">
                        <%-- Breadcrumb --%>
                        <h:panelGrid id="id2" columns="1" styleClass="layoutInhaltKopf">
                            <h:panelGroup id="id3">
                                <h:outputText id="id4" value="#{msgs.startseite}" />
                            </h:panelGroup>
                        </h:panelGrid>
                        <%-- // Breadcrumb --%>

                        <%-- Inhalt --%>
                        <%-- goobi logo for version 151 --%>
                        <h:panelGrid id="id511" columns="2" width="100%" cellpadding="15"
                            cellspacing="0" align="center" border="0" rowClasses="rowTop">

                            <%-- Einführung --%>
                            <x:panelGroup id="id6">

                                <htm:noscript>
                                    <h:outputText
                                        style="color: red;font-weight: bold;margin-bottom:20px;display:block"
                                        value="#{msgs.keinJavascript}" />
                                </htm:noscript>

                                <%-- globale Warn- und Fehlermeldungen --%>
                                <h:messages id="id5" globalOnly="true" errorClass="text_red"
                                    infoClass="text_blue" showDetail="true" showSummary="true"
                                    tooltip="true" />

                                <htm:img src="#{HelperForm.logoUrl}" />

                                <htm:h3 style="margin-top:15px">
                                    <h:outputText id="id7" value="#{msgs.startseite}" />
                                </htm:h3>

                                <%-- globale Warn- und Fehlermeldungen --%>
                                <h:messages id="id8" globalOnly="true" errorClass="text_red"
                                    infoClass="text_blue" showDetail="true" showSummary="true"
                                    tooltip="true" />

                                <htm:p style="text-align: justify;">
                                    <h:outputText id="id9" style="text-align: justify;"
                                        value="#{HelperForm.applicationHomepageMsg}" escape="false"></h:outputText>
                                </htm:p>
                            </x:panelGroup>
                            <%-- // Einführung --%>

                            <%-- Kästen mit der Statistik --%>
                            <h:panelGrid id="id10" columns="1" cellpadding="0px"
                                width="200 px" style="margin-top: 10px" align="center">
                                <%@include file="inc_Main/box1.jsp"%>
                                <%-- <%@include file="inc_Main/box2.jsp"%> --%>
                                <%--
                            <%@include file="inc_Main/box3.jsp"%>
                            --%>
                            </h:panelGrid>
                            <%-- // Kästen mit der Statistik --%>

                        </h:panelGrid>
                        <%-- // Inhalt --%>



                    </h:form>
                    <%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

                </htm:td>
            </htm:tr>
            <%@include file="inc/tbl_Fuss.jsp"%>
        </htm:table>

    </body>
</f:view>

</html>
