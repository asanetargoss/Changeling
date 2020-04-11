package mchorse.metamorph;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.commands.CommandAcquireMorph;
import mchorse.metamorph.commands.CommandMetamorph;
import mchorse.metamorph.commands.CommandMorph;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * Metamorph mod
 * 
 * This mod provides functionality for survival morphing. To gain a morph 
 * you have to kill a mob. Once you killed it, you gain its morphing. Once you 
 * gained its morphing you can use special menu to select a mob into which to 
 * morph.
 * 
 * Except different shape, you gain also special abilities for specific mobs. 
 * In creative you can access all morphings.
 * 
 * Inspired by Morph and Shape Shifter Z mods (mostly due to the fact that 
 * they're outdated), however, iChun saying that he's working on Morph for 
 * 1.10.2, this is really exciting! :D
 */
@Mod(modid = Metamorph.MODID, name = Metamorph.MODNAME, version = Metamorph.VERSION, guiFactory = Metamorph.GUI_FACTORY, updateJSON = "", dependencies = "after:moreplayermodels;required-after:mclib@[%DOMINIONLIB%,)")
public class Metamorph
{
    /* Metadata fields */
    public static final String MODID = "metamorph";
    public static final String MODNAME = "Changeling";
    public static final String VERSION = "%VERSION%";

    public static final String CLIENT_PROXY = "mchorse.metamorph.ClientProxy";
    public static final String SERVER_PROXY = "mchorse.metamorph.CommonProxy";

    public static final String GUI_FACTORY = "mchorse.metamorph.config.gui.GuiFactory";

    /* Forge stuff classes */
    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static Metamorph instance;

    /**
     * Custom payload channel 
     */
    public static FMLEventChannel channel;

    /* Events */
    @EventHandler
    public void preLoad(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("Metamorph");

        proxy.preLoad(event);
    }

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        proxy.load();
    }

    @EventHandler
    public void postLoad(FMLPostInitializationEvent event)
    {
        /* I hope all entities are going to be loaded */
        MorphManager.initiateMap();

        proxy.postLoad(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        /* Setting up the blacklist */

        MorphManager.INSTANCE.setActiveBlacklist(MorphUtils.reloadBlacklist());
        MorphManager.INSTANCE.setActiveSettings(MorphUtils.reloadMorphSettings());

        /* Register commands */
        event.registerServerCommand(new CommandMorph());
        event.registerServerCommand(new CommandAcquireMorph());
        event.registerServerCommand(new CommandMetamorph());
    }

    /* Logging */

    /* TODO: Set to false when publishing and remove all unnecessary printlns */
    public static boolean DEBUG = false;
    public static Logger LOGGER;

    /**
     * Log out the message if in DEBUG mode.
     * 
     * But I always forget to turn it off before releasing the mod.
     */
    public static void log(String message)
    {
        if (DEBUG)
        {
            LOGGER.log(Level.INFO, message);
        }
    }
}