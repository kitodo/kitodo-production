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

package org.kitodo.api.dataformat.mets;

import java.io.InputStream;
import java.net.URI;

/**
 * A functional interface for a function
 * {@code InputStream getInputStream(URI uri, boolean couldHaveToBeWrittenInTheFuture)}.
 */
public interface InputStreamProviderInterface {

    /**
     * A function that opens an input stream from a source identified by an URI.
     * If used, the calling function is responsible of closing the stream.
     *
     * @param uri
     *            the identifier for the file
     * @param couldHaveToBeWrittenInTheFuture
     *            whether the file could have to be written in the future
     * @return an open input stream
     */
    InputStream getInputStream(URI uri, boolean couldHaveToBeWrittenInTheFuture);
}
