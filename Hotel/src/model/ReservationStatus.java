package model;

import util.Messages;

/**
 * Enum representing the different statuses a reservation can have.
 */
public enum ReservationStatus {
    CONFIRMED("reservationstatus.confirmed"),
    CHECKED_IN("reservationstatus.checkedin"),
    CHECKED_OUT("reservationstatus.checkedout"),
    CANCELLED("reservationstatus.cancelled");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return Messages.get(displayName);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
