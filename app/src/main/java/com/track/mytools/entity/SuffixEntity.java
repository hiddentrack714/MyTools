package com.track.mytools.entity;

import com.track.mytools.annotation.TableNameAnnotation;

@TableNameAnnotation("SUFFIX_TABLE")
public class SuffixEntity extends BaseEntity {
    private String suffixPath;
    private String suffixType;
    private String suffixFilter;
}
