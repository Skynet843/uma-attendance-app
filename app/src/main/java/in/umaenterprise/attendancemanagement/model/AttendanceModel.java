package in.umaenterprise.attendancemanagement.model;

import java.io.Serializable;

public class AttendanceModel implements Serializable {

    private String firebaseKey;
    private String punchDate;
    private long punchDateInMillis;
    private String punchInTime;
    private String punchOutTime;;
    private String totalWorkingHours;

    private String punchInTimeS2;
    private String punchOutTimeS2;
    private String totalWorkingHoursS2;
    private double presentDay;

    private String punchInLocationCode;
    private String punchOutLocationCode;

    private double punchInLatitude;
    private double punchInLongitude;
    private double punchOutLatitude;
    private double punchOutLongitude;

    private double punchInLatitudeS2;
    private double punchInLongitudeS2;
    private double punchOutLatitudeS2;
    private double punchOutLongitudeS2;

    private String punchInBy;
    private String punchOutBy;
    private String punchInByS2;
    private String punchOutByS2;
    private double overTimeInMinutes=0;
    private double overTimeInMinutesS2=0;

    private String personName;
    private String personMobileNo;
    private String personFirebaseKey;
    private String punchInImage;
    private String punchOutImage;
    private String punchInImageS2;
    private String punchOutImageS2;

    // Added By Souvik Samanta
    private String adminNote;
    private boolean editedByAdmin;

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public boolean isEditedByAdmin() {
        return editedByAdmin;
    }

    public void setEditedByAdmin(boolean editedByAdmin) {
        this.editedByAdmin = editedByAdmin;
    }

    public AttendanceModel(){
        punchInImage="";
        punchOutImage="";
        punchInImageS2="";
        punchOutImageS2="";
        editedByAdmin=false;
        adminNote="";
    }

    public double getOverTimeInMinutesS2() {
        return overTimeInMinutesS2;
    }

    public void setOverTimeInMinutesS2(double overTimeInMinutesS2) {
        this.overTimeInMinutesS2 = overTimeInMinutesS2;
    }

    public String getPunchInImage() {
        return punchInImage;
    }

    public void setPunchInImage(String punchInImage) {
        this.punchInImage = punchInImage;
    }

    public String getPunchOutImage() {
        return punchOutImage;
    }

    public void setPunchOutImage(String punchOutImage) {
        this.punchOutImage = punchOutImage;
    }

    /**
     * Used For Calender
     */
    private String Id;
    private String Date;
    private int Day;
    private boolean IsTransactionAdded = false;
    private String Type;
    private String Description;//User for public holidays


    public String getPunchInTimeS2() {
        return punchInTimeS2;
    }

    public void setPunchInTimeS2(String punchInTimeS2) {
        this.punchInTimeS2 = punchInTimeS2;
    }

    public String getPunchOutTimeS2() {
        return punchOutTimeS2;
    }

    public void setPunchOutTimeS2(String punchOutTimeS2) {
        this.punchOutTimeS2 = punchOutTimeS2;
    }

    public String getTotalWorkingHoursS2() {
        return totalWorkingHoursS2;
    }

    public void setTotalWorkingHoursS2(String totalWorkingHoursS2) {
        this.totalWorkingHoursS2 = totalWorkingHoursS2;
    }

    public double getPunchInLatitudeS2() {
        return punchInLatitudeS2;
    }

    public void setPunchInLatitudeS2(double punchInLatitudeS2) {
        this.punchInLatitudeS2 = punchInLatitudeS2;
    }

    public double getPunchInLongitudeS2() {
        return punchInLongitudeS2;
    }

    public void setPunchInLongitudeS2(double punchInLongitudeS2) {
        this.punchInLongitudeS2 = punchInLongitudeS2;
    }

    public double getPunchOutLatitudeS2() {
        return punchOutLatitudeS2;
    }

    public void setPunchOutLatitudeS2(double punchOutLatitudeS2) {
        this.punchOutLatitudeS2 = punchOutLatitudeS2;
    }

    public double getPunchOutLongitudeS2() {
        return punchOutLongitudeS2;
    }

    public void setPunchOutLongitudeS2(double punchOutLongitudeS2) {
        this.punchOutLongitudeS2 = punchOutLongitudeS2;
    }

