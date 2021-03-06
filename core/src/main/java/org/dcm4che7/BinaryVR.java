package org.dcm4che7;

import org.dcm4che7.MemoryCache.DicomInput;
import org.dcm4che7.util.OptionalFloat;
import org.dcm4che7.util.TagUtils;
import org.dcm4che7.util.ToggleEndian;

import java.math.BigInteger;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Mar 2021
 */
enum BinaryVR implements VRType {
    AT(4, ToggleEndian.SHORT){
        @Override
        int intAt(DicomInput input, long pos) {
            return input.tagAt(pos);
        }

        @Override
        public OptionalLong longValue(DicomInput input, long valpos, int vallen, int index) {
            return OptionalLong.empty();
        }

        @Override
        public OptionalFloat floatValue(DicomInput input, long valpos, int vallen, int index) {
            return OptionalFloat.empty();
        }

        @Override
        public OptionalDouble doubleValue(DicomInput input, long valpos, int vallen, int index) {
            return OptionalDouble.empty();
        }

        @Override
        String stringAt(DicomInput input, long pos) {
            return TagUtils.toHexString(input.tagAt(pos));
        }

        @Override
        void intToBytes(int val, byte[] b, int off) {
            ByteOrder.LITTLE_ENDIAN.tagToBytes(val, b, off);
        }
    },
    FD(8, ToggleEndian.LONG){
        @Override
        int intAt(DicomInput input, long pos) {
            return (int) doubleAt(input, pos);
        }

        @Override
        long longAt(DicomInput input, long pos) {
            return (long) doubleAt(input, pos);
        }

        @Override
        float floatAt(DicomInput input, long pos) {
            return (float) doubleAt(input, pos);
        }

        @Override
        double doubleAt(DicomInput input, long pos) {
            return Double.longBitsToDouble(input.longAt(pos));
        }

        @Override
        String stringAt(DicomInput input, long pos) {
            return Double.toString(doubleAt(input, pos));
        }

        @Override
        void intToBytes(int val, byte[] b, int off) {
            ByteOrder.LITTLE_ENDIAN.longToBytes(Double.doubleToRawLongBits(val), b, off);
        }
    },
    FL(4, ToggleEndian.INT){
        @Override
        int intAt(DicomInput input, long pos) {
            return (int) floatAt(input, pos);
        }

        @Override
        long longAt(DicomInput input, long pos) {
            return (long) floatAt(input, pos);
        }

        @Override
        float floatAt(DicomInput input, long pos) {
            return Float.intBitsToFloat(input.intAt(pos));
        }

        @Override
        String stringAt(DicomInput input, long pos) {
            return Float.toString(floatAt(input, pos));
        }

        @Override
        void intToBytes(int val, byte[] b, int off) {
            ByteOrder.LITTLE_ENDIAN.intToBytes(Float.floatToRawIntBits(val), b, off);
        }
    },
    OB(1, null){
        @Override
        int intAt(DicomInput input, long pos) {
            return input.byteAt(pos);
        }
    },
    SL(4, ToggleEndian.INT),
    SS(2, ToggleEndian.SHORT){
        @Override
        int intAt(DicomInput input, long pos) {
            return input.shortAt(pos);
        }

        @Override
        void intToBytes(int val, byte[] b, int off) {
            ByteOrder.LITTLE_ENDIAN.shortToBytes(val, b, off);
        }
    },
    SV(8, ToggleEndian.LONG){
        @Override
        int intAt(DicomInput input, long pos) {
            return (int) input.longAt(pos);
        }

        @Override
        long longAt(DicomInput input, long pos) {
            return input.longAt(pos);
        }

        @Override
        float floatAt(DicomInput input, long pos) {
            return input.longAt(pos);
        }

        @Override
        double doubleAt(DicomInput input, long pos) {
            return input.longAt(pos);
        }

        @Override
        String stringAt(DicomInput input, long pos) {
            return Long.toString(input.longAt(pos));
        }

        @Override
        void intToBytes(int val, byte[] b, int off) {
            ByteOrder.LITTLE_ENDIAN.longToBytes(val, b, off);
        }
    },
    UL(4, ToggleEndian.INT){
        @Override
        long longAt(DicomInput input, long pos) {
            return input.intAt(pos) & 0xffffffffL;
        }

        @Override
        String stringAt(DicomInput input, long pos) {
            return Integer.toUnsignedString(input.intAt(pos));
        }
    },
    US(2, ToggleEndian.SHORT) {
        @Override
        int intAt(DicomInput input, long pos) {
            return input.ushortAt(pos);
        }

        @Override
        void intToBytes(int val, byte[] b, int off) {
            ByteOrder.LITTLE_ENDIAN.shortToBytes(val, b, off);
        }
    },
    UV(8, ToggleEndian.LONG){
        @Override
        int intAt(DicomInput input, long pos) {
            return (int) input.longAt(pos);
        }

        @Override
        long longAt(DicomInput input, long pos) {
            return input.longAt(pos);
        }

        @Override
        float floatAt(DicomInput input, long pos) {
            long l = input.longAt(pos);
            return l < 0 ? toUnsignedBigInteger(l).floatValue() : l;
        }

        @Override
        double doubleAt(DicomInput input, long pos) {
            long l = input.longAt(pos);
            return l < 0 ? toUnsignedBigInteger(l).doubleValue() : l;
        }

        @Override
        String stringAt(DicomInput input, long pos) {
            return Long.toUnsignedString(input.longAt(pos));
        }

        @Override
        void intToBytes(int val, byte[] b, int off) {
            ByteOrder.LITTLE_ENDIAN.longToBytes(val & 0xffffffffL, b, off);
        }
    };

