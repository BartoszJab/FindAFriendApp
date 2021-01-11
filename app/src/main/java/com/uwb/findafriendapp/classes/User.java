package com.uwb.findafriendapp.classes;

public class User {

    private String name;
    private String email;
    private String phoneNumber;
    private int age;
    private String username;
    private String avatarUrl;
    private String facebookUrl;
    private String userDescription;
    private int interestsCount;
    private int mainInterestsCount;

    public User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.interestsCount = 0;
        this.mainInterestsCount = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String firstName) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String login) {
        this.username = login;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getUserDescription() {
        return userDescription;
    }

    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }

    public int getMainInterestsCount() {
        return mainInterestsCount;
    }

    public void setMainInterestsCount(int mainInterestsCount) {
        this.mainInterestsCount = mainInterestsCount;
    }

    public int getInterestsCount() {
        return interestsCount;
    }

    public void setInterestsCount(int interestsCount) {
        this.interestsCount = interestsCount;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }
}
