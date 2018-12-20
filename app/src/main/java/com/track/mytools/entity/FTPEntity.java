package com.track.mytools.entity;

import com.track.mytools.annotation.TableNameAnnotation;

@TableNameAnnotation("FTP_TABLE")
public class FTPEntity extends BaseEntity{
    private String ftpIP;
    private String ftpPORT;
    private String ftpUser;
    private String ftpPassword;
    private String ftpServerPath;
    private String ftpLocalPath;
}
