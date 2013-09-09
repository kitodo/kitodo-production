<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="http://myfaces.apache.org/sandbox" prefix="s"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp/easysi" prefix="si"%>
<%@ taglib uri="http://www.jenia.org/jsf/popup" prefix="jp"%>


<htm:h3 style="margin-top: 0px">
	<h:outputText value="#{msgs.fileManipulation}" />
</htm:h3>

<h:form id="fileUpload" enctype="multipart/form-data">
	<htm:h4>
		<h:outputText value="#{msgs.fileUpload}" />
	</htm:h4>
	<htm:table cellpadding="3" cellspacing="0" styleClass="eingabeBoxen" style="margin-bottom: 20px; width: 450px;">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1">
				<h:outputText value="#{msgs.fileUpload}" />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2">
				<h:panelGrid columns="2">
					<h:outputLabel value="#{msgs.filename}:" for="fileupload" />
					<x:inputFileUpload id="fileupload" value="#{Metadaten.fileManipulation.uploadedFile}" storage="file" styleClass="fileUploadInput"
						required="false" />

					<h:outputText value="#{msgs.aktuellerOrdner}:" />
					<h:selectOneMenu style="width:220px" value="#{Metadaten.fileManipulation.currentFolder}">
						<si:selectItems value="#{Metadaten.allTifFolders}"  var="folder" itemLabel="#{folder}" itemValue="#{folder}" />
					</h:selectOneMenu>

					<h:outputLabel value="#{msgs.position}:" for="paginationSelection" rendered="#{Metadaten.alleSeiten != null}" />
					<h:selectOneMenu value="#{Metadaten.fileManipulation.insertPage}" id="paginationSelection" style="width:220px" rendered="#{Metadaten.alleSeiten != null}">
						<f:selectItems value="#{Metadaten.alleSeiten}" />
						<f:selectItem itemLabel="#{msgs.lastPage}" itemValue="#{msgs.lastPage}" />
					</h:selectOneMenu>
	                
	                <h:outputText value="#{msgs.newFileName}" />
                    <h:inputText value="#{Metadaten.fileManipulation.uploadedFileName}"  style="width:220px"/>
					
					<h:outputText value="#{msgs.paginierung}:"  />
					<h:selectOneRadio value="#{Metadaten.fileManipulation.insertMode}">
						<f:selectItem itemValue="uncounted" itemLabel="#{msgs.insertAsUncounted}" />
						<f:selectItem itemValue="insertIntoPagination" itemLabel="#{msgs.insertIntoPagination}" />
					</h:selectOneRadio>
					
				
				</h:panelGrid>
			</htm:td>
		</htm:tr>

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3">
				<h:commandButton value="#{msgs.uploadFile}" id="button3" action="#{Metadaten.fileManipulation.uploadFile}">
				</h:commandButton>
			</htm:td>
		</htm:tr>

	</htm:table>
</h:form>

<h:form id="fileDownload" rendered="#{Metadaten.alleSeiten != null}">
	<htm:h4>
		<h:outputText value="#{msgs.fileDownload}" />
	</htm:h4>
	<htm:table cellpadding="3" cellspacing="0" styleClass="eingabeBoxen" style="margin-bottom: 20px; width: 450px;">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1">
				<h:outputText value="#{msgs.fileDownload}" />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2">

				<h:panelGrid columns="2">
					<h:outputText value="#{msgs.aktuellerOrdner}: " />
					<h:selectOneMenu style="width:220px" value="#{Metadaten.fileManipulation.currentFolder}">
						<si:selectItems value="#{Metadaten.allTifFolders}" var="folder" itemLabel="#{folder}" itemValue="#{folder}" />
					</h:selectOneMenu>

					<h:outputLabel value="#{msgs.imageSelection}: " for="imageSelection" />
					<h:selectOneMenu style="width:220px" value="#{Metadaten.fileManipulation.imageSelection}" id="imageSelection">
						<f:selectItems value="#{Metadaten.alleSeiten}" />
					</h:selectOneMenu>
				</h:panelGrid>
			</htm:td>
		</htm:tr>

		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3">
				<h:commandButton value="#{msgs.fileDownload}" action="#{Metadaten.fileManipulation.downloadFile}">
				</h:commandButton>
			</htm:td>
		</htm:tr>
	</htm:table>
