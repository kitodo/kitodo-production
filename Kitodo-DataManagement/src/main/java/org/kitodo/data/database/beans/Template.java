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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "template")
public class Template implements Serializable {
    private static final long serialVersionUID = 1736135433162833277L;

    @Id
    @Column(name = "id")
    @GeneratedValue
    private Integer id;

    @Column(name = "origin")
    private String origin;

    @ManyToOne
    @JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_template_process_id"))
    private Process process;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("title ASC")
    private List<TemplateProperty> properties;

    @Transient
    private boolean panelShown = true;

    public Template() {
        this.properties = new ArrayList<>();
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrigin() {
        return this.origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Process getProcess() {
        return this.process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public List<TemplateProperty> getProperties() {
        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        return this.properties;
    }

    public void setProperties(List<TemplateProperty> properties) {
        this.properties = properties;
    }

    public boolean isPanelShown() {
        return this.panelShown;
    }

    public void setPanelShown(boolean panelShown) {
        this.panelShown = panelShown;
    }
}
