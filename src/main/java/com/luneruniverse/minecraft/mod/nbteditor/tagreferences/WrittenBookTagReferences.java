package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.ComponentTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.NBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;

import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.network.Filterable;
import net.minecraft.network.chat.Component;

public class WrittenBookTagReferences {
	
	private static WrittenBookContent getComponent(WrittenBookContent content,
                                                   Supplier<String> title, Supplier<String> author, Supplier<Integer> generation, Supplier<List<Component>> pages) {
		if (content == null)
			content = new WrittenBookContent(Filterable.passThrough(""), "", 0, List.of(), false);
		return new WrittenBookContent(
				title == null ? content.title() : Filterable.passThrough(title.get()),
				author == null ? content.author() : author.get(),
				generation == null ? content.generation() : generation.get(),
				pages == null ? content.pages() : pages.get().stream().map(Filterable::passThrough).toList(),
				content.resolved());
	}
	
	public static final TagReference<String, ItemStack> TITLE = Version.<TagReference<String, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.WRITTEN_BOOK_CONTENT,
					null,
					content -> content == null ? "" : content.title().raw(),
					(content, value) -> getComponent(content, () -> value, null, null, null)))
			.range(null, "1.20.4", () -> TagReference.forItems(() -> "", TagReference.alsoRemove("filtered_title",
					new NBTTagReference<>(String.class, "title"))))
			.get();
	
	public static final TagReference<String, ItemStack> AUTHOR = Version.<TagReference<String, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.WRITTEN_BOOK_CONTENT,
					null,
					content -> content == null ? "" : content.author(),
					(content, value) -> getComponent(content, null, () -> value, null, null)))
			.range(null, "1.20.4", () -> TagReference.forItems(() -> "", new NBTTagReference<>(String.class, "author")))
			.get();
	
	public static final TagReference<Integer, ItemStack> GENERATION = Version.<TagReference<Integer, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.WRITTEN_BOOK_CONTENT,
					null,
					content -> content == null ? 0 : content.generation(),
					(content, value) -> getComponent(content, null, null, () -> value, null)))
			.range(null, "1.20.4", () -> TagReference.forItems(() -> 0, new NBTTagReference<>(Integer.class, "generation")))
			.get();
	
	public static final TagReference<List<Component>, ItemStack> PAGES = Version.<TagReference<List<Component>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.WRITTEN_BOOK_CONTENT,
					null,
					content -> content == null ? new ArrayList<>() : content.pages().stream().map(Filterable::raw).collect(Collectors.toList()),
					(content, value) -> getComponent(content, null, null, null, () -> value)))
			.range(null, "1.20.4", () -> TagReference.forItems(ArrayList::new, TagReference.alsoRemove("filtered_pages",
					TagReference.forLists(Component.class, new NBTTagReference<>(Component[].class, "pages")))))
			.get();
	
}
