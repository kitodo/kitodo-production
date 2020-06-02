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

package org.kitodo.production.services.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.FolderDAO;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class FolderService extends SearchDatabaseService<Folder, FolderDAO> {

    /**
     * Creates a new folder service.
     */
    public FolderService() {
        super(new FolderDAO());
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Process");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        return countDatabaseRows();
    }

    @Override
    public List<Folder> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return new ArrayList<>();
    }

    /**
     * Returns the canonical part of the file name for a given media unit.
     *
     * @param project
     *            project to which the process belongs
     * @param mediaUnit
     *            Media unit for which the canonical part of the file name
     *            should be returned
     * @return the canonical part of the file name
     */
    public static String getCanonical(Process process, MediaUnit mediaUnit) {
        for (Entry<MediaVariant, URI> entry : mediaUnit.getMediaFiles().entrySet()) {
            for (Folder folder : process.getProject().getFolders()) {
                if (Objects.equals(folder.getFileGroup(), entry.getKey().getUse())) {
                    Subfolder subfolder = new Subfolder(process, folder);
                    String canonical = subfolder.getCanonical(entry.getValue());
                    if (canonical != null) {
                        return canonical;
                    }
                }
            }
        }
        return null;
    }
}
