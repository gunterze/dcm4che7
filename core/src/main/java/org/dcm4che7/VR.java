package org.dcm4che7;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since May 2021
 */
public enum VR {
    NONE(-1, true, BinaryVR.OB),
    AE(0x4145, true, StringVR.ASCII),
    AS(0x4153, true, StringVR.ASCII),
    AT(0x4154, true, BinaryVR.AT),
    CS(0x4353, true, StringVR.ASCII),
    DA(0x4441, true, StringVR.ASCII),
    DS(0x4453, true, StringVR.DS),
    DT(0x4454, true, StringVR.ASCII),
    FD(0x4644, true, BinaryVR.FD),
    FL(0x464c, true, BinaryVR.FL),
    IS(0x4953, true, StringVR.IS),
    LO(0x4c4f, true, StringVR.STRING),
    LT(0x4c54, true, StringVR.TEXT),
    OB(0x4f42, false, BinaryVR.OB),
    OD(0x4f44, false, BinaryVR.FD),
    OF(0x4f46, false, BinaryVR.FL),
    OL(0x4f4c, false, BinaryVR.SL),
    OV(0x4f56, false, BinaryVR.SV),
    OW(0x4f57, false, BinaryVR.SS),
    PN(0x504e, true, StringVR.PN),
    SH(0x5348, true, StringVR.STRING),
    SL(0x534c, true, BinaryVR.SL),
    SQ(0x5351, false, VRType.SQ),
    SS(0x5353, true, BinaryVR.SS),
    ST(0x5354, true, StringVR.TEXT),
    SV(0x5356, false, BinaryVR.SV),
    TM(0x544d, true, StringVR.ASCII),
    UC(0x5543, false, StringVR.UC),
    UI(0x5549, true, StringVR.UI),
    UL(0x554c, true, BinaryVR.UL),
    UN(0x554e, false, VRType.UN),
    UR(0x5552, false, StringVR.UR),
    US(0x5553, true, BinaryVR.US),
    UT(0x5554, false, StringVR.TEXT),
    UV(0x5556, false, BinaryVR.UV);

    final int code;
    final boolean evr8;
    final VRType type;

    VR(int code, boolean evr8, VRType type) {
        this.code = code;
        this.evr8 = evr8;
        this.type = type;
    }

    public static VR of(int code) {
        switch (code) {
            case 0x4145: return AE;
            case 0x4153: return AS;
            case 0x4154: return AT;
            case 0x4353: return CS;
            case 0x4441: return DA;
            case 0x4453: return DS;
            case 0x4454: return DT;
            case 0x4644: return FD;
            case 0x464c: return FL;
            case 0x4953: return IS;
            case 0x4c4f: return LO;
            case 0x4c54: return LT;
            case 0x4f42: return OB;
            case 0x4f44: return OD;
            case 0x4f46: return OF;
            case 0x4f4c: return OL;
            case 0x4f56: return OV;
            case 0x4f57: return OW;
            case 0x504e: return PN;
            case 0x5348: return SH;
            case 0x534c: return SL;
            case 0x5351: return SQ;
            case 0x5353: return SS;
            case 0x5354: return ST;
            case 0x5356: return SV;
            case 0x544d: return TM;
            case 0x5543: return UC;
            case 0x5549: return UI;
            case 0x554c: return UL;
            case 0x554e: return UN;
            case 0x5552: return UR;
            case 0x5553: return US;
            case 0x5554: return UT;
            case 0x5556: return UV;
        }
        throw new IllegalArgumentException(String.format("Unknown VR code: %04XH", code));
    }
}
