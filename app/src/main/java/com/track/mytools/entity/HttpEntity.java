package com.track.mytools.entity;

import com.track.mytools.annotation.TableNameAnnotation;

@TableNameAnnotation("HTTP_TABLE")
public class HttpEntity extends BaseEntity {
    private String httpThread;
    private String httpSuff;
    private String httpPath;
}
