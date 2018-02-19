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

package de.sub.goobi.metadaten.copier;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.legacy.UghImplementation;

/**
 * A LocalMetadataSelector provides methods to retrieve or modify metadata on a
 * document structure node.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class LocalMetadataSelector extends MetadataSelector {
    private static final Logger logger = LogManager.getLogger(LocalMetadataSelector.class);

    /**
     * Metadata type to return.
     */
    private final MetadataTypeInterface selector = UghImplementation.INSTANCE.createMetadataType();

    /**
     * Creates a new LocalMetadataSelector.
     *
     * @param path
     *            Path to metadatum, must consist of an “@” character followed
     *            by a metadata element name, i.e. “@TitleDocMain”
     * @throws ConfigurationException
     *             if the metadata path doesn’t start with an “@” character
     */
    public LocalMetadataSelector(String path) throws ConfigurationException {
        if (!path.startsWith(METADATA_SEPARATOR)) {
            throw new ConfigurationException(
                    "Cannot create local metadata selector: Path must start with \"@\", but is: " + path);
        }
        selector.setName(path.substring(1));
    }

    /**
     * Checks if no metadata as named by the path is available at that document
     * structure node, and only in this case adds a metadata as named by the
     * path with the value passed to the function.
     *
     * @param data
     *            document to work on, required to access the rule set
     * @param logicalNode
     *            document structure node to check and enrich
     * @param value
     *            value to write if no metadatum of this type is available
     * @see de.sub.goobi.metadaten.copier.MetadataSelector#createIfPathExistsOnly(
     *      de.sub.goobi.metadaten.copier.CopierData, ugh.dl.DocStruct,
     *      java.lang.String)
     */
    @Override
    protected void createIfPathExistsOnly(CopierData data, DocStructInterface logicalNode, String value) {
        if (findMetadatumIn(logicalNode) != null) {
            return;
        }
        tryToCreateANewMetadatum(data, logicalNode, value);
    }

    /**
     * Sets the value of the metadata described by the path to the value passed
     * to the function or creates a metadata as described by the path,
     * respectively.
     *
     * @param data
     *            document to work on, required to access the rule set
     * @param logicalNode
     *            document structure node to check and enrich
     * @param value
     *            value to write if no metadatum of this type is available
     * @see de.sub.goobi.metadaten.copier.MetadataSelector#createIfPathExistsOnly(
     *      de.sub.goobi.metadaten.copier.CopierData, ugh.dl.DocStruct,
     *      java.lang.String)
     */

    @Override
    protected void createOrOverwrite(CopierData data, DocStructInterface logicalNode, String value) {
        MetadataInterface existingMetadatum = findMetadatumIn(logicalNode);
        if (existingMetadatum != null) {
            existingMetadatum.setStringValue(value);
        } else {
            tryToCreateANewMetadatum(data, logicalNode, value);
        }
    }

    /**
     * Returns all concrete metadata selectors the potentially generic metadata
     * selector expression resolves to.
     *
     * @param node
     *            Node of the logical document structure to work on
     * @return all metadata selectors the expression resolves to
     * @see de.sub.goobi.metadaten.copier.MetadataSelector#findAll(ugh.dl.DocStruct)
     */
    @Override
    protected Iterable<MetadataSelector> findAll(DocStructInterface node) {
        ArrayList<MetadataSelector> result = new ArrayList<>(1);
        List<MetadataTypeInterface> addableTypes = node.getAddableMetadataTypes();
        if (addableTypes != null) {
            for (MetadataTypeInterface addable : addableTypes) {
                if (selector.getName().equals(addable.getName())) {
                    result.add(this);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Return the value of the metadata named by the path used to construct the
     * metadata selector, or null if no such metadata is available here.
     *
     * @param node
     *            document structure node to examine
     * @return the value of the metadata, or null if absent
     * @see de.sub.goobi.metadaten.copier.MetadataSelector#findIn(ugh.dl.DocStruct)
     */
    @Override
    protected String findIn(DocStructInterface node) {
        MetadataInterface found = findMetadatumIn(node);
        return found != null ? found.getValue() : null;
    }

    /**
     * Returns the metadata named by the path used to construct the metadata
     * selector, or null if no such metadata is available here.
     *
     * @param node
     *            document structure node to examine
     * @return the metadata, or null if absent
     * @see de.sub.goobi.metadaten.copier.MetadataSelector#findIn(ugh.dl.DocStruct)
     */
    private MetadataInterface findMetadatumIn(DocStructInterface node) {
        List<? extends MetadataInterface> metadata = node.getAllMetadataByType(selector);
        for (MetadataInterface metadatum : metadata) {
            if (selector.getName().equals(metadatum.getMetadataType().getName())) {
                return metadatum;
            }
        }
        return null;
    }

    /**
     * Returns a string that textually represents this LocalMetadataSelector.
     *
     * @return a string representation of this LocalMetadataSelector
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return METADATA_SEPARATOR + selector.getName();
    }

    /**
     * Adds a metadata as named by the path with the value passed to the
     * function. Doesn’t do anything if that isn’t possible.
     *
     * @param data
     *            document to work on, required to access the rule set
     * @param logicalNode
     *            document structure node to check and enrich
     * @param value
     *            value to write if no metadataof this type is available
     */
    private void tryToCreateANewMetadatum(CopierData data, DocStructInterface logicalNode, String value) {
        MetadataInterface copy;
        try {
            copy = UghImplementation.INSTANCE
                    .createMetadata(data.getPreferences().getMetadataTypeByName(selector.getName()));
        } catch (MetadataTypeNotAllowedException e) {
            // copy rules aren’t related to the rule set but depend on it, so
            // copy rules that don’t work with the current rule set are ignored
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot create metadata element " + selector.getName()
                        + ": The type isn’t defined by the rule set used.");
            }
            return;
        } catch (Exception e) {
            // copy rule failed, skip it
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot create metadata element " + selector.getName()
                        + ": Accessing the rule set failed with exception: "
                        + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()),
                    e);
            }
            return;
        }
        try {
            copy.setStringValue(value);
            logicalNode.addMetadata(copy);
        } catch (MetadataTypeNotAllowedException e) {
            // copy rules aren’t related to the rule set but depend on it, so
            // copy rules that don’t work with the current rule set are ignored
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot assign metadata element " + selector.getName() + " (\"" + value
                        + "\") to structural element "
                        + (logicalNode.getDocStructType() != null ? logicalNode.getDocStructType().getName()
                                : "without type")
                        + ": " + e.getMessage());
            }
        }
    }
}
