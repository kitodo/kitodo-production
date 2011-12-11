<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<a4j:keepAlive beanName="BatchForm" />
<html>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="inc/head.jsp"%>
	<body>

		<htm:table cellspacing="5" cellpadding="0" styleClass="layoutTable" align="center">
			<%@include file="inc/tbl_Kopf.jsp"%>
			<htm:tr>
				<%@include file="inc/tbl_Navigation.jsp"%>
				<htm:td valign="top" styleClass="layoutInhalt">

					<%-- ++++++++++++++++     Inhalt      ++++++++++++++++ --%>
					<h:form id="editBatch" rendered="#{((LoginForm.maximaleBerechtigung == 1) || (LoginForm.maximaleBerechtigung == 2))}">
						<%-- Breadcrumb --%>
						<h:panelGrid id="id0" width="100%" columns="1" styleClass="layoutInhaltKopf">
							<h:panelGroup id="id1">
								<h:commandLink id="id2" value="#{msgs.startseite}" action="newMain" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:commandLink id="id3" value="#{msgs.batches}" action="BatchesAll" />
								<f:verbatim> &#8250;&#8250; </f:verbatim>
								<h:outputText value="#{msgs.batchProperties}" />
							</h:panelGroup>
						</h:panelGrid>

						<htm:table border="0" align="center" width="100%" cellpadding="15">
							<htm:tr>
								<htm:td>

									<%-- �?berschrift --%>
									<htm:h3>
										<h:outputText id="id4" value="#{msgs.batches}" />
									</htm:h3>

									<%-- globale Warn- und Fehlermeldungen --%>
									<h:messages id="id5" globalOnly="true" errorClass="text_red" infoClass="text_blue" showDetail="true" showSummary="true" tooltip="true" />


									<htm:table width="100%">
										<htm:tr>
											<htm:td>
												<h:selectOneMenu value="#{BatchForm.batchHelper.processName}">
													<si:selectItems var="process" value="#{BatchForm.batchHelper.processNameList}" itemLabel="#{process}" itemValue="#{process}" />
												</h:selectOneMenu>
												<h:commandButton action="#{NavigationForm.Reload}" value="#{msgs.reload}" />
											</htm:td>
										</htm:tr>

										<htm:tr>
											<htm:td>
												<htm:table>
													<x:dataList var="item" value="#{BatchForm.batchHelper.processes}">
														<htm:tr>
															<htm:td>
																<h:outputText value="#{item.titel}" />
															</htm:td>
														</htm:tr>
													</x:dataList>
												</htm:table>
											</htm:td>
											<htm:td>
												<h:outputText value="#{BatchForm.batchHelper.processName}" />











<htm:table cellspacing="1px" cellpadding="1px" width="100%" styleClass="standardTable"
		rendered="#{BatchForm.modusBearbeiten!='eigenschaft'}">

		<htm:thead styleClass="standardTable_Header">
			<htm:th>
				<h:outputText value="#{msgs.titel}" />
			</htm:th>
			<htm:th>
				<h:outputText value="#{msgs.wert}" />
			</htm:th>
			<htm:th>
				<h:outputText value="#{msgs.auswahl}" />
			</htm:th>
		</htm:thead>

		<x:dataList var="container" value="#{BatchForm.batchHelper.containers}" rowCountVar="rowCount" rowIndexVar="rowIndex">
			<x:dataList var="proc" value="#{BatchForm.batchHelper.containerlessProperties}" rowCountVar="propCount" rowIndexVar="propInd">
				<htm:tr rendered="#{container == 0}" styleClass="standardTable_Row1">
					<htm:td>
						<%-- property title --%>
						<h:outputText value="#{proc.name}" />
					</htm:td>
					<htm:td>
						<%-- property value--%>
						<h:outputText value="#{proc.value}" />
					</htm:td>
					<htm:td>

						<h:commandLink action="BatchProperties" title="#{msgs.bearbeiten}">
							<h:graphicImage value="/newpages/images/buttons/edit.gif" />
							<x:updateActionListener property="#{BatchForm.batchHelper.processProperty}" value="#{proc}" />
							<x:updateActionListener property="#{BatchForm.batchHelper.container}" value="0" />
							<x:updateActionListener property="#{BatchForm.modusBearbeiten}" value="eigenschaft" />
							<a4j:support event="onchange" reRender="editBatch" />
						</h:commandLink>
