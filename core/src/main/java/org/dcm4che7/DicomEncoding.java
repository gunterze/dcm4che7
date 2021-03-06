package org.dcm4che7;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Mar 2021
 */
public enum DicomEncoding {
    IVR_LE(false, ByteOrder.LITTLE_ENDIAN, false, UID.ImplicitVRLittleEndian),
    EVR_LE(true, ByteOrder.LITTLE_ENDIAN, false, UID.ExplicitVRLittleEndian),
    EVR_BE(true, ByteOrder.BIG_ENDIAN, false, UID.ExplicitVRBigEndian),
    DEFL_EVR_LE(true, ByteOrder.LITTLE_ENDIAN, true, UID.DeflatedExplicitVRLittleEndian),
    SERIALIZE(true, ByteOrder.LITTLE_ENDIAN, false, null);

    public final boolean explicitVR;
    public final ByteOrder byteOrder;
    public final boolean deflated;
    public final String transferSyntaxUID;

    DicomEncoding(boolean explicitVR, ByteOrder byteOrder, boolean deflated, String transferSyntaxUID) {
        this.explicitVR = explicitVR;
        this.byteOrder = byteOrder;
        this.deflated = deflated;
        this.transferSyntaxUID = transferSyntaxUID;
    }

    public static DicomEncoding of(String tsuid) {
        switch (tsuid) {
            case UID.ImplicitVRLittleEndian:
                return IVR_LE;
            case UID.ExplicitVRBigEndian:
                return EVR_BE;
            case UID.DeflatedExplicitVRLittleEndian:
            case UID.JPIPReferencedDeflate:
                return DEFL_EVR_LE;
        }
        return EVR_LE;
    }
}
