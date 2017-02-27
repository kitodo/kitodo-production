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

<head>
<meta http-equiv="X-UA-Compatible" content="IE=8" />

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="pragma" content="no-cache"/>
    <meta http-equiv="cache-control" content="no-cache"/>
    <meta name="date" content="1970-01-01T00:00:00+01:00"/>
    <meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
    <meta http-equiv="cache-control" content="max-age=0" />
    <meta http-equiv="expires" content="0" />



    <link rel="shortcut icon" href="favicon.ico"/>

    <!-- Sam Skin CSS for TabView -->
<!--
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.8.1/build/tabview/assets/skins/sam/tabview.css">

<!-- JavaScript Dependencies for Tabview: -->
<!--
<script src="http://yui.yahooapis.com/2.8.1/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script src="http://yui.yahooapis.com/2.8.1/build/element/element-min.js"></script>
 -->
<!-- OPTIONAL: Connection (required for dynamic loading of data) -->
<!--
<script src="http://yui.yahooapis.com/2.8.1/build/connection/connection-min.js"></script>
 -->
<!-- Source file for TabView -->
<!--
<script src="http://yui.yahooapis.com/2.8.1/build/tabview/tabview-min.js"></script>
 -->
    <x:stylesheet media="all" path="/css/system/goobi.css"/>
    <x:stylesheet media="screen" path="#{LoginForm.myBenutzer == null?'/css/default.css':LoginForm.myBenutzer.css}"/>
    <x:stylesheet media="print" path="/css/system/print.css"/>
    <%--Styles für panelTabbedPane unter Menü Administration- Projekte --%>
    <f:loadBundle basename="messages.messages" var="msgs" />
    <title><h:outputText value="#{HelperForm.applicationHeaderTitle} - #{HelperForm.version}"/></title>
    <!-- <h:outputText value="internal version number: #{HelperForm.buildVersion}"/> -->
</head>
