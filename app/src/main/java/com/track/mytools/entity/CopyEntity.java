package com.track.mytools.entity;

import com.track.mytools.annotation.TableNameAnnotation;

@TableNameAnnotation("COPY_TABLE")
public class CopyEntity extends BaseEntity{
    private int copyFile;
    private int copyPath;
}
