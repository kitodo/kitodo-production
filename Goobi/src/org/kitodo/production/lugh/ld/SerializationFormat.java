/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General private License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.lugh.ld;

import java.io.*;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Convenience class to serialize a node.
 *
 * @author Matthias Ronge
 */
public enum SerializationFormat {
    N_TRIPLE {
        @Override
        public void write(Node node, Map<String, String> map, File file) throws FileNotFoundException {
            write(node, map, file, "N-TRIPLE");
        }
    },
    N3 {
        @Override
        public void write(Node node, Map<String, String> map, File file) throws FileNotFoundException {
            write(node, map, file, "N3");
        }
    },
    RDF_XML {
        @Override
        public void write(Node node, Map<String, String> map, File file) throws FileNotFoundException {
            write(node, map, file, "RDF/XML");
        }
    },
    RDF_XML_ABBREV {
        @Override
        public void write(Node node, Map<String, String> map, File file) throws FileNotFoundException {
            write(node, map, file, "RDF/XML-ABBREV");
        }
    },
    TURTLE {
        @Override
        public void write(Node node, Map<String, String> map, File file) throws FileNotFoundException {
            write(node, map, file, "TURTLE");
        }
    };

    /**
     * Write a serialised representation of this model in a specified language.
     *
     * @param file
     *            a file to write to
     * @param map
     *            user defined namespace prefixes, mapped from prefix to
     *            namespace, the namespace must end either in {@code #} or
     *            {@code /}
     * @param lang
     *            the language in which to write the model, predefined values
     *            are {@code RDF/XML}, {@code RDF/XML-ABBREV}, {@code N-TRIPLE},
     *            {@code TURTLE} and {@code N3}.
     */
    private static void write(Node node, Map<String, String> map, File file, String lang) throws FileNotFoundException {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            write(node, map, bos, lang);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    /* close silently */ }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    /* close silently */ }
            }
        }
    }

    /**
     * Write a serialized represention of this model in a specified language.
     *
     * @param out
     *            an output stream to write to
     * @param map
     *            user defined namespace prefixes, mapped from prefix to
     *            namespace, the namespace must end either in {@code #} or
     *            {@code /}
     * @param lang
     *            the language in which to write the model, predefined values
     *            are {@code RDF/XML}, {@code RDF/XML-ABBREV}, {@code N-TRIPLE},
     *            {@code TURTLE} and {@code N3}.
     */
    private static void write(Node node, Map<String, String> map, OutputStream out, String lang) {
        Model model = node.toModel();
        if (map != null) {
            model.setNsPrefixes(map);
        }
        model.write(out, lang);
    }

    /**
     * Write the node to a file.
     *
     * @param node
     *            node to print
     * @param map
     *            map of prefixes to resolve. For XML, mapping from namespaces,
     *            without {@code #}, to abbreviations; for all other formats
     *            mapping from abbreviations to namespaces, with {@code #}.
     */
    public abstract void write(Node node, Map<String, String> map, File file) throws IOException;
}
