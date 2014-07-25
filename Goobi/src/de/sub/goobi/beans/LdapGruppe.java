package de.sub.goobi.beans;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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
