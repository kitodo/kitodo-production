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
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.kitodo.data.database.persistence.ClientDAO;

@Entity
@Table(name = "client")
public class Client extends BaseBean {

    @Column(name = "name")
    private String name;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "client_x_listcolumn", joinColumns = {@JoinColumn(name = "client_id",
            foreignKey = @ForeignKey(name = "FK_client_id"))},
            inverseJoinColumns = {@JoinColumn(name = "column_id",
                    foreignKey = @ForeignKey(name = "FK_column_id"))})
    private List<ListColumn> listColumns;

    @ManyToMany(mappedBy = "clients", cascade = CascadeType.PERSIST)
    private List<ImportConfiguration> importConfigurations;

    /**
     * Gets name.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name
     *            The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof Client) {
            Client client = (Client) object;
            return Objects.equals(this.getId(), client.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /**
     * Get listColumns.
     * @return
     *          ListColumns
     */
    public List<ListColumn> getListColumns() {
        initialize(new ClientDAO(), this.listColumns);
        if (Objects.isNull(this.listColumns)) {
            this.listColumns = new ArrayList<>();
        }
        return this.listColumns;
    }

    /**
     * Set listColumns.
     * @param columns
     *          ListColumns
     */
    public void setListColumns(List<ListColumn> columns) {
        this.listColumns = columns;
    }

    /**
     * Get import configuration.
     *
     * @return import configurations
     */
    public List<ImportConfiguration> getImportConfigurations() {
        initialize(new ClientDAO(), this.importConfigurations);
        if (Objects.isNull(this.importConfigurations)) {
            this.importConfigurations = new ArrayList<>();
        }
        return importConfigurations;
    }

    /**
     * Set import configurations.
     *
     * @param configurations import configurations
     */
    public void setImportConfigurations(List<ImportConfiguration> configurations) {
        this.importConfigurations = configurations;
    }
}
