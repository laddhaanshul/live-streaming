package com.laddha.agoraaudience.model;

import com.google.gson.annotations.SerializedName;

public class CommentData {

    @SerializedName("commentMsg")
    private String commentMsg;
    @SerializedName("userId")
    private long userId;
    @SerializedName("userName")
    private String userName;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setCommentMsg(String commentMsg) {
        this.commentMsg = commentMsg;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCommentMsg() {
        return commentMsg;
    }

    public String getUserName() {
        return userName;
    }
}
