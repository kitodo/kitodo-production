<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<h:form style="margin:0px" id="form1">

	<h:panelGrid columns="3" width="100%" styleClass="layoutFuss">
		<h:outputLink value="#{HelperForm.applicationWebsiteUrl}">
			<h:outputText value="#{HelperForm.applicationWebsiteMsg}" />
		</h:outputLink>
		<h:outputText value=" | #{msgs.toolentwicklung} | " />
		<h:commandLink action="Impressum" value="#{msgs.impressum}" id="impress" />
	</h:panelGrid>

</h:form>

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
