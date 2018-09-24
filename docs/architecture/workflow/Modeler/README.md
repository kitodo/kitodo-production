# Modeler

Kitodo uses standard BPMN model with custom extension, which provides user possibility to add Kitodo specific properties.

## Extension

### BPMN Process

It is extended by TemplateProcess which contains three custom properties:

*  outputName
*  docket - value of docket's id
*  ruleset - value of ruleset's id

### BPMN Task

It is extended by TemplateTask which contains custom properties:

*  priority - integer
*  editType - true / false
*  typeMetadata - true / false
*  typeAutomatic - true / false
*  typeImportFileUpload - true / false
*  typeExportRussian - true / false
*  typeImagesRead - true / false
*  typeImagesWrite - true / false
*  typeExportDms - true / false
*  typeAcceptClose - true / false
*  typeCloseVerify - true / false
*  batchStep - true / false

They are directly mapped from task columns in Task table.

### BPMN ScriptTask

It is extended by TemplateScriptTask which contains two additional custom properties:

*  scriptName - string
*  scriptPath - string

They are directly mapped from task columns in Task table.

## Storage

Diagrams are stored in user local directory, which is defined in kitodo_config.properties:

```
directory.diagrams = path/to/diagram/directory
```

Additionally there was added Workflow table which has two columns - title and file. Title is id of Process defined in this diagram and file is a file name of this diagram.
