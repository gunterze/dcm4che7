package org.dcm4che7;

import org.dcm4che7.MemoryCache.DicomInput;
import org.dcm4che7.util.OptionalFloat;
import org.dcm4che7.util.StringUtils;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since May 2021
 */
interface VRType {
    default OptionalInt intValue(DicomInput dicomInput, long valuePos, int valueLength, int index) {
        return OptionalInt.empty();
    }

    default OptionalLong longValue(DicomInput dicomInput, long valpos, int vallen, int index) {
        return OptionalLong.empty();
    }

    default OptionalFloat floatValue(DicomInput dicomInput, long valpos, int vallen, int index) {
        return OptionalFloat.empty();
    }

    default OptionalDouble doubleValue(DicomInput dicomInput, long valuePos, int valueLength, int index) {
        return OptionalDouble.empty();
    }

    default Optional<String> stringValue(DicomInput dicomInput, long valuePos, int valueLength, int index,
            DicomObject dicomObject) {
        return Optional.empty();
    }

    default String[] stringValues(DicomInput dicomInput, long valuePos, int valueLen, DicomObject dicomObject) {
        return StringUtils.EMPTY_STRINGS;
    }

    default Optional<String> stringValue(String value, int index) {
        return Optional.empty();
    }

    default String[] stringValues(String value) {
        return StringUtils.EMPTY_STRINGS;
    }

    VRType SQ = new VRType(){
        @Override
        public StringBuilder promptValueTo(DicomInput dicomInput, long valuePos, int valueLength,
                                           DicomObject dicomObject, StringBuilder sb, int maxLength) {
            throw new UnsupportedOperationException();
        }
    };

    VRType UN = new VRType(){
        @Override
        public Optional<String> stringValue(DicomInput dicomInput, long valuePos, int valueLength, int index,
                                            DicomObject dicomObject) {
            return index > 0
                    ? Optional.empty()
                    : Optional.of(
                            promptValueTo(dicomInput, valuePos, valueLength,
                                    new StringBuilder(Math.min(valueLength, 16)))
                                    .toString());
        }

        private StringBuilder promptValueTo(DicomInput input, long valpos, int vallen, StringBuilder sb) {
            for (int i = 0; i < vallen; i++) {
                int c = input.byteAt(valpos + i);
                if (c < ' ' || c == '\\' || c == 127) {
                    sb.append('\\');
                    sb.append((char) ('0' + ((c >> 6) & 7)));
                    sb.append((char) ('0' + ((c >> 3) & 7)));
                    sb.append((char) ('0' + (c & 7)));
                } else {
                    sb.append((char) c);
                }
            }
            return sb;
        }

        @Override
        public StringBuilder promptValueTo(DicomInput dicomInput, long valuePos, int valueLength,
                                           DicomObject dicomObject, StringBuilder sb, int maxLength) {
            sb.append(" [");
            int truncate = Math.max(0, valueLength - sb.length() - maxLength);
            promptValueTo(dicomInput, valuePos, valueLength - truncate, sb);
            if (truncate < 0) {
                sb.append(']');
            }
            if (sb.length() > maxLength) {
                sb.setLength(maxLength);
            }
            return sb;
        }
    };

    StringBuilder promptValueTo(DicomInput dicomInput, long valuePos, int valueLength,
                                DicomObject dicomObject, StringBuilder sb, int maxLength);
}
