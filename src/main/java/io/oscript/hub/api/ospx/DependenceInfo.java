package io.oscript.hub.api.ospx;

import io.oscript.hub.api.data.VersionInfo;
import lombok.Data;

import javax.xml.bind.annotation.XmlAttribute;

@Data
public class DependenceInfo implements VersionInfo {
    @XmlAttribute(name = "name")
    String name;

    @XmlAttribute(name = "version")
    String version;
}
