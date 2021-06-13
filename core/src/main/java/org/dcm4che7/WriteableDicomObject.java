package org.dcm4che7;

import org.dcm4che7.util.IORuntimeException;
import org.dcm4che7.util.OptionalFloat;
import org.dcm4che7.util.StringUtils;
import org.dcm4che7.util.TagUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since May 2021
 */
class WriteableDicomObject implements DicomObject {
    static final int TO_STRING_LINES = 50;
    private static record CachedPrivateCreator(int tag, String value){};
    private final DicomInputStream dis;
    private final long itemPosition;
    private final DicomElement sequence;
    private volatile SpecificCharacterSet specificCharacterSet;
    private volatile List<DicomElement> elements;
    private volatile CachedPrivateCreator cachedPrivateCreator;
    private final ThreadLocal<Integer> calculatedItemLength = new ThreadLocal<>();

    WriteableDicomObject() {
        this(null, null, SpecificCharacterSet.getDefaultCharacterSet(), new ArrayList<>());
    }

    WriteableDicomObject(DicomElement sequence) {
        this(null, sequence, sequence.dicomObject().specificCharacterSet(), new ArrayList<>());
    }

    WriteableDicomObject(DicomInputStream dis, DicomElement sequence, SpecificCharacterSet specificCharacterSet,
                         List<DicomElement> elements) {
        this.dis = dis;
        this.itemPosition = dis != null ? dis.streamPosition() - 8 : -1L;
        this.sequence = sequence;
        this.specificCharacterSet = specificCharacterSet;
        this.elements = elements;
    }

