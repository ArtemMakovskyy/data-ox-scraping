package com.dataox.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "job_postings")
@Getter
@Setter
@ToString
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "job_page_url", columnDefinition = "TEXT")
    private String jobPageUrl;

    @Column(name = "position_name")
    private String positionName;

    @Column(name = "organization_url", length = 1000)
    private String organizationUrl;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(name = "organization_title")
    private String organizationTitle;

    @Column(name = "labor_function")
    private String laborFunction;

    @Column(name = "posted_date_unix")
    private long postedDateUnix;

    @Lob
    @Column(name = "description_html", columnDefinition = "TEXT")
    private String descriptionHtml;

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Location> locations;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "job_posting_tags",
            joinColumns = @JoinColumn(name = "job_posting_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;
}
