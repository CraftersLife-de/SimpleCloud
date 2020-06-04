/*
 * MIT License
 *
 * Copyright (C) 2020 SimpleCloud-Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package eu.thesimplecloud.base.manager.network.packets.player

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.player.CloudPlayer
import eu.thesimplecloud.api.player.OfflineCloudPlayer
import eu.thesimplecloud.api.player.connection.DefaultPlayerConnection
import eu.thesimplecloud.api.property.Property
import eu.thesimplecloud.base.manager.startup.Manager
import eu.thesimplecloud.clientserverapi.lib.connection.IConnection
import eu.thesimplecloud.clientserverapi.lib.packet.packettype.JsonPacket
import eu.thesimplecloud.clientserverapi.lib.promise.ICommunicationPromise

class PacketInCreateCloudPlayer() : JsonPacket() {

    override suspend fun handle(connection: IConnection): ICommunicationPromise<out Any> {
        val playerConnection = this.jsonData.getObject("playerConnection", DefaultPlayerConnection::class.java)
                ?: return contentException("playerConnection")
        val proxyName = this.jsonData.getString("proxyName") ?: return contentException("proxyName")
        val offlinePlayer = Manager.instance.offlineCloudPlayerHandler.getOfflinePlayer(playerConnection.getUniqueId())
        val cloudPlayer = if (offlinePlayer == null) {
            CloudPlayer(playerConnection.getName(), playerConnection.getUniqueId(), System.currentTimeMillis(), System.currentTimeMillis(), 0L, proxyName, null, playerConnection, HashMap())
        } else {
            CloudPlayer(
                    playerConnection.getName(),
                    playerConnection.getUniqueId(),
                    offlinePlayer.getFirstLogin(),
                    System.currentTimeMillis(),
                    offlinePlayer.getOnlineTime(),
                    proxyName,
                    null,
                    playerConnection,
                    (offlinePlayer.getProperties() as Map<String, Property<*>>).toMutableMap()
            )
        }
        CloudAPI.instance.getCloudPlayerManager().update(cloudPlayer)
        Manager.instance.offlineCloudPlayerHandler.saveCloudPlayer(cloudPlayer.toOfflinePlayer() as OfflineCloudPlayer)
        return success(cloudPlayer)
    }
}