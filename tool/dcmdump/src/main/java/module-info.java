/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since May 2021
 */
module org.dcm4che.tool.dcmdump {
    requires org.dcm4che.core;
    requires info.picocli;

    opens org.dcm4che7.tool.dcmdump to info.picocli;
}