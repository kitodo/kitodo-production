/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.data.database.enums;

/**
 * Configuration modes when hovering over item in list view (preview item).
 */
public enum PreviewHoverMode {

    /**
     * Show an overlay directly over the item.
     */
    OVERLAY,

    /**
     * Show a tooltip beside the item with the preview as content.
     */
    TOOLTIP_PREVIEW,

    /**
     * Show a tooltip beside the item with the mediaview as content.
     */
    TOOLTIP_MEDIAVIEW
}
