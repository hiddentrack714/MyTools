package com.track.mytools.entity;

import com.track.mytools.annotation.TableNameAnnotation;

@TableNameAnnotation("NL_TABLE")
public class NLEntity extends BaseEntity{
    private int nlX;    //蚂蚁森林x轴
    private int nlY;    //蚂蚁森林y轴
    private int ballX;   //能量球x轴
    private int ballY;   //能量球y轴
    private String nlHour;    //成熟时
    private String nlMinute;  //成熟分
    private String nlClick; // 点击次数

    public NLEntity(int nlX, int nlY, int ballX, int ballY) {
        this.nlX = nlX;
        this.nlY = nlY;
        this.ballX = ballX;
        this.ballY = ballY;
    }

    public int getNlX() {
        return nlX;
    }

    public void setNlX(int nlX) {
        this.nlX = nlX;
    }

    public int getNlY() {
        return nlY;
    }

    public void setNlY(int nlY) {
        this.nlY = nlY;
    }

    public int getBallX() {
        return ballX;
    }

    public void setBallX(int ballX) {
        this.ballX = ballX;
    }

    public int getBallY() {
        return ballY;
    }

    public void setBallY(int ballY) {
        this.ballY = ballY;
    }
}
