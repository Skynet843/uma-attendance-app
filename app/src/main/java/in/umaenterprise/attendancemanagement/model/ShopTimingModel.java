package in.umaenterprise.attendancemanagement.model;

import java.io.Serializable;

public class ShopTimingModel implements Serializable {

    private String day;
    private String fromTime;
    private String toTime;
    private String hoursForFullDay;

    private String fromTimeS2;
    private String toTimeS2;
    private String hoursForFullDayS2;
    private boolean halfDayAllow;
    private String hoursForHalfDay;

    public ShopTimingModel(String day, String fromTime, String toTime,String hoursForFullDay,
                           boolean halfDayAllow,String hoursForHalfDay,String fromTimeS2,String toTimeS2,String hoursForFullDayS2) {
        this.day=day;
        this.fromTime=fromTime;
        this.toTime=toTime;
        this.hoursForFullDay=hoursForFullDay;
        this.halfDayAllow=halfDayAllow;
        this.hoursForHalfDay=hoursForHalfDay;
        this.fromTimeS2=fromTimeS2;
        this.toTimeS2=toTimeS2;
        this.hoursForFullDayS2=hoursForFullDayS2;
    }

    public ShopTimingModel(){

    }

    public String getFromTimeS2() {
        return fromTimeS2;
    }

    public void setFromTimeS2(String fromTimeS2) {
        this.fromTimeS2 = fromTimeS2;
    }

    public String getToTimeS2() {
        return toTimeS2;
    }

    public void setToTimeS2(String toTimeS2) {
        this.toTimeS2 = toTimeS2;
    }

    public String getHoursForFullDayS2() {
        return hoursForFullDayS2;
    }

    public void setHoursForFullDayS2(String hoursForFullDayS2) {
        this.hoursForFullDayS2 = hoursForFullDayS2;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getHoursForFullDay() {
        return hoursForFullDay;
    }

    public void setHoursForFullDay(String hoursForFullDay) {
        this.hoursForFullDay = hoursForFullDay;
    }

    public boolean isHalfDayAllow() {
        return halfDayAllow;
    }

    public void setHalfDayAllow(boolean halfDayAllow) {
        this.halfDayAllow = halfDayAllow;
    }

    public String getHoursForHalfDay() {
        return hoursForHalfDay;
    }

    public void setHoursForHalfDay(String hoursForHalfDay) {
        this.hoursForHalfDay = hoursForHalfDay;
    }
}
