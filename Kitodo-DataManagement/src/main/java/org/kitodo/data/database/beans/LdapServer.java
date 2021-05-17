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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.kitodo.data.database.converter.PasswordEncryptionConverter;
import org.kitodo.data.database.enums.PasswordEncryption;
import org.kitodo.data.database.persistence.LdapServerDAO;

@Entity
@Table(name = "ldapserver")
public class LdapServer extends BaseBean {

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

    @Column(name = "readOnly")
    private boolean readOnly = false;

    @Column(name = "passwordEncryption")
    @Convert(converter = PasswordEncryptionConverter.class)
    private PasswordEncryption passwordEncryption = PasswordEncryption.MD5;

    @Column(name = "rootCertificate")
    private String rootCertificate;

    @Column(name = "pdcCertificate")
    private String pdcCertificate;

    @Column(name = "keystore")
    private String keystore;

    @Column(name = "keystorePassword")
    private String keystorePassword;

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
     * @param title
     *            The title.
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
     * @param url
     *            The url.
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
     * @param managerLogin
     *            The managerLogin.
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
     * @param managerPassword
     *            The managerPassword.
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
        initialize(new LdapServerDAO(), this.ldapGroups);
        if (Objects.isNull(this.ldapGroups)) {
            this.ldapGroups = new ArrayList<>();
        }
        return this.ldapGroups;
    }

    /**
     * Sets ldapGroups.
     *
     * @param ldapGroups
     *            The ldapGroups.
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
     * @param nextFreeUnixIdPattern
     *            The nextFreeUnixIdPattern.
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
     * @param useSsl
     *            The useSsl.
     */
    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    /**
     * Gets readonly.
     *
     * @return The readonly.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets readonly.
     *
     * @param readOnly
     *            The readonly.
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Get password encryption as {@link PasswordEncryption}.
     *
     * @return The passwordEncryption.
     */
    public PasswordEncryption getPasswordEncryption() {
        return this.passwordEncryption;
    }

    /**
     * Set password encryption to specific value from {@link PasswordEncryption}.
     *
     * @param passwordEncryptionEnum
     *            The passwordEncryptionEnum.
     */
    public void setPasswordEncryption(PasswordEncryption passwordEncryptionEnum) {
        this.passwordEncryption = passwordEncryptionEnum;
    }

    /**
     * Gets rootCertificate.
     *
     * @return The rootCertificate.
     */
    public String getRootCertificate() {
        return rootCertificate;
    }

    /**
     * Sets rootCertificate.
     *
     * @param rootCertificate
     *            The rootCertificate.
     */
    public void setRootCertificate(String rootCertificate) {
        this.rootCertificate = rootCertificate;
    }

    /**
     * Gets pdcCertificate.
     *
     * @return The pdcCertificate.
     */
    public String getPdcCertificate() {
        return pdcCertificate;
    }

    /**
     * Sets pdcCertificate.
     *
     * @param pdcCertificate
     *            The pdcCertificate.
     */
    public void setPdcCertificate(String pdcCertificate) {
        this.pdcCertificate = pdcCertificate;
    }

    /**
     * Gets keystore.
     *
     * @return The keystore.
     */
    public String getKeystore() {
        return keystore;
    }

    /**
     * Sets keystore.
     *
     * @param keystore
     *            The keystore.
     */
    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    /**
     * Gets keystorePassword.
     *
     * @return The keystorePassword.
     */
    public String getKeystorePassword() {
        return keystorePassword;
    }

    /**
     * Sets keystorePassword.
     *
     * @param keystorePassword
     *            The keystorePassword.
     */
    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof LdapServer) {
            LdapServer ldapServer = (LdapServer) object;
            return Objects.equals(this.getId(), ldapServer.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, url, managerLogin, managerPassword, nextFreeUnixIdPattern);
    }
}
