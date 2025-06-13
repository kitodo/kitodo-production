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

import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.ListColumn;
import org.kitodo.production.services.ServiceManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListColumnServiceTest {

    @Test
    public void shouldOmitRoleClientListColumn() {
        List<ListColumn> listColumns = new ArrayList<>();
        ListColumn firstColumn = new ListColumn();
        firstColumn.setTitle("firstColumn");
        listColumns.add(firstColumn);
        ListColumn secondColumn = new ListColumn();
        secondColumn.setTitle("secondColumn");
        listColumns.add(secondColumn);
        ListColumn thirdColumn = new ListColumn();
        thirdColumn.setTitle("thirdColumn");
        listColumns.add(thirdColumn);
        assertEquals(3,  listColumns.size(), "Wrong number of list columns before removing column by column title");
        listColumns = ServiceManager.getListColumnService().removeColumnByTitle(listColumns, "thirdColumn");
        assertEquals(2, listColumns.size(), "Wrong number of list columns after removing column by column title");
    }
}
