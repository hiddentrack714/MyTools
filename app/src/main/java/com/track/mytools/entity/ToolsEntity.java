package com.track.mytools.entity;

import com.track.mytools.annotation.TableNameAnnotation;

@TableNameAnnotation("TOOLS_TABLE")
public class ToolsEntity extends BaseEntity {
    private String btnName;
    private String btnUse;
    private String needRoot;
    private String needYC;
}
