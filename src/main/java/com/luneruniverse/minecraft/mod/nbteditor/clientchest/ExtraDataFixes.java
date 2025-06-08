package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import com.mojang.brigadier.StringReader;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.nbt.*;
import net.minecraft.util.StringHelper;

import java.util.ArrayList;
import java.util.List;
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
