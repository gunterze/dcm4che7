package org.dcm4che7;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Aug 2018
 */
class StringElement extends BasicDicomElement {

    private final String value;
    private byte[] encodedValue;

    StringElement(DicomObject dcmObj, int tag, VR vr, String value) {
        super(dcmObj, tag, vr, -1);
        this.value = value;
    }

    @Override
    public Optional<String> stringValue(int index) {
        return vr.type.stringValue(value, index);
    }

    @Override
    public String[] stringValues() {
        return vr.type.stringValues(value);
    }

    @Override
    public int valueLength() {
        byte[] encodedValue = this.encodedValue;
        if (encodedValue == null) {
            SpecificCharacterSet specificCharacterSet = dicomObject.specificCharacterSet();
            int bytesPerChar = specificCharacterSet.bytesPerChar();
            if (bytesPerChar > 0)
                return (bytesPerChar * value.length() + 1) & ~1;
            this.encodedValue = encodedValue = specificCharacterSet.encode(value, vr.type.delimiters());
        }
        return (encodedValue.length + 1) & ~1;
    }

    @Override
    public void writeValueTo(DicomOutputStream dos) throws IOException {
        byte[] encodedValue = this.encodedValue;
        if (encodedValue == null)
            this.encodedValue = encodedValue = dicomObject.specificCharacterSet().encode(value, vr.type.delimiters());
        dos.write(encodedValue, 0, encodedValue.length);
        if ((encodedValue.length & 1) != 0)
            dos.write(vr.paddingByte);
    }

    @Override
    public StringBuilder promptValueTo(StringBuilder appendTo, int maxLength) {
        appendTo.append(' ').append('[');
        int endIndex = maxLength - appendTo.length();
        return (value.length() < endIndex)
            ? appendTo.append(value).append(']')
            : appendTo.append(value, 0, endIndex);
    }
}
