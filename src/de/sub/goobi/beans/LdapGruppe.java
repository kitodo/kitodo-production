/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
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
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getGidNumber() {
      return gidNumber;
   }

   public void setGidNumber(String gidNumber) {
      this.gidNumber = gidNumber;
   }

   public String getHomeDirectory() {
      return homeDirectory;
   }

   public void setHomeDirectory(String homeDirectory) {
      this.homeDirectory = homeDirectory;
   }

   public String getTitel() {
      return titel;
   }

   public void setTitel(String titel) {
      this.titel = titel;
   }

   public String getUserDN() {
      return userDN;
   }

   public void setUserDN(String userDN) {
      this.userDN = userDN;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getDisplayName() {
      return displayName;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public String getGecos() {
      return gecos;
   }

   public void setGecos(String gecos) {
      this.gecos = gecos;
   }

   public String getLoginShell() {
      return loginShell;
   }

   public void setLoginShell(String loginShell) {
      this.loginShell = loginShell;
   }

   public String getObjectClasses() {
      return objectClasses;
   }

   public void setObjectClasses(String objectClasses) {
      this.objectClasses = objectClasses;
   }

   public String getSambaAcctFlags() {
      return sambaAcctFlags;
   }

   public void setSambaAcctFlags(String sambaAcctFlags) {
      this.sambaAcctFlags = sambaAcctFlags;
   }

   public String getSambaLogonScript() {
      return sambaLogonScript;
   }

   public void setSambaLogonScript(String sambaLogonScript) {
      this.sambaLogonScript = sambaLogonScript;
   }

   public String getSambaPrimaryGroupSID() {
      return sambaPrimaryGroupSID;
   }

   public void setSambaPrimaryGroupSID(String sambaPrimaryGroupSID) {
      this.sambaPrimaryGroupSID = sambaPrimaryGroupSID;
   }

   public String getSambaSID() {
      return sambaSID;
   }

   public void setSambaSID(String sambaSID) {
      this.sambaSID = sambaSID;
   }

   public String getSn() {
      return sn;
   }

   public void setSn(String sn) {
      this.sn = sn;
   }

   public String getSambaKickoffTime() {
      return sambaKickoffTime;
   }

   public void setSambaKickoffTime(String sambaKickoffTime) {
      this.sambaKickoffTime = sambaKickoffTime;
   }

   public String getSambaLogonHours() {
      return sambaLogonHours;
   }

   public void setSambaLogonHours(String sambaLogonHours) {
      this.sambaLogonHours = sambaLogonHours;
   }

   public String getSambaPasswordHistory() {
      return sambaPasswordHistory;
   }

   public void setSambaPasswordHistory(String sambaPasswordHistory) {
      this.sambaPasswordHistory = sambaPasswordHistory;
   }

   public String getSambaPwdMustChange() {
      return sambaPwdMustChange;
   }

   public void setSambaPwdMustChange(String sambaPwdMustChange) {
      this.sambaPwdMustChange = sambaPwdMustChange;
   }

   public String getUid() {
      return uid;
   }

   public void setUid(String uid) {
      this.uid = uid;
   }

}
