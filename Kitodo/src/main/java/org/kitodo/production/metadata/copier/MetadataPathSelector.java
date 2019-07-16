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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;

/**
 * A MetadataPathSelector provides methods to retrieve or modify document
 * structure nodes on a document structure node.
 */
public class MetadataPathSelector extends MetadataSelector {
    /**
     * Symbol meaning that all indices are to be matched.
     */
    private static final String ALL_CHILDREN_SYMBOL = "*";

    /**
     * Symbol meaning that any metadata types are to be matched.
     */
    private static final String ANY_METADATA_TYPE_SYMBOL = "*";

    private static final Logger logger = LogManager.getLogger(MetadataPathSelector.class);

    /**
     * The constant METADATA_SPLIT_PATH_SCHEME holds a regular expression used
     * to extract the first metadata path segment.
     */
    private static final Pattern METADATA_SPLIT_PATH_SCHEME = Pattern
            .compile("^" + METADATA_PATH_SEPARATOR + "([^" + METADATA_PATH_SEPARATOR + METADATA_SEPARATOR + "]+)");

    /**
     * The constant SEGMENT_WITH_ELEMENT_SELELCTOR_SCHEME holds a regular
     * expression used to detect and extract a quantifier expression at the end
     * of the string.
     */
    private static final Pattern SEGMENT_WITH_ELEMENT_SELELCTOR_SCHEME = Pattern.compile("(.*?)\\[(.+?)\\]");

    /**
     * DocStructType name of the structure element to look for or create. "*"
     * may be used to look up "any element", but will not work if an element
     * needs to be constructed at this level.
     */
    private final String docStructType;

    /**
     * Integer of the element referenced, where Integer.MAX_VALUE indicates the
     * "last" element, or null if none.
     */
    private final Object index;

    /**
     * A metadata selector resolving the subsequent path.
     */
    private final MetadataSelector selector;

    /**
     * Creates a new MetadataPathSelector.
     *
     * @param path
     *            path to create sub-selector, passed to {
     *            {@link #create(String)}.
     * @throws ConfigurationException
     *             if the path is invalid
     */
    public MetadataPathSelector(String path) throws ConfigurationException {
        String pathSegment = matchCurrentPathSegment(path);
        Matcher pathSelectorHasElementSelector = SEGMENT_WITH_ELEMENT_SELELCTOR_SCHEME.matcher(pathSegment);
        if (pathSelectorHasElementSelector.matches()) {
            docStructType = pathSelectorHasElementSelector.group(1);
            String indexSymbol = pathSelectorHasElementSelector.group(2);
            try {
                index = getIndexValue(indexSymbol);
                if (index instanceof Integer && (Integer) index < 0) {
                    throw new ConfigurationException("Negative element count is not allowed, in path: " + path);
                }
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Cannot create metadata path selector: " + e.getMessage(), e);
            }
        } else {
            docStructType = pathSegment;
            index = null;
        }
        selector = MetadataSelector.create(path.substring(pathSegment.length() + 1));
    }

    /**
     * Creates a new metadata path selector as specified by the arguments
     * passed.
     *
     * @param docStructType
     *            docStructType name to match
     * @param index
     *            index to match
     * @param selector
     *            selector for the subsequent path
     */
    private MetadataPathSelector(String docStructType, int index, MetadataSelector selector) {
        this.docStructType = docStructType;
        this.index = index;
        this.selector = selector;
    }

    /**
     * Creates a metadata with the given value if the full path is applied and
     * no such metadata is already available under at the path. Leaves the
     * document structure element unchanged if such a metadata already exists.
     * This works recursively, by calling itself on the subnode, if found, or
     * returning null otherwise.
     *
     * @param data
     *            data to work on
     * @param logicalNode
     *            document structure node to start from, intended for recursion
     * @param value
     *            value to write if no metadata is available at the path’s end
     * @see MetadataSelector#createIfPathExistsOnly(CopierData, LegacyDocStructHelperInterface, String)
     */
    @Override
    protected void createIfPathExistsOnly(CopierData data, LegacyDocStructHelperInterface logicalNode, String value) {
        LegacyDocStructHelperInterface subnode = getSubnode(logicalNode);
        if (subnode == null) {
            return;
        }
        selector.createIfPathExistsOnly(data, subnode, value);
    }

