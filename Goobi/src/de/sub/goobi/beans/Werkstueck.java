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

package de.sub.goobi.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

public class Werkstueck implements Serializable {
    private static final long serialVersionUID = 123266825187246791L;
    private Integer id;
    private Prozess prozess;
    private Set<Werkstueckeigenschaft> eigenschaften;
    private boolean panelAusgeklappt = true;

    public Werkstueck() {
        this.eigenschaften = new HashSet<Werkstueckeigenschaft>();
    }

    /*
     * ##################################################### ##################################################### ## ## Getter und Setter ##
     * ##################################################### ####################################################
     */

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Prozess getProzess() {
        return this.prozess;
    }

    public void setProzess(Prozess prozess) {
        this.prozess = prozess;
    }

    public boolean isPanelAusgeklappt() {
        return this.panelAusgeklappt;
    }

    public void setPanelAusgeklappt(boolean panelAusgeklappt) {
        this.panelAusgeklappt = panelAusgeklappt;
    }

    public Set<Werkstueckeigenschaft> getEigenschaften() {
        return this.eigenschaften;
    }

    public void setEigenschaften(Set<Werkstueckeigenschaft> eigenschaften) {
        this.eigenschaften = eigenschaften;
    }

    /*
     * ##################################################### ##################################################### ## ## Helper ##
     * ##################################################### ####################################################
     */

    public int getEigenschaftenSize() {
        try {
            Hibernate.initialize(this.eigenschaften);
        } catch (HibernateException e) {
        }
        if (this.eigenschaften == null) {
            return 0;
        } else {
            return this.eigenschaften.size();
        }
    }

    public List<Werkstueckeigenschaft> getEigenschaftenList() {
        try {
            Hibernate.initialize(this.eigenschaften);
        } catch (HibernateException e) {
        }
        if (this.eigenschaften == null) {
            return new ArrayList<Werkstueckeigenschaft>();
        }
        return new ArrayList<Werkstueckeigenschaft>(this.eigenschaften);
    }
}
