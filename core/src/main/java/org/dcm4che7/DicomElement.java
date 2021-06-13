package org.dcm4che7;

import org.dcm4che7.util.OptionalFloat;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since May 2021
 */
public interface DicomElement {
    DicomObject dicomObject();
    int tag();
    VR vr();
    int valueLength();
    boolean isEmpty();
    OptionalInt intValue(int index);
    OptionalLong longValue(int index);
    OptionalFloat floatValue(int index);
    OptionalDouble doubleValue(int index);
    Optional<String> stringValue(int index);
    String[] stringValues();
    int numberOfItems();
    DicomObject addItem();
    void addItem(DicomObject item);
    DicomObject getItem(int index) throws IOException;
    StringBuilder promptTo(StringBuilder appendTo, int maxLength);
    int promptItemsTo(StringBuilder appendTo, int maxColumns, int maxLines);
    StringBuilder promptLevelTo(StringBuilder appendTo);
    int elementLength(DicomOutputStream dos);
    int valueLength(DicomOutputStream dos);
    void writeValueTo(DicomOutputStream dos) throws IOException;
}
