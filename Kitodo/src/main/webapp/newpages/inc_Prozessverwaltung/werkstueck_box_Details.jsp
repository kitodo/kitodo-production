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

<htm:table cellpadding="3" cellspacing="0" width="100%"
           styleClass="main_statistikboxen">

    <htm:tr>
        <htm:td styleClass="main_statistikboxen_row1">
            <h:outputText value="#{msgs.eigenschaften}"/>
        </htm:td>
        <htm:td styleClass="main_statistikboxen_row1" align="right">
            <h:commandLink action="#{ProzessverwaltungForm.Reload}">
                <h:graphicImage value="/newpages/images/reload.gif"/>
            </h:commandLink>
        </htm:td>
    </htm:tr>

    <htm:tr>
        <htm:td styleClass="main_statistikboxen_row2" colspan="2">

            <htm:table border="0" width="90%" cellpadding="2">
                <htm:tr>
                    <htm:td width="150">
                        <h:outputText value="#{msgs.id}:"/>
                    </htm:td>
                    <htm:td>
                        <h:outputText value="#{ProzessverwaltungForm.myWerkstueck.id}"/>
                    </htm:td>
                    <htm:td rowspan="2" align="right">

                        <h:commandLink title="#{msgs.loeschen}"
                                       action="#{ProzessverwaltungForm.WerkstueckLoeschen}" style="margin-right:20px">
                            <h:graphicImage value="/newpages/images/buttons/waste1a_20px.gif"/>
                        </h:commandLink>

                    </htm:td>
                </htm:tr>

            </htm:table>

        </htm:td>
    </htm:tr>
</htm:table>

