package org.bukkit.craftbukkit.util;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.Dynamic;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.init.Blocks;
import net.minecraft.server.DataConverterFlattenData;
import net.minecraft.util.datafix.fixes.ItemIntIDToString;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.init.Bootstrap;
import net.minecraft.server.DynamicOpsNBT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.properties.IProperty;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.material.MaterialData;

/**
 * This class may seem unnecessarily slow and complicated/repetitive however it
 * is able to handle a lot more edge cases and invertible transformations (many
 * of which are not immediately obvious) than any other alternative. If you do
 * make changes to this class please make sure to contribute them back
 * https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse so
 * that all may benefit.
 *
 * @deprecated legacy use only
 */
@Deprecated
public class CraftLegacy {

    private static final Map<Byte, Material> SPAWN_EGGS = new HashMap<>();
    private static final Set<String> whitelistedStates = new HashSet<>(Arrays.asList("explode", "check_decay", "decayable"));
    private static final Map<MaterialData, Item> materialToItem = new HashMap<>();
    private static final Map<Item, MaterialData> itemToMaterial = new HashMap<>();
    private static final Map<MaterialData, IBlockState> materialToData = new HashMap<>();
    private static final Map<IBlockState, MaterialData> dataToMaterial = new HashMap<>();
    private static final Map<MaterialData, Block> materialToBlock = new HashMap<>();
    private static final Map<Block, MaterialData> blockToMaterial = new HashMap<>();

    public static Material toLegacy(Material material) {
        if (material == null || material.isLegacy()) {
            return material;
        }

        return toLegacyData(material).getItemType();
    }

    public static MaterialData toLegacyData(Material material) {
        Preconditions.checkArgument(!material.isLegacy(), "toLegacy on legacy Material");
        MaterialData mappedData;

        if (material.isBlock()) {
            Block block = CraftMagicNumbers.getBlock(material);
            IBlockState blockData = block.getDefaultState();

            // Try exact match first
            mappedData = dataToMaterial.get(blockData);
            // Fallback to any block
            if (mappedData == null) {
                mappedData = blockToMaterial.get(block);
                // Fallback to matching item
                if (mappedData == null) {
                    mappedData = itemToMaterial.get(block.getItem());
                }
            }
        } else {
            Item item = CraftMagicNumbers.getItem(material);
            mappedData = itemToMaterial.get(item);
        }

        return (mappedData == null) ? new MaterialData(Material.LEGACY_AIR) : mappedData;
    }

    public static IBlockState fromLegacyData(Material material, Block block, byte data) {
        Preconditions.checkArgument(material.isLegacy(), "fromLegacyData on modern Material");

        MaterialData materialData = new MaterialData(material, data);

        // Try exact match first
        IBlockState converted = materialToData.get(materialData);
        if (converted != null) {
            return converted;
        }

        // Fallback to any block
        Block convertedBlock = materialToBlock.get(materialData);
        if (convertedBlock != null) {
            return convertedBlock.getDefaultState();
        }

        // Return existing block
        return block.getDefaultState();
    }

    public static Item fromLegacyData(Material material, Item item, short data) {
        Preconditions.checkArgument(material.isLegacy(), "fromLegacyData on modern Material. Did you forget to define api-version: 1.13 in your plugin.yml?");

        MaterialData materialData = new MaterialData(material, (byte) data);

        if (material.isBlock()) {
            // Try exact match first
            IBlockState converted = materialToData.get(materialData);
            if (converted != null) {
                return converted.getBlock().getItem();
            }

            // Fallback to any block
            Block convertedBlock = materialToBlock.get(materialData);
            if (convertedBlock != null) {
                return convertedBlock.getItem();
            }
        }

        // Fallback to matching item
        Item convertedItem = materialToItem.get(materialData);
        if (convertedItem != null) {
            return convertedItem;
        }

        // Return existing item
        return item;
    }

    public static byte toLegacyData(IBlockState blockData) {
        MaterialData mappedData;

        // Try exact match first
        mappedData = dataToMaterial.get(blockData);
        // Fallback to any block
        if (mappedData == null) {
            mappedData = blockToMaterial.get(blockData.getBlock());
        }

        return (mappedData == null) ? 0 : mappedData.getData();
    }

    public static Material fromLegacy(Material material) {
        return fromLegacy(new MaterialData(material));
    }

