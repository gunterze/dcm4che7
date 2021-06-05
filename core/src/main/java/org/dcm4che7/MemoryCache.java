package org.dcm4che7;

import org.dcm4che7.util.OptionalFloat;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.*;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Mar 2021
 */
class MemoryCache {
    private final ArrayList<byte[]> blocks = new ArrayList<>();
    private long limit;
    private boolean eof;

    long limit() {
        return limit;
    }

    byte[] block(int index) {
        return blocks.get(index);
    }

    long fillFrom(InputStream in, long length) throws IOException {
        if (eof) {
            return Math.min(this.limit, length);
        }
        while (this.limit < length) {
            byte[] buf = new byte[blocks.isEmpty() ? 0x100 : 0x80 << blocks.size()];
            int read = in.readNBytes(buf, 0, buf.length);
            blocks.add(buf);
            this.limit += read;
            if (eof = read < buf.length) {
                return Math.min(this.limit, length);
            }
        }
        return length;
    }

    byte byteAt(long pos) {
        byte[] b = blocks.get(blockIndex(pos));
        return b[blockOffset(b, pos)];
    }

    short shortAt(long pos, ByteOrder byteOrder) {
        byte[] b = blocks.get(blockIndex(pos));
        int offset = blockOffset(b, pos);
        return (offset + 1 < b.length)
                ? byteOrder.bytesToShort(b, offset)
                : byteOrder.bytesToShort(byteAt(pos), byteAt(pos + 1));
    }

    int vrcode(long pos) {
        return shortAt(pos, ByteOrder.BIG_ENDIAN);
    }

    int intAt(long pos, ByteOrder byteOrder) {
        byte[] b = blocks.get(blockIndex(pos));
        int offset = blockOffset(b, pos);
        return (offset + 3 < b.length)
                ? byteOrder.bytesToInt(b, offset)
                : byteOrder.bytesToInt(byteAt(pos), byteAt(pos + 1), byteAt(pos + 2), byteAt(pos + 3));
    }

    int tagAt(long pos, ByteOrder byteOrder) {
        byte[] b = blocks.get(blockIndex(pos));
        int offset = blockOffset(b, pos);
        return (offset + 3 < b.length)
                ? byteOrder.bytesToTag(b, offset)
                : byteOrder.bytesToTag(byteAt(pos), byteAt(pos + 1), byteAt(pos + 2), byteAt(pos + 3));
    }

    long longAt(long pos, ByteOrder byteOrder) {
        byte[] b = blocks.get(blockIndex(pos));
        int offset = blockOffset(b, pos);
        return (offset + 7 < b.length)
                ? byteOrder.bytesToLong(b, offset)
                : byteOrder.bytesToLong(byteAt(pos), byteAt(pos + 1), byteAt(pos + 2), byteAt(pos + 3),
                                byteAt(pos + 4), byteAt(pos + 5), byteAt(pos + 6), byteAt(pos + 7));
    }
    String stringAt(long pos, int length, SpecificCharacterSet cs) {
        byte[] b = blocks.get(blockIndex(pos));
        int offset = blockOffset(b, pos);
        return (offset + length <= b.length)
                ? cs.decode(b, offset, length)
                : cs.decode(bytesAt(pos, length), 0, length);
    }

    byte[] bytesAt(long pos, int length) {
        byte[] dest = new byte[length];
        copyBytesTo(pos, dest, 0, length);
        return dest;
    }

    void copyBytesTo(long pos, byte[] dest, int destPos, int length) {
        int i = blockIndex(pos);
        byte[] src = blocks.get(i);
        int srcPos = blockOffset(src, pos);
        int copy =  Math.min(length, src.length - srcPos);
        System.arraycopy(src, srcPos, dest, destPos, copy);
        int remaining = length;
        while ((remaining -= copy) > 0) {
            destPos += copy;
            src = blocks.get(++i);
            copy = Math.min(remaining, src.length);
            System.arraycopy(src, 0, dest, destPos, copy);
        }
    }

