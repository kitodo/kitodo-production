<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta http-equiv="pragma" content="no-cache"/>
	<meta http-equiv="cache-control" content="no-cache"/>
	<meta name="date" content="1970-01-01T00:00:00+01:00"/>
	<link rel="shortcut icon" href="favicon.ico"/> 
	<x:stylesheet media="screen" path="#{LoginForm.myBenutzer == null?'/css/default.css':LoginForm.myBenutzer.css}"/>
	<x:stylesheet media="print" path="/css/system/print.css"/>
	<f:loadBundle basename="Messages.messages" var="msgs" />
	<title><h:outputText value="#{HelperForm.applicationHeaderTitle} - #{HelperForm.version}"/></title>
    <!-- <h:outputText value="internal version number: #{HelperForm.buildVersion}"/> -->
</head>