package com.sensomate.quickswypes;

/**
 * Created by rohan on 14/5/15.
 */
public class AttendanceDetails {
    //private variables
    String uid;
    long capturedAt;
    //the worksite id -> in preference SITE_CODE
    int projcode;
    //SITE_NAME in preference
    String sitename;
    String name;
    int count;
    long time;
    boolean checkedin;
    boolean checkedout;

    // Empty constructor
    public AttendanceDetails(){

    }
    // constructor
    public AttendanceDetails(String uid, long capturedAt, int projcode,String sitename, String name, int count,boolean checkedin,boolean checkedout,long time){
        this.uid = uid;
        this.capturedAt = capturedAt;
        this.projcode=projcode;
        this.sitename = sitename;
        this.name=name;
        this.count=count;
        this.checkedin=checkedin;
        this.checkedout=checkedout;
        this.time=time;

    }



    public String getID(){
        return this.uid;
    }


    public void setID(String uid){
        this.uid = uid;
    }


    public String getName(){
        return this.name;
    }


    public void setName(String name){
        this.name = name;
    }

    public String getSiteName(){
        return this.sitename;
    }


    void setSiteName(String sitename){
        this.sitename = sitename;
    }


    public int getProjCode(){
        return this.projcode;
    }


    public void setProjCode(int projcode){
        this.projcode = projcode;
    }


    public long getCapturedAt(){
        return this.capturedAt;
    }


    public void setCapturedAt(long capturedAt){
        this.capturedAt = capturedAt;
    }

    public int getCount(){
        return this.count;
    }


    public void setCount(int count){
        this.count = count;
    }

    public boolean getCheckedIn(){
        return this.checkedin;
    }


    public void setCheckedIn(boolean checkedin){
        this.checkedin = checkedin;
    }

    public boolean getCheckedOut(){
        return this.checkedout;
    }


    public void setCheckedOut(boolean checkedout){
        this.checkedout = checkedout;
    }



    public void setTime(long time){
        this.time= time;
    }

    public long getTime(){
        return this.time;
    }



}
