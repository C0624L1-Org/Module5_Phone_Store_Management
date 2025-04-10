package com.example.md5_phone_store_management.event;


import org.springframework.context.ApplicationEvent;

public class EntityChangeEvent extends ApplicationEvent {
    private final Object entity;
    private final String action;
    private final Object oldEntity; // Lưu trạng thái cũ nếu cần (cho UPDATE)

    public EntityChangeEvent(Object source, Object entity, String action, Object oldEntity) {
        super(source);
        this.entity = entity;
        this.action = action;
        this.oldEntity = oldEntity;
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
}