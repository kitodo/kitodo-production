<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%-- 
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
--%>
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
	<x:stylesheet media="screen" path="#{LoginForm.myBenutzer == null?'/css/default.css':LoginForm.myBenutzer.css}"/>
	<x:stylesheet media="print" path="/css/system/print.css"/>
	<%--Styles für panelTabbedPane unter Menü Administration- Projekte --%>
	<f:loadBundle basename="messages.messages" var="msgs" />
	<title><h:outputText value="#{HelperForm.applicationHeaderTitle} - #{HelperForm.version}"/></title>
    <!-- <h:outputText value="internal version number: #{HelperForm.buildVersion}"/> -->
</head>
