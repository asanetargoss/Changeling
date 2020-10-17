package mchorse.metamorph.api.morphs;

import mchorse.metamorph.Metamorph;
import mchorse.metamorph.api.MorphSettings;
import mchorse.metamorph.api.abilities.IAbility;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.entity.SoundHandler;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Base class for all different types of morphs
 * 
 * This is an abstract morph. It contains all needed properties for a basic 
 * morph such as abilities, action, attack, health, speed and hotstyle flag.
 * 
 * This class is also responsible for rendering operations. Oh boy, this class 
 * is so huge. I'll have to decompose this thing onto rendering and logic code.
 */
public abstract class AbstractMorph
{
    /* Meta information */

    /**
     * Morph's name
     */
    public String name = "";

    /**
     * Is this morph is favorite 
     */
    public boolean favorite = false;

    /* Morph Settings */

    /**
     * The authoritative settings for the morph.
     */
    @Deprecated
    public MorphSettings settings = MorphSettings.DEFAULT.clone();

    /**
     * If this is false, {@link #settings} will be initialized
     * as needed. If this is true, {@link #settings} will remain
     * at whatever value it was set to in {@link #forceSettings(MorphSettings)}
     */
    protected boolean forcedSettings = false;
    
    protected boolean needSettingsUpdate = false;

    /**
     * The highest priority settings, defined through
     * configuration.
     */
    protected MorphSettings activeSettings = null;
    
    /**
     * This is called to initialize settings for morphs, if
     * settings are out-of-date.
     */
    public void initializeSettings()
    {
        if (!this.needSettingsUpdate)
        {
            return;
        }

        this.settings = MorphSettings.DEFAULT_MORPHED.clone();

        if (this.activeSettings != null)
        {
            this.settings.applyOverrides(this.activeSettings);
        }
        
        finishInitializingSettings();
    }
    
    protected void finishInitializingSettings()
    {
        this.needSettingsUpdate = false;
        this.forcedSettings = false;
    }

    /**
     * This sets the active settings for the morph, usually defined
     * by the user through JSON configuration. These settings usually
     * have the highest priority.
     */
    public void setActiveSettings(MorphSettings activeSettings)
    {
        this.activeSettings = activeSettings;
        this.needSettingsUpdate = true;
    }
    
    /**
     * This forces a morph to use the given settings. These settings
     * will not be overridden and must be complete settings
     * (no null fields allowed).
     */
    public void forceSettings(MorphSettings settings)
    {
        this.settings = settings;
        this.forcedSettings = true;
    }
    
    /**
     * Undoes the effects of {@link #forceSettings}
     */
    public void clearForcedSettings()
    {
        this.settings = null;
        this.forcedSettings = false;
        this.needSettingsUpdate = true;
    }
    
    /**
     * Gets the morph settings or initializes them if
     * not defined.
     */
    public MorphSettings getSettings()
    {
        if (!this.forcedSettings)
        {
            initializeSettings();
        }
        return this.settings;
    }

    /* Rendering */

    /**
     * Client morph renderer. It's for {@link EntityPlayer} only, don't try 
     * using it with other types of entities.
     */
    @SideOnly(Side.CLIENT)
    public Render<? extends Entity> renderer;

    /* Render methods */

    /**
     * Render this morph on 2D screen (used in GUIs)
     */
    @SideOnly(Side.CLIENT)
    public abstract void renderOnScreen(EntityPlayer player, int x, int y, float scale, float alpha);

    /**
     * Render the entity (in the world) 
     */
    @SideOnly(Side.CLIENT)
    public abstract void render(EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks);

    /**
     * Render the arm for given hand 
     */
    @SideOnly(Side.CLIENT)
    public boolean renderHand(EntityPlayer player, EnumHand hand)
    {
        return !getSettings().hands;
    }

    /* Update loop */

    /**
     * Update the player based on its morph abilities and properties. This 
     * method also responsible for updating AABB size. 
     */
    public void update(EntityLivingBase target, IMorphing cap)
    {
        MorphSettings settings = this.getSettings();
        if (getSettings().speed != 0.1F)
        {
            target.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(settings.speed);
        }

        for (IAbility ability : settings.abilities)
        {
            ability.update(target);
        }
    }

    /* Morph and demorph handlers */

    /**
     * Morph into the current morph
     * 
     * This method responsible for setting up the health of the player to 
     * morph's health and invoke ability's onMorph methods.
     */
    public void morph(EntityLivingBase target)
    {
        for (IAbility ability : this.getSettings().abilities)
        {
            ability.onMorph(target);
        }
    }

    /**
     * Demorph from the current morph
     * 
     * This method responsible for setting up the health back to player's 
     * default health and invoke ability's onDemorph methods.
     */
    public void demorph(EntityLivingBase target)
    {
        for (IAbility ability : this.getSettings().abilities)
        {
            ability.onDemorph(target);
        }
    }

    /* Adjusting size */

    /**
     * Update player's size based on given width and height.
     * 
     * This method is responsible for doing trickshots, 360 noscopes while being 
     * morped in a morph. Probably...
     */
    public void updateSize(EntityLivingBase target, float width, float height)
    {
        updateSizeDefault(target, width, height);
    }

