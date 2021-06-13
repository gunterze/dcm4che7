package org.dcm4che7.util;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since May 2021
 */
public class TagUtils {
    private static final char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String toString(int tag) {
        return new String(toCharArray(tag));
    }

    public static char[] toCharArray(int tag) {
        return new char[]{
                '(',
                HEX_DIGITS[(tag >>> 28)],
                HEX_DIGITS[(tag >>> 24) & 0xF],
                HEX_DIGITS[(tag >>> 20) & 0xF],
                HEX_DIGITS[(tag >>> 16) & 0xF],
                ',',
                HEX_DIGITS[(tag >>> 12) & 0xF],
                HEX_DIGITS[(tag >>> 8) & 0xF],
                HEX_DIGITS[(tag >>> 4) & 0xF],
                HEX_DIGITS[tag & 0xF],
                ')'};
    }

    public static String toHexString(int tag) {
        return new String(toHexChars(tag));
    }

    public static char[] toHexChars(int tag) {
        return new char[]{
                HEX_DIGITS[(tag >>> 28)],
                HEX_DIGITS[(tag >>> 24) & 0xF],
                HEX_DIGITS[(tag >>> 20) & 0xF],
                HEX_DIGITS[(tag >>> 16) & 0xF],
                HEX_DIGITS[(tag >>> 12) & 0xF],
                HEX_DIGITS[(tag >>> 8) & 0xF],
                HEX_DIGITS[(tag >>> 4) & 0xF],
                HEX_DIGITS[tag & 0xF]};
    }

    public static int groupNumber(int tag) {
        return tag >>> 16;
    }

    public static int elementNumber(int tag) {
        return tag & 0xFFFF;
    }

    public static boolean isGroupLength(int tag) {
        return elementNumber(tag) == 0;
    }

    public static boolean isPrivateCreator(int tag) {
        return (tag & 0x00010000) != 0 && (tag & 0x0000FF00) == 0 && (tag & 0x000000F0) != 0;
    }

    public static boolean isPrivateTag(int tag) {
        return (tag & 0x00010000) != 0 && (tag & 0x0000FF00) != 0;
    }

    public static int creatorTagOf(int tag) {
        return (tag & 0xffff0000) | ((tag >>> 8) & 0xff);
    }

    public static int groupLengthTagOf(int tag) {
        return tag & 0xffff0000;
    }
}
