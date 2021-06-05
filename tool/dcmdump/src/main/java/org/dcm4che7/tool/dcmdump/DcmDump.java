package org.dcm4che7.tool.dcmdump;

import org.dcm4che7.*;
import org.dcm4che7.util.TagUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since May 2021
 */
@CommandLine.Command(
        name = "dcmdump",
        mixinStandardHelpOptions = true,
        versionProvider = DcmDump.VersionProvider.class,
        descriptionHeading = "%n",
        description = "The dcmdump utility dumps the contents of a DICOM file (file format or raw data set) " +
                "to standard output in textual form.",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        showDefaultValues = true,
        footerHeading = "%nExample:%n",
        footer = { "$ dcmdump image.dcm", "Dump DICOM file image.dcm to standard output." }
)
public class DcmDump implements Callable<Integer> {
    static class VersionProvider implements CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() {
            return new String[]{ DcmDump.class.getModule().getDescriptor().rawVersion().orElse("7") };
        }
    }

    @CommandLine.Parameters(description = "DICOM input file to be dumped.")
    Path file;

    @CommandLine.Option(names = { "-w", "--width" }, description = "Set output width to <cols>.")
    int cols = 80;

    private final StringBuilder sb = new StringBuilder();

    public static void main(String[] args) {
        new CommandLine(new DcmDump()).execute(args);
    }

    @Override
    public Integer call() throws Exception {
        try (DicomInputStream dis = new DicomInputStream(Files.newInputStream(file))) {
            dis.withPreambleHandler(this::onPreamble)
                    .withDicomElementHandler(this::onElement)
                    .withItemHandler(this::onItem)
                    .readDataSet();
        }
        return 0;
    }

    private void onPreamble(DicomInputStream dis) {
        System.out.println(dis.promptPreambleTo(sb.append("0: "), cols));
    }

    private boolean onElement(DicomInputStream dis, long pos, DicomElement dcmElm)
            throws IOException {
        int tag = dcmElm.tag();
        int valueLength = dcmElm.valueLength();
        sb.setLength(0);
        sb.append(pos).append(": ");
        if (valueLength > 0)
            dis.fillCache(dis.getStreamPosition() + Math.min(valueLength, cols));
        dcmElm.promptTo(sb, cols);
        System.out.println(sb);
        if (tag == Tag.TransferSyntaxUID || tag == Tag.SpecificCharacterSet || TagUtils.isPrivateCreator(tag)) {
            dcmElm.dicomObject().add(dcmElm);
        }
        if (valueLength == -1 || dcmElm.vr() == VR.SQ) {
            dis.parseItems(dcmElm, valueLength);
        } else {
            dis.seek(dis.getStreamPosition() + valueLength);
        }
        return true;
    }

    private boolean onItem(DicomInputStream dis, long pos, DicomElement dcmElm, DicomElement itemHeader)
            throws IOException {
        int tag = itemHeader.tag();
        int itemLength = itemHeader.valueLength();
        sb.setLength(0);
        sb.append(pos).append(": ");
        if (tag == Tag.Item) {
            if (dcmElm.vr() == VR.SQ) {
                dcmElm.promptLevelTo(sb)
                        .append("(FFFE,E000) #").append(itemLength)
                        .append(" Item #").append(dcmElm.vm() + 1);
                System.out.println(sb);
                dis.parse(dcmElm.addItem(), itemLength);
            } else {
                itemHeader.promptTo(sb, cols);
                System.out.println(sb);
                dis.seek(dis.getStreamPosition() + itemLength);
            }
        } else {
            itemHeader.promptTo(sb, cols);
            System.out.println(sb);
            dis.seek(dis.getStreamPosition() + itemLength);
        }
        return true;
    }

}
