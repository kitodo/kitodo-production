/**
 * This file is based on source code from www.koders.com:
 * http://www.koders.com/java/fid32AAA6822935312790EAEB6F7232CE6F18CC8EAB.aspx
 *
 * www.koders.com is operated by Black Duck Software, Inc.
 *
 * Black Duck Software, Inc.
 * 8 New England Executive Park
 * Burlington, MA 01803
 * Tel: +1 781.891.5100
 * Fax: +1 781.891.5145
 * Email: info@blackducksoftware.com
 * http://www.blackducksoftware.com
 *
 * Black Duck Software Digital Millennium Copyright Act Notice
 * http://corp.koders.com/dmca
 *
 * Black Duck Software respects the intellectual property of others. Black Duck Software may, in appropriate
 * circumstances and at its sole discretion, terminate the access of users who infringe the copyright or intellectual
 * property rights of others.
 *
 */
package dubious.sub.goobi.helper.exceptions;

/**
 * This exception is used to mark (fatal) failures in infrastructure and system code.
 *
 * @author Christian Bauer <christian@hibernate.org>
 */
public class InfrastructureException
	extends RuntimeException {
	private static final long serialVersionUID = -5431126070176417318L;

	public InfrastructureException() {
	}

	public InfrastructureException(String message) {
		super(message);
	}

	public InfrastructureException(String message, Throwable cause) {
		super(message, cause);
	}

	public InfrastructureException(Throwable cause) {
		super(cause);
	}
}
