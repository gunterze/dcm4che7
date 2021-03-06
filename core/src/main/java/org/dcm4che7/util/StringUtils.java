package org.dcm4che7.util;

import java.util.Objects;
import java.util.Optional;

public class StringUtils {

    public static String[] EMPTY_STRINGS = {};

    public enum Trim {
        NONE,
        TRAILING {
            @Override
            protected int endIndex(String s, int beginIndex, int endIndex) {
                while (beginIndex < endIndex && s.charAt(endIndex - 1) <= ' ')
                    endIndex--;

                return endIndex;
            }
        },
        LEADING {
            @Override
            protected int beginIndex(String s, int beginIndex, int endIndex) {
                while (beginIndex < endIndex && s.charAt(beginIndex) <= ' ')
                    beginIndex++;

                return beginIndex;
            }
        },
        LEADING_AND_TRAILING {
            @Override
            protected int beginIndex(String s, int beginIndex, int endIndex) {
                return LEADING.beginIndex(s, beginIndex, endIndex);
            }

            @Override
            protected int endIndex(String s, int beginIndex, int endIndex) {
                return TRAILING.endIndex(s, beginIndex, endIndex);
            }
        };

        protected int beginIndex(String s, int beginIndex, int endIndex) {
            return beginIndex;
        }

        protected int endIndex(String s, int beginIndex, int endIndex) {
            return endIndex;
        }

        public String substring(String s, int beginIndex, int endIndex) {
            beginIndex = beginIndex(s, beginIndex, endIndex);
            endIndex = endIndex(s, beginIndex, endIndex);
            return s.substring(beginIndex, endIndex);
        }

    }

    public static String trim(String s, Trim trim) {
        return trim.substring(s, 0, s.length());
    }

    public static String requireNonEmpty(String s) {
        if (s.isEmpty())
            throw new IllegalArgumentException("empty");
        return s;
    }

    public static String requireNonBlank(String s) {
        if (s.isBlank())
            throw new IllegalArgumentException("blank");
        return s;
    }

    public static Optional<String> optionalOf(String s) {
        return s == null || s.isEmpty() ? Optional.empty() : Optional.of(s);
    }

    public static String cut(String s, int length, char delim, int index) {
        return cut(s, length, delim, index, Trim.NONE);
    }

    public static String cut(String s, int length, char delim, int index, Trim trim) {
        if (length == 0)
            return "";

        int beginIndex = 0;
        int endIndex;
        while ((endIndex = s.indexOf(delim, beginIndex)) > 0 && endIndex <= length && index-- > 0) {
            beginIndex = endIndex + 1;
        }
        if (index > 0)
            return "";

        return trim.substring(s, beginIndex, index == 0 ? length : endIndex);
    }

    public static String[] split(String s, char delim) {
        return split(s, s.length(), delim, Trim.NONE);
    }

    public static String[] split(String s, int length, char delim) {
        return split(s, length, delim, Trim.NONE);
    }

    public static String[] split(String s, int length, char delim, Trim trim) {
        if (length == 0)
            return EMPTY_STRINGS;

        int index;
        if ((index = s.lastIndexOf(delim, length - 1)) < 0)
            return new String[] { trim.substring(s, 0, length) };

        int count = 2;
        while ((index = s.lastIndexOf(delim, index - 1)) >= 0) {
            count++;
        }
        String[] ss = new String[count];
        int endIndex = length;
        while (--count > 0) {
            index = s.lastIndexOf(delim, endIndex - 1);
            ss[count] = trim.substring(s, index + 1, endIndex);
            endIndex = index;
        }
        ss[0] = trim.substring(s, 0, endIndex);
        return ss;
    }

    public static String join(String[] ss, int off, int len, char delim) {
        if (ss == null)
            return null;

        if (len == 0)
            return "";

        if (len == 1)
            return Objects.requireNonNullElse(ss[off], "");

        StringBuilder sb = new StringBuilder(Objects.requireNonNullElse(ss[off], ""));
        for (int i = off + 1; i < len; i++)
            sb.append(delim).append(Objects.requireNonNullElse(ss[i], ""));

        return sb.toString();
    }

    public static String trimDS(String s) {
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s.replace(".0E", "E");
    }

    public static String trimAndNullifyEmpty(String s) {
        return s == null || (s = s.trim()).isEmpty() ? null : s;
    }
}
