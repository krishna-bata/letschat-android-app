package com.example.letschat.Models;

public class SelectedMembersModel {
    String  name,image,status,state,currentUserId;
    Boolean isSelected;

    public SelectedMembersModel(String name, String image,boolean isSelected) {
        this.name = name;
        this.image = image;
        this.isSelected=isSelected;
    }

    public SelectedMembersModel() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }
}
