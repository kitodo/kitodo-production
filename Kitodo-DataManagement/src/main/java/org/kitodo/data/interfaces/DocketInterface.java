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
 * Interface for configuring the docket generator.
 */
public interface DocketInterface extends BaseBeanInterface {

    /**
     * Returns the name of the configuration file, without a path. The file must
     * exist in the XSLT directory. The directory is set in the configuration
     * file.
     *
     * @return the XSLT file name
     */
    String getFile();

    /**
     * Sets the name of the configuration file. The file must exist in the XSLT
     * directory. The file name must be specified without a path.
     *
     * @param file
     *            XSLT file name
     */
    void setFile(String file);

    /**
     * Returns the display name of the configuration. It is displayed to the
     * user in a selection dialog.
     *
     * @return the display name
     */
    String getTitle();

    /**
     * Sets the display name of the configuration.
     *
     * @param title
     *            the display name
     */
    void setTitle(String title);

    /**
     * Returns the client that this docket generator configuration is associated
     * with. Technically, multiple client can use the same docket generator
     * configuration (file), but they must be made available independently for
     * each client using one configuration object each. This determines which
     * docket generator configurations are visible to a client at all, and they
     * can be named differently.
     *
     * @return client that this docket is associated with
     */
    ClientInterface getClient();

    /**
     * Sets the client to which this docket generator configuration is
     * associated.
     *
     * @param client
     *            client to which this docket is associated
     */
    void setClient(ClientInterface client);
}
