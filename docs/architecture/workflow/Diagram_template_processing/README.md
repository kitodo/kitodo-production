# Diagram - template processing

## Camunda BPMN Model library

Kitodo uses for reading BPMN diagrams as processes Camunda BPMN library.

```java
public void loadProcess() throws IOException {
    String diagramPath = ConfigCore.getKitodoDiagramDirectory() + this.diagramName + ".bpmn20.xml";
    modelInstance = Bpmn.readModelFromStream(fileService.read(new File(diagramPath).toURI()));
}
```
It has bean classes which reads custom XML attributes.
They take as attribute BPMN classes (Process, Task, ScriptTask) and read those attributes:

```java
static final String NAMESPACE = "http://www.kitodo.com/template";
....
task.getAttributeValueNs(NAMESPACE, "priority")
```

## Database

Template table was modified to store reference to Workflow table.
Additionally Task table has a column workflowCondition, which stores information about possible conditions defined in gateways.