    /**
     * Sets the metadatum identified by the given path if available, otherwise
     * creates the path and metadatum. This works recursively. If the DocStructType is set
     * to "*", no path will be created if no path exists.
     *
     * @param data
     *            data to work on
     * @param logicalNode
     *            document structure node to start from, intended for recursion
     * @param value
     *            value to write
     * @see MetadataSelector#createOrOverwrite(CopierData, LegacyDocStructHelperInterface, String)
     */
    @Override
    protected void createOrOverwrite(CopierData data, LegacyDocStructHelperInterface logicalNode, String value) {
        LegacyDocStructHelperInterface subnode = getSubnode(logicalNode);
        if (subnode == null && !ANY_METADATA_TYPE_SYMBOL.equals(docStructType)) {
            try {
                throw new UnsupportedOperationException("Dead code pending removal");
            } catch (RuntimeException e) {
                // copy rule failed, skip it
                String nodeName = logicalNode.getDocStructType() != null ? logicalNode.getDocStructType().getName() : "without type";
                logger.debug("Cannot create structural element {} as child of {}: Accessing the rule set failed with exception: {}",
                    docStructType, nodeName, e.getMessage());
                return;
            }
        }
        if (subnode != null) {
            selector.createOrOverwrite(data, subnode, value);
        }
    }

    /**
     * Returns all concrete metadata selectors the
     * potentially generic metadata selector expression resolves to.
     *
     * @param logicalNode
     *            Node of the logical document structure to work on
     * @return all metadata selectors the expression resolves to
     *
     * @see MetadataSelector#findAll(LegacyDocStructHelperInterface)
     */
    @Override
    protected Iterable<MetadataSelector> findAll(LegacyDocStructHelperInterface logicalNode) {
        LinkedList<MetadataSelector> all = new LinkedList<>();
        List<LegacyDocStructHelperInterface> children = logicalNode.getAllChildren();

        int lastChild = children.size() - 1;
        int count = 0;
        for (LegacyDocStructHelperInterface child : children) {
            if (typeCheck(child) && indexCheck(count, lastChild)) {
                for (MetadataSelector cms : selector.findAll(child)) {
                    all.add(new MetadataPathSelector(ANY_METADATA_TYPE_SYMBOL, count, cms));
                }
            }
            count++;
        }
        return all;
    }

    /**
     * Returns the value of the metadata named by the path used to construct the
     * metadata selector, or null if either the path or the metadata at the end
     * of the path aren’t available. This works recursively, by calling itself
     * on the subnode, if found, or returning null otherwise.
     *
     * @see MetadataSelector#findIn(LegacyDocStructHelperInterface)
     */
    @Override
    protected String findIn(LegacyDocStructHelperInterface superNode) {
        LegacyDocStructHelperInterface subNode = getSubnode(superNode);
        if (Objects.isNull(subNode)) {
            return null;
        } else {
            return selector.findIn(subNode);
        }
    }

    /**
     * Returns the numeric index of the metadata selector, if any. If no index
     * is specified ({@code null}), or generically refers to all or the last
     * element, {@code -1} is returned.
     *
     * @return the index number of the metadata selector
     */
    public int getIndex() {
        if (index instanceof Integer) {
            int a = (Integer) index;
            if (a < Integer.MAX_VALUE) {
                return a;
            }
        }
        return -1;
    }

    /**
     * Returns the numerical value represented by
     * the symbolic (String) representation passed in. Since the method is
     * called from the constructor it must not be overridden in subclasses.
     *
     * @param indexSymbol
     *            an integer value or ">" to refer to Integer.MAX_VALUE
     * @return the integer value of the string, or Integer.MAX_VALUE for the
     *         symbol ">".
     */
    private Object getIndexValue(String indexSymbol) {
        try {
            return Integer.valueOf(indexSymbol);
        } catch (NumberFormatException cannotParseInt) {
            if (LAST_CHILD_QUANTIFIER.equals(indexSymbol)) {
                return Integer.MAX_VALUE;
            } else {
                return indexSymbol;
            }
        }
    }

    /**
     * Returns the selector for the rest of the expression.
     *
     * @return the subsequent selector
     */
    public MetadataSelector getSelector() {
        return selector;
    }

