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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.ListColumn;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ListColumnDAO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class ListColumnService extends SearchDatabaseService<ListColumn, ListColumnDAO> {

    private static volatile ListColumnService instance = null;

    /**
     * Constructor necessary to use searcher in child classes.
     */
    private ListColumnService() {
        super(new ListColumnDAO());
    }

    /**
     * Return singleton variable of type ListColumnService.
     *
     * @return unique instance of ListColumnService
     */
    public static ListColumnService getInstance() {
        ListColumnService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (ListColumnService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new ListColumnService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public List loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM ListColumn");
    }

    @Override
    public Long countResults(Map filters) {
        throw new UnsupportedOperationException();
    }


    /**
     * Retrieve and return all list columns available for list configuration of list with provided name 'listTitle'.
     *
     * @param listTitle name of the list for which all available list columns are returned
     * @return
     *          SelectItemGroup containing available list columns for list 'listTitle'
     * @throws DAOException thrown when list columns cannot be retrieved from database
     */
    public SelectItemGroup getListColumnsForListAsSelectItemGroup(String listTitle) throws DAOException {
        SelectItemGroup itemGroup = new SelectItemGroup(Helper.getTranslation(listTitle));

        itemGroup.setSelectItems(getAll().stream()
                .filter(listColumn -> listColumn.getTitle().startsWith(listTitle + "."))
                .map(listColumn -> new SelectItem(listColumn,
                        Helper.getTranslation(listColumn.getTitle().replace(listTitle + ".", ""))))
                .toArray(SelectItem[]::new));

        return itemGroup;
    }

    /**
     * Retrieve and return the list of list column names for the current client and list with
     * provided name 'listTitle'.
     *
     * @param listTitle the title of the list for which list columns are returned
     * @return
     *          a String array containing the list column titles saved for the current client
     */
    public List<ListColumn> getSelectedListColumnsForListAndClient(String listTitle) {
        Client client = ServiceManager.getUserService().getSessionClientOfAuthenticatedUser();
        if (Objects.nonNull(client)) {
            List<ListColumn> clientColumns = client.getListColumns();
            return clientColumns.stream()
                    .filter(listColumn -> listColumn.getTitle().startsWith(listTitle + "."))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Save given list of selected list columns to client of currently authenticated user.
     * @param columns list columns
     * @throws Exception thrown when prefix cannot be determined from given column names
     */
    public void saveSelectedColumnsToClient(List<ListColumn> columns) throws Exception {
        Client client = ServiceManager.getUserService().getSessionClientOfAuthenticatedUser();

        HashSet<ListColumn> allClientColumns = new HashSet<>(client.getListColumns());
        HashSet<ListColumn> currentListClientColumns = new HashSet<>();

        for (String prefix : getPrefixesFromListColumns(columns)) {
            currentListClientColumns.addAll(getSelectedListColumnsForListAndClient(prefix));
        }

        allClientColumns.removeAll(currentListClientColumns);
        allClientColumns.addAll(columns);

        client.setListColumns(new ArrayList<>(allClientColumns));

        ServiceManager.getClientService().saveToDatabase(client);
    }

    private ArrayList<String> getPrefixesFromListColumns(List<ListColumn> columns) throws Exception {
        LinkedHashSet<String> prefixes = new LinkedHashSet<>();
        String[] columnNameParts;
        for (ListColumn column : columns) {
            columnNameParts = column.getTitle().split("[.]");
            if (columnNameParts.length > 0) {
                prefixes.add(columnNameParts[0]);
            } else {
                throw new Exception("Unable to find prefix of column name '" + column.getTitle() + "!");
            }
        }
        return new ArrayList<>(prefixes);
    }

    /**
     * Retrieve and return all standard list columns from the database.
     * @return list of standard listColumns
     */
    public List<ListColumn> getAllStandardListColumns() {
        return dao.getAllStandard();
    }

    /**
     * Retrieve and return all custom list columns from the database.
     * @return list of custom listColumns
     */
    public List<ListColumn> getAllCustomListColumns() {
        return dao.getAllCustom();
    }

    /**
     * Remove custom list columns from database.
     *
     * @param excludeList String array containing the names of custom columns that are not removed
     */
    public void removeCustomListColumns(List<String> excludeList) throws DAOException {
        // don't remove columns whose titles are in the given excludeList
        List<ListColumn> customColumns = dao.getAllCustom().stream()
                .filter(column -> !excludeList.contains(column.getTitle()))
                .collect(Collectors.toList());

        // remove remaining custom columns from clients
        for (Client client : ServiceManager.getClientService().getAll()) {
            for (ListColumn cc : customColumns) {
                client.getListColumns().remove(cc);
            }
            ServiceManager.getClientService().saveToDatabase(client);
        }

        // remove custom columns themselves
        List<Integer> columnIds = customColumns.stream()
                .map(ListColumn::getId)
                .collect(Collectors.toList());
        for (int id : columnIds) {
            removeFromDatabase(id);
        }
    }
}
