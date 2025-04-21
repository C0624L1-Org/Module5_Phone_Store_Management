
package com.example.md5_phone_store_management.event;

import java.util.HashMap;
import java.util.Map;

public class EntityChangeEvent {
    private final Object source;
    private final Object entity;
    private final String action;
    private final Object oldEntity;
    private final Map<String, Object> metadata;

    public EntityChangeEvent(Object source, Object entity, String action, Object oldEntity) {
        this.source = source;
        this.entity = entity;
        this.action = action;
        this.oldEntity = oldEntity;
        this.metadata = new HashMap<>();
    }

    public Object getSource() {
        return source;
    }

    public Object getEntity() {
        return entity;
    }

    public String getAction() {
        return action;
    }

    public Object getOldEntity() {
        return oldEntity;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
}