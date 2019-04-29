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

@Entity
@Table(name = "ruleset")
public class Ruleset extends BaseIndexedBean {

    @Column(name = "title")
    private String title;

    @Column(name = "file")
    private String file;

    @Column(name = "orderMetadataByRuleset")
    private Boolean orderMetadataByRuleset = false;

    @Column(name = "active")
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "FK_ruleset_client_id"))
    private Client client;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFile() {
        return this.file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Check if metadata should be ordered by ruleset.
     *
     * @return true or false
     */
    public boolean isOrderMetadataByRuleset() {
        if (this.orderMetadataByRuleset == null) {
            this.orderMetadataByRuleset = false;
        }
        return this.orderMetadataByRuleset;
    }

    /**
     * Set if metadata should be ordered by ruleset.
     *
     * @param orderMetadataByRuleset
     *            true or false
     */
    public void setOrderMetadataByRuleset(boolean orderMetadataByRuleset) {
        this.orderMetadataByRuleset = orderMetadataByRuleset;
    }

    /**
     * Check if ruleset is active.
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
     * Set ruleset as active.
     *
     * @param active as boolean
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Get client.
     *
     * @return Client object
     */
    public Client getClient() {
        return this.client;
    }

    /**
     * Set client.
     *
     * @param client
     *            as Client object
     */
    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof Ruleset) {
            Ruleset ruleset = (Ruleset) object;
            return Objects.equals(this.getId(), ruleset.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, file, orderMetadataByRuleset, active);
    }
}
