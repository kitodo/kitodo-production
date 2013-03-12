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
    
    
    /**
     * Handler for onkeypress that clicks {@code targetElement} if the
     * enter key is pressed.
     */
    function ifEnterClick(event, targetElement) {
        event = event || window.event;
        if (event.keyCode == 13) {
            // normalize event target, so it looks the same for all browsers
            if (!event.target) {
                event.target = event.srcElement;
            }

            // don't do anything if the element handles the enter key on its own
            if (event.target.nodeName == 'A') {
                return;
            }
            if (event.target.nodeName == 'INPUT') {
                if (event.target.type == 'button' || event.target.type == 'submit') {
                    if (strEndsWith(event.target.id, 'focusKeeper')) {
                        // inside some Richfaces component such as rich:listShuttle
                    } else {
                        return;
                    }
                }
            }
            if (event.target.nodeName =='TEXTAREA') {
                return;
            }

            // swallow event
            if (event.preventDefault) {
                // Firefox
                event.stopPropagation();
                event.preventDefault();
            } else {
                // IE
                event.cancelBubble = true;
                event.returnValue = false;
            }

            document.getElementById(targetElement).click();
        }
    }

</script>

					</htm:td>
				</htm:tr>
			</htm:table>
		</h:form>
	</htm:td>
</htm:tr>
