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

package de.sub.goobi.forms;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;

import java.io.Serializable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;

public class BasisForm implements Serializable {
    private static final Logger logger = LogManager.getLogger(BasisForm.class);
    private static final long serialVersionUID = 2950419497162710096L;
    private transient ServiceManager serviceManager = new ServiceManager();
    protected Page page;
    protected String zurueck = null;
    protected String filter = "";
    protected User user;
    protected String sortierung = "prozessAsc";
    static final String redirectParameter = "faces-redirect=true";

    private LazyDTOModel lazyDTOModel = null;

    /**
     * Getter: return lazyDTOModel.
     *
     * @return LazyDTOModel
     */
    public LazyDTOModel getLazyDTOModel() {
        return lazyDTOModel;
    }

    /**
     * Setter: set lazyDTOModel.
     *
     * @param lazyDTOModel
     *            LazyDTOModel to set for this class
     */
    public void setLazyDTOModel(LazyDTOModel lazyDTOModel) {
        this.lazyDTOModel = lazyDTOModel;
    }

    public Page getPage() {
        return this.page;
    }

    public String getZurueck() {
        return this.zurueck;
    }

    public void setZurueck(String zurueck) {
        this.zurueck = zurueck;
    }

    /**
     * Get User.
     *
     * @return User
     */
    public User getUser() {
        if (this.user == null) {
            try {
                this.user = serviceManager.getUserService().getAuthenticatedUser();
            } catch (DAOException e) {
                Helper.setFehlerMeldung("noLoggedUser");
                logger.error(e);
            }
        }
        return this.user;
    }

    public String getFilter() {
        return this.filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getSortierung() {
        return this.sortierung;
    }

    public void setSortierung(String sortierung) {
        this.sortierung = sortierung;
    }

    /**
     * Add filter to user.
     */
    public void addFilterToUser() {
        if (this.filter == null || this.filter.length() == 0) {
            return;
        }
        serviceManager.getUserService().addFilter(getUser(), this.filter);
    }

    /**
     * Get user filters.
     */
    public List<String> getUserFilters() {
        return serviceManager.getUserService().getFilters(getUser());
    }

    /**
     * Remove filter from user.
     */
    public void removeFilterFromUser() {
        if (this.filter == null || this.filter.length() == 0) {
            return;
        }
        serviceManager.getUserService().removeFilter(getUser(), this.filter);
    }
}
