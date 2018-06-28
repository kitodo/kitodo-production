package org.kitodo.longtimepreservationvalidationmodule;

import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.HandlerBase;
import edu.harvard.hul.ois.jhove.Message;
import edu.harvard.hul.ois.jhove.Module;
import edu.harvard.hul.ois.jhove.OutputHandler;
import edu.harvard.hul.ois.jhove.RepInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;

public class KitodoOutputHandler extends HandlerBase {

    private static final Pattern LINE_SPLITTER = Pattern.compile(System.lineSeparator());

    private static final Constructor<ValidationResult> VALIDATION_RESULT_CONSTRUCTOR;

    static {
        try {
            VALIDATION_RESULT_CONSTRUCTOR = ValidationResult.class.getDeclaredConstructor(State.class,
                Collection.class);
            VALIDATION_RESULT_CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Method that is executed before the constructor of the parent class.
     *
     * @return {@code null}. The value is not used.
     */
    private static final String beforeParentConstructor() {
        Logger.getLogger("edu.harvard.hul.ois.jhove.handler").setLevel(Level.OFF);
        return null;
    }

    List<String> messages = new ArrayList<String>();

    ValidationResultState state;

    protected KitodoOutputHandler() {
        super(beforeParentConstructor(), null, new int[] {1970, 1, 1 }, null, null);
    }

    private void addMessage(Message message) {
        StringBuilder line = new StringBuilder(127);
        line.append(message.getMessage());
        String subMessage = message.getSubMessage();
        if (subMessage != null) {
            line.append(": ");
            line.append(subMessage);
        }
        messages.add(line.toString());
        long offset = message.getOffset();
        if (offset > -1) {
            messages.add("Offset: " + offset);
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void show(App app) {
    }

    @Override
    public void show(Module module) {
    }

    @Override
    public void show(OutputHandler handler) {
    }

    @Override
    public void show(RepInfo info) {
        state = ValidationResultState.valueOf(info.getWellFormed(),
            super._je.getSignatureFlag() ? RepInfo.UNDETERMINED : info.getValid());
        info.getMessage().forEach(x -> addMessage(x));
    }

    @Override
    public void showFooter() {
    }

    @Override
    public void showHeader() {
    }

    /**
     * Returns a concise textual representation of this Kitodo output handler.
     * Will be called from debuggers.
     *
     * @return representation of this
     */
    @Override
    public String toString() {
        return messages.stream().collect(Collectors.joining(", ", String.valueOf(state).concat(" ("), ")"));
    }

    /**
     * Creates a new validation result. The function uses reflection since the
     * result’s constructor is package private.
     *
     * @param state
     *            state to set
     * @param resultMessages
     *            result messages to set
     * @return the created validation result
     */
    public ValidationResult toValidationResult() {
        try {
            return VALIDATION_RESULT_CONSTRUCTOR.newInstance(state.toState(), messages);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Sets the state to ERROR and the exception as message, including the stack
     * trace.
     *
     * @param e
     *            exception to set
     */
    public void treatException(Exception e) {
        state = ValidationResultState.ERROR;
        if (e.getMessage() != null) {
            messages.add(e.getMessage());
        }
        messages.add(e.getClass().getSimpleName());
        StringWriter buffer = new StringWriter();
        e.printStackTrace(new PrintWriter(buffer));
        LINE_SPLITTER.splitAsStream(buffer.toString()).collect(Collectors.toCollection(() -> messages));
    }

}
