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

package org.kitodo.dataformat.access;


import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.mets.AreaXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;

/**
 * A view on a media unit. The individual levels of the {@link DivXmlElementAccess} refer
 * to {@code View}s on {@link FileXmlElementAccess}s. At the moment, each {@code View}
 * refers to exactly one {@code MediaUnit} as a whole. This concept level has
 * been added here in order to be able to expand it in the future in order to be
 * able to refer to individual parts of a {@code MediaUnit}.
 */
public class AreaXmlElementAccess implements AreaXmlElementAccessInterface {

    /**
     * The data object of this area XML element access.
     */
    private final View view = new View();

    /**
     * Creates a new view. This constructor can be called via the service loader
     * to create a new view.
     */
    public AreaXmlElementAccess() {
    }

    /**
     * Creates a new view that points to a media device. This constructor is
     * called module-internally when loading a METS file.
     * 
     * @param fileXmlElementAccess
     *            media unit in view
     */
    AreaXmlElementAccess(FileXmlElementAccess fileXmlElementAccess) {
        view.setMediaUnit(fileXmlElementAccess.getMediaUnit());
    }

    @Override
    public FileXmlElementAccessInterface getFile() {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    View getView() {
        return view;
    }

    @Override
    public void setFile(FileXmlElementAccessInterface file) {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }
}
