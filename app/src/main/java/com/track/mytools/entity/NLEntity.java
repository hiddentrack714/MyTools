package com.track.mytools.entity;

import android.widget.EditText;

public class NLEntity {
    private int nlX;
    private int nlY;
    private int ballX;
    private int ballY;

    public NLEntity(EditText nlX, EditText nlY, EditText ballX, EditText ballY) {
        this.nlX = Integer.parseInt(nlX.getText().toString());
        this.nlY = Integer.parseInt(nlY.getText().toString());
        this.ballX = Integer.parseInt(ballX.getText().toString());
        this.ballY = Integer.parseInt(ballY.getText().toString());
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
