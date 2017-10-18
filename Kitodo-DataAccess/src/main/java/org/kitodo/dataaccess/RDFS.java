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

package org.kitodo.dataaccess;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

/**
 * The {@code http://www.w3.org/2000/01/rdf-schema} namespace.
 */
public enum RDFS implements NodeReference {
    /**
     * The class of classes.
     */
    CLASS("http://www.w3.org/2000/01/rdf-schema#Class"),

    /**
     * A description of the subject resource.
     */
    COMMENT("http://www.w3.org/2000/01/rdf-schema#comment"),

    /**
     * The class of RDF containers.
     */
    CONTAINER("http://www.w3.org/2000/01/rdf-schema#Container"),

    /**
     * The class of container membership properties, rdf:_1, rdf:_2, ...,<br>
     * all of which are sub-properties of 'member'.
     */
    CONTAINER_MEMBERSHIP_PROPERTY("http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty"),

    /**
     * The class of RDF datatypes.
     */
    DATATYPE("http://www.w3.org/2000/01/rdf-schema#Datatype"),

    /**
     * A domain of the subject property.
     */
    DOMAIN("http://www.w3.org/2000/01/rdf-schema#domain"),

    /**
     * The defininition of the subject resource.
     */
    IS_DEFINED_BY("http://www.w3.org/2000/01/rdf-schema#isDefinedBy"),

    /**
     * A human-readable name for the subject.
     */
    LABEL("http://www.w3.org/2000/01/rdf-schema#label"),

    /**
     * The class of literal values, eg. textual strings and integers.
     */
    LITERAL("http://www.w3.org/2000/01/rdf-schema#Literal"),

    /**
     * A member of the subject resource.
     */
    MEMBER("http://www.w3.org/2000/01/rdf-schema#member"),

    /**
     * A range of the subject property.
     */
    RANGE("http://www.w3.org/2000/01/rdf-schema#range"),

    /**
     * The class resource, everything.
     */
    RESOURCE("http://www.w3.org/2000/01/rdf-schema#Resource"),

    /**
     * Further information about the subject resource.
     */
    SEE_ALSO("http://www.w3.org/2000/01/rdf-schema#seeAlso"),

    /**
     * The subject is a subclass of a class.
     */
    SUB_CLASS_OF("http://www.w3.org/2000/01/rdf-schema#subClassOf"),

    /**
     * The subject is a subproperty of a property.
     */
    SUB_PROPERTY_OF("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");

    /**
     * The RDFS namespace.
     */
    public static final String NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";

    private String identifier;

    /**
     * Creates a new NodeReference.
     *
     * @param identifier
     *            referenced URL
     */
    private RDFS(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public RDFNode toRDFNode(Model model, Boolean unused) {
        return model.createResource(identifier);
    }

    /**
     * Returns a version of this node reference which, in a debugger, will
     * symbolically represent it.
     */
    @Override
    public String toString() {
        return '↗' + identifier;
    }
}
