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

<htm:tr>
	<htm:td colspan="2">
		<h:form id="foot1">
			<htm:table width="100%" cellpadding="0" cellspacing="0px"
				align="center" style="margin-top:0px">
				<htm:tr>
					<htm:td align="center" styleClass="layoutFuss">

						<h:outputLink value="#{HelperForm.applicationWebsiteMsg}">
							<h:outputText value="#{HelperForm.applicationWebsiteMsg}" />
						</h:outputLink>

						<h:outputText value=" | #{msgs.toolentwicklung} | " />

						<h:commandLink action="Impressum" value="#{msgs.impressum}" id="impr" />
						<script language="javascript">
    function submitEnter(commandId,e)
{
        var keycode;
        if (window.event) keycode = window.event.keyCode;
        else if (e) keycode = e.which;
        else return true;
        
        if (keycode == 13)
        {
                document.getElementById(commandId).click();
                return false;
        }
        else
        return true;
}
</script>

					</htm:td>
				</htm:tr>
			</htm:table>
		</h:form>
	</htm:td>
</htm:tr>
