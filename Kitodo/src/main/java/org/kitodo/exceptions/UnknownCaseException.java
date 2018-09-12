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

package org.kitodo.exceptions;

/**
 * Exception to be thrown in the {@code default} case of (for the moment)
 * complete switch statements.
 */
public class UnknownCaseException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public <T extends Object> UnknownCaseException(Class<T> caseType, T caseValue) {
        super(buildMessage(caseType, caseValue));
    }

    private static final <T extends Object> String buildMessage(Class<T> caseType, T caseValue) {
        StringBuilder message = new StringBuilder(96);
        message.append("Unknown case in switch on ");
        boolean isEnumType = caseType == null ? false : Enum.class.isAssignableFrom(caseType);
        if (isEnumType) {
            message.append("Enum<");
        }
        if (caseType == null) {
            message.append('?');
        } else {
            message.append(caseType.getSimpleName());
        }
        if (isEnumType) {
            message.append('>');
        }
        message.append(": ");
        message.append(caseValue);
        return message.toString();
    }
}
