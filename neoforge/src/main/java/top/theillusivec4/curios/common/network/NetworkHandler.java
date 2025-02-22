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

package top.theillusivec4.curios.common.network;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import top.theillusivec4.curios.common.network.client.*;
import top.theillusivec4.curios.common.network.server.*;
import top.theillusivec4.curios.common.network.server.sync.*;

public class NetworkHandler {

  public static void register(final PayloadRegistrar registrar) {
    //Client Packets
    registrar.playToServer(CPacketDestroy.TYPE, CPacketDestroy.STREAM_CODEC,
        CuriosServerPayloadHandler.getInstance()::handleDestroyPacket);
    registrar.playToServer(CPacketOpenCurios.TYPE, CPacketOpenCurios.STREAM_CODEC,
        CuriosServerPayloadHandler.getInstance()::handleOpenCurios);
    registrar.playToServer(CPacketOpenVanilla.TYPE, CPacketOpenVanilla.STREAM_CODEC,
        CuriosServerPayloadHandler.getInstance()::handleOpenVanilla);
    registrar.playToServer(CPacketPage.TYPE, CPacketPage.STREAM_CODEC,
        CuriosServerPayloadHandler.getInstance()::handlePage);
    registrar.playToServer(CPacketToggleRender.TYPE, CPacketToggleRender.STREAM_CODEC,
        CuriosServerPayloadHandler.getInstance()::handlerToggleRender);
    registrar.playToServer(CPacketToggleCosmetics.TYPE, CPacketToggleCosmetics.STREAM_CODEC,
        CuriosServerPayloadHandler.getInstance()::handlerToggleCosmetics);

    // Server Packets
    registrar.playToClient(SPacketSyncStack.TYPE, SPacketSyncStack.STREAM_CODEC,
        CuriosClientPayloadHandler.getInstance()::handle);
    registrar.playToClient(SPacketGrabbedItem.TYPE, SPacketGrabbedItem.STREAM_CODEC,
        CuriosClientPayloadHandler.getInstance()::handle);
    registrar.playToClient(SPacketSyncCurios.TYPE, SPacketSyncCurios.STREAM_CODEC,
        CuriosClientPayloadHandler.getInstance()::handle);
    registrar.playToClient(SPacketSyncData.TYPE, SPacketSyncData.STREAM_CODEC,
        CuriosClientPayloadHandler.getInstance()::handle);
    registrar.playToClient(SPacketSyncModifiers.TYPE, SPacketSyncModifiers.STREAM_CODEC,
        CuriosClientPayloadHandler.getInstance()::handle);
    registrar.playToClient(SPacketSyncRender.TYPE, SPacketSyncRender.STREAM_CODEC,
        CuriosClientPayloadHandler.getInstance()::handle);
    registrar.playToClient(SPacketBreak.TYPE, SPacketBreak.STREAM_CODEC,
        CuriosClientPayloadHandler.getInstance()::handle);
    registrar.playToClient(SPacketPage.TYPE, SPacketPage.STREAM_CODEC,
        CuriosClientPayloadHandler.getInstance()::handle);
    registrar.playToClient(SPacketSetIcons.TYPE, SPacketSetIcons.STREAM_CODEC,
        CuriosClientPayloadHandler.getInstance()::handle);
    registrar.playToClient(SPacketQuickMove.TYPE, SPacketQuickMove.STREAM_CODEC,
        CuriosClientPayloadHandler.getInstance()::handle);
  }
}
