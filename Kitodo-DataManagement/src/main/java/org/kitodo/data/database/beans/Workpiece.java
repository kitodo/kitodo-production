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

package org.kitodo.data.database.beans;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "workpiece")
public class Workpiece extends BaseBean {
    private static final long serialVersionUID = 123266825187246791L;

    @ManyToOne
    @JoinColumn(name = "process_id")
    private Process process;

    @OneToMany(mappedBy = "workpiece", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("title ASC")
    private List<WorkpieceProperty> properties;

    @Transient
    private boolean panelShown = true;

    public Workpiece() {
        this.properties = new ArrayList<>();
    }

    public Process getProcess() {
        return this.process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public boolean isPanelShown() {
        return this.panelShown;
    }

    public void setPanelShown(boolean panelShown) {
        this.panelShown = panelShown;
    }

    public List<WorkpieceProperty> getProperties() {
        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        return this.properties;
    }

    public void setProperties(List<WorkpieceProperty> properties) {
        this.properties = properties;
    }
}
