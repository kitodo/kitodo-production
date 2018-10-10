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

package org.kitodo.filemanagement;

import java.net.URI;
import java.nio.file.Paths;

import org.kitodo.config.KitodoConfig;

/**
 * Class for performing mapping and unmapping of URIs.
 */
class FileMapper {

    /**
     * Map relative URI to absolute kitodo data directory URI.
     *
     * @param uri
     *            relative path
     * @return absolute URI path
     */
    URI mapUriToKitodoDataDirectoryUri(URI uri) {
        String kitodoDataDirectory = KitodoConfig.getKitodoDataDirectory();
        if (uri == null) {
            return Paths.get(KitodoConfig.getKitodoDataDirectory()).toUri();
        } else {
            if (!uri.isAbsolute() && !uri.getRawPath().contains(kitodoDataDirectory)) {
                return Paths.get(KitodoConfig.getKitodoDataDirectory(), uri.getRawPath()).toUri();
            }
        }
        return uri;
    }

    URI unmapUriFromKitodoDataDirectoryUri(URI uri) {
        return unmapDirectory(uri, KitodoConfig.getKitodoDataDirectory());
    }

    private URI unmapDirectory(URI uri, String directory) {
        String path = uri.toString();
        directory = encodeDirectory(directory);
        if (path.contains(directory)) {
            String[] split = path.split(directory);
            String shortUri = split[1];
            return URI.create(shortUri);
        }
        return uri;
    }

    private String encodeDirectory(String directory) {
        if (directory.contains("\\")) {
            directory = directory.replace("\\", "/");
        }
        return directory;
    }
}
