package com.track.mytools.entity;

import com.track.mytools.annotation.TableNameAnnotation;

@TableNameAnnotation("STATIC_IP_TABLE")
public class StaticIPEntity extends BaseEntity {
    private String staticIp;
    private String staticGateWay;
    private String staticSuffix;
}
