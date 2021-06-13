package org.dcm4che7;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.IntBinaryOperator;

class DicomSequence extends BasicDicomElement {
    private final ArrayList<WriteableDicomObject> items = new ArrayList<>();

    DicomSequence(DicomObject dicomObject, int tag, int valueLength) {
        super(dicomObject, tag, VR.SQ, valueLength);
    }

    @Override
    public int numberOfItems() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public void addItem(DicomObject item) {
        items.add((WriteableDicomObject) item);
    }

    @Override
    public DicomObject addItem() {
        WriteableDicomObject item = new WriteableDicomObject(this);
        items.add(item);
        return item;
    }

    @Override
    public DicomObject getItem(int index) {
        return ((WriteableDicomObject)items.get(index)).parse();
    }

    @Override
    public int promptItemsTo(StringBuilder appendTo, int maxColumns, int maxLines) {
        Object[] array = items.toArray();
        for (int i = 0; i < array.length; i++) {
            if (--maxLines < 0) break;
            appendTo.append(System.lineSeparator());
            DicomObject item = (DicomObject) array[i];
            promptLevelTo(appendTo)
                    .append(">(FFFE,E000) #")
                    .append(item.itemLength())
                    .append(" Item #").append(i + 1)
                    .append(System.lineSeparator());
            maxLines = item.promptTo(appendTo, maxColumns, maxLines);
        }
        return maxLines;
    }

    @Override
    protected StringBuilder promptValueTo(StringBuilder appendTo, int maxLength) {
        return appendTo;
    }

    @Override
    public int elementLength(DicomOutputStream dos) {
        IntBinaryOperator totalLength = dos.itemLengthEncoding().totalLength;
        int len = dos.encoding().explicitVR ? 12 : 8;
        for (WriteableDicomObject item : items) {
            len += totalLength.applyAsInt(8, item.calculateItemLength(dos));
        }
        return len;
    }

    @Override
    public int valueLength(DicomOutputStream dos) {
        if (dos.sequenceLengthEncoding().undefined.test(items.size())) return -1;
        IntBinaryOperator totalLength = dos.itemLengthEncoding().totalLength;
        int len = 0;
        for (WriteableDicomObject item : items) {
            len += totalLength.applyAsInt(8, item.calculatedItemLength());
        }
        return len;
    }

    @Override
    public void writeValueTo(DicomOutputStream dos) throws IOException {
        for (WriteableDicomObject item : items) {
            item.writeItemTo(dos);
        }
    }
}
