/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftEnderPortalFrame extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.EndPortalFrame, org.bukkit.block.data.Directional {

    public CraftEnderPortalFrame() {
        super();
    }

    public CraftEnderPortalFrame(minecraft.block.state.IBlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftEndPortalFrame

    private static final minecraft.block.properties.PropertyBool EYE = getBoolean(net.minecraft.block.BlockEndPortalFrame.class, "eye");

    @Override
    public boolean hasEye() {
        return get(EYE);
    }

    @Override
    public void setEye(boolean eye) {
        set(EYE, eye);
    }

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final minecraft.block.properties.PropertyEnum<?> FACING = getEnum(net.minecraft.block.BlockEndPortalFrame.class, "facing");

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return get(FACING, org.bukkit.block.BlockFace.class);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        set(FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return getValues(FACING, org.bukkit.block.BlockFace.class);
    }
}
