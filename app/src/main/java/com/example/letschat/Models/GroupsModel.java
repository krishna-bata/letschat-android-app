package com.example.letschat.Models;

public class GroupsModel {
    String name,image,groupId,date,time,createdBy,groupAdmin;

    public GroupsModel() {
    }

    public GroupsModel(String name, String image, String groupId, String date, String time, String createdBy, String groupAdmin) {
        this.name = name;
        this.image = image;
        this.groupId = groupId;
        this.date = date;
        this.time = time;
        this.createdBy = createdBy;
        this.groupAdmin = groupAdmin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getGroupAdmin() {
        return groupAdmin;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }
}
