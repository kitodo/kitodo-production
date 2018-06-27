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

package org.kitodo.longtimepreservationvalidationmodule;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.util.Collection;

import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.longtimepreservation.FileType;
import org.kitodo.api.validation.longtimepreservation.LongTimePreservationValidationInterface;

/**
 * An LongTimePreservationValidationInterface implementation using Jhove.
 */
public class LongTimePreservationValidationModule implements LongTimePreservationValidationInterface {

    /**
     * {@inheritDoc}
     *
     * @param fileUri
     *            file URI to validate
     * @param fileType
     *            file type to validate
     */
    @Override
    public ValidationResult validate(URI fileUri, FileType fileType) {
        State result = null;
        Collection<String> messages = null;

        return makeValidationResultFrom(result, messages);
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
    private ValidationResult makeValidationResultFrom(State state, Collection<String> resultMessages) {
        try {
            Constructor<ValidationResult> ctor = ValidationResult.class.getDeclaredConstructor(State.class,
                Collection.class);
            ctor.setAccessible(true);
            return ctor.newInstance(state, resultMessages);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

}
