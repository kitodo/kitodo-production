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

package org.kitodo.metadata.comparator;

import de.sub.goobi.config.ConfigCore;

import java.net.URI;
import java.util.Comparator;

import org.kitodo.exceptions.NotImplementedException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class MetadataImageComparator implements Comparator<Object> {

    private FileService fileService = new ServiceManager().getFileService();

    @Override
    public int compare(Object firstObject, Object secondObject) {
        String firstName;
        String secondName;

        if (firstObject instanceof String && secondObject instanceof String) {
            firstName = (String) firstObject;
            secondName = (String) secondObject;

            firstName = firstName.substring(0, firstName.lastIndexOf('.'));
            secondName = secondName.substring(0, secondName.lastIndexOf('.'));
        } else if (firstObject instanceof URI && secondObject instanceof URI) {
            URI firstUri = (URI) firstObject;
            URI secondUri = (URI) secondObject;

            firstName = fileService.getFileName(firstUri);
            secondName = fileService.getFileName(secondUri);
        } else {
            throw new NotImplementedException();
        }

        return compareImages(firstName, secondName);
    }

    private int compareImages(String firstName, String secondName) {
        String imageSorting = ConfigCore.getParameter("ImageSorting", "number");

        if (imageSorting.equalsIgnoreCase("number")) {
            try {
                Integer firstIterator = Integer.valueOf(firstName);
                Integer secondIterator = Integer.valueOf(secondName);
                return firstIterator.compareTo(secondIterator);
            } catch (NumberFormatException e) {
                return firstName.compareToIgnoreCase(secondName);
            }
        } else if (imageSorting.equalsIgnoreCase("alphanumeric")) {
            return firstName.compareToIgnoreCase(secondName);
        } else {
            return firstName.compareToIgnoreCase(secondName);
        }
    }
}
