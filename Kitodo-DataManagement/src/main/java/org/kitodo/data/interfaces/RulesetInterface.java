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

package org.kitodo.data.interfaces;

/**
 * An interface for managing the business domain in the form of a ruleset. The
 * ruleset defines divisions and metadata keys with their value ranges, and
 * describes which division must or may be marked with which metadata. It also
 * contains visual settings of the editor GUI.
 */
public interface RulesetInterface extends BaseBeanInterface {

    /**
     * Returns the name of the configuration file, without a path. The file must
     * exist in the ruleset directory. The directory is set in the configuration
     * file.
     *
     * @return the XML file name
     */
    String getFile();

    /**
     * Sets the name of the configuration file. The file must exist in the
     * ruleset directory. The file name must be specified without a path.
     *
     * @param file
     *            XML file name
     */
    void setFile(String file);

    /**
     * Returns the display name of the business domain model. It is displayed to
     * the user in a selection dialog.
     *
     * @return the display name
     */
    String getTitle();

    /**
     * Sets the display name of the business domain model.
     *
     * @param title
     *            the display name
     */
    void setTitle(String title);

    /**
     * Returns whether the elements of the ruleset should be displayed in the
     * declared order. If not, they are displayed alphabetically. It varies at
     * which points this sorting takes effect and what is sorted on.
     *
     * @return whether the elements should be in declared order
     */
    boolean isOrderMetadataByRuleset();

    /**
     * Sets whether the elements of the ruleset should be displayed in the
     * declared order.
     *
     * @param orderMetadataByRuleset
     *            whether the elements should be in declared order
     */
    void setOrderMetadataByRuleset(boolean orderMetadataByRuleset);

    /**
     * Determines whether the ruleset is active. A deactivated rule set is not
     * offered for selection, but can continue to be used where it is already in
     * use.
     *
     * @return whether the ruleset is active
     */
    Boolean isActive();

    /**
     * Sets whether the ruleset is active.
     *
     * @param active
     *            whether the ruleset is active
     */
    void setActive(boolean active);

    /**
     * Returns the client that this ruleset is associated with. Technically,
     * multiple client can use the same docket generator configuration (file),
     * but they must be made available independently for each client using one
     * configuration object each. This determines which rulesets are visible to
     * a client at all, and they can be named differently.
     *
     * @return client that this ruleset is associated with
     */
    Client getClient();

    /**
     * Sets the client to which this ruleset is associated.
     *
     * @param client
     *            client to which this ruleset is associated
     */
    void setClient(Client client);

}
