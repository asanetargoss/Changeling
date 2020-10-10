package mchorse.vanilla_pack.editors.panels;

import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.metamorph.client.gui.editor.GuiMorphPanel;
import mchorse.vanilla_pack.editors.GuiPlayerMorph;
import mchorse.vanilla_pack.morphs.PlayerMorph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Username morph panel allows editing username of the player morph 
 */
@SideOnly(Side.CLIENT)
@SuppressWarnings("rawtypes")
public class GuiUsernamePanel extends GuiMorphPanel<PlayerMorph, GuiPlayerMorph>
{
    public GuiTextElement username;
    private int counter = -1;

    public GuiUsernamePanel(Minecraft mc, GuiPlayerMorph editor)
    {
        super(mc, editor);

        this.username = new GuiTextElement(mc, 120, (str) -> this.editUsername(str));
        this.username.flex().relative(this).set(10, 10, 0, 20).w(1, -20).y(1, -30);

        this.add(this.username);
    }

    private void editUsername(String str)
    {
        if (str.isEmpty())
        {
            return;
        }

        this.counter = 15;
    }

    @Override
    public void fillData(PlayerMorph morph)
    {
        super.fillData(morph);

        this.username.setText(morph.profile.getName());
    }

    @Override
    public void draw(GuiContext context)
    {
        if (this.counter >= 0)
        {
            if (this.counter == 0 && !this.username.field.getText().isEmpty())
            {
                PlayerMorph morph = this.morph;

                morph.resetEntity();
                morph.setProfile(this.username.field.getText());
            }

            String updating = I18n.format("metamorph.gui.panels.updating");
            int w = this.font.getStringWidth(updating);

            this.font.drawStringWithShadow(updating, this.username.area.ex() - w, this.username.area.y - 12, 0xaaaaaa);
            this.counter--;
        }

        super.draw(context);

        if (this.username.isVisible())
        {
            this.font.drawStringWithShadow(I18n.format("metamorph.gui.panels.username"), this.username.area.x, this.username.area.y - 12, 0xffffff);
        }
    }
}