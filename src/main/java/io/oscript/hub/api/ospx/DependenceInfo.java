package io.oscript.hub.api.ospx;

import lombok.Data;

import javax.xml.bind.annotation.XmlAttribute;

@Data
public class DependenceInfo {
    @XmlAttribute(name = "name")
    String name;

    @XmlAttribute(name = "version")
    String version;
}
