<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<%-- ===================== 
 ====================== --%>
<h:form style="width:100%;margin:0px;#{HelperForm.applicationHeaderBackground}" id="headform">
	<h:graphicImage value="#{HelperForm.applicationLogo}" />
	
	<h:commandLink action="#{SpracheForm.SpracheUmschalten}" id="lang1"
		style="position:absolute;top:15px;right:100px;margin:0px;"
		title="deutsche Version">
		<h:graphicImage value="/newpages/images/flag_de_ganzklein.gif" />
		<f:param name="locale" value="de" />
	</h:commandLink>
	<h:commandLink action="#{SpracheForm.SpracheUmschalten}" id="lang2"
		style="position:absolute;top:15px;right:65px;margin:0px"
		title="english version">
		<h:graphicImage value="/newpages/images/flag_en_ganzklein.gif" />
		<f:param name="locale" value="en" />
	</h:commandLink>
	<h:commandLink action="#{SpracheForm.SpracheUmschalten}" id="lang3"
		style="position:absolute;top:15px;right:30px;margin:0px" rendered="true"
		title="russian version">
		<h:graphicImage value="/newpages/images/flag_ru_ganzklein.gif" />
		<f:param name="locale" value="ru" />
	</h:commandLink>

</h:form>


<%-- ===================== 

<htm:table width="100%" styleClass="layoutKopf"
	style="#{HelperForm.applicationHeaderBackground}" cellpadding="0"
	cellspacing="0" border="0">
	<htm:tr valign="top">
		<htm:td width="20%" >
			<h:graphicImage value="#{HelperForm.applicationLogo}" />
		</htm:td>
		<htm:td valign="middle" align="center">

			<h:outputText style="#{HelperForm.applicationTitleStyle}"
				value="#{HelperForm.applicationTitle}" />

			<htm:noscript>
				<h:outputText style="color: red;font-weight: bold;"
					value="#{msgs.keinJavascript}" />
			</htm:noscript>

		</htm:td>
		<htm:td valign="middle" width="20%" align="right" style="padding:3px">
			<h:form style="margin:0px">
				<h:panelGrid columns="3">
					<h:commandLink action="#{SpracheForm.SpracheUmschalten}"
						title="deutsche Version">
						<h:graphicImage value="/newpages/images/flag_de_ganzklein.gif" />
						<f:param name="locale" value="de" />
					</h:commandLink>
					<h:commandLink action="#{SpracheForm.SpracheUmschalten}"
						title="english version">
						<h:graphicImage value="/newpages/images/flag_en_ganzklein.gif" />
						<f:param name="locale" value="en" />
					</h:commandLink>
					<h:commandLink action="#{SpracheForm.SpracheUmschalten}"
						rendered="true" title="russian version">
						<h:graphicImage value="/newpages/images/flag_ru_ganzklein.gif" />
						<f:param name="locale" value="ru" />
					</h:commandLink>
				</h:panelGrid>
			</h:form>
		</htm:td>
	</htm:tr>
</htm:table>
 ====================== --%>

