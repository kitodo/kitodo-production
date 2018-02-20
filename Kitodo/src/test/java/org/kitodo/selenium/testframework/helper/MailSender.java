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

package org.kitodo.selenium.testframework.helper;

import java.io.File;
import java.util.ArrayList;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailSender {

    private static final Logger logger = LogManager.getLogger(MailSender.class);

    /**
     * Sends an email.
     * 
     * @param user
     *            The user name for login in to email account.
     * @param password
     *            The password for login in to email account.
     * @param subject
     *            The email subject.
     * @param message
     *            The email message.
     * @param attachedFile
     *            The attached file.
     * @param recipient
     *            The recipient email address.
     */
    public static void sendEmail(String user, String password, String subject, String message, File attachedFile,
            String recipient) throws EmailException, AddressException {

        if (user != null && password != null && recipient != null) {
            InternetAddress address = new InternetAddress(recipient);

            ArrayList<InternetAddress> addressList = new ArrayList<>();
            addressList.add(address);

            EmailAttachment attachment = new EmailAttachment();
            if (attachedFile != null) {
                // Create the attachment
                attachment.setPath(attachedFile.getAbsolutePath());
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                attachment.setDescription("SeleniumScreenShot");
                attachment.setName("screenshot.png");
            }

            MultiPartEmail email = new MultiPartEmail();
            email.setHostName("smtp.gmail.com");
            email.setSmtpPort(465);
            email.setAuthenticator(new DefaultAuthenticator(user, password));
            email.setSSLOnConnect(true);
            email.setFrom("Travis CI Screenshot <kitodo.dev@gmail.com>");
            if (subject != null) {
                email.setSubject(subject);
            }
            if (message != null) {
                email.setMsg(message);
            }
            email.setTo(addressList);
            email.attach(attachment);
            email.send();
        } else {
            logger.error("Email was not send due to missing environmental variables");
        }

    }

}
