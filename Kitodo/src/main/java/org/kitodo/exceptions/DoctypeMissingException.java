package org.kitodo.exceptions;

public class DoctypeMissingException extends Exception {
    /**
     * Constructor with given exception message.
     *
     * @param exceptionMessage
     *            as String
     */
    public DoctypeMissingException(String exceptionMessage) {
        super(exceptionMessage);
    }

}
