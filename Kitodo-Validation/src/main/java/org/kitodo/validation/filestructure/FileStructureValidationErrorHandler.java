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

    public FileStructureValidationErrorHandler() {
        this.validationErrors = new ArrayList<>();
    }

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
