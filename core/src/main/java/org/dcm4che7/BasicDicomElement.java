package org.dcm4che7;

import org.dcm4che7.util.OptionalFloat;
import org.dcm4che7.util.StringUtils;
import org.dcm4che7.util.TagUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since May 2021
 */
class BasicDicomElement implements DicomElement {
    static final int TO_STRING_LENGTH = 78;
    protected final DicomObject dicomObject;
    protected final int tag;
    protected final VR vr;
    protected final int valueLength;

    BasicDicomElement(DicomObject dicomObject, int tag, VR vr, int valueLength) {
        this.dicomObject = dicomObject;
        this.tag = tag;
        this.vr = Objects.requireNonNull(vr);
        this.valueLength = valueLength;
    }

    @Override
    public DicomObject dicomObject() {
        return dicomObject;
    }

    @Override
    public int tag() {
        return tag;
    }

    @Override
    public VR vr() {
        return vr;
    }

    @Override
    public int valueLength() {
        return valueLength;
    }

    @Override
    public int vm() {
        return 0;
    }

    @Override
    public OptionalInt intValue(int index) {
        return OptionalInt.empty();
    }

    @Override
    public OptionalLong longValue(int index) {
        return OptionalLong.empty();
    }

    @Override
    public OptionalFloat floatValue(int index) {
        return OptionalFloat.empty();
    }

    @Override
    public OptionalDouble doubleValue(int index) {
        return OptionalDouble.empty();
    }

    @Override
    public Optional<String> stringValue(int index) {
        return Optional.empty();
    }

    @Override
    public String[] stringValues() {
        return StringUtils.EMPTY_STRINGS;
    }

    @Override
    public void addItem(DicomObject item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DicomObject addItem() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<DicomObject> getItem(int index) throws IOException {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(TO_STRING_LENGTH), TO_STRING_LENGTH).toString();
    }

    @Override
    public StringBuilder promptTo(StringBuilder appendTo, int maxLength) {
        promptLevelTo(appendTo).append(TagUtils.toCharArray(tag));
        if (vr != VR.NONE) appendTo.append(' ').append(vr);
        appendTo.append(" #").append(valueLength);
        promptValueTo(appendTo, maxLength);
        if (appendTo.length() < maxLength) {
            appendTo.append(" ").append(
                    ElementDictionary.keywordOf(tag, dicomObject.privateCreatorOf(tag).orElse(null)));
            if (appendTo.length() > maxLength) {
                appendTo.setLength(maxLength);
            }
        }
        return appendTo;
    }

    @Override
    public int promptItemsTo(StringBuilder appendTo, int maxColumns, int maxLines) {
        return maxLines;
    }

    protected StringBuilder promptValueTo(StringBuilder appendTo, int maxLength) {
        return appendTo.append(" []");
    }

    @Override
    public StringBuilder promptLevelTo(StringBuilder appendTo) {
        for (DicomElement seq = dicomObject.sequence(); seq != null; seq = seq.dicomObject().sequence()) {
            appendTo.append('>');
        }
        return appendTo;
    }

}
