package com.yourwebsitespace.solehack.modules.misc;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SoleSuffix extends Module {

    private String suffix = " | SᴏʟᴇHᴀᴄᴋ 1.0"; // customize this

    public SoleSuffix() {
        super("SoleSuffix", Category.MISC);
    }

    @Override
    protected void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        // Don't touch commands (messages starting with /)
        if (event.getMessage().startsWith("/")) return;

        event.setMessage(event.getMessage() + suffix);
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}