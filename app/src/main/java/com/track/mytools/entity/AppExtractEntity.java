package com.track.mytools.entity;

import com.track.mytools.annotation.TableNameAnnotation;

@TableNameAnnotation("APP_EXTRACT_TABLE")
public class AppExtractEntity extends BaseEntity {
    private String appPath;
}
