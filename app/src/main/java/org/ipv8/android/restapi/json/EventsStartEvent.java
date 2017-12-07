package org.ipv8.android.restapi.json;

public class EventsStartEvent {

    public static final String TYPE = "events_start";

    private boolean locked;
    private boolean password_set;

    EventsStartEvent() {
    }

    public boolean getLocked() { return this.locked; }

    public boolean getPasswordSet() { return this.password_set; }

}
