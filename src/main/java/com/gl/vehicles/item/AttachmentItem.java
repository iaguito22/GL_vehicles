package com.gl.vehicles.item;

import net.minecraft.item.Item;

public class AttachmentItem extends Item {
    public enum AttachmentType {
        FRONT, REAR
    }

    private final AttachmentType type;

    public AttachmentItem(Settings settings, AttachmentType type) {
        super(settings);
        this.type = type;
    }

    public AttachmentType getAttachmentType() {
        return type;
    }
}
