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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ruleset")
public class Ruleset extends BaseIndexedBean {
    private static final long serialVersionUID = -6663371963274685060L;

    @Column(name = "title")
    private String title;

    @Column(name = "file")
    private String file;

    @Column(name = "orderMetadataByRuleset")
    private Boolean orderMetadataByRuleset = false;

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
}
