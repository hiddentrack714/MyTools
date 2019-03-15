package com.track.mytools.entity;

import com.track.mytools.annotation.TableNameAnnotation;

@TableNameAnnotation("QRY_SUFFIX_TABLE")
public class QrySuffixEntity extends BaseEntity {
    private String qrySuffixPath;
    private String qrySuffixStr;
}
