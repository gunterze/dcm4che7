package org.dcm4che7;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

class DicomSequence extends BasicDicomElement {
    private final ArrayList<DicomObject> items = new ArrayList<>();

    DicomSequence(DicomObject dicomObject, int tag, int valueLength) {
        super(dicomObject, tag, VR.SQ, valueLength);
    }

    @Override
    public int vm() {
        return items.size();
    }

    @Override
    public void addItem(DicomObject item) {
        items.add(item);
    }

    @Override
    public DicomObject addItem() {
        WriteableDicomObject item = new WriteableDicomObject(this);
        items.add(item);
        return item;
    }

    @Override
    public Optional<DicomObject> getItem(int index) throws IOException {
        return index < items.size() ? Optional.of(((WriteableDicomObject)items.get(index)).parse()) : Optional.empty();
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
}
