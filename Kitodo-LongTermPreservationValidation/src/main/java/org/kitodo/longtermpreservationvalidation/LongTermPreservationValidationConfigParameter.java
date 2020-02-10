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

import java.util.stream.Stream;

import org.kitodo.config.enums.ParameterInterface;

/**
 * Implements the ParameterInterface to access the
 * LongTermPreservationValidation mapping.
 */
enum LongTermPreservationValidationConfigParameter implements ParameterInterface {
    /**
     * Specifies the Kitodo validation result if a JHove file is rated neither
     * well-formed nor valid.
     */
    VALIDATION_RESULT_NOT_WELL_FORMED_NOT_VALID("LongTermPreservationValidation.mapping.FALSE.FALSE"),

    /**
     * Specifies the Kitodo validation result if a JHove file is not well-formed
     * but valid. Whatever that may be.
     */
    VALIDATION_RESULT_NOT_WELL_FORMED_VALID("LongTermPreservationValidation.mapping.FALSE.TRUE"),

    /**
     * Specifies the Kitodo validation result if a file was not well-formed by
     * JHove and JHove was unable to determine if the file is valid.
     */
    VALIDATION_RESULT_NOT_WELL_FORMED_MAYBE_VALID("LongTermPreservationValidation.mapping.FALSE.UNDETERMINED"),

    /**
     * Specifies the Kitodo validation result if a file is judged by JHove to be
     * well-formed but not valid.
     */
    VALIDATION_RESULT_WELL_FORMED_NOT_VALID("LongTermPreservationValidation.mapping.TRUE.FALSE"),

    /**
     * Specifies the Kitodo validation result if a file has been rated as
     * well-formed and valid by JHove.
     */
    VALIDATION_RESULT_WELL_FORMED_VALID("LongTermPreservationValidation.mapping.TRUE.TRUE"),

    /**
     * Specifies the Kitodo validation result if a file was rated well by JHove,
     * but JHove could not determine if the file is valid.
     */
    VALIDATION_RESULT_WELL_FORMED_MAYBE_VALID("LongTermPreservationValidation.mapping.TRUE.UNDETERMINED"),

    /**
     * Specifies the Kitodo validation result if it could not determine if a
     * file is well-formed, but definitely not valid.
     */
    VALIDATION_RESULT_MAYBE_WELL_FORMED_NOT_VALID("LongTermPreservationValidation.mapping.UNDETERMINED.FALSE"),

    /**
     * Specifies the Kitodo validation result if JHove was unable to determine
     * if a file is well-formed, but definitely valid.
     */
    VALIDATION_RESULT_MAYBE_WELL_FORMED_VALID("LongTermPreservationValidation.mapping.UNDETERMINED.TRUE"),

    /**
     * Specifies the Kitodo validation result if JHove is completely undecided
     * about the contents in a file.
     */
    VALIDATION_RESULT_MAYBE_WELL_FORMED_MAYBE_VALID("LongTermPreservationValidation.mapping.UNDETERMINED.UNDETERMINED");

    /**
     * The constant for the indefinite state.
     */
    private static final String UNDETERMINED = "UNDETERMINED";

    /**
     * This is what we call the configuration parameter that is entered in the
     * configuration file.
     */
    private String name;

    /**
     * Private constructor to hide the implicit public one.
     *
     * @param name
     *            of parameter
     */
    LongTermPreservationValidationConfigParameter(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the long term preservation validation config
     * parameter.
     *
     * @return the name
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Retrieves the parameter interface element for a validation result state.
     *
     * @param state
     *            input status
     * @return one of the above possibilities
     */
    public static ParameterInterface valueOf(ValidationResultState state) {
        Stream<LongTermPreservationValidationConfigParameter> constants = Stream
                .of(LongTermPreservationValidationConfigParameter.values()).parallel();
        String result = state.toString().replace(TernaryValue.MAYBE.toString(), UNDETERMINED).replace('/', '.');
        LongTermPreservationValidationConfigParameter constant = constants
                .filter(member -> member.getName().endsWith(result)).findFirst()
                .orElseThrow(IllegalStateException::new);
        return constant;
    }
}
