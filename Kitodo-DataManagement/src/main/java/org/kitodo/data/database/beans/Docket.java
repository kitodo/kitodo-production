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

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.kitodo.data.interfaces.ClientInterface;
import org.kitodo.data.interfaces.DocketInterface;

@Entity
@Table(name = "docket")
public class Docket extends BaseIndexedBean implements DocketInterface {

    @Column(name = "title")
    private String title;

    @Column(name = "file")
    private String file;

    @Column(name = "active")
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "FK_docket_client_id"))
    private Client client;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getFile() {
        return file;
    }

    @Override
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Check if docket is active.
     *
     * @return true or false
     */
    public Boolean isActive() {
        if (Objects.isNull(this.active)) {
            this.active = true;
        }
        return this.active;
    }

    /**
     * Set docket as active.
     *
     * @param active as boolean
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public Client getClient() {
        return this.client;
    }

    @Override
    public void setClient(ClientInterface client) {
        this.client = (Client) client;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof Docket) {
            Docket docket = (Docket) object;
            return Objects.equals(this.getId(), docket.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, file, active);
    }
}