</h:form>

<h:form rendered="#{Metadaten.alleSeiten != null}">
	<htm:h4>
		<h:outputText value="#{msgs.serversideUpload}" />
	</htm:h4>
	<htm:table cellpadding="3" cellspacing="0" styleClass="eingabeBoxen" style="margin-bottom: 20px; width: 450px;">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1">
				<h:outputText value="#{msgs.serversideUpload}" />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2">
				<h:panelGrid columns="2" rowClasses="rowTop">
					<h:outputLabel value="#{msgs.imageSelection}:" for="filenames" />
					<h:selectManyListbox value="#{Metadaten.fileManipulation.selectedFiles}" id="filenames" style="font-size:12px;height:300px;width:220px">
						<f:selectItems value="#{Metadaten.alleSeiten}" />
					</h:selectManyListbox>
					<h:outputLabel for="deleteAfter" value="#{msgs.deleteAfterMove}:" />
					<h:selectBooleanCheckbox id="deleteAfter" value="#{Metadaten.fileManipulation.deleteFilesAfterMove}" />
				
				</h:panelGrid>
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3">
				<h:commandButton value="#{msgs.exportFiles}" onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return false"
					action="#{Metadaten.fileManipulation.exportFiles}">
				</h:commandButton>
			</htm:td>
		</htm:tr>
	</htm:table>
</h:form>

<h:form>
	<htm:h4>
		<h:outputText value="#{msgs.serversideDownload}" />
	</htm:h4>
	<htm:table cellpadding="3" cellspacing="0" styleClass="eingabeBoxen" style="margin-bottom: 20px; width: 450px;">
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row1">
				<h:outputText value="#{msgs.serversideDownload}" />
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row2">
				<h:panelGrid columns="2" rowClasses="rowTop">
					<h:outputLabel for="folderToImport" value="#{msgs.dateien}:" />
					<h:selectManyListbox style="height:300px;width:220px" value="#{Metadaten.fileManipulation.selectedFiles}" id="folderToImport">
						<si:selectItems itemLabel="#{folder}" itemValue="#{folder}" var="folder" value="#{Metadaten.fileManipulation.allImportFolder}" />
					</h:selectManyListbox>

					<h:outputLabel value="#{msgs.position}:" for="paginationSelection2" rendered="#{Metadaten.alleSeiten != null}" />
					<h:selectOneMenu value="#{Metadaten.fileManipulation.insertPage}" id="paginationSelection2" style="width:220px" rendered="#{Metadaten.alleSeiten != null}">
						<f:selectItems value="#{Metadaten.alleSeiten}" />
						<f:selectItem itemLabel="#{msgs.lastPage}" itemValue="#{msgs.lastPage}" />
					</h:selectOneMenu>

					<h:outputText value="#{msgs.paginierung}:" />
					<h:selectOneRadio value="#{Metadaten.fileManipulation.insertMode}">
						<f:selectItem itemValue="uncounted" itemLabel="#{msgs.insertAsUncounted}" />
						<f:selectItem itemValue="insertIntoPagination" itemLabel="#{msgs.insertIntoPagination}" />
					</h:selectOneRadio>
				</h:panelGrid>
			</htm:td>
		</htm:tr>
		<htm:tr>
			<htm:td styleClass="eingabeBoxen_row3">
				<h:commandButton value="#{msgs.importFiles}" onclick="if (!confirm('#{msgs.wirklichAusfuehren}?')) return false"
					action="#{Metadaten.fileManipulation.importFiles}">
				</h:commandButton>
			</htm:td>
		</htm:tr>
	</htm:table>
</h:form>


