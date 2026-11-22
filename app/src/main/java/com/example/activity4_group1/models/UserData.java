package com.example.activity4_group1.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class UserData implements Parcelable {

    private String userId = "";
    private String name = "";
    private String email = "";
    private long createdAt = System.currentTimeMillis();
    private long lastLogin = System.currentTimeMillis();

    // Needed for Firebase (toObject / getValue) and replaces Kotlin's default values
    public UserData() {
    }

    public UserData(String userId, String name, String email, long createdAt, long lastLogin) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public long getCreatedAt() { return createdAt; }
    public long getLastLogin() { return lastLogin; }

    // Setters (Firebase uses these when mapping documents to this class)
    public void setUserId(String userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }

    // Parcelable
    protected UserData(Parcel in) {
        userId = in.readString();
        name = in.readString();
        email = in.readString();
        createdAt = in.readLong();
        lastLogin = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeLong(createdAt);
        dest.writeLong(lastLogin);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserData> CREATOR = new Creator<UserData>() {
        @Override
        public UserData createFromParcel(Parcel in) {
            return new UserData(in);
        }

        @Override
        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };

    // Replaces the methods Kotlin's data class generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserData that = (UserData) o;
        return createdAt == that.createdAt
                && lastLogin == that.lastLogin
                && Objects.equals(userId, that.userId)
                && Objects.equals(name, that.name)
                && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, name, email, createdAt, lastLogin);
    }

    @NonNull
    @Override
    public String toString() {
        return "UserData(userId=" + userId + ", name=" + name + ", email=" + email
                + ", createdAt=" + createdAt + ", lastLogin=" + lastLogin + ")";
    }
}