    public static void updateSizeDefault(EntityLivingBase target, float width, float height)
    {
        /* Any lower than this, and the morph will take damage when hitting the ceiling.
         * Likewise, an eye height less than this will cause suffocation damage when standing
         * on the ground.
         * This is hard-coded in vanilla.
         */
        float minEyeToHeadDifference = 0.1F;
        height = Math.max(height, minEyeToHeadDifference * 2);
        
        if (target instanceof EntityPlayer && !Metamorph.proxy.config.disable_pov)
        {
            float eyeHeight = height * 0.9F;
            if (eyeHeight + minEyeToHeadDifference > height)
            {
                eyeHeight = height - minEyeToHeadDifference;
            }
            ((EntityPlayer) target).eyeHeight = eyeHeight;
        }
        
        /* This is a total rip-off of EntityPlayer#setSize method */
        if (width != target.width || height != target.height)
        {
            AxisAlignedBB aabb = target.getEntityBoundingBox();

            target.width = width;
            target.height = height;
            target.setEntityBoundingBox(new AxisAlignedBB(target.posX - width / 2, aabb.minY, target.posZ - width / 2, target.posX + width / 2, aabb.minY + height, target.posZ + width / 2));
        }
    }

    /* Safe shortcuts for activating action and attack */

    /**
     * Execute action with (or on) given player 
     */
    public void action(EntityLivingBase target)
    {
        if (this.getSettings().action != null)
        {
            this.getSettings().action.execute(target, this);
        }
    }

    /**
     * Attack a target 
     */
    public void attack(Entity target, EntityLivingBase source)
    {
        if (this.getSettings().attack != null)
        {
            this.getSettings().attack.attack(target, source);
        }
    }
    
    /**
     * <p>Used when cloning morphs</p>
     * 
     * <p>
     * <b>IMPORTANT</b>: When you subclass other morphs, don't forget to override
     * their method with your own.
     * </p>
     */
    public abstract AbstractMorph getNewInstance();

    /**
     * <p>Clone this {@link AbstractMorph}</p>
     * 
     * <p>
     * <b>IMPORTANT</b>: If you subclass other morphs, and your morph contains new
     * data, don't for get to override their method with your own.
     * </p>
     */
    public AbstractMorph clone(boolean isRemote)
    {
        AbstractMorph morph = getNewInstance();
        // If this fails, then modder forgot to override getNewInstance()
        assert(this.getClass().isInstance(morph));

        morph.name = this.name;
        morph.favorite = this.favorite;
        morph.settings = this.settings != null ? this.settings.clone() : null;
        morph.activeSettings = this.activeSettings != null ? this.activeSettings.clone() : null;
        morph.forcedSettings = forcedSettings;
        morph.needSettingsUpdate = needSettingsUpdate;

        return morph;
    }

    /* Getting size */

    /**
     * Get width of this morph 
     */
    public abstract float getWidth(EntityLivingBase target);

    /**
     * Get height of this morph 
     */
    public abstract float getHeight(EntityLivingBase target);

    /**
     * Get the eye height of this morph.
     * Not used by updateSize.
     */
    public float getEyeHeight(EntityLivingBase target)
    {
        if (!Metamorph.proxy.config.disable_pov)
        {
            return this.getHeight(target) * 0.9F;
        }
        else
        {
            return 1.62F;
        }
    }

    /**
     * Get the default sound that this morph makes when it
     * is hurt
     */
    public final SoundEvent getHurtSound(EntityLivingBase target)
    {
        return getHurtSound(target, SoundHandler.GENERIC_DAMAGE);
    }

    /**
     * Get the sound that this morph makes when it
     * is hurt by the given DamageSource, or return null
     * for no change.
     */
    public SoundEvent getHurtSound(EntityLivingBase target, DamageSource damageSource)
    {
        return null;
    }

    /**
     * Get the sound that this morph makes when it
     * is killed, or return null for no change.
     */
    public SoundEvent getDeathSound(EntityLivingBase target)
    {
        return null;
    }

    /**
     * Make this return true if you override playStepSound(..)
     */
    public boolean hasCustomStepSound(EntityLivingBase target)
    {
        return false;
    }

    /**
     * Plays the sound that this morph makes when it
     * takes a step, but only if hasCustomStepSound(..) returns true
     */
    public void playStepSound(EntityLivingBase target)
    {}

    /**
     * Called when the player just changed dimensions
     */
    public void onChangeDimension(EntityPlayer player, int oldDim, int currentDim)
    {}

    /**
     * Check either if given object is the same as this morph 
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof AbstractMorph)
        {
            AbstractMorph morph = (AbstractMorph) obj;

            return morph.name.equals(this.name);
        }

        return super.equals(obj);
    }

    /**
     * Check whether the morph can be merged (this should allow 
     * overwriting of a morph instead of completely replacing it)
     */
    public boolean canMerge(AbstractMorph morph, boolean isRemote)
    {
        return false;
    }

    /**
     * Reset data for editing 
     */
    public void reset()
    {
        setActiveSettings(null);
        clearForcedSettings();
    }

    /* Reading / writing to NBT */

    /**
     * Save abstract morph's properties to NBT compound 
     */
    public void toNBT(NBTTagCompound tag)
    {
        tag.setString("Name", this.name);

        if (this.favorite) tag.setBoolean("Favorite", this.favorite);
    }

    /**
     * Read abstract morph's properties from NBT compound 
     */
    public void fromNBT(NBTTagCompound tag)
    {
        this.reset();

        this.name = tag.getString("Name");

        if (tag.hasKey("Favorite")) this.favorite = tag.getBoolean("Favorite");
    }
}