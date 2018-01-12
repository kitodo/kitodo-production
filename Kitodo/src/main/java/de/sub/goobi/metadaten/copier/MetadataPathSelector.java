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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedForParentException;
import org.kitodo.production.exceptions.UnreachableCodeException;

/**
 * A MetadataPathSelector provides methods to retrieve or modify document
 * structure nodes on a document structure node.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
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

    @SuppressWarnings("javadoc")
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
                if (index instanceof Integer && ((Integer) index).intValue() < 0) {
                    throw new ConfigurationException("Negative element count is not allowed, in path: " + path);
                }
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Cannot create metadata path selector: " + e.getMessage(), e);
            }
        } else {
            docStructType = pathSegment;
            index = null;
        }
        selector = super.create(path.substring(pathSegment.length() + 1));
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
     * Creates a metadatum with the given value if the full path is applied and
     * no such metadatum is already available under at the path. Leaves the
     * document structure element unchanged if such a metadatum already exists.
     * This works recursively, by calling itself on the subnode, if found, or
     * returning null otherwise. Metadata creation is, by definition, always
     * done in a {@link LocalMetadataSelector}.
     *
     * @param data
     *            data to work on
     * @param logicalNode
     *            document structure node to start from, intended for recursion
     * @param value
     *            value to write if no metadatum is available at the path’s end
     * @see de.sub.goobi.metadaten.copier.MetadataSelector#createIfPathExistsOnly(CopierData,
     *      DocStructInterface, String)
     */
    @Override
    protected void createIfPathExistsOnly(CopierData data, DocStructInterface logicalNode, String value) {
        DocStructInterface subnode = getSubnode(logicalNode);
        if (subnode == null) {
            return;
        }
        selector.createIfPathExistsOnly(data, subnode, value);
    }

    /**
     * Sets the metadatum identified by the given path if available, otherwise
     * creates the path and metadatum. This works recursively. Metadata creation
     * is done in a {@link LocalMetadataSelector}. If the DocStructType is set
     * to "*", no path will be created if no path exists.
     *
     * @param data
     *            data to work on
     * @param logicalNode
     *            document structure node to start from, intended for recursion
     * @param value
     *            value to write
     * @see de.sub.goobi.metadaten.copier.MetadataSelector#createOrOverwrite(CopierData,
     *      DocStructInterface, String)
     */
    @Override
    protected void createOrOverwrite(CopierData data, DocStructInterface logicalNode, String value) {
        DocStructInterface subnode = getSubnode(logicalNode);
        if (subnode == null && !ANY_METADATA_TYPE_SYMBOL.equals(docStructType)) {
            try {
                subnode = logicalNode.createChild(docStructType, data.getDigitalDocument(), data.getPreferences());
            } catch (TypeNotAllowedAsChildException e) {
                // copy rules aren’t related to the rule set but depend on it,
                // so copy rules that don’t work with the current rule set are
                // ignored
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot create structural element " + docStructType + " as child of "
                            + (logicalNode.getType() != null ? logicalNode.getType().getName() : "without type")
                            + " because it isn’t allowed by the rule set.");
                }
                return;
            } catch (TypeNotAllowedForParentException e) {
                // see https://github.com/kitodo/kitodo-ugh/issues/2
                throw new UnreachableCodeException("TypeNotAllowedForParentException is never thrown");
            } catch (Exception e) {
                // copy rule failed, skip it
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot create structural element " + docStructType + " as child of "
                            + (logicalNode.getType() != null ? logicalNode.getType().getName() : "without type")
                            + ": Accessing the rule set failed with exception: "
                            + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()), e);
                }
                return;
            }
        }
        if (subnode != null) {
            selector.createOrOverwrite(data, subnode, value);
        }
    }

    /**
     * The function findAll() returns all concrete metadata selectors the
     * potentially generic metadata selector expression resolves to.
     *
     * @param logicalNode
     *            Node of the logical document structure to work on
     * @return all metadata selectors the expression resolves to
     *
     * @see de.sub.goobi.metadaten.copier.MetadataSelector#findAll(ugh.dl.DocStruct)
     */
    @Override
    protected Iterable<MetadataSelector> findAll(DocStructInterface logicalNode) {
        LinkedList<MetadataSelector> result = new LinkedList<>();
        List<DocStructInterface> children = logicalNode.getAllChildren();
        if (children == null) {
            children = Collections.emptyList();
        }
        int lastChild = children.size() - 1;
        int count = 0;
        for (DocStructInterface child : children) {
            if (typeCheck(child) && indexCheck(count, lastChild)) {
                for (MetadataSelector cms : selector.findAll(child)) {
                    result.add(new MetadataPathSelector(ANY_METADATA_TYPE_SYMBOL, count, cms));
                }
            }
            count++;
        }
        return result;
    }

    /**
     * Returns the value of the metadatum named by the path used to construct
     * the metadata selector, or null if either the path or the metadatum at the
     * end of the path aren’t available. This works recursively, by calling
     * itself on the subnode, if found, or returning null otherwise.
     *
     * @see de.sub.goobi.metadaten.copier.MetadataSelector#findIn(ugh.dl.DocStruct)
     */
    @Override
    protected String findIn(DocStructInterface supernode) {
        DocStructInterface subnode = getSubnode(supernode);
        if (subnode == null) {
            return null;
        } else {
            return selector.findIn(subnode);
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
        if (index != null && index instanceof Integer) {
            int a = ((Integer) index).intValue();
            if (a < Integer.MAX_VALUE) {
                return a;
            }
        }
        return -1;
    }

    /**
     * The function getIndexValue() returns the numerical value represented by
     * the symbolic (String) representation passed in. Since the method is
     * called from the constructor it must not be overridden in subclasses.
     *
     * @param indexSymbol
     *            an integer value or ">" to refer to Integer.MAX_VALUE
     * @return the integer value of the string, or Integer.MAX_VALUE for the
     *         symbol ">".
     */
    private final Object getIndexValue(String indexSymbol) {
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
    private DocStructInterface getSubnode(DocStructInterface logicalNode) {
        List<DocStructInterface> children = logicalNode.getAllChildrenByTypeAndMetadataType(docStructType,
                ANY_METADATA_TYPE_SYMBOL);
        if (children == null) {
            children = Collections.emptyList();
        }
        switch (children.size()) {
            case 0:
                return null;
            case 1:
                if (index == null || index.equals(0) || index.equals(Integer.MAX_VALUE)) {
                    return children.get(0);
                }
        }

        if (index == null) {
            throw new RuntimeException(
                    "Could not resolve metadata path: Path selector is ambiguous for " + docStructType);
        } else if (!(index instanceof Integer)) {
            throw new RuntimeException(
                    "Could not resolve metadata path: In this regard, index \"" + index + "\" is not allowed.");
        } else if (index.equals(Integer.MAX_VALUE)) {
            return children.get(children.size() - 1);
        } else if (children.size() >= ((Integer) index).intValue()) {
            return children.get(((Integer) index).intValue());
        }

        return null;
    }

    /**
     * The function indexCheck() calculates whether the given child’s index is
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
        int comparee = ((Integer) index).intValue();
        if (childIndex == comparee || (comparee == Integer.MAX_VALUE && childIndex == lastChildIndex)) {
            return true;
        }
        throw new RuntimeException("Could not resolve metadata path: Path selector is ambiguous for " + docStructType);
    }

    /**
     * The function matchCurrentPathSegment() returns the path segment this
     * metadata path selector is responsible to represent. Since the method is
     * called from the constructor it must not be overridden in subclasses.
     *
     * @param path
     *            path expression to parse
     * @return the path segment for this selector
     * @throws ConfigurationException
     *             if the path cannot be parsed
     */
    private final String matchCurrentPathSegment(String path) throws ConfigurationException {
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
     * The function typeCheck() calculates whether the given child is to be
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
    private boolean typeCheck(DocStructInterface child) {
        return ANY_METADATA_TYPE_SYMBOL.equals(docStructType) || docStructType.equals(child.getType().getName());
    }
}