    InputStream inflate(InputStream in, long len) throws IOException {
        if (fillFrom(in, len + 2) != len + 2)
            throw new EOFException();

        int size = (int) (limit - len);
        PushbackInputStream pushbackInputStream = new PushbackInputStream(in, size);
        byte[] b = blocks.get(blockIndex(len));
        int offset = blockOffset(b, len);
        pushbackInputStream.unread(b, offset, size);
        InflaterInputStream inflaterInputStream = new InflaterInputStream(pushbackInputStream,
                new Inflater(b[offset] != 120 || b[offset+1] != -100));
        int read = inflaterInputStream.readNBytes(b, offset, b.length - offset);
        eof = offset + read < b.length;
        limit = len + read;
        return inflaterInputStream;
    }

    private static int blockIndex(long pos) {
        int i = 8;
        while ((pos >>> i) != 0)
            i++;
        return i - 8;
    }

    private static int blockOffset(byte[] block, long pos) {
        return (int) (pos & (block.length - 1));
    }

    DicomInput dicomInput(DicomEncoding encoding) {
        return new DicomInput(encoding);
    }

    class DicomInput {
        final DicomEncoding encoding;

        DicomInput(DicomEncoding encoding) {
            this.encoding = encoding;
        }

        byte byteAt(long pos) {
            return MemoryCache.this.byteAt(pos);
        }

        short shortAt(long pos) {
            return MemoryCache.this.shortAt(pos, encoding.byteOrder);
        }

        int ushortAt(long pos) {
            return shortAt(pos) & 0xffff;
        }

        int intAt(long pos) {
            return MemoryCache.this.intAt(pos, encoding.byteOrder);
        }

        int tagAt(long pos) {
            return MemoryCache.this.tagAt(pos, encoding.byteOrder);
        }

        long longAt(long pos) {
            return MemoryCache.this.longAt(pos, encoding.byteOrder);
        }

        String stringAt(long pos, int len, SpecificCharacterSet cs) {
            return MemoryCache.this.stringAt(pos, len, cs);
        }

        DicomElement newDicomElement(DicomObject dcmObj, int tag, VR vr, int valueLength, long valuePos) {
            return vr == VR.SQ || (vr == VR.UN && valueLength == -1)
                    ? new DicomSequence(dcmObj, tag, valueLength)
                    : new ParsedDicomElement(dcmObj, tag, vr, valueLength, valuePos);
        }

        class ParsedDicomElement extends BasicDicomElement {
            private final long valuePos;

            public ParsedDicomElement(DicomObject dcmObj, int tag, VR vr, int valueLength, long valuePos) {
                super(dcmObj, tag, vr, valueLength);
                this.valuePos = valuePos;
            }

            @Override
            public OptionalInt intValue(int index) {
                return vr.type.intValue(DicomInput.this, valuePos, valueLength, index);
            }

            @Override
            public OptionalLong longValue(int index) {
                return vr.type.longValue(DicomInput.this, valuePos, valueLength, index);
            }

            @Override
            public OptionalFloat floatValue(int index) {
                return vr.type.floatValue(DicomInput.this, valuePos, valueLength, index);
            }

            @Override
            public OptionalDouble doubleValue(int index) {
                return vr.type.doubleValue(DicomInput.this, valuePos, valueLength, index);
            }

            @Override
            public Optional<String> stringValue(int index) {
                return vr.type.stringValue(DicomInput.this, valuePos, valueLength, index, dicomObject);
            }

            @Override
            public String[] stringValues() {
                return vr.type.stringValues(DicomInput.this, valuePos, valueLength, dicomObject);
            }

            @Override
            protected StringBuilder promptValueTo(StringBuilder appendTo, int maxLength) {
                return vr.type.promptValueTo(DicomInput.this, valuePos, valueLength, dicomObject, appendTo, maxLength);
            }
        }
    }
}
