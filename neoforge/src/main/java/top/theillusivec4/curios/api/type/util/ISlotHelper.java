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

package top.theillusivec4.curios.api.type.util;

import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

@Deprecated(forRemoval = true, since = "1.20.1")
@ApiStatus.ScheduledForRemoval(inVersion = "1.22")
public interface ISlotHelper {

  /**
   * @see <a href="https://docs.illusivesoulworks.com/category/curios">Curios Documentation</a>
   * @deprecated Use the datapack-based approach to slot registration
   */
  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  void addSlotType(ISlotType slotType);

  /**
   * @deprecated Moved to internal code and removed from the API
   */
  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  void clear();

  /**
   *  @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#getSlot(String, Level)}
   */
  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  Optional<ISlotType> getSlotType(String identifier);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#getSlots()}
   */
  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  Collection<ISlotType> getSlotTypes();

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#getEntitySlots(LivingEntity)}
   */
  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  Collection<ISlotType> getSlotTypes(LivingEntity livingEntity);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#getSlots()}
   */
  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  Set<String> getSlotTypeIds();

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#getCurios()}
   */
  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  int getSlotsForType(LivingEntity livingEntity, String id);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addTransientSlotModifiers(Multimap)}
   * and {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addPermanentSlotModifiers(Multimap)}
   */
  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  void setSlotsForType(String id, LivingEntity livingEntity, int amount);
}
