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

package org.kitodo.production.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class TreeNode {
    protected boolean expanded = false;
    protected boolean selected = false;
    protected String label;
    protected String id;
    protected List<TreeNode> children;
    private static final String IS_LAST = "islast";
    private static final String LEVEL = "niveau";
    private static final String NODE = "node";
    private static final String STROKE = "striche";

    public TreeNode() {
        this.children = new ArrayList<>();
    }

    /**
     * Constructor.
     *
     * @param expanded
     *            boolean
     * @param label
     *            String
     * @param id
     *            String
     */
    public TreeNode(boolean expanded, String label, String id) {
        this.expanded = expanded;
        this.label = label;
        this.id = id;
        this.children = new ArrayList<>();
    }

    public void addChild(TreeNode inNode) {
        this.children.add(inNode);
    }

    /**
     * Get children as list.
     *
     * @return list
     */
    public List<HashMap<String, Object>> getChildrenAsList() {
        return getChildrenAsListMitStrichen(new ArrayList<>(), 0, this, true, true, new ArrayList<>());
    }

    @SuppressWarnings({"unused", "unchecked", "rawtypes" })
    private List getChildrenAsList(List inList, int level, List strokes, boolean fatherIsLast) {
        for (Iterator<TreeNode> it = this.children.iterator(); it.hasNext();) {
            TreeNode kind = it.next();

            HashMap map = getValueMap(level, kind, !it.hasNext(), fatherIsLast, strokes);
            inList.add(map);

            if (kind.expanded && kind.getHasChildren()) {
                kind.getChildrenAsList(inList, level + 1, (List<Boolean>) map.get(STROKE), !it.hasNext());
            }
        }
        return inList;
    }

    /**
     * Get all children as list.
     *
     * @return List
     */
    public List<HashMap<String, Object>> getChildrenAsListAlle() {
        return getChildrenAsListAlle(new ArrayList<>(), 0, this, true, true, new ArrayList<>());
    }

    @SuppressWarnings({"unchecked"})
    private List<HashMap<String, Object>> getChildrenAsListAlle(List<HashMap<String, Object>> inList, int level,
            TreeNode inNode, boolean isLast, boolean fatherIsLast, List<Boolean> strokes) {
        HashMap<String, Object> map = getValueMap(level, inNode, isLast, fatherIsLast, strokes);
        inList.add(map);

        if (inNode.getHasChildren()) {
            for (Iterator<TreeNode> it = inNode.getChildren().iterator(); it.hasNext();) {
                TreeNode kind = it.next();
                getChildrenAsListAlle(inList, level + 1, kind, !it.hasNext(), isLast, (List<Boolean>) map.get(STROKE));
            }
        }
        return inList;
    }

    /**
     * alle Children des übergebenen Knotens expanden oder collapsen.
     */
    public void expandNodes(Boolean inExpand) {
        expandNode(this, inExpand);
    }

    private void expandNode(TreeNode inNode, boolean inExpand) {
        inNode.expanded = inExpand;
        for (TreeNode treeNode : inNode.children) {
            expandNode(treeNode, inExpand);
        }
    }

    @SuppressWarnings({"unchecked"})
    private List<HashMap<String, Object>> getChildrenAsListMitStrichen(List<HashMap<String, Object>> inList, int level,
            TreeNode inNode, boolean isLast, boolean fatherIsLast, List<Boolean> strokes) {

        HashMap<String, Object> map = getValueMap(level, inNode, isLast, fatherIsLast, strokes);
        inList.add(map);

        if (inNode.getHasChildren() && inNode.expanded) {
            for (Iterator<TreeNode> it = inNode.getChildren().iterator(); it.hasNext();) {
                TreeNode kind = it.next();
                getChildrenAsListMitStrichen(inList, level + 1, kind, !it.hasNext(), isLast, (List<Boolean>) map.get(STROKE));
            }
        }
        return inList;
    }

    private HashMap<String, Object> getValueMap(int level, TreeNode node, boolean isLast, boolean fatherIsLast, List<Boolean> strokes) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(NODE, node);
        map.put(LEVEL, level);
        map.put(IS_LAST, isLast);

        // prepare strokes
        List<Boolean> duplicatedStrokes = new ArrayList<>(strokes);
        duplicatedStrokes.add(fatherIsLast);
        map.put(STROKE, duplicatedStrokes);

        return map;
    }

    /*
     * Getter und Setter
     */
    public List<TreeNode> getChildren() {
        return this.children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get has children.
     *
     * @return boolean
     */
    public boolean getHasChildren() {
        return Objects.nonNull(this.children) && !this.children.isEmpty();
    }
}
