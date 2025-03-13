package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ExtraDataFixes {

    private static final List<Fix> fixes = new ArrayList<>();

    //i reallly tried to make something like this with a mixin into minecraft data fixers, but the errors were beyond my comprehension

    public static void init() {


        fixes.add(new Fix("OOBEnchantFixer", 3950, -1) {
            @Override
            public void fix(NbtCompound nbt) {
                forEveryRecursiveItem(nbt,tag->{
                    NbtCompound components = tag.getCompound("components");
                    if (components.contains("minecraft:enchantments")) {
                        NbtCompound enchantments = components.getCompound("minecraft:enchantments");
                        if(enchantments.contains("levels",NbtCompound.COMPOUND_TYPE)) {
                            NbtCompound levels = enchantments.getCompound("levels");
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
        protected abstract void fix(NbtCompound tag);

    }
}
