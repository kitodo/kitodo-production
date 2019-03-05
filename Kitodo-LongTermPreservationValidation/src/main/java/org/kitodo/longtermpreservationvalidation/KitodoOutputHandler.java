/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.longtermpreservationvalidation;

import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.HandlerBase;
import edu.harvard.hul.ois.jhove.Message;
import edu.harvard.hul.ois.jhove.Module;
import edu.harvard.hul.ois.jhove.OutputHandler;
import edu.harvard.hul.ois.jhove.RepInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;

public class KitodoOutputHandler extends HandlerBase {

    /**
     * The numbers are used to initialize a calendar object which is never used
     * later, Hoverer, the array must be in place or a
     * {@link NullPointerException} will be thrown when JHove tries to access
     * the array members to initialize the unused object. The Java epoch is
     * defined in milliseconds since 1970-01-01, so these numbers practically
     * mean <i>zero</i>.
     */
    private static final int[] EPOCH_ZERO = new int[] {1970, 1, 1 };

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
    private static String beforeParentConstructor() {
        String logger = "edu.harvard.hul.ois.jhove.handler";
        Logger.getLogger(logger).setLevel(Level.OFF);
        return logger;
    }

    private final List<String> messages = new ArrayList<>();

    private ValidationResultState state;

    protected KitodoOutputHandler() {
        super(beforeParentConstructor(), null, EPOCH_ZERO, null, null);
    }

    private void addMessage(Message message) {
        StringBuilder line = new StringBuilder(127);
        line.append(message.getMessage());
        String subMessage = message.getSubMessage();
        if (Objects.nonNull(subMessage)) {
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
        state = new ValidationResultState(info.getWellFormed(),
            super._je.getSignatureFlag() ? RepInfo.UNDETERMINED : info.getValid());
        messages.add(state.getResultString());
        info.getMessage().forEach(this::addMessage);
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
     * Returns the captured results as validation result. The function uses
     * reflection since the result’s constructor is package private.
     *
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
     * @param exception
     *            exception to set
     */
    public void treatException(Exception exception) {
        state = ValidationResultState.ERROR;
        if (exception.getMessage() != null) {
            messages.add(exception.getMessage());
        }
        messages.add(exception.getClass().getSimpleName());
        try (StringWriter buffer = new StringWriter(); PrintWriter bufferPrinter = new PrintWriter(buffer)) {
            exception.printStackTrace(bufferPrinter);
            LINE_SPLITTER.splitAsStream(buffer.toString()).collect(Collectors.toCollection(() -> messages));
        } catch (IOException e) {
            throw new IllegalStateException("this will never happen", e);
        }
    }
}