    public String getPunchInByS2() {
        return punchInByS2;
    }

    public void setPunchInByS2(String punchInByS2) {
        this.punchInByS2 = punchInByS2;
    }

    public String getPunchOutByS2() {
        return punchOutByS2;
    }

    public void setPunchOutByS2(String punchOutByS2) {
        this.punchOutByS2 = punchOutByS2;
    }

    public String getPunchInImageS2() {
        return punchInImageS2;
    }

    public void setPunchInImageS2(String punchInImageS2) {
        this.punchInImageS2 = punchInImageS2;
    }

    public String getPunchOutImageS2() {
        return punchOutImageS2;
    }

    public void setPunchOutImageS2(String punchOutImageS2) {
        this.punchOutImageS2 = punchOutImageS2;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public String getPunchDate() {
        return punchDate;
    }

    public void setPunchDate(String punchDate) {
        this.punchDate = punchDate;
    }

    public long getPunchDateInMillis() {
        return punchDateInMillis;
    }

    public void setPunchDateInMillis(long punchDateInMillis) {
        this.punchDateInMillis = punchDateInMillis;
    }

    public String getPunchInTime() {
        return punchInTime;
    }

    public void setPunchInTime(String punchInTime) {
        this.punchInTime = punchInTime;
    }

    public String getPunchOutTime() {
        return punchOutTime;
    }

    public void setPunchOutTime(String punchOutTime) {
        this.punchOutTime = punchOutTime;
    }

    public double getPresentDay() {
        return presentDay;
    }

    public void setPresentDay(double presentDay) {
        this.presentDay = presentDay;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonMobileNo() {
        return personMobileNo;
    }

    public void setPersonMobileNo(String personMobileNo) {
        this.personMobileNo = personMobileNo;
    }

    public String getPersonFirebaseKey() {
        return personFirebaseKey;
    }

    public void setPersonFirebaseKey(String personFirebaseKey) {
        this.personFirebaseKey = personFirebaseKey;
    }

    public String getTotalWorkingHours() {
        return totalWorkingHours;
    }

    public void setTotalWorkingHours(String totalWorkingHours) {
        this.totalWorkingHours = totalWorkingHours;
    }

    public String getPunchInLocationCode() {
        return punchInLocationCode;
    }

    public void setPunchInLocationCode(String punchInLocationCode) {
        this.punchInLocationCode = punchInLocationCode;
    }

    public String getPunchOutLocationCode() {
        return punchOutLocationCode;
    }

    public void setPunchOutLocationCode(String punchOutLocationCode) {
        this.punchOutLocationCode = punchOutLocationCode;
    }

    public double getPunchInLatitude() {
        return punchInLatitude;
    }

    public void setPunchInLatitude(double punchInLatitude) {
        this.punchInLatitude = punchInLatitude;
    }

    public double getPunchInLongitude() {
        return punchInLongitude;
    }

    public void setPunchInLongitude(double punchInLongitude) {
        this.punchInLongitude = punchInLongitude;
    }

    public double getPunchOutLatitude() {
        return punchOutLatitude;
    }

    public void setPunchOutLatitude(double punchOutLatitude) {
        this.punchOutLatitude = punchOutLatitude;
    }

    public double getPunchOutLongitude() {
        return punchOutLongitude;
    }

    public void setPunchOutLongitude(double punchOutLongitude) {
        this.punchOutLongitude = punchOutLongitude;
    }

    public String getPunchInBy() {
        return punchInBy;
    }

    public void setPunchInBy(String punchInBy) {
        this.punchInBy = punchInBy;
    }

    public String getPunchOutBy() {
        return punchOutBy;
    }

    public void setPunchOutBy(String punchOutBy) {
        this.punchOutBy = punchOutBy;
    }

    public double getOverTimeInMinutes() {
        return overTimeInMinutes;
    }

    public void setOverTimeInMinutes(double overTimeInMinutes) {
        this.overTimeInMinutes = overTimeInMinutes;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public int getDay() {
        return Day;
    }

    public void setDay(int day) {
        Day = day;
    }

    public boolean isTransactionAdded() {
        return IsTransactionAdded;
    }

    public void setTransactionAdded(boolean transactionAdded) {
        IsTransactionAdded = transactionAdded;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
