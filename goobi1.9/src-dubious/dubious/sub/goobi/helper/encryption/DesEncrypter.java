package dubious.sub.goobi.helper.encryption;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

public class DesEncrypter {
	Cipher ecipher;
	Cipher dcipher;
	String pass = "rusDML_Passphrase_for_secure_encryption_2005";
	// 8-byte Salt
	byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03 };
	// Iteration count
	int iterationCount = 19;

	/* =============================================================== */
	public DesEncrypter() {
		Initialise(this.pass);
	}

	/* =============================================================== */
	public DesEncrypter(String passPhrase) {
		Initialise(passPhrase);
	}

	/* =============================================================== */
	private void Initialise(String passPhrase) {
		try {
			// Create the key
			KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), this.salt, this.iterationCount);
			SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
			this.ecipher = Cipher.getInstance(key.getAlgorithm());
			this.dcipher = Cipher.getInstance(key.getAlgorithm());
			// Prepare the parameter to the ciphers
			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(this.salt, this.iterationCount);
			// Create the ciphers
			this.ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			this.dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		} catch (InvalidAlgorithmParameterException e) {
		} catch (InvalidKeySpecException e) {
		} catch (NoSuchPaddingException e) {
		} catch (NoSuchAlgorithmException e) {
		} catch (java.security.InvalidKeyException e) {
		}
	}

	/* =============================================================== */
	public String encrypt(String str) {
		if (str == null) {
			str = "";
		}
		try {
			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");
			if (this.ecipher == null) {
				Initialise(this.pass);
			}
			// Encrypt
			byte[] enc = this.ecipher.doFinal(utf8);
			// Encode bytes to base64 to get a string
			return new String(Base64.encodeBase64(enc));
		} catch (BadPaddingException e) {
			// e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// e.printStackTrace();
		}
		return null;
	}

	/* =============================================================== */
	public String decrypt(String str) {
		try {
			// Decode base64 to get bytes
			byte[] dec = Base64.decodeBase64(str.getBytes("UTF8"));
			// Decrypt
			byte[] utf8 = this.dcipher.doFinal(dec);
			// Decode using utf-8
			return new String(utf8, "UTF8");
		} catch (BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
	
}
