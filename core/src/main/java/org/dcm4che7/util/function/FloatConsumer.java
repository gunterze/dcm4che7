package org.dcm4che7.util.function;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Mar 2019
 */
@FunctionalInterface
public interface FloatConsumer {
    void accept(float value);
}
