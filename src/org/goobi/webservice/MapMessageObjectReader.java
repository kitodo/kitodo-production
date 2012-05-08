package org.goobi.webservice;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.MapMessage;

public class MapMessageObjectReader {

	private MapMessage ticket;

	/**
	 * This instantiates a new MapMessageObjectReader which is attached to a
	 * given MapMessage.
	 * 
	 * @param message
	 */
	public MapMessageObjectReader(MapMessage message) {
		if (message == null)
			throw new IllegalArgumentException(
					"MapMessageObjectReader: null argument in constructor.");
		this.ticket = message;
	}

	/**
	 * The function getMandatorySetOfString() fetches a Set<String> from a MapMessage.
	 * This is a strict implementation that requires the collection not to be
	 * empty and not to contain the null element.
	 * 
	 * Please note that the Set is allowed to contain the empty String (“”).
	 * 
	 * @param key
	 *            the name of the set to return
	 * @return the set requested
	 * @throws IllegalArgumentException
	 *             in case that getObject returns null, the returned object is
	 *             not of type Collection, any of the content elements are not
	 *             of type String or the collection is empty at all.
	 * @throws JMSException
	 *             can be thrown by MapMessage.getObject(String)
	 */
	public Set<String> getMandatorySetOfString(String key)
			throws IllegalArgumentException, JMSException {
		Set<String> result = new HashSet<String>();
		Boolean emptiness = Boolean.TRUE;

		Object collectionObject = ticket.getObject(key);
		if (collectionObject == null)
			throw new IllegalArgumentException("Missing mandatory argument: \""
					+ key + "\"");
		if (!(collectionObject instanceof Collection<?>))
			throw new IllegalArgumentException("Incompatible types: \"" + key
					+ "\" was not found to be of type Collection<?>.");
		for (Object contentObject : (Collection<?>) collectionObject) {
			if (contentObject == null || !(contentObject instanceof String))
				throw new IllegalArgumentException(
						"Incompatible types: An element of \"" + key
								+ "\" was not found to be of type String.");
			result.add((String) contentObject);
			emptiness = false;
		}
		if (emptiness)
			throw new IllegalArgumentException("Missing mandatory argument: \""
					+ key + "\" must not be empty.");
		return result;
	}

	/**
	 * The function getMandatoryString() fetches a String from a MapMessage. This is
	 * a strict implementation that requires the string not to be null and not
	 * to be empty.
	 * 
	 * @param key
	 *            the name of the string to return
	 * @return the string requested
	 * @throws IllegalArgumentException
	 *             in case that getObject returns null or the returned string is
	 *             of length “0”.
	 * @throws JMSException
	 *             can be thrown by MapMessage.getString(String)
	 */
	public String getMandatoryString(String key) throws IllegalArgumentException,
			JMSException {
		String result = ticket.getString(key);
		if (result == null || result.length() == 0)
			throw new IllegalArgumentException("Missing mandatory argument: \""
					+ key + "\"");
		return result;
	}
}
