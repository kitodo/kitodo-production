package org.kitodo.api.command;

import java.util.ArrayList;

public class CommandResult {

    private Integer id;

    private String command;

    private boolean successful;

    private ArrayList<String> messages;

    public CommandResult(Integer id, String command, boolean successful, ArrayList<String> messages) {
        this.id = id;
        this.command = command;
        this.successful = successful;
        this.messages = messages;
    }

    public Integer getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }
}
