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

package org.kitodo.api.dataformat;

import java.util.Objects;

/**
 * A view on a physical division. The individual levels of the
 * {@link LogicalDivision} refer to {@code View}s on
 * {@link PhysicalDivision}s. At the moment, each {@code View} refers to exactly one
 * {@code PhysicalDivision} as a whole.
 */
public class View {

    /**
     * Creates a new view with a physical division.
     * 
     * @param physicalDivision
     *            physical division to set in the view
     * @return a new view with a physical division
     */
    public static View of(PhysicalDivision physicalDivision) {
        View view = new View();
        view.setPhysicalDivision(physicalDivision);
        return view;
    }

    /**
     * Media unit in view.
     */
    private PhysicalDivision physicalDivision;

    /**
     * Returns the physical division in the view.
     *
     * @return the physical division
     */
    public PhysicalDivision getPhysicalDivision() {
        return physicalDivision;
    }

    /**
     * Inserts a physical division into the view.
     *
     * @param physicalDivision
     *            physical division to insert
     */
    public void setPhysicalDivision(PhysicalDivision physicalDivision) {
        this.physicalDivision = physicalDivision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        View view = (View) o;
        return Objects.equals(physicalDivision, view.physicalDivision);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        hashCode = prime * hashCode + ((physicalDivision == null) ? 0 : physicalDivision.hashCode());
        return hashCode;
    }
}
