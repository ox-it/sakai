package uk.ac.ox.oucs.oxam;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

public class Utils {

	/**
	 * Just works out the CRC for an input stream.
	 * @param in
	 * @return
	 */
	public static long getCRC(InputStream in) {
		CRC32 crc = new CRC32();
		CheckedOutputStream out = null;
		if (in != null) {
			try {
				out = new CheckedOutputStream(new NullOutputStream(), crc);
				IOUtils.copy(in, out);
				return out.getChecksum().getValue();
			} catch (IOException e) {
				// Ignore
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// Ignore
					}
				}
			}
		}
		return -1;
	}
	
	public static String getMD5(InputStream in) {
		DigestOutputStream out = null;
		if (in != null) {
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				out = new DigestOutputStream(new NullOutputStream(), md);
				IOUtils.copy(in, out);
				return Base64.encodeBase64String(md.digest());
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("This should never happen in a sensible world, no MD5");
			} catch (IOException e) {
				// Ignore
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// Ignore
					}
				}
			}
		}
		return null;
	}
}
