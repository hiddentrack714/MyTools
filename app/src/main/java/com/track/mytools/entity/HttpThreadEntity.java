package com.track.mytools.entity;

public class HttpThreadEntity {
    private int fileIndex;          //线程编号
    private String fileSize;        //文件大小
    private String fileName;        //文件名称
    private long fileStartTime;   //文件下载起始时间

    public int getFileIndex() {
        return fileIndex;
    }

    public void setFileIndex(int fileIndex) {
        this.fileIndex = fileIndex;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileStartTime() {
        return fileStartTime;
    }

    public void setFileStartTime(long fileStartTime) {
        this.fileStartTime = fileStartTime;
    }

}