    public static Material fromLegacy(MaterialData materialData) {
        Material material = materialData.getItemType();
        if (material == null || !material.isLegacy()) {
            return material;
        }

        Material mappedData = null;

        if (material.isBlock()) {
            // Try exact match first
            IBlockState iblock = materialToData.get(materialData);
            if (iblock != null) {
                mappedData = CraftMagicNumbers.getMaterial(iblock.getBlock());
            }

            // Fallback to any block
            if (mappedData == null) {
                Block block = materialToBlock.get(materialData);
                if (block != null) {
                    mappedData = CraftMagicNumbers.getMaterial(block);
                }
            }
        }

        // Fallback to matching item
        if (mappedData == null) {
            Item item = materialToItem.get(materialData);
            if (item != null) {
                mappedData = CraftMagicNumbers.getMaterial(item);
            }
        }

        return (mappedData == null) ? Material.AIR : mappedData;
    }

    public static Material[] values() {
        Material[] values = Material.values();
        return Arrays.copyOfRange(values, Material.LEGACY_AIR.ordinal(), values.length);
    }

    public static Material valueOf(String name) {
        return (name.startsWith(Material.LEGACY_PREFIX)) ? Material.valueOf(name) : Material.valueOf(Material.LEGACY_PREFIX + name);
    }

    public static Material getMaterial(String name) {
        return (name.startsWith(Material.LEGACY_PREFIX)) ? Material.getMaterial(name) : Material.getMaterial(Material.LEGACY_PREFIX + name);
    }

    public static Material matchMaterial(String name) {
        return (name.startsWith(Material.LEGACY_PREFIX)) ? Material.matchMaterial(name) : Material.matchMaterial(Material.LEGACY_PREFIX + name);
    }

    public static int ordinal(Material material) {
        Preconditions.checkArgument(material.isLegacy(), "ordinal on modern Material");

        return material.ordinal() - Material.LEGACY_AIR.ordinal();
    }

    public static String name(Material material) {
        return material.name().substring(Material.LEGACY_PREFIX.length());
    }

    public static String toString(Material material) {
        return name(material);
    }

    public static Material[] modern_values() {
        Material[] values = Material.values();
        return Arrays.copyOfRange(values, 0, Material.LEGACY_AIR.ordinal());
    }

    public static int modern_ordinal(Material material) {
        if (material.isLegacy()) {
            // SPIGOT-4002: Fix for eclipse compiler manually compiling in default statements to lookupswitch
            throw new NoSuchFieldError("Legacy field ordinal: " + material);
        }

        return material.ordinal();
    }

