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

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Display settings for the edit mask related to a specific key.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Setting {
    /**
     * Key whose representation is influenced.
     */
    @XmlAttribute(required = true)
    private String key;

    /**
     * A field for a value for this key is always displayed in the form, even if
     * there is no value for it yet. Normally, only fields are displayed for
     * which there are values or which the user has additionally selected by
     * hand. In this way you do not have to create frequently needed fields
     * every time by hand.
     */
    @XmlAttribute
    private Boolean alwaysShowing;

    /**
     * Whether the field can be edited. By default this is the case. Here
     * editing can be prohibited for individual fields, which are then only
     * displayed; if in a field e.g. work instructions are available. In
     * particular, in combination with acquisition stages, the function makes
     * sense that a field cannot be changed in a particular acquisition stage,
     * or can only be changed in a specific one. It’s not supposed to bring the
     * editor into a read-only mode, this should be possible in other ways.
     */
    @XmlAttribute
    private Boolean editable;

    /**
     * This will present an input filter for the list of options in select fields if set to true. When entering text,
     * the filter will display only those options that contain the entered text. This attribute exclusively works with
     * keys that have an option list; for other keys, no action will occur.
     */
    @XmlAttribute
    private Boolean filterable;

    /**
     * This will hide a field, even if a value has been entered for this field.
     * Normally, there are rules in the ruleset that say which fields are
     * allowed, but if data is in fields, they will still be displayed, even if
     * the field is not allowed. With this switch you can hide the fields. The
     * application must take care that it retains the data anyway and does not
     * forget, even if they do not run over the input mask.
     */
    @XmlAttribute
    private Boolean excluded;

    /**
     * This can be used to request a larger input field. Normally, the display
     * consists of one-line input fields, and that's usually enough. But if
     * there is a lot of text in a field, for example the abstract of a
     * scientific article, or an e-mail body, then you can switch to a larger
     * field here.
     */
    @XmlAttribute
    private Boolean multiline;

    /**
     * This will specify how to update metadata in repeated imports.
     */
    @XmlAttribute
    private Reimport reimport;

    /**
     * The settings for sub-keys.
     */
    @XmlElement(name = "setting", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<Setting> settings = new LinkedList<>();

    /**
     * Returns the value “always showing” if one is set. This getter returns
     * {@code null} if the attribute was not entered. This is needed, for
     * example, when merging attributes. If only the simple value (with default,
     * if no value was specified) is needed, use {@link #isAlwaysShowing()}.
     * 
     * @return the value “always showing”, if set, else {@code null}
     */
    public Boolean getAlwaysShowing() {
        return alwaysShowing;
    }

    /**
     * Returns the value “editable” if one is set. This getter returns
     * {@code null} if the attribute was not entered. This is needed, for
     * example, when merging attributes. If only the simple value (with default,
     * if no value was specified) is needed, use {@link #isEditable()}.
     * 
     * @return the value “editable”, if set, else {@code null}
     */
    public Boolean getEditable() {
        return editable;
    }

    /**
     * Returns the value “excluded” if one is set. This getter returns
     * {@code null} if the attribute was not entered. This is needed, for
     * example, when merging attributes. If only the simple value (with default,
     * if no value was specified) is needed, use {@link #isExcluded()}.
     * 
     * @return the value “excluded”, if set, else {@code null}
     */
    public Boolean getExcluded() {
        return excluded;
    }

    /**
     * Returns the value “filterable” if one is set. This getter returns
     * {@code null} if the attribute was not entered. This is needed, for
     * example, when merging attributes. If only the simple value (with default,
     * if no value was specified) is needed, use {@link #isFilterable()}.
     *
     * @return the value “filterable”, if set, else {@code null}
     */
    public Boolean getFilterable() {
        return filterable;
    }

    /**
     * Returns the key whose representation is influenced.
     * 
     * @return the key whose representation is influenced
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the value “multiline” if one is set. This getter returns
     * {@code null} if the attribute was not entered. This is needed, for
     * example, when merging attributes. If only the simple value (with default,
     * if no value was specified) is needed, use {@link #isMultiline()}.
     * 
     * @return the value “multiline”, if set, else {@code null}
     */
    public Boolean getMultiline() {
        return multiline;
    }

    /**
     * Returns the value “reimport” if one is set. This getter returns
     * {@code null} if the attribute was not entered. This is needed, for
     * example, when merging attributes.
     * 
     * @return the value “excluded”, if set, else {@code null}
     */
    public Reimport getReimport() {
        return reimport;
    }

    /**
     * Returns the editor settings.
     *
     * @return the editor settings
     */
    public List<Setting> getSettings() {
        return settings;
    }

    /**
     * Returns the “always showing” value or otherwise the default value if the
     * attribute is not set.
     * 
     * @return the “always showing” value or its default value
     */
    public boolean isAlwaysShowing() {
        return alwaysShowing != null ? alwaysShowing : false;
    }

    /**
     * Returns the “editable” value or otherwise the default value if the
     * attribute is not set.
     * 
     * @return the “editable” value or its default value
     */
    public boolean isEditable() {
        return editable != null ? editable : true;
    }

    /**
     * Returns the “filterable” value or otherwise the default value if the
     * attribute is not set.
     *
     * @return the “filterable” value or its default value
     */
    public boolean isFilterable() {
        return filterable != null ? filterable : false;
    }

    /**
     * Returns the “excluded” value or otherwise the default value if the
     * attribute is not set.
     * 
     * @return the “excluded” value or its default value
     */
    public boolean isExcluded() {
        return excluded != null ? excluded : false;
    }

    /**
     * Returns the “multiline” value or otherwise the default value if the
     * attribute is not set.
     * 
     * @return the “multiline” value or its default value
     */
    public boolean isMultiline() {
        return multiline != null ? multiline : false;
    }

    /**
     * This sets the “always showing” value. If you set the value to
     * {@code null}, no attribute is written.
     * 
     * @param alwaysShowing
     *            “always showing” value to set
     */
    public void setAlwaysShowing(Boolean alwaysShowing) {
        this.alwaysShowing = alwaysShowing;
    }

    /**
     * This sets the “editable” value. If you set the value to {@code null}, no
     * attribute is written.
     * 
     * @param editable
     *            “editable” value to set
     */
    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    /**
     * This sets the “filterable” value. If you set the value to {@code null}, no
     * attribute is written.
     *
     * @param filterable
     *            “filterable” value to set
     */
    public void setFilterable(Boolean filterable) {
        this.filterable = filterable;
    }

    /**
     * This sets the “excluded” value. If you set the value to {@code null}, no
     * attribute is written.
     * 
     * @param excluded
     *            “excluded” value to set
     */
    public void setExcluded(Boolean excluded) {
        this.excluded = excluded;
    }

    /**
     * Sets the key whose representation is influenced.
     * 
     * @param key
     *            the key whose representation is influenced
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * This sets the “multi-line” value. If you set the value to {@code null},
     * no attribute is written.
     * 
     * @param multiline
     *            “multi-line” value to set
     */
    public void setMultiline(Boolean multiline) {
        this.multiline = multiline;
    }

    /**
     * This sets the “reimport” value. If you set the value to {@code null}, no
     * attribute is written.
     * 
     * @param reimport
     *            “reimport” value to set
     */
    public void setReimport(Reimport reimport) {
        this.reimport = reimport;
    }

    /**
     * Sets the settings for nested keys.
     * 
     * @param settings
     *            the settings for nested keys
     */
    public void setSettings(List<Setting> settings) {
        this.settings = settings;
    }
}