    private static BigInteger toUnsignedBigInteger(long l) {
        return new BigInteger(1, new byte[]{
                (byte) (l >> 56),
                (byte) (l >> 48),
                (byte) (l >> 40),
                (byte) (l >> 32),
                (byte) (l >> 24),
                (byte) (l >> 16),
                (byte) (l >> 8),
                (byte) l});
    }

    final int bytes;
    final ToggleEndian toggleEndian;

    BinaryVR(int bytes, ToggleEndian toggleEndian) {
        this.bytes = bytes;
        this.toggleEndian = toggleEndian;
    }

    @Override
    public ToggleEndian toggleEndian() {
        return toggleEndian;
    }

    @Override
    public OptionalLong longValue(DicomInput input, long valpos, int vallen, int index) {
        return (vallen / bytes) > index
                ? OptionalLong.of(longAt(input, valpos + (index * bytes)))
                : OptionalLong.empty();
    }

    @Override
    public OptionalInt intValue(DicomInput input, long valpos, int vallen, int index) {
        return (vallen / bytes) > index
                ? OptionalInt.of(intAt(input, valpos + (index * bytes)))
                : OptionalInt.empty();
    }

    @Override
    public OptionalFloat floatValue(DicomInput input, long valpos, int vallen, int index) {
        return (vallen / bytes) > index
                ? OptionalFloat.of(floatAt(input, valpos + (index * bytes)))
                : OptionalFloat.empty();
    }

    @Override
    public OptionalDouble doubleValue(DicomInput input, long valpos, int vallen, int index) {
        return (vallen / bytes) > index
                ? OptionalDouble.of(doubleAt(input, valpos + (index * bytes)))
                : OptionalDouble.empty();
    }

    @Override
    public Optional<String> stringValue(DicomInput input, long valpos, int vallen, int index,
            DicomObject dicomObject) {
        OptionalInt i = intValue(input, valpos, vallen, index);
        return i.isPresent() ? Optional.of(Integer.toString(i.getAsInt())) : Optional.empty();
    }

    @Override
    public DicomElement elementOf(DicomObject dcmObj, int tag, VR vr, int val) {
        byte[] b = new byte[bytes];
        intToBytes(val, b, 0);
        return new ByteArrayElement(dcmObj, tag, vr, b);
    }

    @Override
    public DicomElement elementOf(DicomObject dcmObj, int tag, VR vr, int... vals) {
        if (vals.length == 0) {
            return new BasicDicomElement(dcmObj, tag, vr, 0);
        }
        byte[] b = new byte[vals.length * bytes];
        for (int i = 0; i < vals.length; i++) {
            intToBytes(vals[i], b, i * bytes);
        }
        return new ByteArrayElement(dcmObj, tag, vr, b);
    }

    int intAt(DicomInput input, long pos) {
        return input.intAt(pos);
    }

    long longAt(DicomInput input, long pos) {
        return intAt(input, pos);
    }

    float floatAt(DicomInput input, long pos) {
        return intAt(input, pos);
    }

    double doubleAt(DicomInput input, long pos) {
        return floatAt(input, pos);
    }

    String stringAt(DicomInput input, long pos) {
        return Integer.toString(intAt(input, pos));
    }

    void intToBytes(int val, byte[] b, int off) {
        ByteOrder.LITTLE_ENDIAN.intToBytes(val, b, off);
    }

    @Override
    public StringBuilder promptValueTo(DicomInput input, long valpos, int vallen, DicomObject dicomObject,
                                       StringBuilder appendTo, int maxLength) {
        appendTo.append(" [");
        int n = vallen / bytes;
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                appendTo.append('\\');
            }
            appendTo.append(stringAt(input, valpos + i * bytes));
            if (appendTo.length() >= maxLength) {
                appendTo.setLength(maxLength);
                return appendTo;
            }
        }
        return appendTo.append(']');
    }
}
