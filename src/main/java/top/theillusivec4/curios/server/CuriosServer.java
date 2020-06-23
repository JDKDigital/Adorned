package top.theillusivec4.curios.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.util.ICuriosServer;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncOperation;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncOperation.Operation;

public class CuriosServer implements ICuriosServer {

  private Map<String, ISlotType> idToType = new HashMap<>();

  @Override
  public void addSlotType(ISlotType slotType) {
    this.idToType.put(slotType.getIdentifier(), slotType);
  }

  @Override
  public Optional<ISlotType> getSlotType(String identifier) {
    return Optional.ofNullable(this.idToType.get(identifier));
  }

  @Override
  public Collection<ISlotType> getSlotTypes() {
    return Collections.unmodifiableCollection(idToType.values());
  }

  @Override
  public SortedMap<ISlotType, ICurioStacksHandler> createSlots() {
    SortedMap<ISlotType, ICurioStacksHandler> curios = new TreeMap<>();
    this.getSlotTypes().stream().filter(type -> !type.isLocked()).collect(Collectors.toSet())
        .forEach(type -> curios.put(type, new CurioStacksHandler(type.getSize())));
    return curios;
  }

  @Override
  public Set<String> getSlotTypeIds() {
    return Collections.unmodifiableSet(idToType.keySet());
  }

  @Override
  public int getSlotsForType(@Nonnull final LivingEntity livingEntity, String identifier) {
    return CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).map(
        handler -> handler.getStacksHandler(identifier).map(ICurioStacksHandler::getSlots)
            .orElse(0)).orElse(0);
  }

  @Override
  public void setSlotsForType(String id, final LivingEntity livingEntity, int amount) {
    int difference = amount - getSlotsForType(livingEntity, id);

    if (difference > 0) {
      growSlotType(id, difference, livingEntity);
    } else if (difference < 0) {
      shrinkSlotType(id, Math.abs(difference), livingEntity);
    }
  }

  @Override
  public void growSlotType(String id, final LivingEntity livingEntity) {
    growSlotType(id, 1, livingEntity);
  }

  @Override
  public void growSlotType(String id, int amount, final LivingEntity livingEntity) {
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
      handler.growSlotType(id, amount);

      if (livingEntity instanceof ServerPlayerEntity) {
        NetworkHandler.INSTANCE
            .send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) livingEntity),
                new SPacketSyncOperation(livingEntity.getEntityId(), id, Operation.GROW, amount));
      }
    });
  }


  @Override
  public void shrinkSlotType(String id, final LivingEntity livingEntity) {
    shrinkSlotType(id, 1, livingEntity);
  }

  @Override
  public void shrinkSlotType(String id, int amount, final LivingEntity livingEntity) {
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
      handler.shrinkSlotType(id, amount);

      if (livingEntity instanceof ServerPlayerEntity) {
        NetworkHandler.INSTANCE
            .send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) livingEntity),
                new SPacketSyncOperation(livingEntity.getEntityId(), id, Operation.SHRINK, amount));
      }
    });
  }


  @Override
  public void unlockSlotType(String id, final LivingEntity livingEntity) {
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity)
        .ifPresent(handler -> this.getSlotType(id).ifPresent(type -> {
          handler.unlockSlotType(id, type.getSize());

          if (livingEntity instanceof ServerPlayerEntity) {
            NetworkHandler.INSTANCE
                .send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) livingEntity),
                    new SPacketSyncOperation(livingEntity.getEntityId(), id, Operation.UNLOCK,
                        type.getSize()));
          }
        }));
  }

  @Override
  public void lockSlotType(String id, final LivingEntity livingEntity) {
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
      handler.lockSlotType(id);

      if (livingEntity instanceof ServerPlayerEntity) {
        NetworkHandler.INSTANCE
            .send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) livingEntity),
                new SPacketSyncOperation(livingEntity.getEntityId(), id, Operation.LOCK));
      }
    });
  }
}
