package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.awt.Color;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2ic;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandRegistrationCallback;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders.MVShader;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.opengl.GlProgram;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.gui.components.toasts.SystemToast;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.SuspiciousStewEffects.Entry;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.NonNullList;
import net.minecraft.ReportedException;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Profiler;

public class MVMisc {

	private static final Supplier<Class<?>> SequencedSet = Reflection.getOptionalClass("java.util.SequencedSet");


	private static final Supplier<Reflection.MethodInvoker> ResourceFactory_getResource =
			Reflection.getOptionalMethod(ResourceProvider.class, "method_14486", MethodType.methodType(Resource.class, Identifier.class));
	private static final Supplier<Reflection.MethodInvoker> Resource_getInputStream =
			Reflection.getOptionalMethod(Resource.class, "method_14482", MethodType.methodType(InputStream.class));
	public static Optional<InputStream> getResource(Identifier id) throws IOException {
		try {
			return Version.<Optional<InputStream>>newSwitch()
					.range("1.19.0", null, () -> MainUtil.client.getResourceManager().getResource(id).map(resource -> {
						try {
							return resource.open();
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}))
					.range(null, "1.18.2", () -> {
						Resource resource = ResourceFactory_getResource.get().invoke(MainUtil.client.getResourceManager(), id);
						if (resource == null)
							return Optional.empty();
						return Optional.of(Resource_getInputStream.get().invokeThrowable(UncheckedIOException.class, resource));
					})
					.get();
		} catch (UncheckedIOException e) {
			if (e.getMessage() != null) {
				IOException checkedE = new IOException(e.getMessage(), e.getCause());
				checkedE.setStackTrace(e.getStackTrace());
				throw checkedE;
			}
			throw e.getCause();
		}
	}

	public static Object registryAccess;
	private static final Supplier<Reflection.MethodInvoker> ItemStackArgumentType_itemStack =
			Reflection.getOptionalMethod(ItemArgument.class, "method_9776", MethodType.methodType(ItemArgument.class));
	public static ItemArgument getItemStackArg() {
		return Version.<ItemArgument>newSwitch()
				.range("1.19.0", null, () -> ItemArgument.item((CommandBuildContext) registryAccess))
				.range(null, "1.18.2", () -> ItemStackArgumentType_itemStack.get().invoke(null)) // ItemStackArgumentType.itemStack()
				.get();
	}
	private static final Supplier<Reflection.MethodInvoker> BlockStateArgumentType_blockState =
			Reflection.getOptionalMethod(BlockStateArgument.class, "method_9653", MethodType.methodType(BlockStateArgument.class));
	public static BlockStateArgument getBlockStateArg() {
		return Version.<BlockStateArgument>newSwitch()
				.range("1.19.0", null, () -> BlockStateArgument.block((CommandBuildContext) registryAccess))
				.range(null, "1.18.2", () -> BlockStateArgumentType_blockState.get().invoke(null)) // BlockStateArgumentType.blockState()
				.get();
	}
	private static final Supplier<Reflection.MethodInvoker> TextArgumentType_text =
			Reflection.getOptionalMethod(ComponentArgument.class, "method_9281", MethodType.methodType(ComponentArgument.class));
	public static ComponentArgument getTextArg() {
		return Version.<ComponentArgument>newSwitch()
				.range("1.20.5", null, () -> ComponentArgument.textComponent((CommandBuildContext) registryAccess))
				.range(null, "1.20.4", () -> TextArgumentType_text.get().invoke(null))
				.get();
	}

	public static void registerCommands(Consumer<CommandDispatcher<FabricClientCommandSource>> callback) {
		Version.newSwitch()
				.range("1.19.0", null, () -> ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
					registryAccess = access;
					callback.accept(dispatcher);
				}))
				.range(null, "1.18.2", () -> ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
					callback.accept(dispatcher);
				}))
				.run();
	}

	public static Button newButton(int x, int y, int width, int height, Component message, Button.OnPress onPress, MVTooltip tooltip) {
		if (Version.<Boolean>newSwitch()
				.range("1.19.4", null, false)
				.range(null, "1.19.3", true)
				.get()) {
			if (height > 20) {
				y += (height - 20) / 2;
				height = 20;
			}
		}
		final int finalY = y;
		final int finalHeight = height;
		return Version.<Button>newSwitch()
				.range("1.19.3", null, () -> {
					Tooltip newTooltip = (tooltip == null ? null : tooltip.toNewTooltip());
					return Button.builder(message, onPress).bounds(x, finalY, width, finalHeight).tooltip(newTooltip).build();
				})
				.range(null, "1.19.2", () -> {
					try {
						Object oldTooltip = (tooltip == null ? MVTooltip.EMPTY : tooltip).toOldTooltip();
						return Button.class.getConstructor(int.class, int.class, int.class, int.class, Component.class,
										Button.OnPress.class, Reflection.getClass("net.minecraft.class_4185$class_5316"))
								.newInstance(x, finalY, width, finalHeight, message, onPress, oldTooltip);
					} catch (Exception e) {
						throw new RuntimeException("Error creating old button", e);
					}
				})
				.get();
	}
	public static Button newButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
		return newButton(x, y, width, height, message, onPress, null);
	}

	public static Button newTexturedButton(int x, int y, int width, int height, int hoveredVOffset, Identifier img, Button.OnPress onPress, MVTooltip tooltip) {
		Button output = Version.<Button>newSwitch()
				.range("1.20.2", null, () -> new MVTexturedButtonWidget_1_20_2(
						x, y, width, height, 0, 0, hoveredVOffset, img, width, height + hoveredVOffset, onPress))
				.range(null, "1.20.1", () -> Reflection.newInstance(ImageButton.class,
						new Class<?>[] {int.class, int.class, int.class, int.class, int.class, int.class, int.class, Identifier.class, int.class, int.class, Button.OnPress.class},
						x, y, width, height, 0, 0, hoveredVOffset, img, width, height + hoveredVOffset, onPress))
				.get();
		if (tooltip != null) {
			Version.newSwitch()
					.range("1.19.3", null, () -> output.setTooltip(tooltip.toNewTooltip()))
					.range(null, "1.19.2", () -> {
						Object oldTooltip = tooltip.toOldTooltip();
						Reflection.getField(Button.class, "field_25036", "Lnet/minecraft/class_4185$class_5316;").set(output, oldTooltip);
					})
					.run();
		}
		return output;
	}
	public static Button newTexturedButton(int x, int y, int width, int height, int hoveredVOffset, Identifier img, Button.OnPress onPress) {
		return newTexturedButton(x, y, width, height, hoveredVOffset, img, onPress, null);
	}

	private static final Supplier<Reflection.MethodInvoker> CreativeModeInventoryScreen_getSelectedTab =
			Reflection.getOptionalMethod(CreativeModeInventoryScreen.class, "method_2469", MethodType.methodType(int.class));
	private static final Supplier<Reflection.FieldReference> ItemGroup_INVENTORY =
			Reflection.getOptionalField(CreativeModeTab.class, "field_7918", "Lnet/minecraft/class_1761;");
	private static final Supplier<Reflection.MethodInvoker> ItemGroup_getIndex =
			Reflection.getOptionalMethod(CreativeModeTab.class, "method_7741", MethodType.methodType(int.class));
	public static boolean isCreativeInventoryTabSelected() {
		if (MainUtil.client.screen instanceof CreativeModeInventoryScreen screen) {
			return Version.<Boolean>newSwitch()
					.range("1.19.3", null, () -> screen.isInventoryOpen())
					.range(null, "1.19.2", () -> // screen.getSelectedTab() == ItemGroup.INVENTORY.getIndex()
							(int) CreativeModeInventoryScreen_getSelectedTab.get().invoke(screen) ==
									(int) ItemGroup_getIndex.get().invoke(ItemGroup_INVENTORY.get().get(null)))
					.get();
		}
		return false;
	}

	private static final Supplier<Reflection.MethodInvoker> Keyboard_setRepeatEvents =
			Reflection.getOptionalMethod(KeyboardHandler.class, "method_1462", MethodType.methodType(void.class, boolean.class));
	public static void setKeyboardRepeatEvents(boolean repeatEvents) {
		Version.newSwitch()
				.range("1.19.3", null, () -> {}) // Repeat events are now always on
				.range(null, "1.19.2", () -> Keyboard_setRepeatEvents.get().invoke(MainUtil.client.keyboardHandler, repeatEvents))
				.run();
	}

	public static boolean isValidChar(char c) {
		return c != '§' && c >= ' ' && c != 127;
	}
	public static String stripInvalidChars(String str, boolean allowLinebreaks) {
		StringBuilder output = new StringBuilder();
		for(int i = 0; i <  str.length(); i++) {
			char c = str.charAt(i);
			if(c == '§') {
				i++;
				continue;
			}
			if (isValidChar(c)) {
				output.append(c);
			} else if (allowLinebreaks && c == '\n') {
				output.append(c);
			}
		}
		return output.toString();
	}

	private static final Supplier<Reflection.MethodInvoker> Text_asString =
			Reflection.getOptionalMethod(Component.class, "method_10851", MethodType.methodType(String.class));
	public static String getContent(Component text) {
		return Version.<String>newSwitch()
				.range("1.19.0", null, () -> {
					StringBuilder output = new StringBuilder();
					text.getContents().visit(str -> {
						output.append(str);
						return Optional.empty();
					});
					return output.toString();
				})
				.range(null, "1.18.2", () -> Text_asString.get().invoke(text))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> TooltipPositioner_getPosition =
			Reflection.getOptionalMethod(() -> ClientTooltipPositioner.class, () -> "method_47944", () ->
					MethodType.methodType(Vector2ic.class, Screen.class, int.class, int.class, int.class, int.class));
	public static Vector2ic getPosition(Object positioner, Screen screen, int x, int y, int width, int height) {
		return Version.<Vector2ic>newSwitch()
				.range("1.20.0", null, () -> ((ClientTooltipPositioner) positioner).positionTooltip(
						MainUtil.client.getWindow().getGuiScaledWidth(), MainUtil.client.getWindow().getGuiScaledHeight(), x, y, width, height))
				.range("1.19.3", "1.19.4", () -> TooltipPositioner_getPosition.get().invoke(positioner, screen, x, y, width, height))
				.get();
	}


	public static void addEffectToStew(ItemStack item, MobEffect effect, int duration) {
		item.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, new SuspiciousStewEffects(List.of()).withEffectAdded(new Entry(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), duration)));
	}

	private static final Supplier<Reflection.MethodInvoker> ClientPlayNetworkHandler_sendPacket =
			Reflection.getOptionalMethod(ClientPacketListener.class, "method_2883", MethodType.methodType(void.class, Packet.class));
	public static void sendC2SPacket(Packet<?> packet) {
		Version.newSwitch()
				.range("1.20.2", null, () -> MainUtil.client.getConnection().send(packet))
				.range(null, "1.20.1", () -> ClientPlayNetworkHandler_sendPacket.get().invoke(MainUtil.client.getConnection(), packet))
				.run();
	}

	private static final Supplier<Reflection.MethodInvoker> NbtIo_read =
			Reflection.getOptionalMethod(NbtIo.class, "method_10627", MethodType.methodType(CompoundTag.class, DataInput.class));
	private static final Supplier<Reflection.MethodInvoker> NbtIo_readCompressed =
			Reflection.getOptionalMethod(NbtIo.class, "method_10629", MethodType.methodType(CompoundTag.class, InputStream.class));
	private static final Supplier<Reflection.MethodInvoker> NbtIo_write =
			Reflection.getOptionalMethod(NbtIo.class, "method_10628", MethodType.methodType(void.class, CompoundTag.class, DataOutput.class));
	private static final Supplier<Reflection.MethodInvoker> NbtIo_writeCompressed =
			Reflection.getOptionalMethod(NbtIo.class, "method_10634", MethodType.methodType(void.class, CompoundTag.class, OutputStream.class));
	public static CompoundTag nbtInternal(Supplier<CompoundTag> newWrite, Supplier<CompoundTag> oldWrite) throws IOException {
		try {
			return Version.<CompoundTag>newSwitch()
					.range("1.20.3", null, newWrite)
					.range(null, "1.20.2", () -> {
						try {
							return oldWrite.get();
						} catch (RuntimeException e) {
							if (e.getCause() instanceof InvocationTargetException invocationException) {
								if (invocationException.getCause() instanceof IOException ioException)
									throw new UncheckedIOException(ioException);
							}
							throw e;
						}
					})
					.get();
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}
	public static void nbtInternal(Runnable newWrite, Runnable oldWrite) throws IOException {
		nbtInternal(() -> {
			newWrite.run();
			return null;
		}, () -> {
			oldWrite.run();
			return null;
		});
	}
	public static CompoundTag readNbt(InputStream stream) throws IOException {
		return nbtInternal(() -> {
			try {
				return NbtIo.read(new DataInputStream(stream), NbtAccounter.unlimitedHeap());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, () -> NbtIo_read.get().invoke(null, new DataInputStream(stream)));
	}
	public static CompoundTag readCompressedNbt(InputStream stream) throws IOException {
		return nbtInternal(() -> {
			try {
				return NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, () -> NbtIo_readCompressed.get().invoke(null, stream));
	}
	public static void writeNbt(CompoundTag nbt, OutputStream stream) throws IOException {
		nbtInternal(() -> {
			try {
				NbtIo.writeUnnamedTagWithFallback(nbt, new DataOutputStream(stream));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, () -> NbtIo_write.get().invoke(null, nbt, new DataOutputStream(stream)));
	}
	public static void writeCompressedNbt(CompoundTag nbt, OutputStream stream) throws IOException {
		nbtInternal(() -> {
			try {
				NbtIo.writeCompressed(nbt, stream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, () -> NbtIo_writeCompressed.get().invoke(null, nbt, stream));
	}
	public static CompoundTag readNbt(File file) throws IOException {
		try (FileInputStream stream = new FileInputStream(file)) {
			return readNbt(stream);
		}
	}
	public static CompoundTag readCompressedNbt(File file) throws IOException {
		try (FileInputStream stream = new FileInputStream(file)) {
			return readCompressedNbt(stream);
		}
	}
	public static void writeNbt(CompoundTag nbt, File file) throws IOException {
		try (FileOutputStream stream = new FileOutputStream(file)) {
			writeNbt(nbt, stream);
		}
	}
	public static void writeCompressedNbt(CompoundTag nbt, File file) throws IOException {
		try (FileOutputStream stream = new FileOutputStream(file)) {
			writeCompressedNbt(nbt, stream);
		}
	}

	private static final Supplier<Class<?>> VertexFormat = Reflection.getOptionalClass("net.minecraft.class_293");
	private static final Supplier<Class<?>> VertexFormat$DrawMode = Reflection.getOptionalClass("net.minecraft.class_293$class_5596");
	private static final Supplier<Reflection.MethodInvoker> Tessellator_getBuffer =
			Reflection.getOptionalMethod(Tesselator.class, "method_1349", MethodType.methodType(BufferBuilder.class));
	private static final Supplier<Reflection.MethodInvoker> BufferBuilder_begin =
			Reflection.getOptionalMethod(() -> BufferBuilder.class, () -> "method_1328", () -> MethodType.methodType(void.class, VertexFormat$DrawMode.get(), VertexFormat.get()));
	private static final Supplier<Reflection.MethodInvoker> RenderSystem_setShader =
			Reflection.getOptionalMethod(RenderSystem.class, "setShader", MethodType.methodType(void.class, Supplier.class));
	public static VertexConsumer beginDrawingShader(Matrix3x2fStack matrices, MVShader shader) {
		return Version.<VertexConsumer>newSwitch()
				.range("1.20.0", null, () -> {
					var a = MVDrawableHelper.getVertexConsumerProvider();
					return a.getBuffer(shader.getLayer());
				})
				.get();
	}
	private static final Supplier<Class<?>> BufferBuilder$BuiltBuffer = Reflection.getOptionalClass("net.minecraft.class_287$class_7433");
	private static final Supplier<Class<?>> BufferRenderer = Reflection.getOptionalClass("net.minecraft.class_286");
	private static final Supplier<Reflection.MethodInvoker> BufferBuilder_end_void =
			Reflection.getOptionalMethod(BufferBuilder.class, "method_1326", MethodType.methodType(void.class));
	private static final Supplier<Reflection.MethodInvoker> BufferRenderer_draw =
			Reflection.getOptionalMethod(BufferRenderer, () -> "method_1309", () -> MethodType.methodType(void.class, BufferBuilder.class));
	private static final Supplier<Reflection.MethodInvoker> BufferBuilder_end_BuiltBuffer =
			Reflection.getOptionalMethod(() -> BufferBuilder.class, () -> "method_1326", () -> MethodType.methodType(BufferBuilder$BuiltBuffer.get()));
	private static final Supplier<Reflection.MethodInvoker> BufferRenderer_drawWithGlobalProgram =
			Reflection.getOptionalMethod(BufferRenderer, () -> "method_43433", () -> MethodType.methodType(void.class, BufferBuilder$BuiltBuffer.get()));
	public static void endDrawingShader(Matrix3x2fStack matrices, VertexConsumer vertexConsumer) {
		Version.newSwitch()
				.range("1.20.0", null, () -> {
					var a = MVDrawableHelper.getVertexConsumerProvider();
					a.endBatch();
				})
				.run();
	}

	private static final Supplier<Reflection.MethodInvoker> TextFieldWidget_setCursor =
			Reflection.getOptionalMethod(EditBox.class, "method_1883", MethodType.methodType(void.class, int.class));
	public static void setCursor(EditBox textField, int cursor) {
		Version.newSwitch()
				.range("1.20.2", null, () -> textField.moveCursorTo(cursor, false))
				.range(null, "1.20.1", () -> TextFieldWidget_setCursor.get().invoke(textField, cursor))
				.run();
	}

	public static EntityType<?> getEntityType(ItemStack item) {
		SpawnEggItem spawnEggItem = (SpawnEggItem) item.getItem();
		return Version.<EntityType<?>>newSwitch()
				.range("1.21.4", null, () -> spawnEggItem.getType(item))
				.get();
	}

	public static MobEffectInstance newMobEffectInstance(MobEffect effect, int duration) {
		return Version.<MobEffectInstance>newSwitch()
				.range("1.20.5", null, () -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), duration))
				.range(null, "1.20.4", () -> Reflection.newInstance(MobEffectInstance.class, new Class<?>[] {MobEffect.class, int.class}, effect, duration))
				.get();
	}
	public static MobEffectInstance newMobEffectInstance(MobEffect effect, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
		return Version.<MobEffectInstance>newSwitch()
				.range("1.20.5", null, () -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), duration, amplifier, ambient, showParticles, showIcon))
				.range(null, "1.20.4", () -> Reflection.newInstance(MobEffectInstance.class, new Class<?>[] {MobEffect.class, int.class, int.class, boolean.class, boolean.class, boolean.class}, effect, duration, amplifier, ambient, showParticles, showIcon))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> MobEffectInstance_getEffectType =
			Reflection.getOptionalMethod(MobEffectInstance.class, "method_5579", MethodType.methodType(MobEffect.class));
	public static MobEffect getEffectType(MobEffectInstance effect) {
		return Version.<MobEffect>newSwitch()
				.range("1.20.5", null, () -> effect.getEffect().value())
				.range(null, "1.20.4", () -> MobEffectInstance_getEffectType.get().invoke(effect))
				.get();
	}

	public static BookViewScreen.BookAccess getBookContents(List<Component> pages) {
		if (NBTManagers.COMPONENTS_EXIST)
			return new BookViewScreen.BookAccess(pages);

		return (BookViewScreen.BookAccess) Proxy.newProxyInstance(MVMisc.class.getClassLoader(),
				new Class<?>[] {BookViewScreen.BookAccess.class}, (obj, method, args) -> {
					if (method.getName().equals("method_17560")) // getPageCount
						return pages.size();
					if (method.getName().equals("method_17561")) // getPageUnchecked
						return (FormattedText) pages.get((int) args[0]);

					if (method.getName().equals("method_17563")) { // default getPage
						int index = (int) args[0];
						return (index >= 0 && index < pages.size() ? pages.get(index) : FormattedText.EMPTY);
					}

					throw new IllegalArgumentException("Unknown method: " + method);
				});
	}

	public static boolean isWrittenBookContents(BookViewScreen.BookAccess contents) {
		return Version.<Boolean>newSwitch()
				.range("1.20.5", null, () -> MixinLink.WRITTEN_BOOK_CONTENTS.getIfPresent(contents) != null)
				.range(null, "1.20.4", () -> Reflection.getClass("net.minecraft.class_3872$class_3933").isInstance(contents))
				.get();
	}

	private static final Supplier<Class<?>> SystemToast$Type = Reflection.getOptionalClass("net.minecraft.class_370$class_371");
	private static final Object SystemToast$Type_PACK_LOAD_FAILURE =
			Version.<Object>newSwitch()
					.range("1.20.3", null, () -> null)
					.range(null, "1.20.2", () -> Reflection.getField(SystemToast$Type.get(), "field_21809", "Lnet/minecraft/class_370$class_371;").get(null))
					.get();
	public static void showToast(Component title, Component description) {
		MainUtil.client.getToastManager().addToast(Version.<SystemToast>newSwitch()
				.range("1.20.3", null, () -> new SystemToast(SystemToast.SystemToastId.PACK_LOAD_FAILURE, title, description))
				.range(null, "1.20.2", () -> Reflection.newInstance(SystemToast.class,
						new Class<?>[] {SystemToast$Type.get(), Component.class, Component.class},
						SystemToast$Type_PACK_LOAD_FAILURE, title, description))
				.get());
	}

	private static final Supplier<Reflection.MethodInvoker> ParentElement_setInitialFocus =
			Reflection.getOptionalMethod(ContainerEventHandler.class, "method_20085", MethodType.methodType(void.class, GuiEventListener.class));
	public static void setInitialFocus(Screen screen, GuiEventListener element, Consumer<GuiEventListener> superCall) {
		Version.newSwitch()
				.range("1.19.4", null, () -> {
					superCall.accept(element);
					screen.setFocused(element);
				})
				.range(null, "1.19.3", () -> ParentElement_setInitialFocus.get().invoke(screen, element))
				.run();
	}

	private static final Supplier<Reflection.MethodInvoker> VertexConsumer_next =
			Reflection.getOptionalMethod(VertexConsumer.class, "method_1344", MethodType.methodType(void.class));
	public static void nextVertex(VertexConsumer vertexConsumer) {
		Version.newSwitch()
				.range("1.21.0", null, () -> {})
				.range(null, "1.20.6", () -> VertexConsumer_next.get().invoke(vertexConsumer))
				.run();
	}

	private static final Supplier<Reflection.MethodInvoker> VertexConsumer_vertex =
			Reflection.getOptionalMethod(VertexConsumer.class, "method_22912", MethodType.methodType(VertexConsumer.class, double.class, double.class, double.class));
	public static VertexConsumer startVertex(VertexConsumer vertexConsumer, double x, double y, double z) {
		return Version.<VertexConsumer>newSwitch()
				.range("1.21.0", null, () -> vertexConsumer.addVertex((float) x, (float) y, (float) z))
				.range(null, "1.20.6", () -> VertexConsumer_vertex.get().invoke(vertexConsumer, x, y, z))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> Minecraft_getTickDelta =
			Reflection.getOptionalMethod(Minecraft.class, "method_1488", MethodType.methodType(float.class));
	public static float getTickDelta() {
		return Version.<Float>newSwitch()
				.range("1.21.0", null, () -> MainUtil.client.getDeltaTracker().getGameTimeDeltaPartialTick(true))
				.range(null, "1.20.6", () -> Minecraft_getTickDelta.get().invoke(MainUtil.client))
				.get();
	}

	public static EquipmentSlot getEquipmentSlot(EquipmentSlot.Type type, int entityId) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() == type && slot.getIndex() == entityId)
				return slot;
		}
		throw new IllegalArgumentException("Unknown equipment slot: type=" + type + ", entityId=" + entityId);
	}

	public static void onRegistriesLoad(Runnable callback) {
		Version.newSwitch()
				.range("1.20.5", null, () -> DynamicRegistryManagerHolder.onDefaultManagerLoad(callback))
				.range(null, "1.20.4", callback)
				.run();
	}

	private static final Supplier<Reflection.MethodInvoker> VertexConsumer_light =
			Reflection.getOptionalMethod(VertexConsumer.class, "method_22916", MethodType.methodType(VertexConsumer.class, int.class));
	public static void setVertexLight(VertexConsumer vertexConsumer, int uv) {
		Version.newSwitch()
				.range("1.21.0", null, () -> vertexConsumer.setLight(uv))
				.range(null, "1.20.6", () -> VertexConsumer_light.get().invoke(vertexConsumer, uv))
				.run();
	}

	public static <T> T withDefaultRegistryManager(Supplier<T> callback) {
		if (NBTManagers.COMPONENTS_EXIST)
			return DynamicRegistryManagerHolder.withDefaultManager(callback);
		return callback.get();
	}
	public static void withDefaultRegistryManager(Runnable callback) {
		if (NBTManagers.COMPONENTS_EXIST)
			DynamicRegistryManagerHolder.withDefaultManager(callback);
		else
			callback.run();
	}

	private static final Supplier<Reflection.MethodInvoker> TooltipComponent_getHeight =
			Reflection.getOptionalMethod(ClientTooltipComponent.class, "method_32661", MethodType.methodType(int.class));
	public static int getTooltipComponentHeight(ClientTooltipComponent line) {
		return Version.<Integer>newSwitch()
				.range("1.21.2", null, () -> line.getHeight(MainUtil.client.font))
				.range(null, "1.21.1", () -> TooltipComponent_getHeight.get().invoke(line))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> Entity_getCommandSource =
			Reflection.getOptionalMethod(Entity.class, "method_5671", MethodType.methodType(CommandSourceStack.class));
	public static CommandSourceStack getCommandSource(Entity entity) {
		return Version.<CommandSourceStack>newSwitch()
				.range("1.21.2", null, () -> new CommandSourceStack(
						CommandSource.NULL, entity.position(), entity.getRotationVector(), null, PermissionSet.ALL_PERMISSIONS,
						entity.getName().getString(), entity.getDisplayName(), null, entity))
				.get();
	}

	public static ProfilerFiller getProfiler() {
		return Profiler.get();
	}

	public static PotionContents newPotionContents(Optional<Holder<Potion>> potion, Optional<Integer> customColor, List<MobEffectInstance> customEffects) {
		return Version.<PotionContents>newSwitch()
				.range("1.21.2", null, () -> new PotionContents(potion, customColor, customEffects, Optional.empty()))
				.range(null, "1.21.1", () -> Reflection.newInstance(PotionContents.class, new Class<?>[] {Optional.class, Optional.class, List.class}, potion, customColor, customEffects))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> EntityRenderDispatcher_render =
			Reflection.getOptionalMethod(EntityRenderDispatcher.class, "method_3954", MethodType.methodType(void.class, Entity.class, double.class, double.class, double.class, float.class, float.class, PoseStack.class, MultiBufferSource.class, int.class));
	public static void renderEntity(EntityRenderDispatcher dispatcher, Entity entity, double x, double y, double z, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		Version.newSwitch()
				.range("1.21.2", null, () -> {
					EntityRenderState s = dispatcher.extractEntity(entity,tickDelta);
					CameraRenderState c = Minecraft.getInstance().levelRenderer.levelRenderState.cameraRenderState;
					dispatcher.submit(s,c, x, y, z, matrices, Minecraft.getInstance().levelRenderer.submitNodeStorage);
				})
				.range(null, "1.21.1", () -> EntityRenderDispatcher_render.get().invoke(dispatcher, entity, x, y, z, yaw, tickDelta, matrices, vertexConsumers, light))
				.run();
	}

	// From Minecraft#addBlockEntityNbt (1.21.3)
	// Edited to remove x, y, & z
	@SuppressWarnings("deprecation")
	public static void addBlockEntityNbtWithoutXYZ(ItemStack item, BlockEntity entity) {
		CompoundTag blockEntityTag = entity.saveCustomOnly((MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess()));
		blockEntityTag.remove("x");
		blockEntityTag.remove("y");
		blockEntityTag.remove("z");
		TagValueOutput v = new TagValueOutput(ProblemReporter.DISCARDING,NbtOps.INSTANCE,blockEntityTag);
		entity.removeComponentsFromTag(v);
		BlockItem.setBlockEntityData(item, entity.getType(), v);
		item.applyComponents(entity.collectComponents());
	}

	private static final Supplier<Reflection.MethodInvoker> BlockEntityRenderer_render =
			Reflection.getOptionalMethod(BlockEntityRenderer.class, "method_3569", MethodType.methodType(void.class, BlockEntity.class, float.class, PoseStack.class, MultiBufferSource.class, int.class, int.class));
	// From BlockEntityRenderDispatcher#renderEntity (1.21.3)
	// Edited to input a tickDelta and use default light and overlay values
	public static <T extends BlockEntity> boolean renderBlockEntity(BlockEntityRenderDispatcher dispatcher, T entity, float tickDelta, PoseStack matrices, MultiBufferSource provider) {
		BlockEntityRenderer<T, BlockEntityRenderState> renderer = dispatcher.getRenderer(entity);
		if (renderer == null)
			return true;
		try {
			Version.newSwitch()
					.range("1.21.5", null, () -> renderer.submit(dispatcher.tryExtractRenderState(entity,tickDelta,null),matrices,Minecraft.getInstance().levelRenderer.submitNodeStorage,Minecraft.getInstance().levelRenderer.levelRenderState.cameraRenderState))
					.range(null, "1.21.4", () -> BlockEntityRenderer_render.get().invoke(renderer, entity, tickDelta, matrices, provider, 0xF000F0, OverlayTexture.NO_OVERLAY))
					.run();
		} catch (Throwable e) {
			CrashReport report = CrashReport.forThrowable(e, "Rendering Block Entity");
			CrashReportCategory entitySection = report.addCategory("Block Entity Details");
			entity.fillCrashReportCategory(entitySection);
			throw new ReportedException(report);
		}
		return false;
	}

	public static int scaleRgb(int argb, double scale) {
		Color color = new Color(argb, true);
		int r = (int) (color.getRed() * scale);
		int g = (int) (color.getGreen() * scale);
		int b = (int) (color.getBlue() * scale);
		return new Color(r, g, b, color.getAlpha()).getRGB();
	}

	public static CreativeModeInventoryScreen newCreativeModeInventoryScreen(LocalPlayer player) {
		return Version.<CreativeModeInventoryScreen>newSwitch()
				.range("1.21.0", null, () -> new CreativeModeInventoryScreen(
						player, player.connection.enabledFeatures(), MainUtil.client.options.operatorItemsTab().get()))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> Item_getName =
			Reflection.getOptionalMethod(Item.class, "method_7848", MethodType.methodType(Component.class));
	public static Component getName(ItemStack item) {
		return Version.<Component>newSwitch()
				.range("1.21.2", null, () -> item.getItemName())
				.get();
	}

	public static boolean isSignItem(Item item) {
		if (item instanceof SignItem)
			return true;
		return Version.<Boolean>newSwitch()
				.range("1.20.0", null, () -> false)
				.range("1.19.3", "1.19.4", () -> item instanceof HangingSignItem)
				.range(null, "1.19.2", () -> false)
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> DataResult_result =
			Reflection.getOptionalMethod(DataResult.class, "result", MethodType.methodType(Optional.class));
	public static <T> Optional<T> result(DataResult<T> result) {
		return Version.<Optional<T>>newSwitch()
				.range("1.20.5", null, () -> result.result())
				.range(null, "1.20.4", () -> DataResult_result.get().invoke(result))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> Tag_asString =
			Reflection.getOptionalMethod(Tag.class, "method_10714", MethodType.methodType(String.class));
	public static String value(StringTag str) {
		return Version.<String>newSwitch()
				.range("1.21.5", null, () -> str.value())
				.range(null, "1.21.4", () -> Tag_asString.get().invoke(str))
				.get();
	}

	public static Object newTooltipDisplay(boolean hideTooltip, LinkedHashSet<DataComponentType<?>> hiddenComponents) {
		return Reflection.newInstance(TooltipDisplay.class, new Class<?>[] {boolean.class, SequencedSet.get()}, hideTooltip, hiddenComponents);
	}

	private static final Supplier<Reflection.MethodInvoker> TooltipDisplay_hiddenComponents =
			Reflection.getOptionalMethod(() -> TooltipDisplay.class, () -> "hiddenComponents", () -> MethodType.methodType(SequencedSet.get()));
	public static Set<DataComponentType<?>> hiddenComponents(Object TooltipDisplay) {
		return TooltipDisplay_hiddenComponents.get().invoke(TooltipDisplay);
	}

	private static final Supplier<Reflection.FieldReference> Inventory_armor =
			Reflection.getOptionalField(Inventory.class, "field_7548", "Lnet/minecraft/class_2371;");
	@SuppressWarnings("unchecked")
	public static void setArmor(EquipmentSlot slot, ItemStack item) {
		Version.newSwitch()
				.range("1.21.5", null, () -> MainUtil.client.player.setItemSlot(slot, item))
				.range(null, "1.21.4", () -> ((NonNullList<ItemStack>) Inventory_armor.get()
						.get(MainUtil.client.player.getInventory())).set(slot.getIndex(), item))
				.run();
	}

	private static final Supplier<Reflection.MethodInvoker> TagParser_parseElement =
			Reflection.getOptionalMethod(TagParser.class, "method_10723", MethodType.methodType(Tag.class));
	public static Tag parseNbt(StringReader snbt) throws CommandSyntaxException {
		if (Version.<Boolean>newSwitch()
				.range("1.21.5", null, true)
				.range(null, "1.21.4", false)
				.get()) {
			return TagParser.create(NbtOps.INSTANCE).parseFully(snbt);
		}

		return TagParser_parseElement.get().invokeThrowable(CommandSyntaxException.class,
				Reflection.newInstance(TagParser.class, new Class<?>[] {StringReader.class}, snbt));
	}
	public static Tag parseNbt(String snbt) throws CommandSyntaxException {
		return parseNbt(new StringReader(snbt));
	}

	private static final Supplier<Reflection.FieldReference> StringTagVisitor_SIMPLE_NAME =
			Reflection.getOptionalField(StringTagVisitor.class, "field_27829", "Ljava/util/regex/Pattern;");
	public static boolean isSimpleName(String name) {
		return Version.<Boolean>newSwitch()
				.range("1.21.5", null, () -> !name.equalsIgnoreCase("true") && !name.equalsIgnoreCase("false") &&
						StringTagVisitor.UNQUOTED_KEY_MATCH.matcher(name).matches())
				.range(null, "1.21.4", () -> ((Pattern) StringTagVisitor_SIMPLE_NAME.get().get(null)).matcher(name).matches())
				.get();
	}

	private static final Supplier<Reflection.FieldReference> ItemEnchantments_showInTooltip =
			Reflection.getOptionalField(ItemEnchantments.class, "field_49390", "Z");
	public static Object withEnchantments(Object component, Object2IntOpenHashMap<Holder<Enchantment>> enchantments) {
		return Version.<Object>newSwitch()
				.range("1.21.5", null, () -> new ItemEnchantments(enchantments))
				.range(null, "1.21.4", () -> Reflection.newInstance(ItemEnchantments.class,
						new Class<?>[] {Object2IntOpenHashMap.class, boolean.class},
						enchantments, component == null ? true : ItemEnchantments_showInTooltip.get().get(component)))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> ItemAttributeModifiers_showInTooltip =
			Reflection.getOptionalMethod(ItemAttributeModifiers.class, "comp_2394", MethodType.methodType(boolean.class));
	public static Object withAttributes(Object component, List<ItemAttributeModifiers.Entry> list) {
		return Version.<Object>newSwitch()
				.range("1.21.5", null, () -> new ItemAttributeModifiers(list))
				.range(null, "1.21.4", () -> Reflection.newInstance(ItemAttributeModifiers.class,
						new Class<?>[] {List.class, boolean.class},
						list, component == null ? true : ItemAttributeModifiers_showInTooltip.get().invoke(component)))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> MultiPlayerGameMode_hasCreativeInventory =
			Reflection.getOptionalMethod(MultiPlayerGameMode.class, "method_2914", MethodType.methodType(boolean.class));
	public static boolean hasCreativeInventory() {
		return Version.<Boolean>newSwitch()
				.range("1.21.5", null, () -> MainUtil.client.player.hasInfiniteMaterials())
				.range(null, "1.21.4", () -> MultiPlayerGameMode_hasCreativeInventory.get().invoke(MainUtil.client.gameMode))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> AbstractContainerMenu_setPreviousCursorStack =
			Reflection.getOptionalMethod(AbstractContainerMenu.class, "method_34250", MethodType.methodType(void.class, ItemStack.class));
	public static void setPreviousCursorStack(AbstractContainerMenu handler, ItemStack item) {
		Version.newSwitch()
				.range("1.21.5", null, () -> handler.remoteCarried.force(item))
				.range(null, "1.21.4", () -> AbstractContainerMenu_setPreviousCursorStack.get().invoke(handler, item))
				.run();
	}

	private static final Supplier<Reflection.MethodInvoker> ServerboundContainerClickPacket_getActionType =
			Reflection.getOptionalMethod(ServerboundContainerClickPacket.class, "method_12195", MethodType.methodType(ContainerInput.class));
	public static ContainerInput getActionType(ServerboundContainerClickPacket packet) {
		return Version.<ContainerInput>newSwitch()
				.range("1.21.5", null, () -> packet.containerInput())
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> ServerboundContainerClickPacket_getButton =
			Reflection.getOptionalMethod(ServerboundContainerClickPacket.class, "method_12193", MethodType.methodType(int.class));
	public static int getButton(ServerboundContainerClickPacket packet) {
		return Version.<Integer>newSwitch()
				.range("1.21.5", null, () -> (int) packet.buttonNum())
				.range(null, "1.21.4", () -> ServerboundContainerClickPacket_getButton.get().invoke(packet))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> ServerboundContainerClickPacket_getSlot =
			Reflection.getOptionalMethod(ServerboundContainerClickPacket.class, "method_12192", MethodType.methodType(int.class));
	public static int getSlot(ServerboundContainerClickPacket packet) {
		return Version.<Integer>newSwitch()
				.range("1.21.5", null, () -> (int) packet.slotNum())
				.range(null, "1.21.4", () -> ServerboundContainerClickPacket_getSlot.get().invoke(packet))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> ClientboundContainerSetContentPacket_getContents =
			Reflection.getOptionalMethod(ClientboundContainerSetContentPacket.class, "method_11441", MethodType.methodType(List.class));
	public static List<ItemStack> getContents(ClientboundContainerSetContentPacket packet) {
		return Version.<List<ItemStack>>newSwitch()
				.range("1.21.5", null, () -> packet.items())
				.range(null, "1.21.4", () -> ClientboundContainerSetContentPacket_getContents.get().invoke(packet))
				.get();
	}

	private static final Supplier<Reflection.MethodInvoker> ClientboundContainerSetContentPacket_getSyncId =
			Reflection.getOptionalMethod(ClientboundContainerSetContentPacket.class, "method_11440", MethodType.methodType(int.class));
	public static int getSyncId(ClientboundContainerSetContentPacket packet) {
		return Version.<Integer>newSwitch()
				.range("1.21.5", null, () -> packet.stateId())
				.range(null, "1.21.4", () -> ClientboundContainerSetContentPacket_getSyncId.get().invoke(packet))
				.get();
	}

	private static final Supplier<Class<?>> BoatEntity$Type = Reflection.getOptionalClass("net.minecraft.class_1690$class_1692");
	private static final Supplier<Reflection.MethodInvoker> BoatEntity$Type_getType =
			Reflection.getOptionalMethod(BoatEntity$Type, () -> "method_7561", () -> MethodType.methodType(BoatEntity$Type.get(), String.class));
	private static final Supplier<Reflection.FieldReference> BoatItem_type =
			Reflection.getOptionalField(BoatItem.class, "field_7902", "Lnet/minecraft/class_1690$class_1692;");
	public static Item getBoatItem(EntityType<?> entityType, CompoundTag nbt) {
		return Version.<Item>newSwitch()
				.range("1.21.2", null, () -> {
					for (Item item : MVRegistry.ITEM) {
						if (item instanceof BoatItem boat && entityType == boat.entityType)
							return item;
					}
					throw new IllegalStateException("Unknown boat entity type: " + EntityType.getKey(entityType));
				})
				.get();
	}

}
