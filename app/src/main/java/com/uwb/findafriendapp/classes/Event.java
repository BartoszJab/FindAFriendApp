package com.uwb.findafriendapp.classes;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event {

    private String owner;
    private String eventDescription;
    private String date;
    private String time;
    private String localization;
    private String iconRef;

    public Event() {

    }

    public Event(String owner, String eventDescription, String dateString, String timeString, String localization, String iconRef) {
        this.owner = owner;
        this.eventDescription = eventDescription;
        this.date = dateString;
        this.time = timeString;
        this.localization = localization;
        this.iconRef = iconRef;
    }

    public String getIconRef() {
        return iconRef;
    }

    public void setIconRef(String iconRef) {
        this.iconRef = iconRef;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getLocalization() {
        return localization;
    }

    public void setLocalization(String localization) {
        this.localization = localization;
    }

    @Override
    public String toString() {
        return "Event{" +
                "localization='" + localization + '\'' +
                '}';
    }
}
