package org.dimasik.liteauction.api.events;

import lombok.Getter;

public abstract class Event {
    private String name;
    @Getter
    private final boolean async;

    public Event() {
        this(false);
    }

    public Event(boolean isAsync) {
        this.async = isAsync;
    }

    public String getEventName() {
        if (name == null) {
            name = getClass().getSimpleName();
        }
        return name;
    }
}