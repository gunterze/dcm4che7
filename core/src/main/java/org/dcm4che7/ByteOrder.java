package org.dcm4che7;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Mar 2021
 */
public enum ByteOrder {
    LITTLE_ENDIAN {
        @Override
        public short bytesToShort(int b0, int b1) {
            return (short) ((b1 << 8) | b0) ;
        }

        @Override
        public int bytesToInt(int b0, int b1, int b2, int b3) {
            return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        }

        @Override
        public int bytesToTag(int b0, int b1, int b2, int b3) {
            return (b1 << 24) | (b0 << 16) | (b3 << 8) | b2;
        }

        @Override
        public long bytesToLong(int b0, int b1, int b2, int b3, int b4, int b5, int b6, int b7) {
            return ((long) bytesToInt(b4, b5, b6, b7) << 32)
                        | (bytesToInt(b0, b1, b2, b3) & 0xffffffffL);
        }

        @Override
        public void shortToBytes(int val, byte[] dest, int destPos) {
            dest[destPos] = (byte) val;
            dest[destPos + 1] = (byte) (val >> 8);
        }

        @Override
        public void intToBytes(int val, byte[] dest, int destPos) {
            dest[destPos] = (byte) val;
            dest[destPos + 1] = (byte) (val >> 8);
            dest[destPos + 2] = (byte) (val >> 16);
            dest[destPos + 3] = (byte) (val >> 24);
        }

        @Override
        public void tagToBytes(int val, byte[] dest, int destPos) {
            dest[destPos] = (byte) (val >> 16);
            dest[destPos + 1] = (byte) (val >> 24);
            dest[destPos + 2] = (byte) val;
            dest[destPos + 3] = (byte) (val >> 8);
        }

        @Override
        public void longToBytes(long val, byte[] dest, int destPos) {
            intToBytes((int) val, dest, destPos);
            intToBytes((int) (val >> 32), dest, destPos + 4);
        }
    },
    BIG_ENDIAN {
        @Override
        public short bytesToShort(int b0, int b1) {
            return (short) ((b0 << 8) | b1);
        }

        @Override
        public int bytesToInt(int b0, int b1, int b2, int b3) {
            return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        }

        @Override
        public int bytesToTag(int b0, int b1, int b2, int b3) {
            return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        }

        @Override
        public long bytesToLong(int b0, int b1, int b2, int b3, int b4, int b5, int b6, int b7) {
            return ((long) bytesToInt(b0, b1, b2, b3) << 32)
                        | (bytesToInt(b4, b5, b6, b7) & 0xffffffffL);
        }

        @Override
        public void shortToBytes(int val, byte[] dest, int destPos) {
            dest[destPos] = (byte) (val >> 8);
            dest[destPos + 1] = (byte) val;
        }

        @Override
        public void intToBytes(int val, byte[] dest, int destPos) {
            dest[destPos] = (byte) (val >> 24);
            dest[destPos + 1] = (byte) (val >> 16);
            dest[destPos + 2] = (byte) (val >> 8);
            dest[destPos + 3] = (byte) val;
        }

        @Override
        public void tagToBytes(int val, byte[] dest, int destPos) {
            intToBytes(val, dest, destPos);
        }

        @Override
        public void longToBytes(long val, byte[] dest, int destPos) {
            intToBytes((int) (val >> 32), dest, destPos);
            intToBytes((int) val, dest, destPos + 4);
        }
    };

    public short bytesToShort(byte[] b, int index) {
        return bytesToShort(b[index], b[index + 1]);
    }

    public short bytesToShort(byte b0, byte b1) {
        return bytesToShort(b0 & 0xff, b1 & 0xff);
    }

    public abstract short bytesToShort(int b0, int b1);

    public int bytesToInt(byte[] b, int index) {
        return bytesToInt(b[index], b[index + 1], b[index + 2], b[index + 3]);
    }

    public int bytesToInt(byte b0, byte b1, byte b2, byte b3) {
        return bytesToInt(b0 & 0xff, b1 & 0xff, b2 & 0xff, b3 & 0xff);
    }

    public abstract int bytesToInt(int b0, int b1, int b2, int b3);

    public int bytesToTag(byte[] b, int index) {
        return bytesToTag(b[index], b[index + 1], b[index + 2], b[index + 3]);
    }

    public int bytesToTag(byte b0, byte b1, byte b2, byte b3) {
        return bytesToTag(b0 & 0xff, b1 & 0xff, b2 & 0xff, b3 & 0xff);
    }

    public abstract int bytesToTag(int b0, int b1, int b2, int b3);

    public long bytesToLong(byte[] b, int index) {
        return bytesToLong(b[index], b[index + 1], b[index + 2], b[index + 3],
                b[index + 4], b[index + 5], b[index + 6], b[index + 7]);
    }

    public long bytesToLong(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7) {
        return bytesToLong(b0 & 0xff, b1 & 0xff, b2 & 0xff, b3 & 0xff,
                           b4 & 0xff, b5 & 0xff, b6 & 0xff, b7 & 0xff);
    }

    public abstract long bytesToLong(int b0, int b1, int b2, int b3, int b4, int b5, int b6, int b7);

    public abstract void shortToBytes(int val, byte[] dest, int destPos);

    public abstract void intToBytes(int val, byte[] dest, int destPos);

    public abstract void tagToBytes(int val, byte[] dest, int destPos);

    public abstract void longToBytes(long val, byte[] dest, int destPos);
}
