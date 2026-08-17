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

package org.kitodo.production.security.password;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;

/**
 * Migrate user passwords to the newest password encoding strategy using Spring's UserDetailsPasswordService.
 * 
 * <p>Spring will call "updatePassword" with a new encoding of the user's password after login if this password neeeds to migrated.</p>
 */
public class KitodoUserDetailsPasswordService implements UserDetailsPasswordService {

    private static final Logger logger = LogManager.getLogger(KitodoUserDetailsPasswordService.class);

    private static UserService userService = ServiceManager.getUserService();

    @Override
    public UserDetails updatePassword(UserDetails userDetails, String newEncodedPassword) {
        if (userDetails instanceof SecurityUserDetails) {
            try {
                User user = new User((SecurityUserDetails)userDetails);
                user.setPassword(newEncodedPassword);
                userService.save(user);
                logger.debug("password for user '{}' migrated to newest encoding strategy", userDetails.getUsername());
            } catch (DAOException e) {
                logger.error(String.format("failed to migrate user password for user '%s'", userDetails.getUsername()), e);
            }
        } else {
            logger.error(
                String.format("cannot migrate user password for user '%s', wrong user details instance", userDetails.getUsername())
            );
        }

        return userDetails;
    }
    
}
