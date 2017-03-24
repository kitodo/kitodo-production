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
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x" %>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j" %>

<a4j:commandLink reRender="auflistung" style="color:black" id="plusminusbutton">
    <h:graphicImage value="/newpages/images/plus.gif"
                    style="margin-right:4px" rendered="#{!item.panelShown}"/>
    <h:graphicImage value="/newpages/images/minus.gif"
                    style="margin-right:4px" rendered="#{item.panelShown}"/>
    <x:updateActionListener value="#{item.panelShown?false:true}"
                            property="#{item.panelShown}"/>
    <h:outputText value="#{item.title}"/>
</a4j:commandLink>
