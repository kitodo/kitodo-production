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

package org.kitodo.api.ocr;

import java.net.URI;

/**
 * The result of the file generation.
 */
public class FileResult {

    /**
     * The state of the Result.
     */
    private State state;
    /**
     * The uri of the inputfile.
     */
    private URI inputFileUri;
    /**
     * The uri of the outputfile.
     */
    private URI outputFileUri;

    /**
     * The Constructor.
     * 
     * @param state
     *            The given state
     * @param inputFileUri
     *            the given input file
     * @param outputFileUri
     *            the given outputfile
     */
    public FileResult(State state, URI inputFileUri, URI outputFileUri) {
        this.state = state;
        this.inputFileUri = inputFileUri;
        this.outputFileUri = outputFileUri;
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * Gets the inputFileUri.
     * 
     * @return the inout file uri
     */
    public URI getInputFileUri() {
        return inputFileUri;
    }

    /**
     * Gets the output file uri.
     * 
     * @return the output file uri.
     */
    public URI getOutputFileUri() {
        return outputFileUri;
    }
}
