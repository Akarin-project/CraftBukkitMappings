/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftChorusFlower extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.Ageable {

    public CraftChorusFlower() {
        super();
    }

    public CraftChorusFlower(minecraft.block.state.IBlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.CraftAgeable

    private static final minecraft.block.properties.PropertyInteger AGE = getInteger(net.minecraft.block.BlockChorusFlower.class, "age");

    @Override
    public int getAge() {
        return get(AGE);
    }

    @Override
    public void setAge(int age) {
        set(AGE, age);
    }

    @Override
    public int getMaximumAge() {
        return getMax(AGE);
    }
}
