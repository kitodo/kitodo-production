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

package org.kitodo.data.elasticsearch;

import javax.persistence.Table;

/**
 * Super class for Indexer and Searcher.
 */
public abstract class Index {

    protected String index;
    protected String type;

    /**
     * Constructor with type names equal to table names.
     *
     * @param index
     *            as String
     * @param beanClass
     *            as Class
     */
    public Index(String index, Class<?> beanClass) {
        Table table = beanClass.getAnnotation(Table.class);
        this.setIndex(index);
        this.setType(table.name());
    }

    /**
     * Constructor with type names not equal to table names.
     *
     * @param index
     *            as String
     * @param type
     *            as String
     */
    public Index(String index, String type) {
        this.setIndex(index);
        this.setType(type);
    }

    /**
     * Getter for index.
     *
     * @return index name
     */
    public String getIndex() {
        return index;
    }

    /**
     * Setter for index.
     *
     * @param index
     *            - equal to the name of database, default kitodo
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * Getter for type.
     *
     * @return type name
     */
    public String getType() {
        return type;
    }

    /**
     * Setter for type.
     *
     * @param type
     *            - equal to the name of table in database, but not necessary
     */
    public void setType(String type) {
        this.type = type;
    }
}
