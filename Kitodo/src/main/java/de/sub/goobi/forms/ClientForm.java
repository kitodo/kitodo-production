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

package de.sub.goobi.forms;

import de.sub.goobi.helper.Helper;

import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.ObjectType;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;

@Named("ClientForm")
@SessionScoped
public class ClientForm extends BaseForm {
    private static final long serialVersionUID = -445707351975817243L;
    private Client client;
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(ClientForm.class);
    private int clientId;

    /**
     * Empty default constructor that also sets the LazyDTOModel instance of this
     * bean.
     */
    public ClientForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getClientService()));
    }

    /**
     * Save user group.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            this.serviceManager.getClientService().save(this.client);
            return "/pages/users?" + REDIRECT_PARAMETER;
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.CLIENT.getTranslationSingular() }, logger, e);
            return null;
        }
    }

    /**
     * Method being used as viewAction for client edit form. If 'clientId' is '0',
     * the form for creating a new client will be displayed.
     */
    public void loadClient() {
        try {
            if (!Objects.equals(this.clientId, 0)) {
                this.client = this.serviceManager.getClientService().getById(this.clientId);
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.CLIENT.getTranslationSingular(), this.clientId },
                logger, e);
        }
    }

    /**
     * Create new client.
     *
     * @return page address
     */
    public String newClient() {
        this.client = new Client();
        this.clientId = 0;
        return "/pages/clientEdit?" + REDIRECT_PARAMETER;
    }

    /**
     * Gets clientId.
     *
     * @return The clientId.
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Sets clientId.
     *
     * @param clientId
     *            The clientId.
     */
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets client.
     *
     * @return The client.
     */
    public Client getClient() {
        return client;
    }

    /**
     * Sets client.
     *
     * @param client
     *            The client.
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Set client by ID.
     *
     * @param clientID
     *          ID of client to set.
     */
    public void setClientById(int clientID) {
        try {
            setClient(this.serviceManager.getClientService().getById(clientID));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.CLIENT.getTranslationSingular(), clientID }, logger, e);
        }
    }

    /**
     * Delete client.
     */
    public void deleteClient() {
        try {
            this.serviceManager.getClientService().remove(this.client);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.CLIENT.getTranslationSingular() }, logger, e);
        }
    }
}
