package com.track.mytools.entity;


import com.track.mytools.annotation.TableNameAnnotation;

@TableNameAnnotation("PWD_TABLE")
public class PwdEntity extends BaseEntity{
    private String pwdName;
    private String pwdAccount;
    private String pwdPsd;
    private String pwdIcon;
}
