<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>

<htm:table cellpadding="3" cellspacing="0" style="width:100%">
	<htm:tr style="vertical-align:top">
		<htm:td>

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
							<htm:tr>
								<htm:td width="150">
									<h:outputText value="#{msgs.titel}:" />
								</htm:td>
								<htm:td>
									<h:outputText value="#{BatchForm.batch.currentStep.stepTitle}" />
								</htm:td>
							</htm:tr>

							<htm:tr>
								<htm:td width="150">
									<h:outputText value="#{msgs.prozessTitel}:" />
								</htm:td>
								<htm:td>
									<h:panelGroup>
										<x:dataTable var="process" value="#{BatchForm.batch.batchList}">
											<x:column>
												<h:outputText value="#{process.titel}" />
											</x:column>
										</x:dataTable>
									</h:panelGroup>
								</htm:td>
							</htm:tr>
							<htm:tr>
								<htm:td width="150">
									<h:outputText value="#{msgs.reihenfolge}:" />
								</htm:td>
								<htm:td>
									<h:outputText value="#{BatchForm.batch.currentStep.stepOrder}" />
								</htm:td>
							</htm:tr>
							<htm:tr>
								<htm:td width="150">
									<h:outputText value="#{msgs.user}:" />
								</htm:td>
								<htm:td>
									<h:outputText value="#{BatchForm.batch.user.nachVorname}" />
								</htm:td>
							</htm:tr>
							<htm:tr>
								<htm:td width="150">
									<h:outputText value="#{msgs.status}:" />
								</htm:td>
								<htm:td>
									<h:outputText value="#{BatchForm.batch.currentStep.stepStatus}" />
								</htm:td>
							</htm:tr>
						</htm:table>
					</htm:td>
				</htm:tr>
			</htm:table>
		</htm:td>
	</htm:tr>
</htm:table>
