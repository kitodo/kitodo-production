package org.kitodo.longtimepreservationvalidationmodule;

import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.HandlerBase;
import edu.harvard.hul.ois.jhove.Message;
import edu.harvard.hul.ois.jhove.Module;
import edu.harvard.hul.ois.jhove.OutputHandler;
import edu.harvard.hul.ois.jhove.RepInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;

public class KitodoOutputHandler extends HandlerBase {

    ValidationResultState state;
    List<String> messages = new ArrayList<String>();

    protected KitodoOutputHandler() {
        super("", null, new int[] {1970, 1, 1 }, null, null);
    }

    @Override
    public void show() {
    }

    @Override
    public void show(Module module) {
    }

    @Override
    public void show(RepInfo info) {
        state = ValidationResultState.valueOf(info.getWellFormed(),
            super._je.getSignatureFlag() ? RepInfo.UNDETERMINED : info.getValid());
        info.getMessage().forEach(x -> addMessage(x));
    }

    private void addMessage(Message message) {
        StringBuilder line = new StringBuilder(127);
        line.append(message.getMessage());
        String subMessage = message.getSubMessage();
        if (subMessage != null) {
            line.append(": ");
            line.append(subMessage);
        }
        long offset = message.getOffset();
        if (offset > -1) {
            line.append(" (offset: ");
            line.append(offset);
            line.append(')');
        }
        messages.add(line.toString());
    }

    @Override
    public void show(OutputHandler handler) {
    }

    @Override
    public void show(App app) {
    }

    @Override
    public void showFooter() {
    }

    @Override
    public void showHeader() {
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
    public org.kitodo.api.validation.ValidationResult toValidationResult() {

        try {
            Constructor<ValidationResult> ctor = ValidationResult.class.getDeclaredConstructor(State.class,
                Collection.class);
            ctor.setAccessible(true);
            return ctor.newInstance(state.toState(), messages);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public void catchException(Exception e) {
        state = ValidationResultState.ERROR;
        if (e.getMessage() == null) {
            messages = Collections.emptyList();
        } else {
            messages = Arrays.asList(e.getMessage());
        }
    }

}
