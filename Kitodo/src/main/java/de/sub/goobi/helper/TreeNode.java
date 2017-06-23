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

package de.sub.goobi.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TreeNode {
    protected boolean expanded = false;
    protected boolean selected = false;
    protected String label;
    protected String id;
    protected List<TreeNode> children;

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
        List<HashMap<String, Object>> myList = new ArrayList<>();
        getChildrenAsListMitStrichen(myList, 0, this, true, true, new ArrayList<>());
        return myList;
    }

    @SuppressWarnings({"unused", "unchecked", "rawtypes" })
    private List getChildrenAsList(List inList, int niveau, List inStriche, boolean vaterIstLetzter) {
        for (Iterator<TreeNode> it = this.children.iterator(); it.hasNext();) {
            TreeNode kind = it.next();
            HashMap map = new HashMap();
            map.put("node", kind);
            map.put("niveau", niveau);
            map.put("islast", !it.hasNext());

            // die Striche vorbereiten
            List striche = new ArrayList(inStriche);
            striche.add(vaterIstLetzter);
            map.put("striche", striche);

            inList.add(map);
            if (kind.expanded && kind.getHasChildren()) {
                kind.getChildrenAsList(inList, niveau + 1, striche, !it.hasNext());
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
        List<HashMap<String, Object>> myList = new ArrayList<>();
        getChildrenAsListAlle(myList, 0, this, true, true, new ArrayList<>());
        return myList;
    }

    private List<HashMap<String, Object>> getChildrenAsListAlle(List<HashMap<String, Object>> inList, int niveau,
            TreeNode inNode, boolean istLetzter, boolean vaterIstLetzter, List<Boolean> inStriche) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("node", inNode);
        map.put("niveau", niveau);
        map.put("islast", istLetzter);

        // die Striche vorbereiten
        List<Boolean> striche = new ArrayList<>(inStriche);
        striche.add(vaterIstLetzter);
        map.put("striche", striche);

        inList.add(map);

        if (inNode.getHasChildren()) {
            for (Iterator<TreeNode> it = inNode.getChildren().iterator(); it.hasNext();) {
                TreeNode kind = it.next();
                getChildrenAsListAlle(inList, niveau + 1, kind, !it.hasNext(), istLetzter, striche);
            }
        }
        return inList;
    }

    /**
     * alle Children des übergebenen Knotens expanden oder collapsen.
     */
    public void expandNodes(Boolean inExpand) {
        expandNode(this, inExpand.booleanValue());
    }

    private void expandNode(TreeNode inNode, boolean inExpand) {
        inNode.expanded = inExpand;
        for (Iterator<TreeNode> iter = inNode.children.iterator(); iter.hasNext();) {
            TreeNode t = iter.next();
            expandNode(t, inExpand);
        }
    }

    private List<HashMap<String, Object>> getChildrenAsListMitStrichen(List<HashMap<String, Object>> inList, int niveau,
            TreeNode inNode, boolean istLetzter, boolean vaterIstLetzter, List<Boolean> inStriche) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("node", inNode);
        map.put("niveau", niveau);
        map.put("islast", istLetzter);

        // die Striche vorbereiten
        List<Boolean> striche = new ArrayList<>(inStriche);
        striche.add(vaterIstLetzter);
        map.put("striche", striche);

        inList.add(map);

        if (inNode.getHasChildren() && inNode.expanded) {
            for (Iterator<TreeNode> it = inNode.getChildren().iterator(); it.hasNext();) {
                TreeNode kind = it.next();
                getChildrenAsListMitStrichen(inList, niveau + 1, kind, !it.hasNext(), istLetzter, striche);
            }
        }
        return inList;
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
        return this.children != null && !this.children.isEmpty();
    }

}
