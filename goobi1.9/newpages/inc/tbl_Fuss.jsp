<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

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
