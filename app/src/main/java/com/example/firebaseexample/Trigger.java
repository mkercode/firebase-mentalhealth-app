package com.example.firebaseexample;

public class Trigger {
    private String trigger;
    private int numTimes;
    private String userId;

    public Trigger(String trigger, int numTimes, String userId) {
        this.trigger = trigger;
        this.numTimes = numTimes;
        this.userId = userId;
    }

    //required empty constructor for firebase
    public Trigger() {
    }

    public CharSequence getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public int getNumTimes() {
        return numTimes;
    }

    public void setNumTimes(int numTimes) {
        this.numTimes = numTimes;
    }
}
