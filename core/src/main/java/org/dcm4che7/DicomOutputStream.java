/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dcm4che7;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.function.IntPredicate;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Jun 2021
 */
public class DicomOutputStream extends OutputStream {

    private static final int BUFFER_LENGTH = 0x2000;
    private OutputStream out;
    private DicomEncoding encoding;
    private boolean includeGroupLength;
    private LengthEncoding sequenceLengthEncoding = LengthEncoding.UNDEFINED_OR_ZERO;
    private LengthEncoding itemLengthEncoding = LengthEncoding.UNDEFINED_OR_ZERO;
    private final byte[] header = new byte[12];
    private byte[] swapBuffer;

    public DicomOutputStream(OutputStream out) {
        this.out = Objects.requireNonNull(out);
    }

    public DicomEncoding encoding() {
        return encoding;
    }

    public DicomOutputStream withEncoding(DicomEncoding encoding) {
        this.encoding = Objects.requireNonNull(encoding);
        if (encoding.deflated) {
            out = new DeflaterOutputStream(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        }
        return this;
    }

    public DicomOutputStream withEncoding(DicomObject fmi) {
        String tsuid = fmi.getString(Tag.TransferSyntaxUID).orElseThrow(
                () -> new IllegalArgumentException("Missing Transfer Syntax UID in File Meta Information"));

        return withEncoding(DicomEncoding.of(tsuid));
    }

    public DicomOutputStream withIncludeGroupLength(boolean includeGroupLength) {
        this.includeGroupLength = includeGroupLength;
        return this;
    }

    public boolean includeGroupLength() {
        return includeGroupLength;
    }

    public DicomOutputStream withSequenceLengthEncoding(LengthEncoding sequenceLengthEncoding) {
        this.sequenceLengthEncoding = Objects.requireNonNull(sequenceLengthEncoding);
        return this;
    }

    public LengthEncoding sequenceLengthEncoding() {
        return sequenceLengthEncoding;
    }

    public DicomOutputStream withItemLengthEncoding(LengthEncoding itemLengthEncoding) {
        this.itemLengthEncoding = Objects.requireNonNull(itemLengthEncoding);
        return this;
    }

    public LengthEncoding itemLengthEncoding() {
        return itemLengthEncoding;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    public void writeHeader(int tag, VR vr, int length) throws IOException {
        int headerLength = 8;
        byte[] header = this.header;
        ByteOrder byteOrder = encoding.byteOrder;
        byteOrder.tagToBytes(tag, header, 0);
        if (vr == VR.NONE || !encoding.explicitVR) {
            byteOrder.intToBytes(length, header, 4);
        } else {
            header[4] = (byte) (vr.code >>> 8);
            header[5] = (byte) vr.code;
            if (vr.evr8) {
                byteOrder.shortToBytes(length, header, 6);
            } else {
                header[6] = 0;
                header[7] = 0;
                byteOrder.intToBytes(length, header, 8);
                headerLength = 12;
            }
        }
        write(header, 0, headerLength);
    }

    public DicomOutputStream writeFileMetaInformation(DicomObject fmi) throws IOException {
        if (encoding != null)
            throw new IllegalStateException("encoding already initialized: " + encoding);

        byte[] b = new byte[132];
        b[128] = 'D';
        b[129] = 'I';
        b[130] = 'C';
        b[131] = 'M';
        write(b);
        encoding = DicomEncoding.EVR_LE;
        boolean tmp = includeGroupLength;
        includeGroupLength = true;
        try {
            fmi.calculateItemLength(this);
            fmi.writeTo(this);
        } finally {
            includeGroupLength = tmp;
        }
        return withEncoding(fmi);
    }

    public void writeDataSet(DicomObject dcmobj) throws IOException {
        if (encoding == null)
            throw new IllegalStateException("encoding not initialized");

        Objects.requireNonNull(dcmobj);
        if (includeGroupLength || sequenceLengthEncoding.calculate || itemLengthEncoding.calculate) {
            dcmobj.calculateItemLength(this);
        }
        dcmobj.writeTo(this);
        if (out instanceof DeflaterOutputStream) {
            ((DeflaterOutputStream) out).finish();
        }
    }

    public void writeCommandSet(DicomObject dcmobj) throws IOException {
        if (encoding != null)
            throw new IllegalStateException("encoding already initialized: " + encoding);

        if (dcmobj.isEmpty())
            throw new IllegalStateException("empty command set");

        encoding = DicomEncoding.IVR_LE;
        includeGroupLength = true;
        dcmobj.calculateItemLength(this);
        dcmobj.writeTo(this);
    }

    public byte[] swapBuffer() {
        if (swapBuffer == null) {
            swapBuffer = new byte[BUFFER_LENGTH];
        }
        return swapBuffer;
    }

    public void writeUTF(String s) throws IOException {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        ByteOrder.LITTLE_ENDIAN.shortToBytes(b.length, header, 0);
        write(header, 0, 2);
        write(b);
    }

    public enum LengthEncoding {
        UNDEFINED_OR_ZERO(false, x -> x > 0, (h, x) -> x == 0 ? h : h + x + 8),
        UNDEFINED(false, x -> true, (h, x) -> h + x + 8),
        EXPLICIT(true, x -> false, (h, x) -> h + x);

        public final boolean calculate;
        public final IntPredicate undefined;
        public final IntBinaryOperator totalLength;

        LengthEncoding(boolean calculate, IntPredicate undefined, IntBinaryOperator totalLength) {
            this.calculate = calculate;
            this.undefined = undefined;
            this.totalLength = totalLength;
        }
    }
}