<%-- 
						<h:commandLink action="#{BatchForm.batchHelper.duplicateContainerForAll}" title="#{msgs.duplicateForAll}">
							<h:graphicImage value="/newpages/images/buttons/copy.gif" />
							<x:updateActionListener property="#{BatchForm.batchHelper.processProperty}" value="#{proc}" />
						</h:commandLink>
--%>					</htm:td>
				</htm:tr>
			</x:dataList>


			<htm:tr rendered="#{container != 0}" styleClass="standardTable_Row1">
				<htm:td colspan="2">
					<htm:table  width="100%" styleClass="standardTable">
						<x:dataList var="process_item" value="#{BatchForm.batchHelper.sortedProperties}" rowCountVar="propCount" rowIndexVar="propInd">
							<htm:tr styleClass="standardTable_Row1" rendered="#{process_item.container==container}">
								<htm:td>
									<h:outputText value="#{process_item.name}" />
								</htm:td>
								<htm:td>
									<h:outputText value="#{process_item.value}" />
								</htm:td>
							</htm:tr>
						</x:dataList>
					</htm:table>
				</htm:td>
				<htm:td>



					<h:commandLink action="BatchProperties" title="#{msgs.bearbeiten}">
						<h:graphicImage value="/newpages/images/buttons/edit.gif" />
						<x:updateActionListener property="#{BatchForm.batchHelper.container}" value="#{container}" />
						<x:updateActionListener property="#{BatchForm.modusBearbeiten}" value="eigenschaft" />
						<a4j:support event="onchange" reRender="editBatch" />
					</h:commandLink>
					<%-- 
					<h:commandLink action="#{BatchForm.batchHelper.duplicateContainerForAll}" title="#{msgs.duplicateForAll}">
						<h:graphicImage value="/newpages/images/buttons/copy.gif" />
						<x:updateActionListener property="#{BatchForm.batchHelper.container}" value="#{container}" />
					</h:commandLink>
