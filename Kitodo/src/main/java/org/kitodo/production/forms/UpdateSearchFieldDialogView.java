/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.production.forms;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.kitodo.data.database.beans.SearchField;

@Named
@ViewScoped
public class UpdateSearchFieldDialogView implements Serializable {

    private SearchField searchField;

    private int searchFieldIndex;

    public UpdateSearchFieldDialogView() {

    }

    /**
     * Get searchField.
     *
     * @return value of searchField
     */
    public SearchField getSearchField() {
        return searchField;
    }

    /**
     * Set searchField.
     *
     * @param searchField as org.kitodo.data.database.beans.SearchField
     */
    public void setSearchField(SearchField searchField) {
        this.searchField = searchField;
        searchFieldIndex = searchField.getImportConfiguration().getSearchFields().indexOf(searchField);
    }

    /**
     * Get searchFieldIndex.
     *
     * @return value of searchFieldIndex
     */
    public int getSearchFieldIndex() {
        return searchFieldIndex;
    }

    /**
     * Set searchFieldIndex.
     *
     * @param searchFieldIndex as int
     */
    public void setSearchFieldIndex(int searchFieldIndex) {
        this.searchFieldIndex = searchFieldIndex;
    }
}
