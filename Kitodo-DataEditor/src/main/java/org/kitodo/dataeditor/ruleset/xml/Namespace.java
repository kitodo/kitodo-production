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

package org.kitodo.dataeditor.ruleset.xml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A namespace serves as a container for the members of an externally defined
 * namespace. Unfortunately, the switch to Linked Open Data as an internal data
 * format was stalled when it was already three-quarters finished. There, this
 * would not have been necessary.
 */
@XmlRootElement(name = "namespace", namespace = "http://names.kitodo.org/ruleset/v2")
public class Namespace {
    /**
     * Identifier URI of the namespace.
     */
    @XmlAttribute(required = true)
    private String about;

    /**
     * Whether unspecified values are allowed. Defaults to {@code forbidden}.
     */
    @XmlAttribute
    private Unspecified unspecified;

    /**
     * The labels for the namespace. (The label is not currently in use, but
     * could be used to display the namespace in the ruleset editor.)
     */
    @XmlElement(name = "label", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<Label> labels = new LinkedList<>();

    /**
     * The members of the namespace.
     */
    @XmlElement(name = "option", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<Option> options = new LinkedList<>();

    public Collection<Option> getOptions() {
        return options;
    }

    public boolean isAbout(String otherAboutURI) {
        return normalize(about).equals(normalize(otherAboutURI));
    }

    /**
     * Normalizes a namespace URI.
     * 
     * @param uri
     *            URI to normalize
     * @return normalized URI
     */
    public String normalize(String uri) {
        return !uri.startsWith("http") || uri.endsWith("/") || uri.endsWith("#") ? uri : uri.concat("#");
    }
}
