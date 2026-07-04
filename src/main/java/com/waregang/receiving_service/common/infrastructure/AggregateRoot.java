package com.waregang.receiving_service.common.infrastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot {
    private final List<Object> eventsToPublish = new ArrayList<>();

    protected final void registerEvent(Object event) {
        eventsToPublish.add(event);
    }

    public List<Object> pullDomainEvents() {
         List<Object> events = List.copyOf(this.eventsToPublish);
         eventsToPublish.clear();
         return events;
    }
}
