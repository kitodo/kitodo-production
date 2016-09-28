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

package de.sub.goobi.beans;

import java.io.Serializable;

public class LdapGruppe implements Serializable {
   private static final long serialVersionUID = -1657514909731889712L;
   private Integer id;
   private String titel;
   private String homeDirectory;
   private String gidNumber;
   private String userDN;
   private String objectClasses;
   private String sambaSID;
   private String sn;
   private String uid;
   private String description;
   private String displayName;
   private String gecos;
   private String loginShell;
   private String sambaAcctFlags;
   private String sambaLogonScript;
   private String sambaPrimaryGroupSID;

   private String sambaPwdMustChange;
   private String sambaPasswordHistory;
   private String sambaLogonHours;
   private String sambaKickoffTime;

   public LdapGruppe() {
   }

   /*=====================================================
                      Getter und Setter
    ====================================================*/

   public Integer getId() {
      return this.id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getGidNumber() {
      return this.gidNumber;
   }

   public void setGidNumber(String gidNumber) {
      this.gidNumber = gidNumber;
   }

   public String getHomeDirectory() {
      return this.homeDirectory;
   }

   public void setHomeDirectory(String homeDirectory) {
      this.homeDirectory = homeDirectory;
   }

   public String getTitel() {
      return this.titel;
   }

   public void setTitel(String titel) {
      this.titel = titel;
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

}
