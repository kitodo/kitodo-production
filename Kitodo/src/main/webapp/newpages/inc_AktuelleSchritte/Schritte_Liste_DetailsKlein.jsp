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

<htm:table cellpadding="3" cellspacing="0" width="100%"
	styleClass="main_statistikboxen">

	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row1">
			<h:outputText value="#{msgs.eigenschaften}" />
		</htm:td>
	</htm:tr>

	<htm:tr>
		<htm:td styleClass="main_statistikboxen_row2">

			<htm:table border="0" width="90%" cellpadding="2">
				<htm:tr valign="top">
					<htm:td width="150">
						<h:outputText value="#{msgs.titel}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.localizedTitle}" />
					</htm:td>
				</htm:tr>
				<htm:tr rendered="false">
					<htm:td width="150">
						<h:outputText value="#{msgs.id}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.id}" />
					</htm:td>
				</htm:tr>
				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.prioritaet}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.priority}" />
					</htm:td>
				</htm:tr>
				<htm:tr rendered="#{item.processingBegin !=null && !HelperForm.anonymized}">
					<htm:td width="150">
						<h:outputText value="#{msgs.bearbeitungsbeginn}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.processingBeginAsFormattedString}" />
					</htm:td>
				</htm:tr>
				<htm:tr rendered="#{item.processingTime !=null && !HelperForm.anonymized}">
					<htm:td width="150">
						<h:outputText value="#{msgs.zuletztBearbeitet}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.processingTimeAsFormattedString}" />
					</htm:td>
				</htm:tr>
				<htm:tr rendered="#{item.processingUser.id !=0 && item.processingUser !=null && !HelperForm.anonymized}">
					<htm:td width="150">
						<h:outputText value="#{msgs.letzteAktualisierungDurch}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.processingUser.fullName}" />
					</htm:td>
				</htm:tr>
				<htm:tr>
					<htm:td width="150">
						<h:outputText value="#{msgs.aktualisierungstyp}:" />
					</htm:td>
					<htm:td>
						<h:outputText value="#{item.editTypeEnum.title}" />
					</htm:td>
				</htm:tr>
			</htm:table>
		</htm:td>
	</htm:tr>

</htm:table>
