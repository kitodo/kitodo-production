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

package org.kitodo.production.helper.metadata.legacytypeimplementations;

import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;

/**
 * Represents a legacy metadata. This is a soldering class to keep legacy code
 * operational which is about to be removed. Do not use this class.
 */
public class LegacyMetadataHelper {

    /**
     * The legacy type of the legacy metadata.
     */
    private LegacyMetadataTypeHelper type;

    /**
     * The value of the legacy metadata.
     */
    private String value;

    /**
     * The legacy doc struct of the legacy metadata.
     */
    private LegacyInnerPhysicalDocStructHelper legacyInnerPhysicalDocStructHelper;

    private BindingSaveInterface bindingSaver;

    private MetadataEntry binding;

    private Domain domain;

    LegacyMetadataHelper(LegacyInnerPhysicalDocStructHelper legacyInnerPhysicalDocStructHelper,
            LegacyMetadataTypeHelper type, String value) {
        this.type = type;
        this.value = value;
        this.legacyInnerPhysicalDocStructHelper = legacyInnerPhysicalDocStructHelper;
    }

    @Deprecated
    public LegacyMetadataHelper(LegacyMetadataTypeHelper type) {
        this.type = type;
        this.value = "";
    }

    @Deprecated
    public MetadataEntry getBinding() {
        return binding;
    }

    @Deprecated
    public Domain getDomain() {
        return domain;
    }

    @Deprecated
    public LegacyMetadataTypeHelper getMetadataType() {
        return type;
    }

    @Deprecated
    public String getValue() {
        return value;
    }

    /**
     * This allows the metadata to be saved.
     */
    public void saveToBinding() {
        bindingSaver.saveMetadata(this);
    }

    /**
     * Sets the binding to a metadata XML access interface through a binding
     * save interface. This is needed so that the metadata in its container can
     * automatically save itself if its value is subsequently changed. In fact,
     * the value may be, aside from the value of a metadata entry, the value of
     * a field of the container, which makes the matter a bit unwieldy.
     *
     * @param bindingSaver
     *            thee binding save interface via which the metadata can
     *            automatically save itself afterwards
     * @param binding
     *            the metadata entry where the value should be stored, if
     *            applicable
     * @param domain
     *            the domain where the metadata entry is stored
     */
    public void setBinding(BindingSaveInterface bindingSaver, MetadataEntry binding, Domain domain) {
        this.bindingSaver = bindingSaver;
        this.binding = binding;
        this.domain = domain;
    }

    /**
     * Sets the document structure entity to which this object belongs to.
     *
     * @param docStruct
     *            document structure entity to which this object belongs
     */
    @Deprecated
    public void setDocStruct(LegacyDocStructHelperInterface docStruct) {
        if (docStruct instanceof LegacyInnerPhysicalDocStructHelper) {
            this.legacyInnerPhysicalDocStructHelper = (LegacyInnerPhysicalDocStructHelper) docStruct;
        }
    }

    /**
     * Set string value.
     *
     * @param value String value
     */
    @Deprecated
    public void setStringValue(String value) {
        this.value = value;
        if (bindingSaver != null) {
            saveToBinding();
        }
    }
}
