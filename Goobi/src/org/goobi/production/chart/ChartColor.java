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

package org.goobi.production.chart;

import java.awt.Color;

public enum ChartColor {

    green(0, 130, 80), yellow(250, 220, 50), red(200, 0, 0);

    private Color color;

    private ChartColor(int red, int green, int blue) {
        color = new Color(red, green, blue);
    }

    public Color getColor() {
        return color;
    }
}
