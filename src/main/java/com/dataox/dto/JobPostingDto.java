package com.dataox.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class JobPostingDto {
    private String jobPageUrl;
    private String positionName;
    private String organizationUrl;
    private String logoUrl;
    private String organizationTitle;
    private String laborFunction;
    private List<String> locations;
    private long postedDateUnix;
    private String descriptionHtml;
    private List<String> tags;
}
