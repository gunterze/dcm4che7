package org.dcm4che7;

import org.dcm4che7.util.OptionalFloat;
import org.dcm4che7.util.ToggleEndian;

import java.io.*;
import java.util.*;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Mar 2021
 */
class MemoryCache {
    private static final int MAX_BUFFER_SIZE = 2048;
    private final ArrayList<byte[]> blocks = new ArrayList<>();
    private long limit;
    private boolean eof;
    private final LinkedList<Segment> skippedBytes = new LinkedList<>();

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

    public void skipFrom(InputStream in, long pos, int len, OutputStream out) throws IOException {
        int skip = (int) (pos + len - limit);
        long pos1 = pos - skippedBytes(pos);
        if (out != null) {
            writeTo(out, pos1, skip > 0 ? len - skip : len);
        }
        if (skip > 0) {
            if (eof)
                throw new EOFException();

            if (out == null)
                skipAll(in, skip);
            else
                transferTo(in, out, skip);

            limit += skip;
        } else if (skip < 0) {
            pos1 = arraycopy(pos1 + len, pos1, -skip);
        }
        int index = blockIndex(pos1);
        byte[] b = blocks.get(index);
        int off = blockOffset(b, pos1);
        while (blocks.size() > index + 1) {
            blocks.remove(index + 1);
        }
        if (!eof) {
            int read = in.readNBytes(b, off, b.length - off);
            eof = off + read < b.length;
            this.limit += read;
        }
        skippedBytes.add(new Segment(pos, len));
    }

    private void writeTo(OutputStream out, long pos, int len) throws IOException {
        while (len > 0) {
            byte[] b = blocks.get(blockIndex(pos));
            int off = blockOffset(b, pos);
            int write = Math.min(b.length - off, len);
            out.write(b, off, write);
            pos += write;
            len -= write;
        }
    }

    private long arraycopy(long srcPos, long destPos, int len) {
        while (len > 0) {
            byte[] src = blocks.get(blockIndex(srcPos));
            int srcOff = blockOffset(src, srcPos);
            byte[] dest = blocks.get(blockIndex(destPos));
            int destOff = blockOffset(dest, destPos);
            int copy = Math.min(Math.min(src.length - srcOff, dest.length - destOff), len);
            System.arraycopy(src, srcOff, dest, destOff, copy);
            srcPos += copy;
            destPos += copy;
            len -= copy;
        }
        return destPos;
    }

    private void skipAll(InputStream in, long n) throws IOException {
        long nr;
        do {
            if ((nr = in.skip(n)) == 0)
                throw new EOFException();

        } while ((n -= nr) > 0);
    }

    private void transferTo(InputStream in, OutputStream out, long n) throws IOException {
        byte[] b = new byte[(int) Math.min(MAX_BUFFER_SIZE, n)];
        int nr;
        do {
            nr = (int) Math.min(b.length, n);
            if (in.readNBytes(b, 0, nr) < nr)
                throw new EOFException();

            out.write(b, 0, nr);
        } while ((n -= nr) > 0);
    }

    private long skippedBytes(long pos) {
        long len = 0L;
        for (Segment skipped : skippedBytes) {
            if (pos <= skipped.pos) return len;
            len += skipped.length;
        }
        return len;
    }

    private static record Segment(long pos, int length){
        long end() {
            return pos + length;
        }
    };

    byte byteAt(long pos) {
        pos -= skippedBytes(pos);
        byte[] b = blocks.get(blockIndex(pos));
        return b[blockOffset(b, pos)];
    }

    short shortAt(long pos, ByteOrder byteOrder) {
        pos -= skippedBytes(pos);
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
        pos -= skippedBytes(pos);
        byte[] b = blocks.get(blockIndex(pos));
        int offset = blockOffset(b, pos);
        return (offset + 3 < b.length)
                ? byteOrder.bytesToInt(b, offset)
                : byteOrder.bytesToInt(byteAt(pos), byteAt(pos + 1), byteAt(pos + 2), byteAt(pos + 3));
    }

    int tagAt(long pos, ByteOrder byteOrder) {
        pos -= skippedBytes(pos);
        byte[] b = blocks.get(blockIndex(pos));
        int offset = blockOffset(b, pos);
        return (offset + 3 < b.length)
                ? byteOrder.bytesToTag(b, offset)
                : byteOrder.bytesToTag(byteAt(pos), byteAt(pos + 1), byteAt(pos + 2), byteAt(pos + 3));
    }

    long longAt(long pos, ByteOrder byteOrder) {
        pos -= skippedBytes(pos);
        byte[] b = blocks.get(blockIndex(pos));
        int offset = blockOffset(b, pos);
        return (offset + 7 < b.length)
                ? byteOrder.bytesToLong(b, offset)
                : byteOrder.bytesToLong(byteAt(pos), byteAt(pos + 1), byteAt(pos + 2), byteAt(pos + 3),
                                byteAt(pos + 4), byteAt(pos + 5), byteAt(pos + 6), byteAt(pos + 7));
    }
    String stringAt(long pos, int length, SpecificCharacterSet cs) {
        pos -= skippedBytes(pos);
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

    void writeBytesTo(long pos, int length, OutputStream out) throws IOException {
        int i = blockIndex(pos);
        byte[] src = blocks.get(i);
        int srcPos = blockOffset(src, pos);
        int rlen = Math.min(length, src.length - srcPos);
        out.write(src, srcPos, rlen);
        int remaining = length;
        while ((remaining -= rlen) > 0L) {
            src = blocks.get(++i);
            rlen = Math.min(remaining, src.length);
            out.write(src, 0, rlen);
        }
    }

    void writeSwappedBytesTo(long pos, int length, OutputStream out, ToggleEndian toggleEndian, byte[] buf)
            throws IOException {
        if (buf.length == 0 || (buf.length & 7) != 0) {
            throw new IllegalArgumentException("buf.length: " + buf.length);
        }
        int remaining = length;
        int copy = 0;
        while ((remaining -= copy) > 0) {
            pos += copy;
            copy =  Math.min(remaining, buf.length);
            copyBytesTo(pos, buf, 0, copy);
            toggleEndian.apply(buf, copy);
            out.write(buf, 0, copy);
        }
    }

    InputStream inflate(InputStream in, long pos) throws IOException {
        if (fillFrom(in, pos + 2) != pos + 2)
            throw new EOFException();

        int size = (int) (limit - pos);
        PushbackInputStream pushbackInputStream = new PushbackInputStream(in, size);
        long pos1 = pos - skippedBytes(pos);
        byte[] b = blocks.get(blockIndex(pos1));
        int offset = blockOffset(b, pos1);
        pushbackInputStream.unread(b, offset, size);
        InflaterInputStream inflaterInputStream = new InflaterInputStream(pushbackInputStream,
                new Inflater(b[offset] != 120 || b[offset+1] != -100));
        int read = inflaterInputStream.readNBytes(b, offset, b.length - offset);
        eof = offset + read < b.length;
        limit = pos + read;
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

            @Override
            public void writeValueTo(DicomOutputStream dos) throws IOException {
                if (encoding.byteOrder == dos.encoding().byteOrder || vr.type.toggleEndian() == null) {
                    writeBytesTo(valuePos, valueLength, dos);
                } else {
                    writeSwappedBytesTo(valuePos, valueLength, dos, vr.type.toggleEndian(), dos.swapBuffer());
                }
            }
        }
    }
}
