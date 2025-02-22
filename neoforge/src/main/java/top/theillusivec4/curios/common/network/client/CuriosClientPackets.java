/*
 * Copyright (c) 2018-2024 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package top.theillusivec4.curios.common.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;
import top.theillusivec4.curios.api.type.ICuriosMenu;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.common.data.CuriosEntityManager;
import top.theillusivec4.curios.common.data.CuriosSlotManager;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;
import top.theillusivec4.curios.common.network.server.*;
import top.theillusivec4.curios.common.network.server.sync.*;
import top.theillusivec4.curios.server.command.CurioArgumentType;

import java.util.*;

public class CuriosClientPackets {

  public static void handle(final SPacketSetIcons data) {
    ClientLevel world = Minecraft.getInstance().level;
    Set<String> slotIds = new HashSet<>();

    if (world != null) {
      CuriosApi.getIconHelper().clearIcons();
      Map<String, ResourceLocation> icons = new HashMap<>();

      for (Map.Entry<String, ResourceLocation> entry : data.map.entrySet()) {
        CuriosApi.getIconHelper().addIcon(entry.getKey(), entry.getValue());
        icons.put(entry.getKey(), entry.getValue());
        slotIds.add(entry.getKey());
      }
      CuriosSlotManager.CLIENT.setIcons(icons);
    }
    CurioArgumentType.slotIds = slotIds;
  }

  public static void handle(final SPacketQuickMove data) {
    Minecraft mc = Minecraft.getInstance();
    LocalPlayer clientPlayer = mc.player;

    if (clientPlayer != null &&
        clientPlayer.containerMenu instanceof CuriosContainer container) {
      container.quickMoveStack(clientPlayer, data.moveIndex());
    }
  }

  public static void handle(final SPacketPage data) {
    Minecraft mc = Minecraft.getInstance();
    LocalPlayer clientPlayer = mc.player;
    Screen screen = mc.screen;

    if (clientPlayer != null) {
      AbstractContainerMenu container = clientPlayer.containerMenu;

      if (container instanceof CuriosContainer && container.containerId == data.windowId()) {
        ((CuriosContainer) container).setPage(data.page());
      }
    }

    if (screen instanceof CuriosScreen) {
      ((CuriosScreen) screen).updateRenderButtons();
    }
  }

  public static void handle(final SPacketBreak data) {
    ClientLevel world = Minecraft.getInstance().level;

    if (world != null) {
      Entity entity = world.getEntity(data.entityId());

      if (entity instanceof LivingEntity livingEntity) {
        CuriosApi.getCuriosInventory(livingEntity)
            .flatMap(handler -> handler.getStacksHandler(data.curioId())).ifPresent(stacks -> {
              ItemStack stack = stacks.getStacks().getStackInSlot(data.slotId());
              Optional<ICurio> possibleCurio = CuriosApi.getCurio(stack);
              NonNullList<Boolean> renderStates = stacks.getRenders();
              possibleCurio.ifPresent(curio -> curio.curioBreak(
                  new SlotContext(data.curioId(), livingEntity, data.slotId(), false,
                      renderStates.size() > data.slotId() && renderStates.get(data.slotId()))));

              if (possibleCurio.isEmpty()) {
                ICurio.playBreakAnimation(stack, livingEntity);
              }
            });
      }
    }
  }

  public static void handle(final SPacketSyncRender data) {
    ClientLevel world = Minecraft.getInstance().level;

    if (world != null) {
      Entity entity = world.getEntity(data.entityId());

      if (entity instanceof LivingEntity) {
        CuriosApi.getCuriosInventory((LivingEntity) entity)
            .flatMap(handler -> handler.getStacksHandler(data.curioId()))
            .ifPresent(stacksHandler -> {
              int index = data.slotId();
              NonNullList<Boolean> renderStatuses = stacksHandler.getRenders();

              if (renderStatuses.size() > index) {
                renderStatuses.set(index, data.value());
              }
            });
      }
    }
  }

  public static void handle(final SPacketSyncModifiers data) {
    ClientLevel world = Minecraft.getInstance().level;

    if (world != null) {
      Entity entity = world.getEntity(data.entityId);

      if (entity instanceof LivingEntity livingEntity) {
        CuriosApi.getCuriosInventory(livingEntity)
            .ifPresent(handler -> {
              Map<String, ICurioStacksHandler> curios = handler.getCurios();

              for (Map.Entry<String, CompoundTag> entry : data.updates.entrySet()) {
                String id = entry.getKey();
                ICurioStacksHandler stacksHandler = curios.get(id);

                if (stacksHandler != null) {
                  stacksHandler.applySyncTag(entry.getValue());
                }
              }

              if (!data.updates.isEmpty()) {
                NeoForge.EVENT_BUS.post(
                    new SlotModifiersUpdatedEvent(livingEntity, data.updates.keySet()));
              }

              if (entity instanceof LocalPlayer localPlayer) {

                if (localPlayer.containerMenu instanceof ICuriosMenu curiosMenu) {
                  curiosMenu.resetSlots();
                }
              }
            });
      }
    }
  }

  public static void handle(final SPacketSyncData data) {
    CuriosSlotManager.applySyncPacket(data.slotData);
    CuriosEntityManager.applySyncPacket(data.entityData);
  }

  public static void handle(final SPacketSyncCurios data) {
    ClientLevel world = Minecraft.getInstance().level;

    if (world != null) {
      Entity entity = world.getEntity(data.entityId);

      if (entity instanceof LivingEntity) {
        CuriosApi.getCuriosInventory((LivingEntity) entity)
            .ifPresent(handler -> {
              Map<String, ICurioStacksHandler> stacks = new LinkedHashMap<>();

              for (Map.Entry<String, CompoundTag> entry : data.map.entrySet()) {
                ICurioStacksHandler stacksHandler =
                    new CurioStacksHandler(handler, entry.getKey());
                stacksHandler.applySyncTag(entry.getValue());
                stacks.put(entry.getKey(), stacksHandler);
              }
              handler.setCurios(stacks);

              if (entity instanceof LocalPlayer localPlayer &&
                  localPlayer.containerMenu instanceof ICuriosMenu curiosContainer) {
                curiosContainer.resetSlots();
              }
            });
      }
    }
  }

  public static void handle(final SPacketGrabbedItem data) {
    LocalPlayer clientPlayer = Minecraft.getInstance().player;

    if (clientPlayer != null) {
      clientPlayer.containerMenu.setCarried(data.stack().copy());
    }
  }

  public static void handle(final SPacketSyncStack data) {
    ClientLevel world = Minecraft.getInstance().level;

    if (world != null) {
      Entity entity = world.getEntity(data.entityId());

      if (entity instanceof LivingEntity livingEntity) {
        CuriosApi.getCuriosInventory(livingEntity)
            .flatMap(handler -> handler.getStacksHandler(data.curioId()))
            .ifPresent(stacksHandler -> {
              ItemStack stack = data.stack().copy();
              CompoundTag compoundNBT = data.compoundTag();
              int slot = data.slotId();
              boolean cosmetic = SPacketSyncStack.HandlerType.fromValue(data.handlerType()) ==
                  SPacketSyncStack.HandlerType.COSMETIC;

              if (!compoundNBT.isEmpty()) {
                NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                CuriosApi.getCurio(stack).ifPresent(curio -> curio.readSyncData(
                    new SlotContext(data.curioId(), livingEntity, slot, cosmetic,
                        renderStates.size() > slot && renderStates.get(slot)), compoundNBT));
              }

              if (cosmetic) {
                stacksHandler.getCosmeticStacks().setStackInSlot(slot, stack);
              } else {
                stacksHandler.getStacks().setStackInSlot(slot, stack);
              }
            });
      }
    }
  }
}
