package org.kitodo.production.services.command;

public class MetadataScript {

    private String goal;
    private String root;
    private String value;

    /**
     * Creates a MetadataScript with given command.
     * @param command the given command.
     */
    public MetadataScript(String command) {
        String[] commandParts = command.split("=");
        goal = commandParts[0];
        String rootOrValue = commandParts[1];
        if (rootOrValue.startsWith("@")) {
            root = rootOrValue;
        } else {
            value = rootOrValue;
        }
    }

    /**
     * Get goal.
     * @return goal.
     */
    public String getGoal() {
        return goal;
    }

    /**
     * Get root.
     * @return root.
     */
    public String getRoot() {
        return root;
    }

    /**
     * Get value.
     * @return value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set Value
     * @param value the value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get name of root metadata.
     * @return the name of the root metadata.
     */
    public String getRootName() {
        return root.substring(1);
    }
}
