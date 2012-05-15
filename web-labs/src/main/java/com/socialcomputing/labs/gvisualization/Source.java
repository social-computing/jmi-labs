package com.socialcomputing.labs.gvisualization;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Index;

@Entity
@Table(name="gvisualization_source")
@XmlRootElement
public class Source {

    @Id
    @XmlElement
    @Column(columnDefinition = "varchar(255)")
    private String      source;
    
    @XmlElement
    @Column(columnDefinition = "varchar(64)")
    @Index(name="sourceIdIndex")
    private String      sourceId;
    
    @XmlAttribute
    private int         count;
    @XmlAttribute
    private int         quota;
    @XmlAttribute
    private Date        created;
    @XmlAttribute
    private Date        updated;
    
    public Source() {
        source = null;
        sourceId = null;
        count = 0;
        quota = -1;
    }
    
    public Source(String source, String sourceId) {
        super();
        this.source = source;
        this.sourceId = sourceId;
        this.count = 1;
        this.quota = -1;
        this.created = new Date();
        this.updated = new Date();
    }

    public void incrementUpdate() {
        this.count++;
        this.updated = new Date();
    }
}
