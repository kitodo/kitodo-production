/*
 * This file is part of the SUB Commons project.
 * Visit the websites for more information. 
 * 		- http://gdz.sub.uni-goettingen.de 
 * 		- http://www.intranda.com 
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 * intranda software.
 * 
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.unigoettingen.sub.commons.util.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;


public class StreamUtils {

	/**
	 * get MimeType as {@link String} from given URL
	 * 
	 * @param url
	 *            the url from where to get the MimeType
	 * @return MimeType as {@link String}
	 * @throws IOException
	 */
	public static String getMimeTypeFromUrl(URL url) throws IOException {
	
		URLConnection con = url.openConnection();
		return con.getContentType();
	}

	/**
	 * get MimeType as {@link String} from given URL including proxy details
	 * 
	 * @param url
	 *            the url from where to get the MimeType
	 * @param httpproxyhost
	 *            host of proxy
	 * @param httpproxyport
	 *            port of proxy
	 * @param httpproxyusername
	 *            username for proxy
	 * @param httpproxypassword
	 *            password for proxy
	 * @return MimeType as {@link String}
	 * @throws IOException
	 */
	
	public static String getMimeTypeFromUrl(URL url, String httpproxyhost,
			String httpproxyport, String httpproxyusername,
			String httpproxypassword) throws IOException {
		if (httpproxyhost != null) {
			Properties properties = System.getProperties();
			properties.put("http.proxyHost", httpproxyhost);
			if (httpproxyport != null) {
				properties.put("http.proxyPort", httpproxyport);
			} else {
				properties.put("http.proxyPort", "80");
			}
		}
		URLConnection con = url.openConnection();
		if (httpproxyusername != null) {
			String login = httpproxyusername + ":" + httpproxypassword;
			String encodedLogin = new String(Base64.encodeBase64(login
					.getBytes()));
			con.setRequestProperty("Proxy-Authorization", "Basic "
					+ encodedLogin);
		}
		return con.getContentType();
	}

	/**
	 * get {@link InputStream} from given URL
	 * 
	 * @param url
	 *            the url from where to get the {@link InputStream}
	 * @return {@link InputStream} for url
	 * @throws IOException
	 */
	public static InputStream getInputStreamFromUrl(URL url) throws IOException {
		return StreamUtils.getInputStreamFromUrl(url, null);
	}

	/**
	 * get {@link InputStream} from given URL using a basis path and proxy informations
	 * 
	 * @param url
	 *            the url from where to get the {@link InputStream}
	 * @param basepath the basispath
	 * @param httpproxyhost the host for proxy
	 * @param httpproxyport the port for proxy
	 * @param httpproxyusername the username for the proxy
	 * @param httpproxypassword the password for the proxy
	 * @return {@link InputStream} for url
	 * @throws IOException
	 */
	public static InputStream getInputStreamFromUrl(URL url, String basepath,
			String httpproxyhost, String httpproxyport,
			String httpproxyusername, String httpproxypassword)
			throws IOException {
		InputStream inStream = null;
	
		if (url.getProtocol().equalsIgnoreCase("http")) {
			if (httpproxyhost != null) {
				Properties properties = System.getProperties();
				properties.put("http.proxyHost", httpproxyhost);
				if (httpproxyport != null) {
					properties.put("http.proxyPort", httpproxyport);
				} else {
					properties.put("http.proxyPort", "80");
				}
			}
			URLConnection con = url.openConnection();
			if (httpproxyusername != null) {
				String login = httpproxyusername + ":" + httpproxypassword;
				String encodedLogin = new String(Base64.encodeBase64(login
						.getBytes()));
				con.setRequestProperty("Proxy-Authorization", "Basic "
						+ encodedLogin);
			}
			inStream = con.getInputStream();
		} else if (url.getProtocol().equalsIgnoreCase("file")) {
			String filepath = url.getFile();
	
			filepath = URLDecoder.decode(filepath, System
					.getProperty("file.encoding"));
	
			File f = new File(filepath);
			inStream = new FileInputStream(f);
		} else if (url.getProtocol().equalsIgnoreCase("")) {
			String filepath = url.getFile();
			// we just have the relative path, need to find the absolute path
			String path = basepath + filepath;
	
			// call this method again
			URL completeurl = new URL(path);
			inStream = getInputStreamFromUrl(completeurl);
		}
	
		return inStream;
	}

	/**
	 * get {@link InputStream} from given URL using a basis path
	 * 
	 * @param url
	 *            the url from where to get the {@link InputStream}
	 * @param basepath the basispath
	 * @return {@link InputStream} for url
	 * @throws IOException
	 */
	public static InputStream getInputStreamFromUrl(URL url, String basepath)
			throws IOException {
		return getInputStreamFromUrl(url, basepath, null, null, null, null);
	}

	
	/**
	 * Dump {@link InputStream} into a String
	 *
	 * @param in the InputStream
	 * @return the String
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String dumpInputStream (InputStream in) throws IOException {
		if (in != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				in.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
}
