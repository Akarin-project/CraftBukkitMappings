/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftBubbleColumn extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.BubbleColumn {

    public CraftBubbleColumn() {
        super();
    }

    public CraftBubbleColumn(minecraft.block.state.IBlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftBubbleColumn

    private static final minecraft.block.properties.PropertyBool DRAG = getBoolean(net.minecraft.server.BlockBubbleColumn.class, "drag");

    @Override
    public boolean isDrag() {
        return get(DRAG);
    }

    @Override
    public void setDrag(boolean drag) {
        set(DRAG, drag);
    }
}
