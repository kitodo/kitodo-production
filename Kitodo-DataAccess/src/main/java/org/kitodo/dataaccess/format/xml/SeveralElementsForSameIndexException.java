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

package org.kitodo.dataaccess.format.xml;

/**
 * Exception to be thrown if an XML file cannot be written because there are
 * several data elements for one index.
 *
 * <p>
 * Child tags of XML elements define an order, first element is 1, second is 2,
 * … If object data contains more than one element for the same index, it is not
 * possible to represent this state (by the order of the child tags) in XML. In
 * this case, the data cannot be written as XML.
 */
public class SeveralElementsForSameIndexException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a SeveralElementsForSameIndexException without message.
     */
    public SeveralElementsForSameIndexException() {
        // nothing to do
    }

    /**
     * Creates a SeveralElementsForSameIndexException with a message string.
     *
     * @param message
     *            message
     */
    public SeveralElementsForSameIndexException(String message) {
        super(message);
    }

    /**
     * Creates a SeveralElementsForSameIndexException with a message and a
     * cause.
     *
     * @param message
     *            message
     * @param cause
     *            cause
     */
    public SeveralElementsForSameIndexException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a SeveralElementsForSameIndexException with a cause.
     *
     * @param cause
     *            cause
     */
    public SeveralElementsForSameIndexException(Throwable cause) {
        super(cause);
    }
}
