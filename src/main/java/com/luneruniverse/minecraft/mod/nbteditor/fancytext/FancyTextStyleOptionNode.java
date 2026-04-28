package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTextEvents;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.IdentifierException;
import net.minecraft.world.item.ItemStackTemplate;

public record FancyTextStyleOptionNode(StyleOption option, String value, List<FancyTextNode> contents) implements FancyTextNode {
	
	@Override
	public Style modifyStyle(Style style) {
		return switch (option) {
			case OPEN_URL, RUN_COMMAND, SUGGEST_COMMAND, CHANGE_PAGE, COPY_TO_CLIPBOARD -> MVTextEvents.ClickAction.fromName(
					option.name().toLowerCase()).newEventParse(value == null ? "" : value).map(style::withClickEvent).orElse(style);
			case SHOW_TEXT -> style.withHoverEvent(
					MVTextEvents.HoverAction.SHOW_TEXT.newEvent(value == null ? TextInst.of("") : FancyText.parse(value)));
			case SHOW_ITEM -> {
				ItemStack item;
				try {
					item = MainUtil.client.player.getInventory().getItem(Integer.parseInt(value));
				} catch (NumberFormatException e) {
					try {
						item = ItemReference.getHeldItem().getItem();
					} catch (CommandSyntaxException e2) {
						item = ItemStack.EMPTY;
					}
				}
				yield style.withHoverEvent(MVTextEvents.HoverAction.SHOW_ITEM.newEvent(new ItemStackTemplate(item.typeHolder(),item.count(),item.getComponentsPatch())));
			}
			case SHOW_ENTITY -> {
				Entity entity;
				try {
					if (value == null)
						throw new IllegalArgumentException();
					String uuid = value;
					if (!uuid.contains("-"))
						uuid = uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5");
					UUID uuidObj = UUID.fromString(uuid);
					entity = StreamSupport.stream(MainUtil.client.level.entitiesForRendering().spliterator(), false)
							.filter(testEntity -> testEntity.getUUID().equals(uuidObj)).findFirst()
							.orElseThrow(IllegalArgumentException::new);
				} catch (IllegalArgumentException e) {
					if (MainUtil.client.crosshairPickEntity != null)
						entity = MainUtil.client.crosshairPickEntity;
					else
						entity = MainUtil.client.player;
				}
				yield style.withHoverEvent(MVTextEvents.HoverAction.SHOW_ENTITY.newEvent(
						new HoverEvent.EntityTooltipInfo(entity.getType(), entity.getUUID(), entity.getName())));
			}
			case INSERTION -> style.withInsertion(value);
			case FONT -> {
				try {
					yield style.withFont(new FontDescription.Resource(IdentifierInst.of(value)));
				} catch (IdentifierException e) {
					yield style.withFont(Style.EMPTY.getFont());
				}
			}
		};
	}
	
	@Override
	public int getNumberOfTextNodes() {
		return contents.stream().mapToInt(FancyTextNode::getNumberOfTextNodes).sum();
	}
	
}
