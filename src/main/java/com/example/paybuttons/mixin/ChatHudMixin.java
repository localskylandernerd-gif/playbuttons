package com.example.paybuttons.mixin;

import com.example.paybuttons.PayButtonsClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * A "mixin" lets us hook into Minecraft's own code.
 * This one catches every message right before it is added to your chat window,
 * no matter how the server sent it.
 */
@Mixin(ChatHud.class)
public class ChatHudMixin {

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Text paybuttons$modifyMessage(Text message) {
        return PayButtonsClient.modify(message);
    }
}
