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

<%-- ######################################## 

							Kopie einer Prozessvorlage anlegen

	#########################################--%>

<html>
<f:view locale="#{SpracheForm.locale}">
    <%@include file="/newpages/inc/head.jsp" %>
    <body>

    <htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable"
               align="center">
        <%@include file="/newpages/inc/tbl_Kopf.jsp" %>
        <htm:tr>
            <%@include file="/newpages/inc/tbl_Navigation.jsp" %>
            <htm:td valign="top" styleClass="layoutInhalt">

                <%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
                <h:form id="pageform2">
                    <%-- Breadcrumb --%>
                    <h:panelGrid width="100%" columns="1" styleClass="layoutInhaltKopf">
                        <h:panelGroup>
                            <h:commandLink value="#{msgs.startseite}" action="newMain"/>
                            <f:verbatim> &#8250;&#8250; </f:verbatim>
                            <h:commandLink value="#{msgs.prozessverwaltung}"
                                           action="ProzessverwaltungAlle"/>
                            <f:verbatim> &#8250;&#8250; </f:verbatim>
                            <h:outputText value="#{msgs.einenNeuenProzessAnlegen}"/>
                        </h:panelGroup>
                    </h:panelGrid>

                    <htm:table border="0" align="center" width="100%" cellpadding="15">
                        <htm:tr>
                            <htm:td>
                                <htm:h3>
                                    <h:outputText value="#{msgs.einenNeuenProzessAnlegen}"/>
                                </htm:h3>

                                <%-- globale Warn- und Fehlermeldungen --%>
                                <h:messages globalOnly="true" errorClass="text_red"
                                            infoClass="text_blue" showDetail="true" showSummary="true"
                                            tooltip="true"/>

                                <%-- ===================== Eingabe der Details ====================== --%>
                                <htm:table cellpadding="3" cellspacing="0" width="100%"
                                           styleClass="eingabeBoxen">

                                    <htm:tr>
                                        <htm:td styleClass="eingabeBoxen_row1" colspan="2">
                                            <h:outputText value="#{msgs.zusaetzlicheDetails}"/>
                                        </htm:td>
                                    </htm:tr>

                                    <htm:tr>
                                        <htm:td styleClass="eingabeBoxen_row2" colspan="2">

                                            <x:dataTable cellspacing="1px" cellpadding="1px"
                                                         headerClass="standardTable_Header"
                                                         columnClasses="standardTable_Column,standardTable_Column,standardTable_ColumnCentered"
                                                         var="item"
                                                         value="#{ProzesskopieForm.prozessKopie.properties}">

                                                <h:column>
                                                    <h:outputText value="#{item.title}"/>
                                                </h:column>

                                                <h:column>
                                                    <h:inputText value="#{item.value}" style="width:500px"/>
                                                </h:column>
                                            </x:dataTable>

                                        </htm:td>
                                    </htm:tr>
                                    <htm:tr>
                                        <htm:td styleClass="eingabeBoxen_row3" align="left">
                                            <h:commandButton value="#{msgs.abbrechen}" immediate="true"
                                                             action="ProzessverwaltungAlle">
                                            </h:commandButton>
                                        </htm:td>
                                        <htm:td styleClass="eingabeBoxen_row3" align="right">
                                            <h:commandButton value="#{msgs.zurueck}"
                                                             action="#{ProzesskopieForm.GoToSeite1}">
                                            </h:commandButton>
                                            <h:commandButton value="#{msgs.speichern}"
                                                             action="#{ProzesskopieForm.NeuenProzessAnlegen}">
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
        <%@include file="/newpages/inc/tbl_Fuss.jsp" %>
    </htm:table>

    </body>
</f:view>

</html>
