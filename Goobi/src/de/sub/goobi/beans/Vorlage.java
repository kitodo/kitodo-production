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

public class Vorlage implements Serializable {
    private static final long serialVersionUID = 1736135433162833277L;
    private Integer id;
    private String herkunft;
    private Prozess prozess;
    private Set<Vorlageeigenschaft> eigenschaften;

    private boolean panelAusgeklappt = true;

    public Vorlage() {
        this.eigenschaften = new HashSet<Vorlageeigenschaft>();
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

    public Set<Vorlageeigenschaft> getEigenschaften() {
        return this.eigenschaften;
    }

    public void setEigenschaften(Set<Vorlageeigenschaft> eigenschaften) {
        this.eigenschaften = eigenschaften;
    }

    /*
     * ##################################################### ##################################################### ## ## Helper ##
     * ##################################################### ####################################################
     */

    public String getHerkunft() {
        return this.herkunft;
    }

    public void setHerkunft(String herkunft) {
        this.herkunft = herkunft;
    }

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

    public List<Vorlageeigenschaft> getEigenschaftenList() {
        try {
            Hibernate.initialize(this.eigenschaften);
        } catch (HibernateException e) {
        }
        if (this.eigenschaften == null) {
            return new ArrayList<Vorlageeigenschaft>();
        }
        return new ArrayList<Vorlageeigenschaft>(this.eigenschaften);
    }

}
