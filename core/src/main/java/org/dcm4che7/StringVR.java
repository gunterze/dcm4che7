package org.dcm4che7;

import org.dcm4che7.MemoryCache.DicomInput;
import org.dcm4che7.util.StringUtils;
import org.dcm4che7.util.StringUtils.Trim;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Mar 2021
 */
enum StringVR implements VRType {
    ASCII(VM.MULTI, Trim.LEADING_AND_TRAILING, StringVR::ascii),
    STRING(VM.MULTI, Trim.LEADING_AND_TRAILING, DicomObject::specificCharacterSet),
    TEXT(VM.SINGLE, Trim.TRAILING, DicomObject::specificCharacterSet),
    DS(VM.MULTI, Trim.LEADING_AND_TRAILING, StringVR::ascii) {
        @Override
        public OptionalInt intValue(DicomInput dicomInput, long valuePos, int valueLength, int index) {
            Optional<String> s = stringValue(dicomInput, valuePos, valueLength, index, null);
            return s.isPresent() ? OptionalInt.of((int) Double.parseDouble(s.get())) : OptionalInt.empty();
        }
    },
    IS(VM.MULTI, Trim.LEADING_AND_TRAILING, StringVR::ascii) {
        @Override
        public OptionalInt intValue(DicomInput dicomInput, long valuePos, int valueLength, int index) {
            Optional<String> s = stringValue(dicomInput, valuePos, valueLength, index, null);
            return s.isPresent() ? OptionalInt.of(Integer.parseInt(s.get())) : OptionalInt.empty();
        }
    },
    PN(VM.MULTI, Trim.LEADING_AND_TRAILING, DicomObject::specificCharacterSet),
    UC(VM.MULTI, Trim.TRAILING, StringVR::ascii),
    UR(VM.SINGLE, Trim.LEADING_AND_TRAILING, StringVR::ascii),
    UI(VM.MULTI, Trim.LEADING_AND_TRAILING, StringVR::ascii);

    private final VM vm;
    private final StringUtils.Trim trim;
    private final Function<DicomObject, SpecificCharacterSet> asciiOrCS;

    StringVR(VM vm, StringUtils.Trim trim, Function<DicomObject, SpecificCharacterSet> asciiOrCS) {
        this.vm = vm;
        this.trim = trim;
        this.asciiOrCS = asciiOrCS;
    }

    @Override
    public Optional<String> stringValue(DicomInput input, long valuePos, int valueLen, int index, DicomObject dcmobj) {
        return stringValue(input.stringAt(valuePos, valueLen, asciiOrCS.apply(dcmobj)), index);
    }

    @Override
    public String[] stringValues(DicomInput input, long valuePos, int valueLen, DicomObject dcmobj) {
        return stringValues(input.stringAt(valuePos, valueLen, asciiOrCS.apply(dcmobj)));
    }

    @Override
    public Optional<String> stringValue(String value, int index) {
        return vm.cut(value, index, trim);
    }

    @Override
    public String[] stringValues(String value) {
        return vm.split(value, trim);
    }

    @Override
    public StringBuilder promptValueTo(DicomInput input, long valuePos, int valueLen, DicomObject dcmobj,
            StringBuilder sb, int maxLength) {
        sb.append(" [");
        sb.append(StringUtils.trim(input.stringAt(valuePos, valueLen, asciiOrCS.apply(dcmobj)), trim));
        sb.append(']');
        if (sb.length() > maxLength) {
            sb.setLength(maxLength);
        }
        return sb;
    }

    private static SpecificCharacterSet ascii(DicomObject dicomObject) {
        return SpecificCharacterSet.ASCII;
    }

    enum VM {
        SINGLE {
            @Override
            Optional<String> cut(String s, int index, StringUtils.Trim trim) {
                return index == 0
                        ? StringUtils.optionalOf(StringUtils.trim(s, trim))
                        : Optional.empty();
            }

            @Override
            String[] split(String s, StringUtils.Trim trim) {
                return s.isEmpty()
                        ? StringUtils.EMPTY_STRINGS
                        : new String[]{StringUtils.trim(s, trim)};
            }

            @Override
            String join(VR vr, String[] s) {
                if (s.length == 1) {
                    return s[0];
                }
                throw new IllegalArgumentException(String.format("VR: %s does not allow multiple values", vr));
            }
        },
        MULTI {
            @Override
            Optional<String> cut(String s, int index, StringUtils.Trim trim) {
                return StringUtils.optionalOf(StringUtils.cut(s, s.length(), '\\', index, trim));
            }

            @Override
            String[] split(String s, StringUtils.Trim trim) {
                return StringUtils.split(s, s.length(), '\\', trim);
            }

            @Override
            String join(VR vr, String[] ss) {
                return StringUtils.join(ss, 0, ss.length, '\\');
            }
        };

        abstract Optional<String> cut(String s, int index, StringUtils.Trim trim);

        abstract String[] split(String s, StringUtils.Trim trim);

        abstract String join(VR vr, String[] s);
    }
}
