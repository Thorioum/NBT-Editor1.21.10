package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import com.mojang.brigadier.StringReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.nbt.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ExtraDataFixes {

    private static final List<Fix> fixes = new ArrayList<>();

    //i reallly tried to make something like this with a mixin into minecraft data fixers, but the errors were beyond my comprehension

    public static void init() {


        fixes.add(new Fix("OOB Enchant Fixer", 3950, -1) {
            @Override
            public void fix(CompoundTag nbt) {
                forEveryRecursiveItem(nbt,tag->{
                    CompoundTag components = tag.getCompoundOrEmpty("components");
                    if (components.contains("minecraft:enchantments")) {
                        CompoundTag enchantments = components.getCompoundOrEmpty("minecraft:enchantments");
                        if(enchantments.get("levels") instanceof CompoundTag) {
                            CompoundTag levels = enchantments.getCompoundOrEmpty("levels");
                            for(String key : levels.keySet()) {
                                if(levels.get(key) instanceof IntTag i && i.intValue() == 0) {
                                    levels.put(key, IntTag.valueOf(1));
                                }
                            }
                        }
                    }
                });
            }
        });
        fixes.add(new Fix("Empty Entity ID Fixer", 3950, -1) {

            private String getBEntityId(String blockId) {
                try {
                    Block b = BuiltInRegistries.BLOCK.getValue(Identifier.parse(blockId));
                    if (b instanceof EntityBlock p) {
                        BlockEntity e = p.newBlockEntity(new BlockPos(0, 0, 0), b.defaultBlockState());
                        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(e.getType()).toString();
                    }
                } catch (Exception ignored) {
                }
                return blockId;
            }
            private String getEntityId(String itemId) {
                try {
                    if (BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId)) instanceof SpawnEggItem i) {
                        EntityType<?> t = i.getType(new ItemStack(i));
                        return BuiltInRegistries.ENTITY_TYPE.getKey(t).toString();
                    }
                } catch (Exception e) {
                }
                return itemId;
            }
            @Override
            public void fix(CompoundTag nbt) {
                forEveryRecursiveItem(nbt,tag->{
                    Optional<String> id = tag.getString("id");
                    if(id.isPresent()) {
                        CompoundTag components = tag.getCompoundOrEmpty("components");
                        if (components.contains("minecraft:block_entity_data")) {
                            CompoundTag blockEntityData = components.getCompoundOrEmpty("minecraft:block_entity_data");
                            String bid = getBEntityId(id.get());
                            if (blockEntityData.get("id") instanceof StringTag) {
                                String s = blockEntityData.getString("id").get();
                                if(s.startsWith("minecraft:")) s = s.substring("minecraft:".length());
                                if(s.equals("spawner")) {
                                    blockEntityData.put("id", StringTag.valueOf("minecraft:mob_spawner"));
                                    return;
                                }
                                try {
                                    if(s.isEmpty()) throw new Exception();
                                    if(s.endsWith("command_block") && !s.equals("command_block")) throw new Exception();
                                    if(!s.endsWith("hanging_sign") && s.endsWith("sign") && !s.equals("sign")) throw new Exception();
                                    if(s.endsWith("hanging_sign") && !s.equals("hanging_sign")) throw new Exception();
                                    Identifier.parse(s);
                                } catch (Exception e) {
                                    blockEntityData.put("id", StringTag.valueOf(bid));
                                }
                            } else {
                                blockEntityData.put("id", StringTag.valueOf(bid));
                            }
                        }

                        if (components.contains("minecraft:entity_data")) {
                            CompoundTag entityData = components.getCompoundOrEmpty("minecraft:entity_data");
                            String eid = getEntityId(id.get());
                            if (entityData.get("id") instanceof StringTag) {
                                String s = entityData.getString("id").get();
                                try {
                                    if(s.isEmpty()) throw new Exception();
                                    Identifier.parse(s);
                                } catch (Exception e) {
                                    entityData.put("id", StringTag.valueOf(eid));
                                }
                            } else {
                                entityData.put("id", StringTag.valueOf(eid));
                            }
                        }
                    }
                });
            }
        });

    }

    public static void applyFixes(CompoundTag tag, int currentDataVer) {
        for(Fix each : fixes) {
            if(each.isValidDataVersion(currentDataVer)) {
                each.applyFix(tag);
            }
        }
    }



    private abstract static class Fix {
        public final String name;
        private final int minDataVersion, maxDataVersion;
        public Fix(String name, int minDataVersion, int maxDataVersion) {
            this.name = name;
            this.minDataVersion = minDataVersion;
            this.maxDataVersion = maxDataVersion;
        }
        public boolean isValidDataVersion(int dataVersion) {
            int min = minDataVersion == -1 ? 0 : minDataVersion;
            int max = maxDataVersion == -1 ? Integer.MAX_VALUE : maxDataVersion;
            return dataVersion >= min && dataVersion <= max;
        }
        public void applyFix(CompoundTag tag) {
            fix(tag);
        }
        protected void forEveryRecursiveItem(Tag nbt, Consumer<CompoundTag> itemConsumer) {
            if(nbt instanceof CompoundTag item) {
                if(item.contains("components") && item.contains("id")) itemConsumer.accept(item);
                for(String each : item.keySet()) {
                    Tag nbt2 = item.get(each);
                    forEveryRecursiveItem(nbt2, itemConsumer);

                }
            }
            if(nbt instanceof ListTag list) {
                for(Tag i : list) {
                    forEveryRecursiveItem(i, itemConsumer);
                }
            }
        }
        protected abstract void fix(CompoundTag nbt);

    }
}
