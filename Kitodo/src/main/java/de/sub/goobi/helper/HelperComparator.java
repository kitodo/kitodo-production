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
import org.kitodo.enums.SortType;
import org.kitodo.services.ServiceManager;

public class HelperComparator implements Comparator<Object>, Serializable {

    private static final long serialVersionUID = -1124724462982810327L;
    private static ServiceManager serviceManager = new ServiceManager();
    private SortType sortType;

    @Override
    public int compare(Object firstObject, Object secondObject) {
        int result = 0;

        switch (sortType) {
            case DOC_STRUCT_TYPE:
                result = compareDocStructTypes(firstObject, secondObject);
                break;
            case METADATA:
                result = compareMetadata(firstObject, secondObject);
                break;
            case METADATA_TYPE:
                result = compareMetadataTypes(firstObject, secondObject);
                break;
            default:
                break;
        }

        return result;
    }

    public void setSortType(SortType sortType) {
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
