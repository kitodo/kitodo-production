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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "template")
public class Template extends BaseIndexedBean {
    private static final long serialVersionUID = 1736135433162833277L;

    @Column(name = "origin")
    private String origin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_template_process_id"))
    private Process process;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "template_x_property", joinColumns = {
            @JoinColumn(name = "template_id", foreignKey = @ForeignKey(name = "FK_template_x_property_template_id")) }, inverseJoinColumns = {
                    @JoinColumn(name = "property_id", foreignKey = @ForeignKey(name = "FK_template_x_property_property_id")) })
    private List<Property> properties;

    @Transient
    private boolean panelShown = true;

    public Template() {
        this.properties = new ArrayList<>();
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

    public List<Property> getProperties() {
        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        return this.properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public boolean isPanelShown() {
        return this.panelShown;
    }

    public void setPanelShown(boolean panelShown) {
        this.panelShown = panelShown;
    }
}
