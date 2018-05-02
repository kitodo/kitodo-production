# Create process out of template

## Copy process attributes

It works exactly like in previous version.

## Copy tasks

Here logic has changed. New templates allow to create processes with conditional task execution.
That means that during process creation user can give conditions (they are defined in diagram gateways and stored in workflowCondition column of Task table).
According to those conditions certain tasks are copied to newly created process.

```java
public static void copyTasks(Template processTemplate, Process processCopy, List<String> workflowConditions) {
        List<Task> tasks = new ArrayList<>();
        for (Task templateTask : processTemplate.getTasks()) {
            String taskWorkflowCondition = templateTask.getWorkflowCondition();
            if (Objects.isNull(workflowConditions) || workflowConditions.isEmpty()) {
                // tasks created before workflow functionality was introduced has null value
                if (Objects.isNull(taskWorkflowCondition) || taskWorkflowCondition.contains("default")) {
                    Task task = getCopiedTask(templateTask);
                    task.setProcess(processCopy);
                    tasks.add(task);
                }
            } else {
                for (String workflowCondition : workflowConditions) {
                    if (taskWorkflowCondition.contains("default")) {
                        Task task = getCopiedTask(templateTask);
                        task.setProcess(processCopy);
                        tasks.add(task);
                    } else if (taskWorkflowCondition.contains(workflowCondition)) {
                        Task task = getCopiedTask(templateTask);
                        task.setProcess(processCopy);
                        tasks.add(task);
                    }
                }
            }
        }
        adjustTaskOrdering(tasks);
        processCopy.setTasks(tasks);
    }
```

At the end right task ordering is determined.