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

import java.io.Serializable;
import java.util.Comparator;
import org.kitodo.api.ugh.DocStructType;
import org.kitodo.api.ugh.Metadata;
import org.kitodo.api.ugh.MetadataType;

public class HelperComparator implements Comparator<Object>, Serializable {

    private static final long serialVersionUID = -1124724462982810327L;
    private String sortType;

    @Override
    public int compare(Object firstObject, Object secondObject) {
        // TODO: Use a Enum or Integer conts
        int rueckgabe = 0;
        if (this.sortType.equals("MetadatenTypen")) {
            rueckgabe = compareMetadataTypes(firstObject, secondObject);
        }
        if (this.sortType.equals("Metadata")) {
            rueckgabe = compareMetadata(firstObject, secondObject);
        }
        if (this.sortType.equals("DocStructTypen")) {
            rueckgabe = compareDocStructTypes(firstObject, secondObject);
        }
        return rueckgabe;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    private int compareMetadataTypes(Object firstObject, Object secondObject) {
        MetadataType firstMetadata = (MetadataType) firstObject;
        MetadataType secondMetadata = (MetadataType) secondObject;
        String firstName = firstMetadata
                .getLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"));
        String secondName = secondMetadata
                .getLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"));
        if (firstName == null) {
            firstName = "";
        }
        if (secondName == null) {
            secondName = "";
        }
        return firstName.compareToIgnoreCase(secondName);
    }

    private int compareMetadata(Object firstObject, Object secondObject) {
        Metadata firstMetadata = (Metadata) firstObject;
        Metadata secondMetadata = (Metadata) secondObject;
        String firstName = firstMetadata.getType()
                .getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"));
        String secondName = secondMetadata.getType()
                .getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"));
        if (firstName == null) {
            firstName = firstMetadata.getType().getName();
        }
        if (secondName == null) {
            secondName = secondMetadata.getType().getName();
        }
        return firstName.compareToIgnoreCase(secondName);
    }

    private int compareDocStructTypes(Object firstObject, Object secondObject) {
        DocStructType firstDocStructType = (DocStructType) firstObject;
        DocStructType secondDocStructType = (DocStructType) secondObject;
        String firstName = firstDocStructType
                .getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"));
        String secondName = secondDocStructType
                .getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"));
        if (firstName == null) {
            firstName = "";
        }
        if (secondName == null) {
            secondName = "";
        }
        return firstName.compareToIgnoreCase(secondName);
    }

}
