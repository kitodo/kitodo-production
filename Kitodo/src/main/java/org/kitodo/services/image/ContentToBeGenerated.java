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

package org.kitodo.services.image;

import java.net.URI;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.model.Subfolder;

/**
 * Content to be generated. The object can be considered as an instruction to
 * the producer. At the moment, only images can be generated, but later, e.g.
 * OCR can also be generated. That depends only on the configuration of the
 * subfolder.
 */
public class ContentToBeGenerated {
    /**
     * Description of the data source.
     */
    private Pair<String, URI> source;
    /**
     * Specifies the subfolders for which content is to be generated. What
     * content is specified in the subfolder.
     */
    private List<Subfolder> generations;

    public ContentToBeGenerated(Pair<String, URI> source, List<Subfolder> generations) {
        this.source = source;
        this.generations = generations;
    }

    /**
     * Returns the source of the content to be generated.
     * 
     * @return the source
     */
    public Pair<String, URI> getSource() {
        return source;
    }

    /**
     * Returns the canonical part of the file name of the content to be
     * generated.
     * 
     * @return the canonical part of the file name
     */
    public String getCanonical() {
        return source.getKey();
    }

    /**
     * Returns the subfolders for which content is to be generated of the
     * content to be generated.
     * 
     * @return the subfolders for which content is to be generated
     */
    public List<Subfolder> getGenerations() {
        return generations;
    }

    /**
     * Returns the source URI of the content to be generated.
     * 
     * @return the source URI
     */
    public URI getSourceURI() {
        return source.getValue();
    }

}
