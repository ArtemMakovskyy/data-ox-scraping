package com.dataox.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class JobPostingDTO {
    private String jobPageUrl;
    private String positionName;
    private String organizationUrl;
    private String logoUrl;
    private String organizationTitle;
    private String laborFunction;
    private List<String> locations;
    private int postedDateUnix;
    private String descriptionHtml;
    private List<String> tags;

}
