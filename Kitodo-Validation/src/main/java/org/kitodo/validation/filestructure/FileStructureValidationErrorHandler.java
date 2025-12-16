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


package org.kitodo.validation.filestructure;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class FileStructureValidationErrorHandler implements ErrorHandler {

    private final List<SAXParseException> validationErrors;

    /**
     * Constructor for initializing a handler for capturing validation errors during
     * XML schema validation. This handler collects any encountered {@link SAXParseException}
     * instances into an internal list for later retrieval.
     */
    public FileStructureValidationErrorHandler() {
        this.validationErrors = new ArrayList<>();
    }

    /**
     * Retrieves the list of validation errors encountered during XML schema validation.
     * These errors are represented as {@link SAXParseException} instances, which include
     * details such as the line number, column number, and description of the error.
     *
     * @return a list of {@link SAXParseException} objects representing validation errors,
     * or an empty list if no errors were encountered.
     */
    public List<SAXParseException> getValidationErrors() {
        return validationErrors;
    }

    @Override
    public void warning(SAXParseException exception) {
        validationErrors.add(exception);
    }

    @Override
    public void error(SAXParseException exception) {
        validationErrors.add(exception);
    }

    @Override
    public void fatalError(SAXParseException exception) {
        validationErrors.add(exception);
    }
}
