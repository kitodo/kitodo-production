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

<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>

<%-- ########################################

    zusaetzliche Daten aus der Konfiguration

    #########################################--%>
<h:panelGroup>
    <f:verbatim>
        <hr width="90%" />
    </f:verbatim>
</h:panelGroup>

<h:outputText value="#{msgs.zusaetzlicheDetails}" style="font-size:13;font-weight:bold;color:#00309C" />

<htm:table style="margin-left:30px" cellspacing="0">
    <x:dataList var="intern" style="font-weight: normal;margin-left:30px" value="#{ProzesskopieForm.additionalFields}" layout="ordered list"
        rowCountVar="rowCount" rowIndexVar="rowIndex">
        <htm:tr rendered="#{intern.showDependingOnDoctype}">
            <htm:td width="150">
                <h:outputText value="#{intern.titel}:" />
            </htm:td>
            <htm:td>
                <h:inputText value="#{intern.wert}" styleClass="prozessKopieFeldbreite" rendered="#{intern.selectList==null}" />
        <%--        <h:selectOneMenu  value="#{intern.wert}" styleClass="prozessKopieFeldbreite" rendered="#{intern.selectList!=null}">
                    <f:selectItems value="#{intern.selectList}" />
                </h:selectOneMenu> --%>
                <h:selectOneListbox  value="#{intern.wert}" styleClass="prozessKopieFeldbreite" rendered="#{intern.selectList!=null}">
                    <f:selectItems value="#{intern.selectList}" />
                </h:selectOneListbox>


                <h:outputText value="*" rendered="#{intern.required}" />
            </htm:td>
        </htm:tr>

    </x:dataList>

    <htm:tr styleClass="rowTop">
        <htm:td width="150" style="">
            <h:outputText value="#{msgs.addToProcessLog}:" />
        </htm:td>
        <htm:td>
            <h:inputTextarea value="#{ProzesskopieForm.addToWikiField}" styleClass="prozessKopieFeldbreite" />
        </htm:td>
    </htm:tr>

</htm:table>
<h:outputText value="#{msgs.requiredField}" />
