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

package org.kitodo.api.ugh;

import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;

/**
 * A file format is an abstract description of a serialization of a complete
 * {@code DigitalDocument}. A file format may store or read a
 * {@code DigitalDocument} object to/from a file.
 *
 * <p>
 * Depending on the implementation a file format may store all or only part of
 * the information.
 *
 * <p>
 * Every file format may have methods to load, save or update a file. In an
 * implementation not all methods need to be available. Certain file formats are
 * just readable; other may not be updateable.
 *
 * <p>
 * <b>Differences between readable, updateable, writeable:</b>
 * <ul>
 * <li>readable: the file format can be read from a file
 * <li>updateable: after reading a file format, some information can be updated.
 * The result can be written back (to the same file).
 * <li>writeable: a {@code DigitalDocument} can be written to a completely new
 * file.
 * </ul>
 *
 * <p>
 * Internally, every file format has a digital document instance, which will be
 * created while reading a file successfully. This instance can be obtained by
 * calling {@code getDigitalDocument()}. Before writing a file, a
 * DigitalDocument instance must be available.
 */
public interface FileformatInterface {

    /**
     * Returns the digital document. The digital document was created while
     * reading the file. If a file was unreadable, {@code null} is returned.
     *
     * @return the digital document instance
     * @throws PreferencesException
     *             error on creating process
     */
    DigitalDocumentInterface getDigitalDocument() throws PreferencesException;

    /**
     * Reads a file and creates a digital document instance.
     *
     * @param path
     *            full path to file which should be read
     * @throws ReadException
     *             may be thrown if reading fails
     */
    void read(String path) throws ReadException;

    /**
     * Sets a DigitalDocument instance. This instance must be available before a
     * file can be written or updated.
     *
     * @param digitalDocument
     *            digital document instance to be set
     */
    void setDigitalDocument(DigitalDocumentInterface digitalDocument);

    /**
     * Writes the content of the DigitalDocument instance to a file. The file
     * format must already have a DigitalDocument instance.
     *
     * @param filename
     *            full path to the file
     * @throws PreferencesException
     *             may be thrown if there is a problem with the preferences
     * @throws WriteException
     *             may be thrown if writing fails
     */
    void write(String filename) throws PreferencesException, WriteException;

}
