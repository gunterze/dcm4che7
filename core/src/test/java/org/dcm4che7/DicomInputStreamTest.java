package org.dcm4che7;

import org.dcm4che6.core.UID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class DicomInputStreamTest {

    private static final byte[] IVR_LE = {
            0x72, 0, 0x5E, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x5F, 0, 4, 0, 0, 0, '0', '9', '9', 'Y',
            0x72, 0, 0x60, 0, 4, 0, 0, 0, 0x72, 0, 0x60, 0,
            0x72, 0, 0x61, 0, 8, 0, 0, 0, '2', '0', '2', '1', '0', '4', '0', '3',
            0x72, 0, 0x62, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x63, 0, 12, 0, 0, 0, '2', '0', '2', '1', '0', '4', '0', '3', '2', '3', '2', '1',
            0x72, 0, 0x64, 0, 2, 0, 0, 0, '+', '1',
            0x72, 0, 0x65, 0, 2, 0, 0, 0, -1, -1,
            0x72, 0, 0x66, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x67, 0, 4, 0, 0, 0, 0, 0, -128, 63,
            0x72, 0, 0x68, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x69, 0, 2, 0, 0, 0, -1, -1,
            0x72, 0, 0x6A, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x6B, 0, 4, 0, 0, 0, '2', '3', '2', '1',
            0x72, 0, 0x6C, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x6D, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x6E, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x6F, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x70, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x71, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x72, 0, 2, 0, 0, 0, '1', '.',
            0x72, 0, 0x73, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16, 63,
            0x72, 0, 0x74, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16, 63,
            0x72, 0, 0x75, 0, 4, 0, 0, 0, -1, -1, -1, -1,
            0x72, 0, 0x76, 0, 4, 0, 0, 0, 0, 0, -128, 63,
            0x72, 0, 0x78, 0, 4, 0, 0, 0, -1, -1, -1, -1,
            0x72, 0, 0x7A, 0, 2, 0, 0, 0, -1, -1,
            0x72, 0, 0x7C, 0, 4, 0, 0, 0, -1, -1, -1, -1,
            0x72, 0, 0x7E, 0, 2, 0, 0, 0, -1, -1,
            0x72, 0, 0x7F, 0, 18, 0, 0, 0, 49, 46, 50, 46, 56, 52, 48, 46, 49, 48, 48, 48, 56, 46, 49, 46, 49, 0,
            0x72, 0, (byte) 0x80, 0, 0, 0, 0, 0,
            0x72, 0, (byte) 0x81, 0, 8, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1,
            0x72, 0, (byte) 0x82, 0, 8, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1,
            0x72, 0, (byte) 0x83, 0, 8, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1,
    };
    private static final byte[] EVR_LE = {
            0x72, 0, 0x5E, 0, 'A', 'E', 4, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x5F, 0, 'A', 'S', 4, 0, '0', '9', '9', 'Y',
            0x72, 0, 0x60, 0, 'A', 'T', 4, 0, 0x72, 0, 0x60, 0,
            0x72, 0, 0x61, 0, 'D', 'A', 8, 0, '2', '0', '2', '1', '0', '4', '0', '3',
            0x72, 0, 0x62, 0, 'C', 'S', 4, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x63, 0, 'D', 'T', 12, 0, '2', '0', '2', '1', '0', '4', '0', '3', '2', '3', '2', '1',
            0x72, 0, 0x64, 0, 'I', 'S', 2, 0, '+', '1',
            0x72, 0, 0x65, 0, 'O', 'B', 0, 0, 2, 0, 0, 0, -1, -1,
            0x72, 0, 0x66, 0, 'L', 'O', 4, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x67, 0, 'O', 'F', 0, 0, 4, 0, 0, 0, 0, 0, -128, 63,
            0x72, 0, 0x68, 0, 'L', 'T', 4, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x69, 0, 'O', 'W', 0, 0, 2, 0, 0, 0, -1, -1,
            0x72, 0, 0x6A, 0, 'P', 'N', 4, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x6B, 0, 'T', 'M', 4, 0, '2', '3', '2', '1',
            0x72, 0, 0x6C, 0, 'S', 'H', 4, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x6D, 0, 'U', 'N', 0, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x6E, 0, 'S', 'T', 4, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x6F, 0, 'U', 'C', 0, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x70, 0, 'U', 'T', 0, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x71, 0, 'U', 'R', 0, 0, 4, 0, 0, 0, 'T', 'E', 'X', 'T',
            0x72, 0, 0x72, 0, 'D', 'S', 2, 0, '1', '.',
            0x72, 0, 0x73, 0, 'O', 'D', 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16, 63,
            0x72, 0, 0x74, 0, 'F', 'D', 8, 0, 0, 0, 0, 0, 0, 0, -16, 63,
            0x72, 0, 0x75, 0, 'O', 'L', 0, 0, 4, 0, 0, 0, -1, -1, -1, -1,
            0x72, 0, 0x76, 0, 'F', 'L', 4, 0, 0, 0, -128, 63,
            0x72, 0, 0x78, 0, 'U', 'L', 4, 0, -1, -1, -1, -1,
            0x72, 0, 0x7A, 0, 'U', 'S', 2, 0, -1, -1,
            0x72, 0, 0x7C, 0, 'S', 'L', 4, 0, -1, -1, -1, -1,
            0x72, 0, 0x7E, 0, 'S', 'S', 2, 0, -1, -1,
            0x72, 0, 0x7F, 0, 'U', 'I', 18, 0, 49, 46, 50, 46, 56, 52, 48, 46, 49, 48, 48, 48, 56, 46, 49, 46, 49, 0,
            0x72, 0, (byte) 0x80, 0, 'S', 'Q', 0, 0, 0, 0, 0, 0,
            0x72, 0, (byte) 0x81, 0, 'O', 'V', 0, 0, 8, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1,
            0x72, 0, (byte) 0x82, 0, 'S', 'V', 0, 0, 8, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1,
            0x72, 0, (byte) 0x83, 0, 'U', 'V', 0, 0, 8, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1,
    };
    private static final byte[] EVR_BE = {
            0, 0x72, 0, 0x5E, 'A', 'E', 0, 4, 'T', 'E', 'X', 'T',
            0, 0x72, 0, 0x5F, 'A', 'S', 0, 4, '0', '9', '9', 'Y',
            0, 0x72, 0, 0x60, 'A', 'T', 0, 4, 0, 0x72, 0, 0x60,
            0, 0x72, 0, 0x61, 'D', 'A', 0, 8, '2', '0', '2', '1', '0', '4', '0', '3',
            0, 0x72, 0, 0x62, 'C', 'S', 0, 4, 'T', 'E', 'X', 'T',
            0, 0x72, 0, 0x63, 'D', 'T', 0, 12, '2', '0', '2', '1', '0', '4', '0', '3', '2', '3', '2', '1',
            0, 0x72, 0, 0x64, 'I', 'S', 0, 2, '+', '1',
            0, 0x72, 0, 0x65, 'O', 'B', 0, 0, 0, 0, 0, 2, -1, -1,
            0, 0x72, 0, 0x66, 'L', 'O', 0, 4, 'T', 'E', 'X', 'T',
            0, 0x72, 0, 0x67, 'O', 'F', 0, 0, 0, 0, 0, 4, 63, -128, 0, 0,
            0, 0x72, 0, 0x68, 'L', 'T', 0, 4, 'T', 'E', 'X', 'T',
            0, 0x72, 0, 0x69, 'O', 'W', 0, 0, 0, 0, 0, 2, -1, -1,
            0, 0x72, 0, 0x6A, 'P', 'N', 0, 4, 'T', 'E', 'X', 'T',
            0, 0x72, 0, 0x6B, 'T', 'M', 0, 4, '2', '3', '2', '1',
            0, 0x72, 0, 0x6C, 'S', 'H', 0, 4, 'T', 'E', 'X', 'T',
            0, 0x72, 0, 0x6D, 'U', 'N', 0, 0, 0, 0, 0, 4, 'T', 'E', 'X', 'T',
            0, 0x72, 0, 0x6E, 'S', 'T', 0, 4, 'T', 'E', 'X', 'T',
            0, 0x72, 0, 0x6F, 'U', 'C', 0, 0, 0, 0, 0, 4, 'T', 'E', 'X', 'T',
            0, 0x72, 0, 0x70, 'U', 'T', 0, 0, 0, 0, 0, 4, 'T', 'E', 'X', 'T',
            0, 0x72, 0, 0x71, 'U', 'R', 0, 0, 0, 0, 0, 4, 'T', 'E', 'X', 'T',
            0, 0x72, 0, 0x72, 'D', 'S', 0, 2, '1', '.',
            0, 0x72, 0, 0x73, 'O', 'D', 0, 0, 0, 0, 0, 8, 63, -16, 0, 0, 0, 0, 0, 0,
            0, 0x72, 0, 0x74, 'F', 'D', 0, 8, 63, -16, 0, 0, 0, 0, 0, 0,
            0, 0x72, 0, 0x75, 'O', 'L', 0, 0, 0, 0, 0, 4, -1, -1, -1, -1,
            0, 0x72, 0, 0x76, 'F', 'L', 0, 4, 63, -128, 0, 0,
            0, 0x72, 0, 0x78, 'U', 'L', 0, 4, -1, -1, -1, -1,
            0, 0x72, 0, 0x7A, 'U', 'S', 0, 2, -1, -1,
            0, 0x72, 0, 0x7C, 'S', 'L', 0, 4, -1, -1, -1, -1,
            0, 0x72, 0, 0x7E, 'S', 'S', 0, 2, -1, -1,
            0, 0x72, 0, 0x7F, 'U', 'I', 0, 18, 49, 46, 50, 46, 56, 52, 48, 46, 49, 48, 48, 48, 56, 46, 49, 46, 49, 0,
            0, 0x72, 0, (byte) 0x80, 'S', 'Q', 0, 0, 0, 0, 0, 0,
            0, 0x72, 0, (byte) 0x81, 'O', 'V', 0, 0, 0, 0, 0, 8, -1, -1, -1, -1, -1, -1, -1, -1,
            0, 0x72, 0, (byte) 0x82, 'S', 'V', 0, 0, 0, 0, 0, 8, -1, -1, -1, -1, -1, -1, -1, -1,
            0, 0x72, 0, (byte) 0x83, 'U', 'V', 0, 0, 0, 0, 0, 8, -1, -1, -1, -1, -1, -1, -1, -1,
    };
    private static final byte[] DEFL_EVR_LE = {'D', 'I', 'C', 'M',
            2, 0, 0, 0, 'U', 'L', 4, 0, 30, 0, 0, 0,
            2, 0, 16, 0, 'U', 'I', 22, 0, 49, 46, 50, 46, 56, 52, 48, 46, 49, 48, 48, 48, 56, 46, 49, 46, 50, 46, 49, 46, 57, 57
    };
    private static final byte[] C_ECHO_RQ = {
            0, 0, 0, 0, 4, 0, 0, 0, 56, 0, 0, 0,
            0, 0, 2, 0, 18, 0, 0, 0, 49, 46, 50, 46, 56, 52, 48, 46, 49, 48, 48, 48, 56, 46, 49, 46, 49, 0,
            0, 0, 0, 1, 2, 0, 0, 0, 48, 0,
            0, 0, 16, 1, 2, 0, 0, 0, 1, 0,
            0, 0, 0, 8, 2, 0, 0, 0, 1, 1
    };
    private static final byte[] WF_SEQ_EVR_LE = {
            0, 84, 0, 1, 'S', 'Q', 0, 0, 8, 0, 0, 0,
            -2, -1, 0, -32, 0, 0, 0, 0
    };
    private static final byte[] WF_SEQ_IVR_LE = {
            0, 84, 0, 1, -1, -1, -1, -1,
            -2, -1, 0, -32, -1, -1, -1, -1,
            -2, -1, 13, -32, 0, 0, 0, 0,
            -2, -1, -35, -32, 0, 0, 0, 0
    };
    private static final byte[] UN_SEQ_IVR_LE = {
            55, 0, 16, 0, 'L', 'O', 20, 0,
            'Q', 'U', 'A', 'S', 'A', 'R', '_', 'I', 'N', 'T', 'E', 'R', 'N', 'A', 'L', '_', 'U', 'S', 'E', ' ',
            55, 0, 16, 16, 'U', 'N', 0, 0, -1, -1, -1, -1,
            -2, -1, 0, -32, 28, 0, 0, 0,
            55, 0, 16, 0, 20, 0, 0, 0,
            'Q', 'U', 'A', 'S', 'A', 'R', '_', 'I', 'N', 'T', 'E', 'R', 'N', 'A', 'L', '_', 'U', 'S', 'E', ' ',
            -2, -1, -35, -32, 0, 0, 0, 0
    };
    private static final byte[] UN_SEQ_EVR_LE = {
            55, 0, 16, 0, 'L', 'O', 20, 0,
            'Q', 'U', 'A', 'S', 'A', 'R', '_', 'I', 'N', 'T', 'E', 'R', 'N', 'A', 'L', '_', 'U', 'S', 'E', ' ',
            55, 0, 16, 16, 'U', 'N', 0, 0, -1, -1, -1, -1,
            -2, -1, 0, -32, 28, 0, 0, 0,
            55, 0, 16, 0, 'L', 'O', 20, 0,
            'Q', 'U', 'A', 'S', 'A', 'R', '_', 'I', 'N', 'T', 'E', 'R', 'N', 'A', 'L', '_', 'U', 'S', 'E', ' ',
            -2, -1, -35, -32, 0, 0, 0, 0
    };
    private static final byte[] PER_FRAME_FUNCTIONAL_GROUPS_SEQ_IVR_LE = {
            0, 82, 48, -110, 40, 0, 0, 0,
            -2, -1, 0, -32, 32, 0, 0, 0,
            24, 0, 20, -111, 24, 0, 0, 0,
            -2, -1, 0, -32, 16, 0, 0, 0,
            24, 0, -126, -112, 8, 0, 0, 0, -1, -1, -1, -1, 102, 102, -10, 63
    };
    private static final byte[] PER_FRAME_FUNCTIONAL_GROUPS_SEQ_EVR_LE = {
            0, 82, 48, -110, 83, 81, 0, 0, -1, -1, -1, -1,
            -2, -1, 0, -32, -1, -1, -1, -1,
            24, 0, 20, -111, 83, 81, 0, 0, -1, -1, -1, -1,
            -2, -1, 0, -32, -1, -1, -1, -1,
            24, 0, -126, -112, 70, 68, 8, 0, -1, -1, -1, -1, 102, 102, -10, 63,
            -2, -1, 13, -32, 0, 0, 0, 0,
            -2, -1, -35, -32, 0, 0, 0, 0,
            -2, -1, 13, -32, 0, 0, 0, 0,
            -2, -1, -35, -32, 0, 0, 0, 0
    };

    @Test
    void readDataSetIVR_LE() throws IOException {
        assertDataSet(parseGuessEncoding(IVR_LE, DicomEncoding.IVR_LE));
    }

    @Test
    void readDataSetEVR_LE() throws IOException {
        assertDataSet(parseGuessEncoding(EVR_LE, DicomEncoding.EVR_LE));
    }

    @Test
    void readDataSetEVR_BE() throws IOException {
        assertDataSet(parseGuessEncoding(EVR_BE, DicomEncoding.EVR_BE));
    }

    @Test
    void readDataSetDEFL() throws IOException {
        assertEquals(34, parseGuessEncoding(DEFL_EVR_LE(), DicomEncoding.DEFL_EVR_LE).size());
    }

    @Test
    void readCommandSet() throws IOException {
        DicomObject cmd;
        try (InputStream in = new ByteArrayInputStream(C_ECHO_RQ)) {
            cmd = new DicomInputStream(in).readCommandSet();
        }
        assertNotNull(cmd);
        assertEquals(48, cmd.getInt(Tag.CommandField).orElseGet(Assertions::fail));
        Assertions.assertEquals(UID.Verification, cmd.getString(Tag.AffectedSOPClassUID).orElseGet(Assertions::fail));
    }

    @Test
    void parseSequenceEVR_LE() throws IOException {
        parseSequence(WF_SEQ_EVR_LE, DicomEncoding.EVR_LE, Tag.WaveformSequence);
    }

    @Test
    void parseSequenceIVR_LE() throws IOException {
        parseSequence(WF_SEQ_IVR_LE, DicomEncoding.IVR_LE, Tag.WaveformSequence);
    }

    @Test
    void parseSequenceUN_IVR_LE() throws IOException {
        parseSequence(UN_SEQ_IVR_LE, DicomEncoding.EVR_LE, 0x00371010);
    }

    @Test
    void parseSequenceUN_EVR_LE() throws IOException {
        parseSequence(UN_SEQ_EVR_LE, DicomEncoding.EVR_LE, 0x00371010);
    }

    @Test
    void parsePerFrameFunctionalGroupsSequenceLazyIVR_LE() throws IOException {
        parsePerFrameFunctionalGroupsSequenceLazy(PER_FRAME_FUNCTIONAL_GROUPS_SEQ_IVR_LE, DicomEncoding.IVR_LE);
    }

    @Test
    void parsePerFrameFunctionalGroupsSequenceLazyEVR_LE() throws IOException {
        parsePerFrameFunctionalGroupsSequenceLazy(PER_FRAME_FUNCTIONAL_GROUPS_SEQ_EVR_LE, DicomEncoding.EVR_LE);
    }

    private static byte[] DEFL_EVR_LE() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(new byte[128]);
            out.write(DEFL_EVR_LE);
            DeflaterOutputStream deflout = new DeflaterOutputStream(out,
                    new Deflater(Deflater.DEFAULT_COMPRESSION, true));
            deflout.write(EVR_LE);
            deflout.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void parseSequence(byte[] b, DicomEncoding encoding, int tag) throws IOException {
        DicomElement el = parseWithEncoding(b, encoding).get(tag).orElseGet(Assertions::fail);
        assertEquals(VR.SQ, el.vr());
        Optional<DicomObject> item = el.getItem(0);
        assertTrue(item.isPresent());
    }

    static DicomObject parseWithEncoding(byte[] b, DicomEncoding encoding) throws IOException {
        try (DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(b)).withEncoding(encoding)) {
            return dis.readDataSet();
        }
    }

    static DicomObject parseGuessEncoding(byte[] b, DicomEncoding encoding) throws IOException {
        try (DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(b))) {
            DicomObject dcmObj = dis.readDataSet();
            assertEquals(encoding, dis.getEncoding());
            return dcmObj;
        }
    }

    private void assertDataSet(DicomObject dcmObj) {
        assertEquals("TEXT", dcmObj.getString(Tag.SelectorAEValue).orElseGet(Assertions::fail));
        assertEquals("099Y", dcmObj.getString(Tag.SelectorASValue).orElseGet(Assertions::fail));
        assertEquals(Tag.SelectorATValue, dcmObj.getInt(Tag.SelectorATValue).orElseGet(Assertions::fail));
        assertEquals("20210403", dcmObj.getString(Tag.SelectorDAValue).orElseGet(Assertions::fail));
        assertEquals("TEXT", dcmObj.getString(Tag.SelectorCSValue).orElseGet(Assertions::fail));
        assertEquals("202104032321", dcmObj.getString(Tag.SelectorDTValue).orElseGet(Assertions::fail));
        assertEquals(1, dcmObj.getInt(Tag.SelectorISValue).orElseGet(Assertions::fail));
        assertEquals(-1, dcmObj.getInt(Tag.SelectorOBValue).orElseGet(Assertions::fail));
        assertEquals("TEXT", dcmObj.getString(Tag.SelectorLOValue).orElseGet(Assertions::fail));
        assertEquals(1.f, dcmObj.getFloat(Tag.SelectorOFValue).orElseGet(Assertions::fail));
        assertEquals("TEXT", dcmObj.getString(Tag.SelectorLTValue).orElseGet(Assertions::fail));
        assertEquals(-1, dcmObj.getInt(Tag.SelectorOWValue).orElseGet(Assertions::fail));
        assertEquals("TEXT", dcmObj.getString(Tag.SelectorPNValue).orElseGet(Assertions::fail));
        assertEquals("2321", dcmObj.getString(Tag.SelectorTMValue).orElseGet(Assertions::fail));
        assertEquals("TEXT", dcmObj.getString(Tag.SelectorSHValue).orElseGet(Assertions::fail));
        assertEquals("TEXT", dcmObj.getString(Tag.SelectorUNValue).orElseGet(Assertions::fail));
        assertEquals("TEXT", dcmObj.getString(Tag.SelectorSTValue).orElseGet(Assertions::fail));
        assertEquals("TEXT", dcmObj.getString(Tag.SelectorUCValue).orElseGet(Assertions::fail));
        assertEquals("TEXT", dcmObj.getString(Tag.SelectorUTValue).orElseGet(Assertions::fail));
        assertEquals("TEXT", dcmObj.getString(Tag.SelectorURValue).orElseGet(Assertions::fail));
        assertEquals(1, dcmObj.getInt(Tag.SelectorDSValue).orElseGet(Assertions::fail));
        assertEquals(1., dcmObj.getDouble(Tag.SelectorODValue).orElseGet(Assertions::fail));
        assertEquals(1., dcmObj.getDouble(Tag.SelectorFDValue).orElseGet(Assertions::fail));
        assertEquals(-1L, dcmObj.getLong(Tag.SelectorOLValue).orElseGet(Assertions::fail));
        assertEquals(1.f, dcmObj.getFloat(Tag.SelectorFLValue).orElseGet(Assertions::fail));
        assertEquals(0xffffffffL, dcmObj.getLong(Tag.SelectorULValue).orElseGet(Assertions::fail));
        assertEquals(0xffff, dcmObj.getInt(Tag.SelectorUSValue).orElseGet(Assertions::fail));
        assertEquals(-1L, dcmObj.getLong(Tag.SelectorSLValue).orElseGet(Assertions::fail));
        assertEquals(-1, dcmObj.getInt(Tag.SelectorSSValue).orElseGet(Assertions::fail));
        assertEquals(UID.Verification, dcmObj.getString(Tag.SelectorUIValue).orElseGet(Assertions::fail));
        assertEquals(0, dcmObj.get(Tag.SelectorCodeSequenceValue).orElseGet(Assertions::fail).valueLength());
        assertEquals(-1L, dcmObj.getLong(Tag.SelectorOVValue).orElseGet(Assertions::fail));
        assertEquals(-1L, dcmObj.getLong(Tag.SelectorSVValue).orElseGet(Assertions::fail));
        assertEquals(-1L, dcmObj.getLong(Tag.SelectorUVValue).orElseGet(Assertions::fail));
    }

    static void parsePerFrameFunctionalGroupsSequenceLazy(byte[] b, DicomEncoding encoding) throws IOException {
        DicomElement functionalGroupSeq = parseLazy(b, encoding, Tag.PerFrameFunctionalGroupsSequence)
                .get(Tag.PerFrameFunctionalGroupsSequence).orElseGet(Assertions::fail);
        DicomObject functionalGroup = functionalGroupSeq.getItem(0).orElseGet(Assertions::fail);
        DicomElement mrEchoSeq = functionalGroup.get(Tag.MREchoSequence).orElseGet(Assertions::fail);
        DicomObject mrEcho = mrEchoSeq.getItem(0).orElseGet(Assertions::fail);
        assertEquals(1.4000005722045896, mrEcho.getDouble(Tag.EffectiveEchoTime).orElseGet(Assertions::fail));
    }

    static DicomObject parseLazy(byte[] b, DicomEncoding encoding, int seqTag) throws IOException {
        try (DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(b))
                .withEncoding(encoding)
                .withParseItemsLazy(seqTag)) {
            return dis.readDataSet();
        }
    }
}