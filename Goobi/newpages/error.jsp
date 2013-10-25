<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@page pageEncoding="UTF-8"%>
<%@ page
	import="java.util.List,java.io.PrintWriter,org.apache.myfaces.shared_tomahawk.util.ExceptionUtils"
	isErrorPage="true"%>
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
<%-- ######################################## 

												Startseite

	#########################################--%>

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
				<jsp:include page="/newpages/inc/tbl_Navigation.jsp" />
				<htm:td valign="top" styleClass="layoutInhalt">

					<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>

					<%-- Breadcrumb --%>
					<h:panelGrid id="id2" columns="1" styleClass="layoutInhaltKopf">
						<h:panelGroup id="id3">
							<h:outputText id="id4" value="#{msgs.fehler}" />
						</h:panelGroup>
					</h:panelGrid>
					<%-- // Breadcrumb --%>
					<x:div forceId="true" id="mydiv">
						<h:form id="myform" style="margin:3px">
							<%-- �?berschrift --%>
							<htm:h3>
								<h:outputText id="id5" value="#{msgs.esIstEinFehlerAufgetreten}" />
							</htm:h3>

							<%
								if (exception != null) {
															List exceptions = ExceptionUtils
																	.getExceptions(exception);
															Throwable throwable = (Throwable) exceptions
																	.get(exceptions.size() - 1);
															String customaryMessage = exception
																	.getMessage();
															if (customaryMessage == null) {
																customaryMessage = "";
															} else {
																customaryMessage = customaryMessage
																		+ "<hr/>";
															}
															String exceptionMessage = ExceptionUtils
																	.getExceptionMessage(exceptions);
							%>

							<htm:p style="color:red;font-weight:bold;">
								<%=customaryMessage
													+ exceptionMessage%>
							</htm:p>

							<a href="#"
								onclick="toggle('trace1');toggle('trace2'); return false;">
								<span style="display: inline;" id="buttonOff"> + </span> <span
								id="buttonOn" style="display: none;"> - </span> Stack Trace</a>
							<%
								PrintWriter pw = new PrintWriter(out);
							%>
							<x:div id="trace1" forceId="true"
								style="border: grey 1px solid; background-color:#EFEFEF;margin:20px;display: none">
								<pre>
							<%
								throwable.printStackTrace(pw);
							%>
							</pre>
							</x:div>
							<%
								throwable = (Throwable) exceptions
																	.get(0);
							%>
							<x:div id="trace2" forceId="true"
								style="border: grey 1px solid; background-color:#EFEFEF;margin:20px;display: none">
								<pre>
							<%
								throwable.printStackTrace(pw);
							%>
							</pre>
							</x:div>

							<%
								} else {
							%>
							<h:outputText id="id6" value="#{msgs.unbekannterFehler}" />
							<%
								}
							%>

						</h:form>
					</x:div>
					<%-- ++++++++++++++++    // Inhalt      ++++++++++++++++ --%>

				</htm:td>
			</htm:tr>
			<jsp:include page="/newpages/inc/tbl_Fuss.jsp" />
		</htm:table>

		<script language="javascript" type="text/javascript">
			function toggle(id) {
				var style = document.getElementById(id).style;
				if ("block" == style.display) {
					style.display = "none";
					document.getElementById("buttonOff").style.display = "inline";
					document.getElementById("buttonOn").style.display = "none";
				} else {
					style.display = "block";
					document.getElementById("buttonOff").style.display = "none";
					document.getElementById("buttonOn").style.display = "inline";
				}
			}
		</script>

	</body>
</f:view>

</html>

