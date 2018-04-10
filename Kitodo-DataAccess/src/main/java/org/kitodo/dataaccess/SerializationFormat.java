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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.kitodo.dataaccess.format.xml.XMLWriter;

/**
 * Convenience class to serialize a node.
 */
public enum SerializationFormat {
    /**
     * The N-Triples serialization format ({@code application/n-triples}).
     *
     * @see "https://en.wikipedia.org/wiki/N-Triples"
     */
    N_TRIPLES {
        @Override
        public void write(Node node, Map<String, String> namespaces, File file) throws IOException {
            write(node, namespaces, file, "N-TRIPLE");
        }
    },
    /**
     * The N3 serialization format ({@code text/n3;charset=utf-8}).
     *
     * @see "https://en.wikipedia.org/wiki/Notation3"
     */
    N3 {
        @Override
        public void write(Node node, Map<String, String> namespaces, File file) throws IOException {
            write(node, namespaces, file, "N3");
        }
    },
    /**
     * The RDF/XML serialization format ({@code application/rdf+xml}), linear
     * style. This serializer creates identifiers for all nodes and serializes
     * them in a linear way. It produces an output which reminds you of
     * N-Triples and is hardly readable. However, it is comparably faster for
     * larger models than the {@link #RDF_XML_ABBREV} serializer.
     *
     * @see "https://en.wikipedia.org/wiki/RDF/XML"
     * @see "https://jena.apache.org/documentation/io/rdfxml_howto.html#rdfxml-rdfxml-abbrev"
     */
    RDF_XML {
        @Override
        public void write(Node node, Map<String, String> namespaces, File file) throws IOException {
            write(node, namespaces, file, "RDF/XML");
        }
    },
    /**
     * The RDF/XML serialization format ({@code application/rdf+xml}), pretty
     * printed. This serializer creates serializes the nodes in a way which
     * reminds you of Turtle and improves readability. However, its runtime may
     * become unacceptable for large models.
     *
     * @see "https://en.wikipedia.org/wiki/RDF/XML"
     * @see "https://jena.apache.org/documentation/io/rdfxml_howto.html#rdfxml-rdfxml-abbrev"
     */
    RDF_XML_ABBREV {
        @Override
        public void write(Node node, Map<String, String> namespaces, File file) throws IOException {
            write(node, namespaces, file, "RDF/XML-ABBREV");
        }
    },
    /**
     * The Turtle serialization format ({@code text/turtle}).
     *
     * @see "https://en.wikipedia.org/wiki/Turtle_(syntax)"
     */
    TURTLE {
        @Override
        public void write(Node node, Map<String, String> namespaces, File file) throws IOException {
            write(node, namespaces, file, "TURTLE");
        }
    },
    /**
     * The XML serialization format ({@code application/xml}, {@code text/xml}).
     *
     * @see "https://en.wikipedia.org/wiki/XML"
     */
    XML {
        @Override
        public void write(Node node, Map<String, String> namespaces, File file) throws IOException {
            XMLWriter.toFile(node, file, 2, namespaces);
        }
    };

    /**
     * Write a serialised representation of this model in a specified language.
     *
     * @param file
     *            a file to write to
     * @param namespaces
     *            user defined namespace prefixes, mapped from prefix to
     *            namespace, the namespace must end either in {@code #} or
     *            {@code /}
     * @param lang
     *            the language in which to write the model, predefined values
     *            are {@code RDF/XML}, {@code RDF/XML-ABBREV}, {@code N-TRIPLE},
     *            {@code TURTLE} and {@code N3}.
     */
    static void write(Node node, Map<String, String> namespaces, File file, String lang) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            write(node, namespaces, bos, lang);
        }
    }

    /**
     * Write a serialized representation of this model in a specified language.
     *
     * @param out
     *            an output stream to write to
     * @param namespaces
     *            user defined namespace prefixes, mapped from prefix to
     *            namespace, the namespace must end either in {@code #} or
     *            {@code /}
     * @param lang
     *            the language in which to write the model, predefined values
     *            are {@code RDF/XML}, {@code RDF/XML-ABBREV}, {@code N-TRIPLE},
     *            {@code TURTLE} and {@code N3}.
     */
    private static void write(Node node, Map<String, String> namespaces, OutputStream out, String lang) {
        Model model = ModelFactory.createDefaultModel();
        node.toRDFNode(model, true);
        if (namespaces != null) {
            model.setNsPrefixes(namespaces);
        }
        model.write(out, lang);
    }

    /**
     * Write the node to a file.
     *
     * @param node
     *            node to print
     * @param namespaces
     *            user defined namespace prefixes, mapped from prefix to
     *            namespace, the namespace must end either in {@code #} or
     *            {@code /}
     * @param file
     *            path to the file to write to
     * @throws IOException
     *             if the writing fails
     */
    public abstract void write(Node node, Map<String, String> namespaces, File file) throws IOException;
}