    /**
     * Returns the subnode identified by the path segment this metadata path
     * selector is responsible for. Returns null if no such node can be found.
     *
     * @param logicalNode
     *            document structure node to retrieve the subnode from
     * @return the subnode in question
     * @throws RuntimeException
     *             if there is more than one element matching but no index was
     *             given to chose among them
     */
    private LegacyDocStructHelperInterface getSubnode(LegacyDocStructHelperInterface logicalNode) {
        List<LegacyDocStructHelperInterface> children = logicalNode.getAllChildrenByTypeAndMetadataType(docStructType,
            ANY_METADATA_TYPE_SYMBOL);

        switch (children.size()) {
            case 0:
                return null;
            case 1:
                if (index == null || index.equals(0) || index.equals(Integer.MAX_VALUE)) {
                    return children.get(0);
                }
                break;
            default:
                break;
        }

        if (index == null) {
            throw new RuntimeException(
                    "Could not resolve metadata path: Path selector is ambiguous for " + docStructType);
        } else if (!(index instanceof Integer)) {
            throw new RuntimeException(
                    "Could not resolve metadata path: In this regard, index \"" + index + "\" is not allowed.");
        } else if (index.equals(Integer.MAX_VALUE)) {
            return children.get(children.size() - 1);
        } else if (children.size() >= (Integer) index) {
            return children.get((Integer) index);
        }

        return null;
    }

    /**
     * Calculates whether the given child’s index is
     * to be matched by this metadata path selector. A child index is to match
     * if
     * <ul>
     * <li>the metadata path selector doesn’t specify an index and the index of
     * the last child is equal to {@code 0},</li>
     * <li>the metadata path selector specifies all children,</li>
     * <li>the metadata path selector exactly points to the given index, or</li>
     * <li>generically to the last element, and the given index is the last
     * index.</li>
     * </ul>
     *
     * @param childIndex
     *            index to check
     * @param lastChildIndex
     *            last available index
     * @return whether the index is to be matched
     */
    private boolean indexCheck(int childIndex, int lastChildIndex) {
        if (index == null && lastChildIndex == 0 || ALL_CHILDREN_SYMBOL.equals(index)) {
            return true;
        }
        if (index != null) {
            int compare = (int) index;
            return childIndex == compare || (compare == Integer.MAX_VALUE && childIndex == lastChildIndex);
        }
        return false;
    }

    /**
     * Returns the path segment this
     * metadata path selector is responsible to represent. Since the method is
     * called from the constructor it must not be overridden in subclasses.
     *
     * @param path
     *            path expression to parse
     * @return the path segment for this selector
     * @throws ConfigurationException
     *             if the path cannot be parsed
     */
    private String matchCurrentPathSegment(String path) throws ConfigurationException {
        Matcher metadataPathSplitter = METADATA_SPLIT_PATH_SCHEME.matcher(path);
        if (!metadataPathSplitter.find()) {
            throw new ConfigurationException(
                    "Cannot create metadata path selector: Path must contain path segment, but is: " + path);
        }
        return metadataPathSplitter.group(1);
    }

    /**
     * Returns a string that textually represents this MetadataPathSelector.
     *
     * @return a string representation of this MetadataPathSelector
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(40);
        result.append(METADATA_PATH_SEPARATOR);
        result.append(docStructType);
        if (index != null) {
            result.append('[');
            result.append(index.equals(Integer.MAX_VALUE) ? LAST_CHILD_QUANTIFIER : index.toString());
            result.append(']');
        }
        result.append(selector);
        return result.toString();
    }

    /**
     * Calculates whether the given child is to be
     * matched by type name by this metadata path selector. A child is to match
     * if
     * <ul>
     * <li>the metadata path selector specifies all children, or</li>
     * <li>the metadata path selector specifies exactly the type of the
     * child.</li>
     * </ul>
     *
     * @param child
     *            child whose type shall be checked
     * @return whether the child type is to be matched
     */
    private boolean typeCheck(LegacyDocStructHelperInterface child) {
        return ANY_METADATA_TYPE_SYMBOL.equals(docStructType) || docStructType.equals(child.getDocStructType().getName());
    }
}
