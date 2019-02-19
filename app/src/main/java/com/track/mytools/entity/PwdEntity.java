package com.track.mytools.entity;


import com.track.mytools.annotation.TableNameAnnotation;

@TableNameAnnotation("PWD_TABLE")
public class PwdEntity extends BaseEntity{
    private String pwdName;
    private String pwdAccount;
    private String pwdPsd;

    public String getPwdName() {
        return pwdName;
    }

    public void setPwdName(String pwdName) {
        this.pwdName = pwdName;
    }

    public String getPwdAccount() {
        return pwdAccount;
    }

    public void setPwdAcount(String pwdAcount) {
        this.pwdAccount = pwdAccount;
    }

    public String getPwdPsd() {
        return pwdPsd;
    }

    public void setPwdPsd(String pwdPsd) {
        this.pwdPsd = pwdPsd;
    }
}
