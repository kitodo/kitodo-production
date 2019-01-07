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

import java.io.Serializable;
import java.util.Comparator;

import org.kitodo.production.enums.SortType;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyLogicalDocStructTypeHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataTypeHelper;
import org.kitodo.production.services.ServiceManager;

public class HelperComparator implements Comparator<Object>, Serializable {

    private static final long serialVersionUID = -1124724462982810327L;
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
        LegacyMetadataTypeHelper firstMetadata = (LegacyMetadataTypeHelper) firstObject;
        LegacyMetadataTypeHelper secondMetadata = (LegacyMetadataTypeHelper) secondObject;

        String language = ServiceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();

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
        LegacyMetadataHelper firstMetadata = (LegacyMetadataHelper) firstObject;
        LegacyMetadataHelper secondMetadata = (LegacyMetadataHelper) secondObject;

        String language = ServiceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();

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
        LegacyLogicalDocStructTypeHelper firstDocStructType = (LegacyLogicalDocStructTypeHelper) firstObject;
        LegacyLogicalDocStructTypeHelper secondDocStructType = (LegacyLogicalDocStructTypeHelper) secondObject;

        String language = ServiceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();

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
