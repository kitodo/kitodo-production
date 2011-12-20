
<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%--
  ~ This file is part of the Goobi Application - a Workflow tool for the support of
  ~ mass digitization.
  ~
  ~ Visit the websites for more information.
  ~     - http://gdz.sub.uni-goettingen.de
  ~     - http://www.goobi.org
  ~     - http://launchpad.net/goobi-production
  ~
  ~ This program is free software; you can redistribute it and/or modify it under
  ~ the terms of the GNU General Public License as published by the Free Software
  ~ Foundation; either version 2 of the License, or (at your option) any later
  ~ version.
  ~
  ~ This program is distributed in the hope that it will be useful, but WITHOUT ANY
  ~ WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  ~ PARTICULAR PURPOSE. See the GNU General Public License for more details. You
  ~ should have received a copy of the GNU General Public License along with this
  ~ program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  ~ Suite 330, Boston, MA 02111-1307 USA
  --%>

<html>
<f:view locale="#{SpracheForm.locale}">

	<%@include file="/newpages/inc/head.jsp"%>

	<script type="text/javascript">

	function checkFrameLoad(){

		var testObject = rechts.document.getElementById("metadatenRechts");
		if(testObject!=null){
		}else{
			rechts.location.href=rechts.location.href;
		}
		testObject = oben.document.getElementById("formularOben");
		if (testObject!=null){
		}else{
			oben.location.href=oben.location.href;
		}
		
		testObject = links.document.getElementById("treeform:tabelle");
		if (testObject!=null){
		}else{
			links.location.href = links.location.href;
		}
	}
			
	</script>

	<frameset rows="59px,*" bordercolor="#003399" onload="setTimeout('checkFrameLoad()',100);">
		<frame name="oben" src="../pages/Metadaten2oben.jsf" scrolling="no" />
		<frameset cols="210px,*" bordercolor="#003399">
			<frame name="links" src="../pages/Metadaten3links.jsf"
				scrolling="auto" />
			<frame name="rechts" src="../pages/Metadaten2rechts.jsf" />
		</frameset>
	</frameset>
</f:view>
</html>
