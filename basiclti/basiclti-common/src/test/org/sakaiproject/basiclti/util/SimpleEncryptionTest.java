package org.sakaiproject.basiclti.util;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;


public class SimpleEncryptionTest {

	private String CIPHER = "AES/CBC/PKCS5Padding";
	// Result of SimpleEncryption.encrypt("key", "plain text"));
	private String goodEncrypt128 = "0bdd94442e437fac:d8e4be4ae67a7bdf8f0717cebf425832:133df2f919b2e686a0c4ed5451b5af6f:"+CIPHER;
	private String goodEncrypt256 = "35f744dd15559e9e:afbf6031d0033d5e3c71a00e95f3ec8a:8ebd6a18a8c4e1e3374a5718216cefa6:"+CIPHER;

	private String badMissingEnd128 = "0bdd94442e437fac:d8e4be4ae67a7bdf8f0717cebf425832:133df2f919b2e686a0c4ed5451b5af:"+CIPHER;
	private String badMissingEnd256 = "35f744dd15559e9e:afbf6031d0033d5e3c71a00e95f3ec8a:8ebd6a18a8c4e1e3374a5718216cef:"+CIPHER;

	private String badMissingStart128 = "dd94442e437fac:d8e4bee67a7bdf8f0717cebf425832:133df2f919b2e686a0c4ed5451b5af6f:"+CIPHER;
	private String badMissingStart256 = "f744dd15559e9e:afbf60d0033d5e3c71a00e95f3ec8a:8ebd6a18a8c4e1e3374a5718216cefa6:"+CIPHER;

	private String badNotHex128 = "0bzz94442e437fac:d8e4be4ae67a7bdf8f0717cebf425832:133df2f919b2e686a0c4ed5451b5af6f:"+CIPHER;
	private String badNotHex256 = "35zz44dd15559e9e:afbf6031d0033d5e3c71a00e95f3ec8a:8ebd6a18a8c4e1e3374a5718216cefa6:"+CIPHER;


	@Before
	public void setUp() throws Exception {
	}

	
	@Test(expected=Exception.class)
	public void testBadDecrypt() {
		SimpleEncryption.decrypt("a key", "bad text");
	}
	
	@Test(expected=Exception.class)
	public void testBadDecryptButSplits() {
		SimpleEncryption.decrypt("key", "salt:iv:data:"+CIPHER);
	}

	@Test(expected=Exception.class)
	public void testBadDecryptButSplits3() {
		SimpleEncryption.decrypt("key", "salt:iv:data");
	}
	
	
	@Test(expected=Exception.class)
	public void testBadDecryptLength1() {
		SimpleEncryption.decrypt("key", (SimpleEncryption.getKeyLength() == 128?badMissingEnd128:badMissingEnd256));
	}

	@Test(expected=Exception.class)
	public void testBadDecryptLength2() {
		SimpleEncryption.decrypt("key", (SimpleEncryption.getKeyLength() == 128?badMissingStart128:badMissingStart256));
	}

	@Test(expected=Exception.class)
	public void testBadDecryptNotHex() {
		SimpleEncryption.decrypt("key", (SimpleEncryption.getKeyLength() == 128?badNotHex128:badNotHex256));
	}
	
	@Test(expected=Exception.class)
	public void testBadKey() {
		SimpleEncryption.decrypt("badkey", SimpleEncryption.encrypt("goodkey", "Hello"));
	}

	@Test
	public void test() {
		System.out.println(SimpleEncryption.encrypt("key", "plain text"));
		System.out.println(SimpleEncryption.getKeyLength());
		assertNotNull(SimpleEncryption.encrypt("key", "Hello").length());
		assertFalse("Hello".equals(SimpleEncryption.encrypt("key", "Hello")));
		assertEquals("Hello", SimpleEncryption.decrypt("key", SimpleEncryption.encrypt("key", "Hello")));
		assertEquals("plain text", SimpleEncryption.decrypt("key",
				(SimpleEncryption.getKeyLength() == 128?goodEncrypt128: goodEncrypt256)));
		assertFalse("wrong text".equals(SimpleEncryption.decrypt("key",
				(SimpleEncryption.getKeyLength() == 128?goodEncrypt128: goodEncrypt256))));
	}
	
	@Test
	public void testSalted() {
		assertFalse(SimpleEncryption.encrypt("key", "Hello").equals(SimpleEncryption.encrypt("key", "Hello")));
	}
	
	@Test
	public void testLongerString() {
		String longer = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus sit amet lectus et mi dictum dapibus. Aenean ac nibh non leo tristique ultricies ac eu tellus. Phasellus posuere arcu sollicitudin massa fringilla viverra. Donec sodales orci id odio dignissim eget bibendum odio rutrum. Cras id sem eget felis consequat bibendum. Nam id mauris nec lacus condimentum semper. Nullam eleifend risus et dui aliquam ut luctus tortor suscipit. Proin porta tellus tortor, eu pellentesque ante. Aenean sem eros, porttitor nec laoreet et, aliquam a justo. Vivamus condimentum risus quis lorem sodales viverra. Vivamus mollis lacinia congue. Nulla imperdiet enim sit amet ante sodales in euismod ante porta. Fusce in ullamcorper dolor. Nulla facilisi. Nullam risus nisi, pellentesque at convallis id, commodo a orci. Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
		String encrypted = SimpleEncryption.encrypt("key", longer);
		assertEquals(longer, SimpleEncryption.decrypt("key", encrypted));
	}
	
	@Test
	public void testNullEncrypt() {
		assertNull(SimpleEncryption.encrypt("key", null));
	}
	
	@Test
	public void testNullDecrypt() {
		assertNull(SimpleEncryption.decrypt("key", null));
	}
}
