package io.oscript.hub.api.ospx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.oscript.hub.api.storage.IPackageMetadata;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "opm-metadata")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Metadata implements IPackageMetadata {

    @XmlElement(name = "name")
    String name;

    @XmlElement(name = "version")
    String version;

    @JacksonXmlProperty(localName = "engine-version")
    String engineVersion;

    @XmlElement(name = "author")
    String author;

    @JacksonXmlProperty(localName = "author-email")
    String authorEmail;

    @XmlElement(name = "description")
    String description;

    @JacksonXmlProperty(localName = "depends-on")
    List<DependenceInfo> dependencies = new ArrayList<>();

    @JsonSetter
    public void setDependencies(DependenceInfo card) {
        this.dependencies.add(card);
    }
}

