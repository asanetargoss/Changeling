package mchorse.metamorph.network.server.creative;

import mchorse.mclib.network.ServerMessageHandler;
import mchorse.metamorph.api.MorphAPI;
import mchorse.metamorph.network.common.creative.PacketAcquireMorph;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Server handler acquire morph
 * 
 * This handler is responsible for sending acquired morph for players in 
 * creative morph.
 */
public class ServerHandlerAcquireMorph extends ServerMessageHandler<PacketAcquireMorph>
{
    @Override
    public void run(EntityPlayerMP player, PacketAcquireMorph message)
    {
        if (player.isCreative() || player.isSpectator())
        {
            MorphAPI.acquire(player, message.morph);
        }
    }
}