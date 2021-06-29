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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

import org.kitodo.data.database.enums.IndexAction;

/**
 * Base bean class.
 */
@MappedSuperclass
public abstract class BaseIndexedBean extends BaseBean {

    @Column(name = "indexAction")
    @Enumerated(EnumType.STRING)
    private IndexAction indexAction;

    /**
     * Get action which should be performed on ElasticSearch index.
     *
     * @return action which needs to be performed on index
     */
    public IndexAction getIndexAction() {
        return indexAction;
    }

    /**
     * Set DONE if record is already indexed in ElasticSearch, set INDEX if it
     * needs to be indexed and DELETE if it needs to be deleted.
     *
     * @param indexAction
     *            index, delete or done
     */
    public void setIndexAction(IndexAction indexAction) {
        this.indexAction = indexAction;
    }
}
