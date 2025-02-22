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

package top.theillusivec4.curios.common.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

import javax.annotation.Nonnull;
import java.util.*;

public class CurioInventory implements INBTSerializable<CompoundTag>
{
    final Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
    ICuriosItemHandler curiosItemHandler;
    NonNullList<ItemStack> invalidStacks = NonNullList.create();
    Set<ICurioStacksHandler> updates = new HashSet<>();
    CompoundTag deserialized = new CompoundTag();
    boolean markDeserialized = false;

    public void init(final ICuriosItemHandler curiosItemHandler) {
        this.curiosItemHandler = curiosItemHandler;
        this.curios.clear();
        LivingEntity livingEntity = curiosItemHandler.getWearer();

        if (!this.markDeserialized) {
            SortedSet<ISlotType> sorted = new TreeSet<>(CuriosApi.getEntitySlots(livingEntity).values());

            for (ISlotType slotType : sorted) {
                this.curios.put(slotType.getIdentifier(),
                        new CurioStacksHandler(curiosItemHandler, slotType.getIdentifier(), slotType.getSize(),
                                slotType.useNativeGui(), slotType.hasCosmetic(), slotType.canToggleRendering(),
                                slotType.getDropRule()));
            }
        } else {
            this.markDeserialized = false;
            ListTag tagList = this.deserialized.getList("Curios", Tag.TAG_COMPOUND);
            Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
            SortedMap<ISlotType, ICurioStacksHandler> sortedCurios = new TreeMap<>();
            SortedSet<ISlotType> sorted = new TreeSet<>(CuriosApi.getEntitySlots(livingEntity).values());

            for (ISlotType slotType : sorted) {
                sortedCurios.put(slotType, new CurioStacksHandler(curiosItemHandler, slotType.getIdentifier(), slotType.getSize(), slotType.useNativeGui(), slotType.hasCosmetic(), slotType.canToggleRendering(), slotType.getDropRule()));
            }

            for (int i = 0; i < tagList.size(); i++) {
                CompoundTag tag = tagList.getCompound(i);
                String identifier = tag.getString("Identifier");
                CurioStacksHandler prevStacksHandler = new CurioStacksHandler(curiosItemHandler, identifier);
                prevStacksHandler.deserializeNBT(tag.getCompound("StacksHandler"));

                Optional<ISlotType> optionalType = Optional.ofNullable(CuriosApi.getEntitySlots(livingEntity).get(identifier));
                optionalType.ifPresent(slotType -> {
                    CurioStacksHandler newStacksHandler =
                            new CurioStacksHandler(curiosItemHandler, slotType.getIdentifier(),
                                    slotType.getSize(), slotType.useNativeGui(), slotType.hasCosmetic(),
                                    slotType.canToggleRendering(), slotType.getDropRule());
                    newStacksHandler.copyModifiers(prevStacksHandler);
                    int index = 0;

                    while (index < newStacksHandler.getSlots() && index < prevStacksHandler.getSlots()) {
                        ItemStack prevStack = prevStacksHandler.getStacks().getStackInSlot(index);

                        if (!prevStack.isEmpty()) {
                            if (newStacksHandler.getStacks().isItemValid(index, prevStack)) {
                                newStacksHandler.getStacks().setStackInSlot(index, prevStack);
                            } else {
                                this.curiosItemHandler.loseInvalidStack(prevStack);
                            }
                        }
                        ItemStack prevCosmetic = prevStacksHandler.getCosmeticStacks().getStackInSlot(index);

                        if (!prevCosmetic.isEmpty()) {
                            if (newStacksHandler.getStacks().isItemValid(index, prevCosmetic)) {
                                newStacksHandler.getCosmeticStacks().setStackInSlot(index, prevStacksHandler.getCosmeticStacks().getStackInSlot(index));
                            } else {
                                this.curiosItemHandler.loseInvalidStack(prevCosmetic);
                            }
                        }
                        index++;
                    }

                    while (index < prevStacksHandler.getSlots()) {
                        this.curiosItemHandler.loseInvalidStack(prevStacksHandler.getStacks().getStackInSlot(index));
                        this.curiosItemHandler.loseInvalidStack(prevStacksHandler.getCosmeticStacks().getStackInSlot(index));
                        index++;
                    }
                    sortedCurios.put(slotType, newStacksHandler);

                    for (int j = 0; j < newStacksHandler.getRenders().size() && j < prevStacksHandler.getRenders().size(); j++) {
                        newStacksHandler.getRenders().set(j, prevStacksHandler.getRenders().get(j));
                    }
                });

                if (optionalType.isEmpty()) {
                    IDynamicStackHandler stackHandler = prevStacksHandler.getStacks();
                    IDynamicStackHandler cosmeticStackHandler = prevStacksHandler.getCosmeticStacks();

                    for (int j = 0; j < stackHandler.getSlots(); j++) {
                        ItemStack stack = stackHandler.getStackInSlot(j);

                        if (!stack.isEmpty()) {
                            this.curiosItemHandler.loseInvalidStack(stack);
                        }

                        ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(j);

                        if (!cosmeticStack.isEmpty()) {
                            this.curiosItemHandler.loseInvalidStack(cosmeticStack);
                        }
                    }
                }
            }
            sortedCurios.forEach((slotType, stacksHandler) -> curios.put(slotType.getIdentifier(), stacksHandler));
            this.curios.putAll(curios);
            this.deserialized = new CompoundTag();
        }
    }

    public Map<String, ICurioStacksHandler> asMap() {
        return this.curios;
    }

    public void replace(Map<String, ICurioStacksHandler> curios) {
        this.curios.clear();
        this.curios.putAll(curios);
    }

    @Override
    public CompoundTag serializeNBT(@Nonnull HolderLookup.Provider provider) {
        CompoundTag compound = new CompoundTag();

        ListTag taglist = new ListTag();
        this.curios.forEach((key, stacksHandler) -> {
            CompoundTag tag = new CompoundTag();
            tag.put("StacksHandler", stacksHandler.serializeNBT());
            tag.putString("Identifier", key);
            taglist.add(tag);
        });
        compound.put("Curios", taglist);
        return compound;
    }

    @Override
    public void deserializeNBT(@Nonnull HolderLookup.Provider provider, @Nonnull CompoundTag nbt) {
        this.deserialized = nbt;
        this.markDeserialized = true;
    }
}
