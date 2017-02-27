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

import java.io.Serializable;

import org.apache.log4j.Logger;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;

public class BasisForm implements Serializable {
    private static final Logger logger = Logger
    .getLogger(BasisForm.class);
    private static final long serialVersionUID = 2950419497162710096L;
    protected Page page;
    protected String zurueck = "";
    protected String filter = "";
    protected Benutzer user;

    protected String sortierung = "prozessAsc";

    public Page getPage() {
        return this.page;
    }

    public String getZurueck() {
        return this.zurueck;
    }

    public void setZurueck(String zurueck) {
        this.zurueck = zurueck;
    }

    public Benutzer getUser() {
        if(this.user==null) {
            LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
            this.user = login.getMyBenutzer();
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

    public void addFilterToUser(){
        if (this.filter==null || this.filter.length()==0){
            return;
        }
        this.user.addFilter(this.filter);
//      try {
//          new BenutzerDAO().save(this.user);
//      } catch (DAOException e) {
//          logger.error(e);
//      }
    }

    public void removeFilterFromUser(){
        if (this.filter==null || this.filter.length()==0){
            return;
        }
        this.user.removeFilter(this.filter);
//      try {
//          new BenutzerDAO().save(this.user);
//      } catch (DAOException e) {
//          logger.error(e);
//      }
    }
}