    @Override
    public int size() {
        List<DicomElement> elements = this.elements;
        return elements != null ? elements.size() : -1;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int itemLength() {
        return dis != null ? dis.itemLength(itemPosition) : -1;
    }

    @Override
    public boolean isRoot() {
        return sequence == null;
    }

    @Override
    public DicomElement sequence() {
        return sequence;
    }

    @Override
    public SpecificCharacterSet specificCharacterSet() {
        return specificCharacterSet;
    }

    @Override
    public Optional<String> privateCreatorOf(int tag) {
        return TagUtils.isPrivateTag(tag)
                ? Optional.ofNullable(privateCreator(TagUtils.creatorTagOf(tag)).value)
                : Optional.empty();
    }

    @Override
    public Optional<DicomElement> get(int tag) {
        int i = binarySearch(elements, tag);
        if (i >= 0) {
            return Optional.of(elements.get(i));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getString(int tag) {
        return getString(tag, 0);
    }

    @Override
    public Optional<String> getString(int tag, int index) {
        return get(tag).flatMap(el -> el.stringValue(index));
    }

    @Override
    public String[] getStrings(int tag) {
        return get(tag).map(DicomElement::stringValues).orElse(StringUtils.EMPTY_STRINGS);
    }

    @Override
    public DicomElement setEmpty(int tag, VR vr) {
        return null;
    }

    @Override
    public DicomElement setString(int tag, VR vr, String val) {
        return add(vr.type.elementOf(this, tag, vr, val));
    }

    @Override
    public DicomElement setStrings(int tag, VR vr, String... vals) {
        return add(vr.type.elementOf(this, tag, vr, vals));
    }

    @Override
    public OptionalInt getInt(int tag) {
        return getInt(tag, 0);
    }

    @Override
    public OptionalInt getInt(int tag, int index) {
        return get(tag).map(el -> el.intValue(index)).orElse(OptionalInt.empty());
    }

    @Override
    public DicomElement setInt(int tag, VR vr, int val) {
        return add(vr.type.elementOf(this, tag, vr, val));
    }

    @Override
    public DicomElement setInt(int tag, VR vr, int... vals) {
        return add(vr.type.elementOf(this, tag, vr, vals));
    }

    @Override
    public OptionalLong getLong(int tag) {
        return getLong(tag, 0);
    }

    @Override
    public OptionalLong getLong(int tag, int index) {
        return get(tag).map(el -> el.longValue(index)).orElse(OptionalLong.empty());
    }

    @Override
    public OptionalFloat getFloat(int tag) {
        return getFloat(tag, 0);
    }

    @Override
    public OptionalFloat getFloat(int tag, int index) {
        return get(tag).map(el -> el.floatValue(index)).orElse(OptionalFloat.empty());
    }

    @Override
    public OptionalDouble getDouble(int tag) {
        return getDouble(tag, 0);
    }

    @Override
    public OptionalDouble getDouble(int tag, int index) {
        return get(tag).map(el -> el.doubleValue(index)).orElse(OptionalDouble.empty());
    }

    @Override
    public DicomElement add(DicomElement dcmElm) {
        if (dcmElm.dicomObject() != this) {
            throw new IllegalArgumentException("dcmElm belongs to different Dicom Object");
        }
        int tag = dcmElm.tag();
        if (tag == Tag.ItemDelimitationItem) return null;
        if (tag == Tag.SpecificCharacterSet) {
            specificCharacterSet = SpecificCharacterSet.valueOf(dcmElm.stringValues());
        }

        List<DicomElement> list = this.elements;
        int i, cmp;
        if ((i = list.size()) == 0 || (cmp = Integer.compareUnsigned(tag, list.get(--i).tag())) > 0) {
            list.add(dcmElm);
        } else if (cmp == 0 || (i = binarySearch(list, tag)) >= 0) {
            list.set(i, dcmElm);
        } else {
            list.add(-(i + 1), dcmElm);
        }
        return dcmElm;
    }

    @Override
    public String toString() {
        StringBuilder appendTo = new StringBuilder(512);
        if (promptTo(appendTo, BasicDicomElement.TO_STRING_LENGTH, TO_STRING_LINES) < 0) {
            appendTo.append(System.lineSeparator()).append("...");
        }
        return appendTo.toString();
    }

    @Override
    public int promptTo(StringBuilder appendTo, int maxColumns, int maxLines) {
        List<DicomElement> elements = this.elements;
        if (elements != null) {
            Object[] array = elements.toArray();
            for (int i = 0; i < array.length; i++) {
                if (--maxLines < 0) break;
                if (i > 0) appendTo.append(System.lineSeparator());
                DicomElement dcmElm = (DicomElement) array[i];
                dcmElm.promptTo(appendTo, appendTo.length() + maxColumns);
                maxLines = dcmElm.promptItemsTo(appendTo, maxColumns, maxLines);
            }
        } else if (--maxLines > 0) {
            ((BasicDicomElement) sequence).promptLevelTo(appendTo).append("> not parsed");
        }
        return maxLines;
    }

    @Override
    public void writeTo(DicomOutputStream dos)
            throws IOException {
        for (DicomElement element : elements()) {
            int tag = element.tag();
            if (dos.includeGroupLength() || !TagUtils.isGroupLength(tag)) {
                int valueLength = element.valueLength(dos);
                dos.writeHeader(tag, element.vr(), valueLength);
                element.writeValueTo(dos);
                if (valueLength == -1) {
                    dos.writeHeader(Tag.SequenceDelimitationItem, VR.NONE, 0);
                }
            }
        }
    }

    @Override
    public int calculateItemLength(DicomOutputStream dos) {
        List<DicomElement> elements = elements();
        int len = 0;
        boolean includeGroupLength = dos.includeGroupLength();
        int group = 0;
        int glen = 0;
        int count = 0;
        DicomElement groupLength = null;
        List<DicomElement> groupLengths = new LinkedList<>();
        for (DicomElement el : elements) {
            int nextGroup = TagUtils.groupLengthTagOf(el.tag());
            if (count++ == 0) {
                group = nextGroup;
            } else if (group != nextGroup) {
                if (includeGroupLength) {
                    createGroupLength(groupLength, group, glen, groupLengths);
                    len += 12;
                }
                len += glen;
                glen = 0;
                group = nextGroup;
            }
            if (TagUtils.isGroupLength(el.tag())) {
                groupLength = el;
            } else {
                glen += el.elementLength(dos);
            }
        }
        if (count > 0) {
            if (includeGroupLength) {
                createGroupLength(groupLength, group, glen, groupLengths);
                for (DicomElement elm : groupLengths) {
                    add(elm);
                }
                len += 12;
            }
            len += glen;
        }
        calculatedItemLength.set(len);
        return len;
    }

    private void createGroupLength(DicomElement prev, int gggg0000, int glen, List<DicomElement> list) {
        if (prev == null
                || prev.tag() != gggg0000
                || prev.valueLength() != 12
                || prev.intValue(0).getAsInt() != glen)
            list.add(VR.UL.type.elementOf(this, gggg0000, VR.UL, glen));
    }

    @Override
    public int calculatedItemLength() {
        return calculatedItemLength.get();
    }

    void writeItemTo(DicomOutputStream dos) throws IOException {
        int size = elements.size();
        boolean undefinedLength = dos.itemLengthEncoding().undefined.test(size);
        dos.writeHeader(Tag.Item, VR.NONE, undefinedLength ? -1 : size == 0 ? 0 : calculatedItemLength.get());
        writeTo(dos);
        if (undefinedLength) {
            dos.writeHeader(Tag.ItemDelimitationItem, VR.NONE, 0);
        }
    }

    WriteableDicomObject parse() {
        elements();
        return this;
    }

    List<DicomElement> elements() {
        List<DicomElement> elements;
        if ((elements = this.elements) == null)
            synchronized (this) {
                if ((elements = this.elements) == null) {
                    this.elements = elements = new ArrayList<>();
                    try {
                        dis.parseItem(itemPosition, this);
                    } catch (IOException e) {
                        throw new IORuntimeException("Failed to parse item at " + itemPosition, e);
                    }
                }
            }
        return elements;
    }

    private static int binarySearch(List<DicomElement> l, int tag) {
        int low = 0;
        int high = l.size()-1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = Integer.compareUnsigned(l.get(mid).tag(), tag);
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // tag found
        }
        return -(low + 1);  // tag not found
    }

    CachedPrivateCreator privateCreator(int tag) {
        CachedPrivateCreator cachedPrivateCreator = this.cachedPrivateCreator;
        if (cachedPrivateCreator == null || cachedPrivateCreator.tag != tag) {
            this.cachedPrivateCreator = cachedPrivateCreator =
                    new CachedPrivateCreator(tag, getString(tag).orElse(null));
        }
        return cachedPrivateCreator;
    }
}
