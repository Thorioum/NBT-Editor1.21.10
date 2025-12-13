package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import com.mojang.brigadier.StringReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.BlockPos;

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
            public void fix(NbtCompound nbt) {
                forEveryRecursiveItem(nbt,tag->{
                    NbtCompound components = tag.getCompoundOrEmpty("components");
                    if (components.contains("minecraft:enchantments")) {
                        NbtCompound enchantments = components.getCompoundOrEmpty("minecraft:enchantments");
                        if(enchantments.get("levels") instanceof NbtCompound) {
                            NbtCompound levels = enchantments.getCompoundOrEmpty("levels");
                            for(String key : levels.getKeys()) {
                                if(levels.get(key) instanceof NbtInt i && i.intValue() == 0) {
                                    levels.put(key,NbtInt.of(1));
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
                    Block b = Registries.BLOCK.get(Identifier.of(blockId));
                    if (b instanceof BlockEntityProvider p) {
                        BlockEntity e = p.createBlockEntity(new BlockPos(0, 0, 0), b.getDefaultState());
                        return Registries.BLOCK_ENTITY_TYPE.getId(e.getType()).toString();
                    }
                } catch (Exception ignored) {
                }
                return blockId;
            }
            private String getEntityId(String itemId) {
                try {
                    if (Registries.ITEM.get(Identifier.of(itemId)) instanceof SpawnEggItem i) {
                        EntityType<?> t = i.getEntityType(new ItemStack(i));
                        return Registries.ENTITY_TYPE.getId(t).toString();
                    }
                } catch (Exception e) {
                }
                return itemId;
            }
            @Override
            public void fix(NbtCompound nbt) {
                forEveryRecursiveItem(nbt,tag->{
                    Optional<String> id = tag.getString("id");
                    if(id.isPresent()) {
                        NbtCompound components = tag.getCompoundOrEmpty("components");
                        if (components.contains("minecraft:block_entity_data")) {
                            NbtCompound blockEntityData = components.getCompoundOrEmpty("minecraft:block_entity_data");
                            String bid = getBEntityId(id.get());
                            if (blockEntityData.get("id") instanceof NbtString) {
                                String s = blockEntityData.getString("id").get();
                                if(s.startsWith("minecraft:")) s = s.substring("minecraft:".length());
                                if(s.equals("spawner")) {
                                    blockEntityData.put("id",NbtString.of("minecraft:mob_spawner"));
                                    return;
                                }
                                try {
                                    if(s.isEmpty()) throw new Exception();
                                    if(s.endsWith("command_block") && !s.equals("command_block")) throw new Exception();
                                    if(!s.endsWith("hanging_sign") && s.endsWith("sign") && !s.equals("sign")) throw new Exception();
                                    if(s.endsWith("hanging_sign") && !s.equals("hanging_sign")) throw new Exception();
                                    Identifier.of(s);
                                } catch (Exception e) {
                                    blockEntityData.put("id",NbtString.of(bid));
                                }
                            } else {
                                blockEntityData.put("id",NbtString.of(bid));
                            }
                        }

                        if (components.contains("minecraft:entity_data")) {
                            NbtCompound entityData = components.getCompoundOrEmpty("minecraft:entity_data");
                            String eid = getEntityId(id.get());
                            if (entityData.get("id") instanceof NbtString) {
                                String s = entityData.getString("id").get();
                                try {
                                    if(s.isEmpty()) throw new Exception();
                                    Identifier.of(s);
                                } catch (Exception e) {
                                    entityData.put("id",NbtString.of(eid));
                                }
                            } else {
                                entityData.put("id",NbtString.of(eid));
                            }
                        }
                    }
                });
            }
        });

    }

    public static void applyFixes(NbtCompound tag, int currentDataVer) {
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
        public void applyFix(NbtCompound tag) {
            fix(tag);
        }
        protected void forEveryRecursiveItem(NbtElement nbt, Consumer<NbtCompound> itemConsumer) {
            if(nbt instanceof NbtCompound item) {
                if(item.contains("components") && item.contains("id")) itemConsumer.accept(item);
                for(String each : item.getKeys()) {
                    NbtElement nbt2 = item.get(each);
                    forEveryRecursiveItem(nbt2, itemConsumer);

                }
            }
            if(nbt instanceof NbtList list) {
                for(NbtElement i : list) {
                    forEveryRecursiveItem(i, itemConsumer);
                }
            }
        }
        protected abstract void fix(NbtCompound nbt);

    }
}
