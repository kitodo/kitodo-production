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

package org.kitodo.data.database.persistence;

import java.util.List;

import org.kitodo.data.database.beans.DataEditorSetting;
import org.kitodo.data.database.exceptions.DAOException;

public class DataEditorSettingDAO extends BaseDAO<DataEditorSetting> {

    @Override
    public DataEditorSetting getById(Integer dataEditorSettingId) throws DAOException {
        DataEditorSetting dataEditorSetting = retrieveObject(DataEditorSetting.class, dataEditorSettingId);
        if (dataEditorSetting == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return dataEditorSetting;
    }

    @Override
    public List<DataEditorSetting> getAll() throws DAOException {
        return retrieveAllObjects(DataEditorSetting.class);
    }

    @Override
    public List<DataEditorSetting> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM DataEditorSetting ORDER BY id ASC", offset, size);
    }

    @Override
    public List<DataEditorSetting> getAllNotIndexed(int offset, int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Integer dataEditorSettingId) throws DAOException {
        removeObject(DataEditorSetting.class, dataEditorSettingId);
    }
}