    static {
        SPAWN_EGGS.put((byte) 0, Material.PIG_SPAWN_EGG); // Will be fixed by updateMaterial if possible

        SPAWN_EGGS.put((byte) EntityType.BAT.getTypeId(), Material.BAT_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.BLAZE.getTypeId(), Material.BLAZE_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.CAVE_SPIDER.getTypeId(), Material.CAVE_SPIDER_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.CHICKEN.getTypeId(), Material.CHICKEN_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.COW.getTypeId(), Material.COW_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.CREEPER.getTypeId(), Material.CREEPER_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.ENDERMAN.getTypeId(), Material.ENDERMAN_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.ENDERMITE.getTypeId(), Material.ENDERMITE_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.GHAST.getTypeId(), Material.GHAST_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.GUARDIAN.getTypeId(), Material.GUARDIAN_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.HORSE.getTypeId(), Material.HORSE_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.MAGMA_CUBE.getTypeId(), Material.MAGMA_CUBE_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.MUSHROOM_COW.getTypeId(), Material.MOOSHROOM_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.OCELOT.getTypeId(), Material.OCELOT_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.PIG.getTypeId(), Material.PIG_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.RABBIT.getTypeId(), Material.RABBIT_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.SHEEP.getTypeId(), Material.SHEEP_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.SHULKER.getTypeId(), Material.SHULKER_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.SILVERFISH.getTypeId(), Material.SILVERFISH_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.SKELETON.getTypeId(), Material.SKELETON_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.SLIME.getTypeId(), Material.SLIME_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.SPIDER.getTypeId(), Material.SPIDER_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.SQUID.getTypeId(), Material.SQUID_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.VILLAGER.getTypeId(), Material.VILLAGER_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.WITCH.getTypeId(), Material.WITCH_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.WOLF.getTypeId(), Material.WOLF_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.PIG_ZOMBIE.getTypeId(), Material.ZOMBIE_PIGMAN_SPAWN_EGG);
        SPAWN_EGGS.put((byte) EntityType.ZOMBIE.getTypeId(), Material.ZOMBIE_SPAWN_EGG);

        Bootstrap.register();

        for (Material material : Material.values()) {
            if (!material.isLegacy()) {
                continue;
            }

            // Handle blocks
            if (material.isBlock()) {
                for (byte data = 0; data < 16; data++) {
                    MaterialData matData = new MaterialData(material, data);
                    Dynamic blockTag = DataConverterFlattenData.b(material.getId() << 4 | data);
                    // TODO: better skull conversion, chests
                    if (blockTag.getString("Name").contains("%%FILTER_ME%%")) {
                        continue;
                    }

                    String name = blockTag.getString("Name");
                    // TODO: need to fix
                    if (name.equals("minecraft:portal")) {
                        name = "minecraft:nether_portal";
                    }

                    Block block = Block.REGISTRY.get(new ResourceLocation(name));
                    IBlockState blockData = block.getDefaultState();
                    BlockStateContainer states = block.getStates();

                    Optional<Dynamic> propMap = blockTag.get("Properties");
                    if (propMap.isPresent()) {
                        NBTTagCompound properties = (NBTTagCompound) propMap.get().getValue();
                        for (String dataKey : properties.getKeys()) {
                            IProperty state = states.getProperty(dataKey);

                            if (state == null) {
                                if (whitelistedStates.contains(dataKey)) {
                                    continue;
                                }
                                throw new IllegalStateException("No state for " + dataKey);
                            }

                            Preconditions.checkState(!properties.getString(dataKey).isEmpty(), "Empty data string");
                            Optional opt = state.b(properties.getString(dataKey));

                            blockData = blockData.set(state, (Comparable) opt.get());
                        }
                    }

                    if (block == Blocks.AIR) {
                        continue;
                    }

                    materialToData.put(matData, blockData);
                    if (!dataToMaterial.containsKey(blockData)) {
                        dataToMaterial.put(blockData, matData);
                    }

                    materialToBlock.put(matData, block);
                    if (!blockToMaterial.containsKey(block)) {
                        blockToMaterial.put(block, matData);
                    }
                }
            }

            // Handle items (and second fallback for blocks)
            int maxData = material.getMaxDurability() == 0 ? 16 : 1;
            // Manually do oldold spawn eggs
            if (material == Material.LEGACY_MONSTER_EGG) {
                maxData = 121; // Vilager + 1
            }

            for (byte data = 0; data < maxData; data++) {
                // Manually skip invalid oldold spawn
                if (material == Material.LEGACY_MONSTER_EGG /*&& data != 0 && EntityType.fromId(data) == null*/) { // Mojang broke 18w19b
                    continue;
                }
                // Skip non item stacks for now (18w19b)
                if (ItemIntIDToString.a(material.getId()) == null) {
                    continue;
                }

                MaterialData matData = new MaterialData(material, data);

                NBTTagCompound stack = new NBTTagCompound();
                stack.setInteger("id", material.getId());
                stack.setShort("Damage", data);

                Dynamic<NBTBase> converted = DataFixesManager.a().update(FixTypes.ITEM_STACK, new Dynamic<NBTBase>(DynamicOpsNBT.a, stack), -1, CraftMagicNumbers.DATA_VERSION);

                String newId = converted.getString("id");
                // Recover spawn eggs with invalid data
                if (newId.equals("minecraft:spawn_egg")) {
                    newId = "minecraft:pig_spawn_egg";
                }

                // Preconditions.checkState(newId.contains("minecraft:"), "Unknown new material for " + matData);
                Item newMaterial = Item.REGISTRY.get(new ResourceLocation(newId));

                materialToItem.put(matData, newMaterial);
                if (!itemToMaterial.containsKey(newMaterial)) {
                    itemToMaterial.put(newMaterial, matData);
                }
            }

            for (Map.Entry<Byte, Material> entry : SPAWN_EGGS.entrySet()) {
                MaterialData matData = new MaterialData(Material.LEGACY_MONSTER_EGG, entry.getKey());
                Item newMaterial = CraftMagicNumbers.getItem(entry.getValue());

                materialToItem.put(matData, newMaterial);
                itemToMaterial.put(newMaterial, matData);
            }
        }
    }
}
