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

package org.kitodo.production.services.image;

import java.net.URI;
import java.util.List;

import org.kitodo.production.model.Subfolder;

/**
 * Content to be generated. The object can be considered as an instruction to
 * the producer. At the moment, only images can be generated, but later, e.g.
 * OCR can also be generated. That depends only on the configuration of the
 * subfolder.
 */
public class ContentToBeGenerated {
    /**
     * The canonical part of the file name.
     */
    private final String canonical;

    /**
     * The source URI of the content to be generated.
     */
    private final URI sourceURI;

    /**
     * Specifies the subfolders for which content is to be generated. What
     * content is specified in the subfolder.
     */
    private final List<Subfolder> subfoldersWhoseContentsAreToBeGenerated;

    /**
     * Creates a new content to generate.
     *
     * @param canonical
     *            the canonical part of the file name
     * @param sourceURI
     *            the source URI of the content to be generated
     * @param subfoldersWhoseContentsAreToBeGenerated
     *            the subfolders for which content is to be generated
     */
    public ContentToBeGenerated(String canonical, URI sourceURI,
            List<Subfolder> subfoldersWhoseContentsAreToBeGenerated) {
        this.canonical = canonical;
        this.sourceURI = sourceURI;
        this.subfoldersWhoseContentsAreToBeGenerated = subfoldersWhoseContentsAreToBeGenerated;
    }

    /**
     * Returns the canonical part of the file name of the content to be
     * generated.
     *
     * @return the canonical part of the file name
     */
    public String getCanonical() {
        return canonical;
    }

    /**
     * Returns the subfolders for which content is to be generated of the
     * content to be generated.
     *
     * @return the subfolders for which content is to be generated
     */
    public List<Subfolder> getSubfoldersWhoseContentsAreToBeGenerated() {
        return subfoldersWhoseContentsAreToBeGenerated;
    }

    /**
     * Returns the source URI of the content to be generated.
     *
     * @return the source URI
     */
    public URI getSourceURI() {
        return sourceURI;
    }

}
