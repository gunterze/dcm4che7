package org.dcm4che7;

import java.io.IOException;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Mar 2021
 */
public class DicomParseException extends IOException {
    public DicomParseException(String message) {
        super(message);
    }
}
