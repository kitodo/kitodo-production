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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "ldapServer")
public class LdapServer extends BaseBean {

    private static final long serialVersionUID = -1657514909496728712L;

    @Column(name = "title")
    private String title;

    @Column(name = "url")
    private String url;

    @Column(name = "managerLogin")
    private String managerLogin;

    @Column(name = "managerPassword")
    private String managerPassword;

    @OneToMany(mappedBy = "ldapServer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LdapGroup> ldapGroups;

    @Column(name = "nextFreeUnixIdPattern")
    private String nextFreeUnixIdPattern;

    @Column(name = "useSsl")
    private boolean useSsl = false;

    @Column(name = "readonly")
    private boolean readonly = false;

    @Column(name = "passwordEncryption")
    private String passwordEncryption;

    /**
     * Gets title.
     *
     * @return The title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title The title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets url.
     *
     * @return The url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets url.
     *
     * @param url The url.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets managerLogin.
     *
     * @return The managerLogin.
     */
    public String getManagerLogin() {
        return managerLogin;
    }

    /**
     * Sets managerLogin.
     *
     * @param managerLogin The managerLogin.
     */
    public void setManagerLogin(String managerLogin) {
        this.managerLogin = managerLogin;
    }

    /**
     * Gets managerPassword.
     *
     * @return The managerPassword.
     */
    public String getManagerPassword() {
        return managerPassword;
    }

    /**
     * Sets managerPassword.
     *
     * @param managerPassword The managerPassword.
     */
    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }

    /**
     * Gets ldapGroups.
     *
     * @return The ldapGroups.
     */
    public List<LdapGroup> getLdapGroups() {
        return ldapGroups;
    }

    /**
     * Sets ldapGroups.
     *
     * @param ldapGroups The ldapGroups.
     */
    private void setLdapGroups(List<LdapGroup> ldapGroups) {
        this.ldapGroups = ldapGroups;
    }

    /**
     * Gets nextFreeUnixIdPattern.
     *
     * @return The nextFreeUnixIdPattern.
     */
    public String getNextFreeUnixIdPattern() {
        return nextFreeUnixIdPattern;
    }

    /**
     * Sets nextFreeUnixIdPattern.
     *
     * @param nextFreeUnixIdPattern The nextFreeUnixIdPattern.
     */
    public void setNextFreeUnixIdPattern(String nextFreeUnixIdPattern) {
        this.nextFreeUnixIdPattern = nextFreeUnixIdPattern;
    }

    /**
     * Gets useSsl.
     *
     * @return The useSsl.
     */
    public boolean isUseSsl() {
        return useSsl;
    }

    /**
     * Sets useSsl.
     *
     * @param useSsl The useSsl.
     */
    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    /**
     * Gets readonly.
     *
     * @return The readonly.
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * Sets readonly.
     *
     * @param readonly The readonly.
     */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    /**
     * Gets passwordEncryption.
     *
     * @return The passwordEncryption.
     */
    public String getPasswordEncryption() {
        return passwordEncryption;
    }

    /**
     * Sets passwordEncryption.
     *
     * @param passwordEncryption The passwordEncryption.
     */
    public void setPasswordEncryption(String passwordEncryption) {
        this.passwordEncryption = passwordEncryption;
    }
}
