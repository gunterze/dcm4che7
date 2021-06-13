package org.dcm4che7;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Mar 2021
 */
public class MemoryCacheTest {

    private static InputStream createInputStream(int length, int modulo) {
        byte[] buf = new byte[length];
        for (int i = 0; i < length; i++) {
            buf[i] = (byte) (i % modulo);
        }
        return new ByteArrayInputStream(buf);
    }

    @Test
    public void fillFrom() throws IOException {
        MemoryCache memoryCache = new MemoryCache();
        InputStream in = createInputStream(1000, 255);
        assertEquals(400, memoryCache.fillFrom(in, 400));
        assertEquals(800, memoryCache.fillFrom(in, 800));
        assertEquals(1000, memoryCache.fillFrom(in, 1200));
        assertEquals((short) 0x100, memoryCache.shortAt(510, ByteOrder.LITTLE_ENDIAN));
        assertEquals((short) 0x102, memoryCache.shortAt(511, ByteOrder.BIG_ENDIAN));
        assertEquals(0xfefdfc, memoryCache.intAt(252, ByteOrder.LITTLE_ENDIAN));
        assertEquals(0xfe000102, memoryCache.intAt(254, ByteOrder.BIG_ENDIAN));
        assertEquals(0x0100fefdfcfbfaf9L, memoryCache.longAt(504, ByteOrder.LITTLE_ENDIAN));
        assertEquals(0xfdfe000102030405L, memoryCache.longAt(508, ByteOrder.BIG_ENDIAN));
    }
}