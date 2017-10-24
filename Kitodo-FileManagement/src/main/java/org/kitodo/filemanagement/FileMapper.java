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

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.kitodo.config.Config;

/**
 * Class for performing mapping and unmapping of URIs.
 */
public class FileMapper {

    /**
     * Execute right mapping type according to value of enum MappingType.
     *
     * @param uri
     *            to map
     * @return mapped URI
     */
    URI mapAccordingToMappingType(URI uri) {
        if (uri != null) {
            if (uri.toString().contains("css")) {
                return mapUriToKitodoRootFolderUri(uri);
            }
            return mapUriToKitodoDataDirectoryUri(uri);
        }
        return mapUriToKitodoDataDirectoryUri(null);
    }

    /**
     * Execute right unpmapping type according to value of enum MappingType.
     *
     * @param uri
     *            to unamp
     * @return unmapped URI
     */
    URI unmapAccordingToMappingType(URI uri) {
        if (uri.toString().contains(".css")) {
            return unmapUriFromKitodoRootFolderUri(uri);
        } else {
            return unmapUriFromKitodoDataDirectoryUri(uri);
        }
    }

    /**
     * Map resource to its absolute path inside the Kitodo root folder.
     *
     * @param uri
     *            directory or file to map eg. css file
     * @return absolute path to mapped resource
     */
    private URI mapUriToKitodoRootFolderUri(URI uri) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        if (uri == null) {
            return Paths.get(session.getServletContext().getContextPath()).toUri();
        } else  {
            return Paths.get(session.getServletContext().getContextPath(), uri.toString()).toUri();
        }
    }

    /**
     * Map relative URI to absolute kitodo data directory URI.
     *
     * @param uri
     *            relative path
     * @return absolute URI path
     */
    private URI mapUriToKitodoDataDirectoryUri(URI uri) {
        String kitodoDataDirectory = Config.getKitodoDataDirectory();
        if (uri == null) {
            return Paths.get(Config.getKitodoDataDirectory()).toUri();
        } else {
            if (!uri.isAbsolute() && !uri.toString().contains(kitodoDataDirectory)) {
                return Paths.get(Config.getKitodoDataDirectory(), uri.toString()).toUri();
            }
        }
        return uri;
    }

    private URI unmapUriFromKitodoRootFolderUri(URI uri) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String directory;
        if (uri == null) {
            directory = session.getServletContext().getContextPath();
        } else {
            directory = session.getServletContext().getRealPath(uri.toString());
        }
        return unmapDirectory(uri, directory);
    }

    private URI unmapUriFromKitodoDataDirectoryUri(URI uri) {
        return unmapDirectory(uri, Config.getKitodoDataDirectory());
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
