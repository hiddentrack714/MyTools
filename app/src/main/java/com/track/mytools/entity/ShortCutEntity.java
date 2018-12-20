package com.track.mytools.entity;

public class ShortCutEntity {
    public String shortName;
    public String LongName;
    public Class cla;

    public ShortCutEntity(String shortName,String LongName,Class cla){
        this.shortName = shortName;
        this.LongName = LongName;
        this.cla = cla;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLongName() {
        return LongName;
    }

    public void setLongName(String longName) {
        LongName = longName;
    }

    public Class getCla() {
        return cla;
    }

    public void setCla(Class cla) {
        this.cla = cla;
    }
}
