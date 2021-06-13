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

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Jun 2021
 */
class ByteArrayElement extends BasicDicomElement {
    private final byte[] value;

    public ByteArrayElement(DicomObject dcmObj, int tag, VR vr, byte[] value) {
        super(dcmObj, tag, vr, (value.length + 1) & ~1);
        this.value = value;
    }

    @Override
    public void writeValueTo(DicomOutputStream dos) throws IOException {
        if (dos.encoding().byteOrder == ByteOrder.LITTLE_ENDIAN || vr.type.toggleEndian() == null) {
            dos.write(value, 0, value.length);
            if ((value.length & 1) != 0)
                dos.write(0);
        } else {
            byte[] b = dos.swapBuffer();
            System.arraycopy(value, 0, b, 0, value.length);
            vr.type.toggleEndian().apply(b, value.length);
            dos.write(b, 0, value.length);
        }
    }
}
