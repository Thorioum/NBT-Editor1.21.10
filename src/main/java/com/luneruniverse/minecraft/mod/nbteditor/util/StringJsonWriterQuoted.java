package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.ArrayList;
import java.util.Collections;

import com.google.common.collect.Lists;
import com.luneruniverse.minecraft.mod.nbteditor.mixin.StringTagVisitorAccessor;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.StringTagVisitor;

public class StringJsonWriterQuoted extends StringTagVisitor {
	
	// From StringNbtWriter.apply in <= 1.21.4
	public String apply(Tag element) {
		element.accept(this);
		return ((StringTagVisitorAccessor) this).getBuilder().toString();
	}
	
	@Override
	public void visitByte(ByteTag element) {
		if (element.byteValue() == 0)
			((StringTagVisitorAccessor) this).getBuilder().append(false);
		else if (element.byteValue() == 1)
			((StringTagVisitorAccessor) this).getBuilder().append(true);
		else
			super.visitByte(element);
	}
	
	@Override
	public void visitString(StringTag element) {
		((StringTagVisitorAccessor) this).getBuilder().append(escape(MVMisc.value(element)));
	}
	
    @Override
    public void visitList(ListTag element) {
		StringBuilder result = ((StringTagVisitorAccessor) this).getBuilder();
		
        result.append('[');
        for (int i = 0; i < element.size(); ++i) {
            if (i != 0) {
                result.append(',');
            }
            result.append(new StringJsonWriterQuoted().apply(element.get(i)));
        }
        result.append(']');
    }
	
	@Override
	public void visitCompound(CompoundTag compound) {
		StringBuilder result = ((StringTagVisitorAccessor) this).getBuilder();
		
		result.append('{');
        ArrayList<String> list = Lists.newArrayList(compound.keySet());
        Collections.sort(list);
        for (String string : list) {
            if (result.length() != 1) {
                result.append(',');
            }
            result.append(escape(string)).append(':').append(new StringJsonWriterQuoted().apply(compound.get(string)));
        }
        result.append('}');
	}
	
	// From NbtString.escape
	// Edited to optionally force double quotes
	private static String escape(String value) {
		StringBuilder builder = new StringBuilder(" ");
		char quote = (ConfigScreen.isSingleQuotesAllowed() ? '\0' : '"');
		
		for (char c : value.toCharArray()) {
			if (c == '\\') {
				builder.append('\\');
			} else if (c == '"' || c == '\'') {
				if (quote == '\0')
					quote = (c == '"' ? '\'' : '"');
				if (quote == c)
					builder.append('\\');
			}
			builder.append(c);
		}
		
		if (quote == '\0')
			quote = '"';
		builder.setCharAt(0, quote);
		builder.append(quote);
		return builder.toString();
	}
	
}
