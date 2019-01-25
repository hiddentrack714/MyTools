package com.track.mytools.enums;

public enum AssetsEnum {

    ASSETS_DB_PATH("/sdcard/Android/data/com.track.mytools/databases/mytools.db"),

    ASSETS_PROPERTIES_PATH("/sdcard/Android/data/com.track.mytools/properties/mytools.properties");

    // 成员变量
    private String name;
    // 构造方法
    private AssetsEnum(String name) {
        this.name = name;
    }
    //覆盖方法
    @Override
    public String toString() {
        return this.name;
    }
}
