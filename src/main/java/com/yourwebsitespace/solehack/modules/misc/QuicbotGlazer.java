package com.yourwebsitespace.solehack.modules.misc;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class QuicbotGlazer extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Throttled pacing limit configuration: 10 seconds = exactly 10,000 milliseconds
    public long messageDelayMs = 10000;

    private long lastMessageTime = 0L;
    private int messageIndex = 0;

    // Array holding exactly 10 different customizable messages
    private final String[] messages = new String[] {
            "I love quicbot so fuckin much",
            "quicbot is the best bot <3",
            "Whoever made quicbot is the best player on the server",
            "quicbot helped me with my homework",
            "Idk where I will be without quicbot",
            "I'm in love with quicbot <3",
            "!kiss Quicbot",
            "Would you rather, Dinner with Jay-Z, or kiss quicbot",
            "We all will take care and love quicbot",
            "quicbot is the king of 5b5t 👑"
    };

    public QuicbotGlazer() {
        super("QuicbotGlazer", Category.MISC);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        lastMessageTime = System.currentTimeMillis(); // Delay the first message slightly on activation
        messageIndex = 0;
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        // High-precision clock gate: Enforces the exact 10-second spacing constraint
        if (currentTime - lastMessageTime >= messageDelayMs) {

            // Fetch the current text row from our dictionary sequence safely
            String selectedMessage = messages[messageIndex];

            // Enforce safe packet length properties to pass vanilla server boundaries
            if (selectedMessage.length() > 256) {
                selectedMessage = selectedMessage.substring(0, 256);
            }

            // Stream the message straight down the network server connection pipe line
            mc.player.sendChatMessage(selectedMessage);

            // Increment the tracking pointer to move to the next unique text phrase row
            messageIndex++;

            // Loop boundaries reset cleanly back to index 0 once all 10 messages finish broadcasting
            if (messageIndex >= messages.length) {
                messageIndex = 0;
            }

            // Lock the time snapshot stamp parameter for the next calculation gate pass
            lastMessageTime = currentTime;
        }
    }

    @Override
    protected void onUpdate() {}
}
