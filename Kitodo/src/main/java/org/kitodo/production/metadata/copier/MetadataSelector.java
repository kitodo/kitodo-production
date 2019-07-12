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

package org.kitodo.production.metadata.copier;

import org.apache.commons.configuration.ConfigurationException;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;

/**
 * Abstract base class that different types of metadata selectors are based on.
 * Provides a factory method to create its subclasses depending on a a given
 * String path, and defines methods that shall be implemented by the
 * implementing metadata selectors.
 */
public abstract class MetadataSelector extends DataSelector {

    /**
     * Factory method to create a metadata selector. Depending on the path, the
     * required implementation will be constructed.
     *
     * @param path
     *            path to create a metadata selector from.
     * @return a metadata selector instance representing the given paht
     * @throws ConfigurationException
     *             if the path cannot be evaluated
     */
    public static MetadataSelector create(String path) throws ConfigurationException {

        if (path.startsWith(METADATA_SEPARATOR)) {
            throw new UnsupportedOperationException("Dead code pending removal");
        }

        if (path.startsWith(METADATA_PATH_SEPARATOR)) {
            if (path.indexOf(METADATA_SEPARATOR, 1) > -1) {
                throw new UnsupportedOperationException("Dead code pending removal");
            } else {
                return new MetadataPathSelector(path);
            }
        }
        throw new ConfigurationException(
                "Cannot create metadata selector: Path must start with \"@\" or \"/\", but is: " + path);
    }

    /**
     * Calling createIfPathExistsOnly() on the implementing instance should
     * check if the document structure node the metadata selector is pointing at
     * is available, but no metadata as named by the path is available at that
     * document structure node, and only in this case add a metadata as named by
     * the path with the value passed to the function.
     *
     * @param data
     *            data to work on
     * @param logicalNode
     *            document structure node to start from, intended for recursion
     * @param value
     *            value to write if no metadata is available at the path’s end
     * @throws RuntimeException
     *             if the operation fails for unfulfilled dependencies
     */
    protected abstract void createIfPathExistsOnly(CopierData data, LegacyDocStructHelperInterface logicalNode, String value);

    /**
     * Checks if the document structure node the metadata selector is pointing
     * at is available, but no metadata as named by the path is available at
     * that document structure node, and only in this case adds a metadata as
     * named by the path with the value passed to the function.
     *
     * @param data
     *            data to work on
     * @param value
     *            value to write if no metadata is available at the path’s end
     * @throws RuntimeException
     *             if the operation fails for unfulfilled dependencies
     */
    public void createIfPathExistsOnly(CopierData data, String value) {
        createIfPathExistsOnly(data, data.getLogicalDocStruct(), value);
    }

    /**
     * Calling createOrOverwrite() on the implementing instance should check if
     * the document structure node as named by the path is available, and set
     * the metadata as named by the path to the value passed to the function. If
     * the document structure node isn’t yet present, it should be created. If
     * the metadata already exists, it shall be overwritten, otherwise it shall
     * be created.
     *
     * @param data
     *            data to work on
     * @param logicalNode
     *            document structure node to start from, intended for recursion
     * @param value
     *            value to write if no metadata is available at the path’s end
     * @throws RuntimeException
     *             if the operation fails for unfulfilled dependencies
     */
    protected abstract void createOrOverwrite(CopierData data, LegacyDocStructHelperInterface logicalNode, String value);

    /**
     * Checks if the document structure node as named by the path is available,
     * and sets the metadata as named by the path to the value passed to the
     * function. If the document structure node isn’t yet present, it will be
     * created. If the metadata already exists, it will be overwritten,
     * otherwise it will be created.
     *
     * @param data
     *            data to work on
     * @param value
     *            value to write if no metadata is available at the path’s end
     * @throws RuntimeException
     *             if the operation fails for unfulfilled dependencies
     */
    public void createOrOverwrite(CopierData data, String value) {
        createOrOverwrite(data, data.getLogicalDocStruct(), value);
    }

    /**
     * Returns all concrete metadata selectors the potentially generic metadata
     * selector expression resolves to.
     *
     * @param data
     *            copier data to work on
     * @return all metadata selectors the expression resolves to
     */
    public Iterable<MetadataSelector> findAll(CopierData data) {
        return findAll(data.getLogicalDocStruct());
    }

    /**
     * Must be implemented to return all concrete metadata selectors the
     * potentially generic metadata selector expression resolves to.
     *
     * @param logicalNode
     *            Node of the logical document structure to work on
     * @return all metadata selectors the expression resolves to
     */
    protected abstract Iterable<MetadataSelector> findAll(LegacyDocStructHelperInterface logicalNode);

    /**
     * Returns the value of the metadata named by the path used to construct the
     * metadata selector, or null if either the path or the metadata at the end
     * of the path aren’t available.
     *
     * @return the value the path points to, or null if absent
     * @see org.kitodo.production.metadata.copier.DataSelector#findIn(org.kitodo.production.metadata.copier.CopierData)
     */
    @Override
    public String findIn(CopierData data) {
        return findIn(data.getLogicalDocStruct());
    }

    /**
     * Calling findIn() on the implementing instance should return the value of
     * the metadata named by the path used to construct the metadata selector.
     * Should return null if either the path or the metadata at the end of the
     * path aren’t available.
     *
     * @param logicalNode
     *            document structure node to examine, intended for recursion
     * @return the value the path points to, or null if absent
     */
    protected abstract String findIn(LegacyDocStructHelperInterface logicalNode);
}
