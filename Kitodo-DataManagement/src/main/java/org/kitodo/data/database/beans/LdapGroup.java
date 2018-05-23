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

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ldapGroup")
public class LdapGroup extends BaseBean {
    private static final long serialVersionUID = -1657514909731889712L;

    @Column(name = "title")
    private String title;

    @Column(name = "homeDirectory")
    private String homeDirectory;

    @Column(name = "gidNumber")
    private String gidNumber;

    @Column(name = "userDn")
    private String userDN;

    @Column(name = "objectClasses")
    private String objectClasses;

    @Column(name = "sambaSid")
    private String sambaSID;

    @Column(name = "sn")
    private String sn;

    @Column(name = "uid")
    private String uid;

    @Column(name = "description")
    private String description;

    @Column(name = "displayName")
    private String displayName;

    @Column(name = "gecos")
    private String gecos;

    @Column(name = "loginShell")
    private String loginShell;

    @Column(name = "sambaAcctFlags")
    private String sambaAcctFlags;

    @Column(name = "sambaLogonScript")
    private String sambaLogonScript;

    @Column(name = "sambaPrimaryGroupSid")
    private String sambaPrimaryGroupSID;

    @Column(name = "sambaPasswordMustChange")
    private String sambaPwdMustChange;

    @Column(name = "sambaPasswordHistory")
    private String sambaPasswordHistory;

    @Column(name = "sambaLogonHours")
    private String sambaLogonHours;

    @Column(name = "sambaKickoffTime")
    private String sambaKickoffTime;

    @ManyToOne
    @JoinColumn(name = "ldapServer_id", foreignKey = @ForeignKey(name = "FK_ldapGroup_ldapServer_id"))
    private LdapServer ldapServer;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHomeDirectory() {
        return this.homeDirectory;
    }

    public void setHomeDirectory(String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    public String getGidNumber() {
        return this.gidNumber;
    }

    public void setGidNumber(String gidNumber) {
        this.gidNumber = gidNumber;
    }

    public String getUserDN() {
        return this.userDN;
    }

    public void setUserDN(String userDN) {
        this.userDN = userDN;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGecos() {
        return this.gecos;
    }

    public void setGecos(String gecos) {
        this.gecos = gecos;
    }

    public String getLoginShell() {
        return this.loginShell;
    }

    public void setLoginShell(String loginShell) {
        this.loginShell = loginShell;
    }

    public String getObjectClasses() {
        return this.objectClasses;
    }

    public void setObjectClasses(String objectClasses) {
        this.objectClasses = objectClasses;
    }

    public String getSambaAcctFlags() {
        return this.sambaAcctFlags;
    }

    public void setSambaAcctFlags(String sambaAcctFlags) {
        this.sambaAcctFlags = sambaAcctFlags;
    }

    public String getSambaLogonScript() {
        return this.sambaLogonScript;
    }

    public void setSambaLogonScript(String sambaLogonScript) {
        this.sambaLogonScript = sambaLogonScript;
    }

    public String getSambaPrimaryGroupSID() {
        return this.sambaPrimaryGroupSID;
    }

    public void setSambaPrimaryGroupSID(String sambaPrimaryGroupSID) {
        this.sambaPrimaryGroupSID = sambaPrimaryGroupSID;
    }

    public String getSambaSID() {
        return this.sambaSID;
    }

    public void setSambaSID(String sambaSID) {
        this.sambaSID = sambaSID;
    }

    public String getSn() {
        return this.sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getSambaKickoffTime() {
        return this.sambaKickoffTime;
    }

    public void setSambaKickoffTime(String sambaKickoffTime) {
        this.sambaKickoffTime = sambaKickoffTime;
    }

    public String getSambaLogonHours() {
        return this.sambaLogonHours;
    }

    public void setSambaLogonHours(String sambaLogonHours) {
        this.sambaLogonHours = sambaLogonHours;
    }

    public String getSambaPasswordHistory() {
        return this.sambaPasswordHistory;
    }

    public void setSambaPasswordHistory(String sambaPasswordHistory) {
        this.sambaPasswordHistory = sambaPasswordHistory;
    }

    public String getSambaPwdMustChange() {
        return this.sambaPwdMustChange;
    }

    public void setSambaPwdMustChange(String sambaPwdMustChange) {
        this.sambaPwdMustChange = sambaPwdMustChange;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets ldapServer.
     *
     * @return The ldapServer.
     */
    public LdapServer getLdapServer() {
        return ldapServer;
    }

    /**
     * Sets ldapServer.
     *
     * @param ldapServer
     *            The ldapServer.
     */
    public void setLdapServer(LdapServer ldapServer) {
        this.ldapServer = ldapServer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (Objects.isNull(o) || !Objects.equals(getClass(), o.getClass())) {
            return false;
        }
        LdapGroup ldapGroup = (LdapGroup) o;
        return Objects.equals(title, ldapGroup.title)
                && Objects.equals(homeDirectory, ldapGroup.homeDirectory)
                && Objects.equals(gidNumber, ldapGroup.gidNumber)
                && Objects.equals(userDN, ldapGroup.userDN)
                && Objects.equals(objectClasses, ldapGroup.objectClasses)
                && Objects.equals(sambaSID, ldapGroup.sambaSID)
                && Objects.equals(sn, ldapGroup.sn)
                && Objects.equals(uid, ldapGroup.uid)
                && Objects.equals(description, ldapGroup.description)
                && Objects.equals(displayName, ldapGroup.displayName)
                && Objects.equals(gecos, ldapGroup.gecos)
                && Objects.equals(loginShell, ldapGroup.loginShell)
                && Objects.equals(sambaAcctFlags, ldapGroup.sambaAcctFlags)
                && Objects.equals(sambaLogonScript, ldapGroup.sambaLogonScript)
                && Objects.equals(sambaPrimaryGroupSID, ldapGroup.sambaPrimaryGroupSID)
                && Objects.equals(sambaPwdMustChange, ldapGroup.sambaPwdMustChange)
                && Objects.equals(sambaPasswordHistory, ldapGroup.sambaPasswordHistory)
                && Objects.equals(sambaLogonHours, ldapGroup.sambaLogonHours)
                && Objects.equals(sambaKickoffTime, ldapGroup.sambaKickoffTime);
    }
}
