package mchorse.metamorph.network.client.creative;

import mchorse.mclib.network.ClientMessageHandler;
import mchorse.metamorph.ClientProxy;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import mchorse.metamorph.network.common.creative.PacketAcquireMorph;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientHandlerAcquireMorph extends ClientMessageHandler<PacketAcquireMorph>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketAcquireMorph message)
    {
        IMorphing morphing = Morphing.get(player);

        morphing.acquireMorph(message.morph);

        ClientProxy.morphOverlay.add(message.morph);
    }
}