--%>
				</htm:td>
			</htm:tr>
			<htm:tr rendered="#{rowIndex + 1 < rowCount}">
				<htm:td colspan="3" styleClass="standardTable_Row1">
					<h:outputText value="&nbsp;" escape="false" />
				</htm:td>
			</htm:tr>
		</x:dataList>
	</htm:table>
	
	
	
	
	<%-- // Box für die Bearbeitung der Details --%>

	<htm:h4 style="margin-top:15" rendered="#{BatchForm.modusBearbeiten=='eigenschaft'}">
		<h:outputText value="#{msgs.eigenschaftBearbeiten}" />
	</htm:h4>
	<%-- Box für die Bearbeitung der Details --%>
	<htm:table cellpadding="3" cellspacing="0" width="100%" styleClass="eingabeBoxen" rendered="#{BatchForm.modusBearbeiten=='eigenschaft'}">

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1" colspan="2">
				<h:outputText value="#{msgs.eigenschaft}" />
			</htm:td>
		</htm:tr>

		<%-- Formular für die Bearbeitung der Eigenschaft --%>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2" colspan="2">
				<x:dataList var="myprocess_item" value="#{BatchForm.batchHelper.containerProperties}">

					<%-- 	<x:aliasBean alias="#{myprocess_item}" value="#{BatchForm.batchHelper.processProperty}">--%>
					<h:panelGrid columns="2">




						<h:outputText id="eigenschafttitel" style="width: 500px;margin-right:15px" value="#{myprocess_item.name}: " />
						<%-- textarea --%>
						<h:panelGroup id="prpvw15_1" rendered="#{((myprocess_item.type.name == 'text') || (myprocess_item.type.name == 'null'))}">
							<h:inputText id="file" style="width: 500px;margin-right:15px" value="#{myprocess_item.value}" />
						</h:panelGroup>

						<%-- numbers only --%>
						<h:panelGroup id="prpvw15_1mnk" rendered="#{myprocess_item.type.name == 'integer' || myprocess_item.type.name == 'number'}">

							<h:inputText id="numberstuff122334mnktodo" style="width: 500px;margin-right:15px" value="#{myprocess_item.value}">
								<f:validateLongRange minimum="0" />
							</h:inputText>
						</h:panelGroup>

						<%--  SelectOneMenu --%>
						<h:panelGroup id="prpvw15_2" rendered="#{(myprocess_item.type.name == 'list')}">
							<h:selectOneMenu value="#{myprocess_item.value}" style="width: 500px;margin-right:15px" id="prpvw15_2_1">
								<si:selectItems id="prpvw15_2_2" value="#{myprocess_item.possibleValues}" var="myprocess_items" itemLabel="#{myprocess_items}"
									itemValue="#{myprocess_items}" />
							</h:selectOneMenu>
						</h:panelGroup>

						<%--  SelectManyMenu --%>
						<h:panelGroup id="prpvw15_3" rendered="#{(myprocess_item.type.name == 'listmultiselect')}">
							<h:selectManyListbox id="prpvw15_3_1" style="width: 500px;margin-right:15px" value="#{myprocess_item.valueList}" size="5">
								<si:selectItems id="prpvw15_3_2" value="#{myprocess_item.possibleValues}" var="myprocess_items" itemLabel="#{myprocess_items}"
									itemValue="#{myprocess_items}" />
							</h:selectManyListbox>
						</h:panelGroup>

						<%--  Boolean --%>
						<h:panelGroup id="prpvw15_4" rendered="#{(myprocess_item.type.name == 'boolean')}">
							<h:selectOneMenu value="#{myprocess_item.booleanValue}" style="width: 500px;margin-right:15px" id="prpvw15_4_1">
								<f:selectItem id="prpvw15_4_2" itemValue="true" itemLabel="#{msgs.yes}" />
								<f:selectItem id="prpvw15_4_3" itemValue="false" itemLabel="#{msgs.no}" />
							</h:selectOneMenu>
						</h:panelGroup>

						<%--  Date  --%>
						<h:panelGroup id="prpvw15_5" rendered="#{(myprocess_item.type.name == 'date')}">
							<rich:calendar id="prpvw15_5_1" style="width: 500px;margin-right:15px" datePattern="dd.MM.yyyy" value="#{myprocess_item.value}"
								enableManualInput="true">
							</rich:calendar>
						</h:panelGroup>

					</h:panelGrid>
					<%-- 	</x:aliasBean>--%>
				</x:dataList>
			</htm:td>
		</htm:tr>

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3" align="left">
				<h:commandButton value="#{msgs.abbrechen}" action="#{NavigationForm.Reload}" immediate="true">
					<x:updateActionListener property="#{BatchForm.modusBearbeiten}" value="" />
				</h:commandButton>
			</htm:td>
			<htm:td styleClass="eingabeBoxen_row3" align="right">
				<h:commandButton value="#{msgs.applyToThisProcess}" action="#{BatchForm.batchHelper.saveCurrentProperty}">

					<x:updateActionListener property="#{BatchForm.modusBearbeiten}" value="" />
				</h:commandButton>
				<h:commandButton value="#{msgs.applyToAllProcesses}" action="#{BatchForm.batchHelper.saveCurrentPropertyForAll}">
					<x:updateActionListener property="#{BatchForm.modusBearbeiten}" value="" />
				</h:commandButton>
			</htm:td>
		</htm:tr>
	</htm:table>








											</htm:td>
										</htm:tr>

									</htm:table>
								</htm:td>
							</htm:tr>
						</htm:table>
					</h:form>
				</htm:td>
			</htm:tr>
		</htm:table>
	</body>
</f:view>
</html>