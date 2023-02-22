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

package org.kitodo.production.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The class contains functions for handling ranges in RangeStreamContentHandler.
 */
public class RangeStreamHelper {

    public static final int DEFAULT_BUFFER_SIZE = 250000; // 2MB.

    /**
     * The provides a long value from the range part.
     *
     * @param value
     *         the range part
     * @param beginIndex
     *         The index to start
     * @param endIndex
     *         The index to end
     * @return long value or -1 as fallback
     */
    public static long sublong(String value, int beginIndex, int endIndex) {
        String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }

    /**
     * Copy range of input stream to output stream.
     *
     * @param input
     *         The input stream
     * @param output
     *         The output stream
     * @param inputSize
     *         The length of available bytes
     * @param start
     *         The start of range.
     * @param length
     *         The length of range.
     * @throws IOException
     *         The exception when working with the streams
     */
    public static void copy(InputStream input, OutputStream output, long inputSize, long start, long length)
            throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;

        if (inputSize == length) {
            // Write full range.
            while ((read = input.read(buffer)) > 0) {
                output.write(buffer, 0, read);
                output.flush();
            }
        } else {
            input.skip(start);
            long toRead = length;

            while ((read = input.read(buffer)) > 0) {
                toRead -= read;
                if (toRead > 0) {
                    output.write(buffer, 0, read);
                    output.flush();
                } else {
                    output.write(buffer, 0, (int) toRead + read);
                    output.flush();
                    break;
                }
            }
        }
    }
}
