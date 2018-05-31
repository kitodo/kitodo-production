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

import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.services.ServiceManager;

public class HelperComparator implements Comparator<Object>, Serializable {

    private static final long serialVersionUID = -1124724462982810327L;
    private static ServiceManager serviceManager = new ServiceManager();
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

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    private int compareMetadataTypes(Object firstObject, Object secondObject) {
        MetadataTypeInterface firstMetadata = (MetadataTypeInterface) firstObject;
        MetadataTypeInterface secondMetadata = (MetadataTypeInterface) secondObject;

        String language = serviceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();

        String firstName = firstMetadata.getLanguage(language);
        String secondName = secondMetadata.getLanguage(language);
        if (firstName == null) {
            firstName = "";
        }
        if (secondName == null) {
            secondName = "";
        }
        return firstName.compareToIgnoreCase(secondName);
    }

    private int compareMetadata(Object firstObject, Object secondObject) {
        MetadataInterface firstMetadata = (MetadataInterface) firstObject;
        MetadataInterface secondMetadata = (MetadataInterface) secondObject;

        String language = serviceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();

        String firstName = firstMetadata.getMetadataType().getNameByLanguage(language);
        String secondName = secondMetadata.getMetadataType().getNameByLanguage(language);
        if (firstName == null) {
            firstName = firstMetadata.getMetadataType().getName();
        }
        if (secondName == null) {
            secondName = secondMetadata.getMetadataType().getName();
        }
        return firstName.compareToIgnoreCase(secondName);
    }

    private int compareDocStructTypes(Object firstObject, Object secondObject) {
        DocStructTypeInterface firstDocStructType = (DocStructTypeInterface) firstObject;
        DocStructTypeInterface secondDocStructType = (DocStructTypeInterface) secondObject;

        String language = serviceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();

        String firstName = firstDocStructType.getNameByLanguage(language);
        String secondName = secondDocStructType.getNameByLanguage(language);
        if (firstName == null) {
            firstName = "";
        }
        if (secondName == null) {
            secondName = "";
        }
        return firstName.compareToIgnoreCase(secondName);
    }

}
