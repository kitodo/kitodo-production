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

package org.kitodo.api.externaldatamanagement;

import java.util.EventObject;

public class ExternalDataEvent extends EventObject {

    /** The source, where the change has taken place. */
    private Source source;

    /** The identifier of the entry, which has changed. */
    private String identifier;

    /**
     * Constructor
     * @param eventInitiator The event initiator.
     * @param source The source.
     * @param identifier The identifier.
     */
    public ExternalDataEvent(Object eventInitiator, Source source, String identifier) {
        super(eventInitiator);
        this.source = source;
        this.identifier = identifier;
    }

    /** Gets the source.
     * @return The source.
     */
    public Source getSource() {
        return source;
    }

    /** Gets the identifier.
     * @return The identifier.
     */
    public String getIdentifier() {
        return identifier;
    }
}
