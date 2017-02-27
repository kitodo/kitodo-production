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

/**
 * Usergroups owning different access rights, represented by integer values
 *
 * 1: Administration - can do anything 2: Projectmanagement - may do a lot (but
 * not user management, no user switch, no administrative form) 3: User and
 * process (basically like 4 but can be used for setting aditional boundaries
 * later, if so desired) 4: User only: can see current steps
 *
 * ================================================================
 */
public class Benutzergruppe implements Serializable, Comparable<Benutzergruppe> {
    private static final long serialVersionUID = -5924845694417474352L;
    private Integer id;
    private String titel;
    private Integer berechtigung;
    private Set<Benutzer> benutzer;
    private Set<Schritt> schritte;
    private boolean panelAusgeklappt = false;

    public Benutzergruppe() {
        this.schritte = new HashSet<Schritt>();
        this.benutzer = new HashSet<Benutzer>();
    }

    /*
     * Getter und Setter
     */

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBerechtigung() {
        if (this.berechtigung == null) {
            this.berechtigung = 4;
        } else if (this.berechtigung == 3) {
            this.berechtigung = 4;
        }
        return this.berechtigung;
    }

    public void setBerechtigung(int berechtigung) {
        this.berechtigung = berechtigung;
    }

    public String getBerechtigungAsString() {
        if (this.berechtigung == null) {
            this.berechtigung = 4;
        } else if (this.berechtigung == 3) {
            this.berechtigung = 4;
        }
        return String.valueOf(this.berechtigung.intValue());
    }

    public void setBerechtigungAsString(String berechtigung) {
        this.berechtigung = Integer.parseInt(berechtigung);
    }

    public String getTitel() {
        if (this.titel == null) {
            return "";
        } else {
            return this.titel;
        }
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public Set<Benutzer> getBenutzer() {
        return this.benutzer;
    }

    public void setBenutzer(Set<Benutzer> benutzer) {
        this.benutzer = benutzer;
    }

    public List<Benutzer> getBenutzerList() {
        try {
            Hibernate.initialize(getBenutzer());
        } catch (org.hibernate.HibernateException e) {

        }

        if (this.benutzer == null) {
            return new ArrayList<Benutzer>();
        } else {
            return new ArrayList<Benutzer>(this.benutzer);
        }
    }

    public Set<Schritt> getSchritte() {
        return this.schritte;
    }

    public void setSchritte(Set<Schritt> schritte) {
        this.schritte = schritte;
    }

    public int getSchritteSize() {
        Hibernate.initialize(getSchritte());
        if (this.schritte == null) {
            return 0;
        } else {
            return this.schritte.size();
        }
    }

    public List<Schritt> getSchritteList() {
        Hibernate.initialize(getSchritte());
        if (this.schritte == null) {
            this.schritte = new HashSet<Schritt>();
        }
        return new ArrayList<Schritt>(this.schritte);
    }

    public boolean isPanelAusgeklappt() {
        return this.panelAusgeklappt;
    }

    public void setPanelAusgeklappt(boolean panelAusgeklappt) {
        this.panelAusgeklappt = panelAusgeklappt;
    }

    @Override
    public int compareTo(Benutzergruppe o) {
        return this.getTitel().compareTo(o.getTitel());
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Benutzergruppe)) {
            return false;
        }
        Benutzergruppe other = (Benutzergruppe) obj;
        return this.getTitel().equals(other.getTitel());
    }

    @Override
    public int hashCode() {
        return this.getTitel().hashCode();
    }
}
