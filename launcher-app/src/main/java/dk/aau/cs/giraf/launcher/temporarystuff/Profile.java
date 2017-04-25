package dk.aau.cs.giraf.launcher.temporarystuff;

import android.graphics.Bitmap;

import dk.aau.cs.giraf.launcher.temporarystuff.Entity;

/**
 * Created by Ejer on 25-04-2017.
 */

public class Profile implements Entity{
    private long id;
    private String name;
    private String phone;
    private String email;
    private Roles role;
    private String address;
    //private Setting<String, String, String> settings;
    //private Settings newSettings = new Settings();
    private long userId;
    private long departmentId;
    private long author;
    public enum Roles {
        ADMIN(0),
        PARENT(1),
        GUARDIAN(2),
        CHILD(3);

        private final int value;

        Roles(int val)
        {
            value = val;
        }

        public int getValue(){
            return value;
        }
        private static Roles[] allValues = values();
        public static Roles fromInt(int value){
            return allValues[value];
        }
    }
    public Profile(String name, String phone, String email, Roles role, String address, long departmentId, long author) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.address = address;
        //this.settings = settings;
        //this.newSettings = newSetting;
        this.departmentId = departmentId;
        this.author = author;
    }
    /*
    public Profile(String name, long phone, String email, Roles role, String address, long departmentId, long author) {
        this(name,Long.toString(phone), email,role,address,null,departmentId,author);
    }

    public Profile(String name, long phone, String email, Roles role, String address, Setting<String, String, String> settings, long departmentId, long author, long userId) {
        this(name,Long.toString(phone), email,role,address,settings,null,departmentId,author);
        setUserId(userId);
    }

    public Profile(String name, Roles roles, String address, long departmentId) {
        this.setName(name);
        this.setRole(roles);
        this.setAddress(address);
        this.setDepartmentId(departmentId);
    }
    */
    /**
     * Empty constructor
     */
    public Profile() {
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Roles getRole() {
        return role;
    }
    public void setRole(Roles Role) {this.role = Role;}
    public String getAddress() {
        return address;
    }
    public void setAddress(String Address) {
        this.address = Address;
    }
    /*
    @Deprecated
    public Setting<String, String, String> getSettings() {
        return settings;
    }
    @Deprecated
    public void setSettings(Setting<String, String, String> settings) {
        this.settings = settings;
    }
    public Settings getNewSettings() { return newSettings; }
    public void setNewSettings(Settings setting) { this.newSettings = setting; }
    */
    public long getUserId() {
        return userId;
    }
    public void setUserId(long id) {
        this.userId = id;
    }
    public long getDepartmentId() {
        return departmentId;
    }
    public void setDepartmentId(long id) {
        this.departmentId = id;
    }
    public long getAuthor() {
        return author;
    }
    public void setAuthor(long id) {
        this.author = id;
    }
    @Override
    public boolean equals(Object aProfile) {
        if ( this == aProfile ) return true;
        if ( !(aProfile instanceof Profile) ) return false;
        Profile profile = (Profile)aProfile;
        return
                profile.getId() == this.getId();
        /*
                EqualsUtil.areEqual(this.getId(), profile.getId()) &&
                        EqualsUtil.areEqual(this.getName(), profile.getName()) &&
                        EqualsUtil.areEqual(this.getPhone(), profile.getPhone()) &&
                        EqualsUtil.areEqual(this.getEmail(), profile.getEmail()) &&
                        EqualsUtil.areEqual(this.getRole(), profile.getRole()) &&
                        EqualsUtil.areEqual(this.getAddress(), profile.getAddress()) &&
                        EqualsUtil.areEqual(this.getSettings(), profile.getSettings()) &&
                        EqualsUtil.areEqual(this.getNewSettings(), profile.getNewSettings()) &&
                        EqualsUtil.areEqual(this.getUserId(), profile.getUserId()) &&
                        EqualsUtil.areEqual(this.getDepartmentId(), profile.getDepartmentId()) &&
                        EqualsUtil.areEqual(this.getAuthor(), profile.getAuthor());
                        */
    }
}