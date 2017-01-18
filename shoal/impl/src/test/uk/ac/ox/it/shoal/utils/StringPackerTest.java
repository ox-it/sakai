package uk.ac.ox.it.shoal.utils;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.it.shoal.utils.StringPacker;

import java.util.Arrays;
import java.util.Collection;

/**
 * Test simple packing/unpacking.
 */
public class StringPackerTest {

    @Test
    public void testSimplePack() {
        Assert.assertEquals("hello"+ StringPacker.SEP+"world", StringPacker.pack(Arrays.asList(new String[]{"hello", "world"})));
    }

    @Test
    public void testSimpleUnpack() {
        Assert.assertArrayEquals(new String[]{"hello", "world"}, StringPacker.unpack("hello"+StringPacker.SEP+ "world").toArray());
    }

    @Test
    public void testRoundTrip() {
        Collection<String> list = Arrays.asList("hello", "world", "", StringPacker.ESC, StringPacker.SEP);
        String packed = StringPacker.pack(list);
        Collection<String> unpackedList = StringPacker.unpack(packed);
        Assert.assertArrayEquals(list.toArray(), unpackedList.toArray());
    }

    @Test
    public void testNullPack() {
        Assert.assertNull(StringPacker.pack(null));
    }

    @Test
    public void testNullUnpack() {
        Assert.assertNull(StringPacker.unpack(null));
    }




}
