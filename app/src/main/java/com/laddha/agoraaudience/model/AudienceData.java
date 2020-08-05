package com.laddha.agoraaudience.model;

public class AudienceData {
    private long userId;
    private String name;
    private String profilePic;

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
