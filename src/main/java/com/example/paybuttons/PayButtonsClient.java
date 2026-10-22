package com.example.paybuttons;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayButtonsClient implements ClientModInitializer {

    // ------------------------------------------------------------------
    // SETTINGS - easy things to change
    // ------------------------------------------------------------------

    /**
     * false = clicking a button TYPES the command into your chat box (you press Enter).
     *         This is safer: a mis-click can't send money by accident.
     * true  = clicking a button SENDS the command instantly.
     */
    private static final boolean AUTO_SEND = false;

    // Button / text colors (hex RGB)
    private static final int RED = 0xFF5555;
    private static final int GREEN = 0x55FF55;
    private static final int PURPLE = 0xB44CFF;

    /** true = write every chat line the mod sees into latest.log (set to false once it works). */
    private static final boolean DEBUG = true;

    private static boolean firstLineLogged = false;

    public static final Logger LOGGER = LoggerFactory.getLogger("paybuttons");

    /**
     * Matches messages like:  "Steve Paid you $ 1"  /  "Steve paid you $2.5K"
     * - not case-sensitive
     * - allows spaces and up to 2 odd characters (like "$") before the number
     * Group 1 = username, group 2 = the "$" part, group 3 = number, group 4 = K/M/B suffix.
     *
     * If DonutSMP words the message differently, change the text in this pattern!
     */
    private static final Pattern PAID_PATTERN = Pattern.compile(
            "(\\S+)\\s+paid\\s+you\\s*([^\\d\\s]{0,2})\\s*(\\d[\\d,]*(?:\\.\\d+)?)([KkMmBb]?)",
            Pattern.CASE_INSENSITIVE);

    // ------------------------------------------------------------------

    @Override
    public void onInitializeClient() {
        // Hook 1: the chat HUD mixin (ChatHudMixin) sends every chat line through modify().
        // Hook 2: Fabric's message event does the same for server/system messages.
        // Having both is safe: a message that already has buttons is never changed twice.
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> overlay ? message : modify(message));
        LOGGER.info("Pay Buttons loaded - waiting for 'paid you' messages");
    }

    /** Called for EVERY line that is about to be shown in chat. */
    public static Text modify(Text original) {
        try {
            String raw = original.getString();

            if (!firstLineLogged) {
                firstLineLogged = true;
                LOGGER.info("Pay Buttons: first chat line reached the mod: [{}]", raw);
            }
            if (DEBUG) {
                LOGGER.info("Pay Buttons sees: [{}]", raw);
            }

            // Never touch a message we already changed
            if (raw.contains("[1x]")) {
                return original;
            }

            Matcher m = PAID_PATTERN.matcher(raw);
            if (!m.find()) {
                if (raw.toLowerCase().contains("paid")) {
                    // Helps us troubleshoot: shows the exact text the mod could not match
                    LOGGER.info("Saw a 'paid' message but the pattern did not match: [{}]", raw);
                }
                return original;
            }

            String username = m.group(1);
            String number = m.group(3);
            String suffix = m.group(4);

            // Work out the real amount (so "2.5K" becomes 2500)
            BigDecimal amount = new BigDecimal(number.replace(",", "")).multiply(suffixValue(suffix));

            // The part of the message that is the money, e.g. "$ 1"
            int moneyStart = m.start(2);
            int moneyEnd = m.end(4);
            String before = raw.substring(0, moneyStart);
            String money = raw.substring(moneyStart, moneyEnd);
            String after = raw.substring(moneyEnd);

            // Rebuild the message: normal text + GREEN money + normal text + buttons
            MutableText result = Text.literal(before);
            result.append(Text.literal(money).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GREEN))));
            result.append(Text.literal(after));
            result.append(Text.literal(" "));
            result.append(button(1, RED, username, amount));
            result.append(Text.literal(" "));
            result.append(button(2, GREEN, username, amount));
            result.append(Text.literal(" "));
            result.append(button(3, PURPLE, username, amount));
            return result;
        } catch (Exception e) {
            LOGGER.error("Pay Buttons failed on a message", e);
            return original; // never break chat
        }
    }

    /** Creates one clickable [Nx] button. */
    private static Text button(int multiplier, int color, String username, BigDecimal amount) {
        String sendAmount = amount
                .multiply(BigDecimal.valueOf(multiplier))
                .setScale(0, RoundingMode.DOWN)
                .toPlainString();
        String command = "/pay " + username + " " + sendAmount;

        ClickEvent.Action action = AUTO_SEND
                ? ClickEvent.Action.RUN_COMMAND
                : ClickEvent.Action.SUGGEST_COMMAND;

        Style style = Style.EMPTY
                .withColor(TextColor.fromRgb(color))
                .withBold(true)
                .withClickEvent(new ClickEvent(action, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(command)));

        return Text.literal("[" + multiplier + "x]").setStyle(style);
    }

    /** K = thousand, M = million, B = billion. */
    private static BigDecimal suffixValue(String suffix) {
        return switch (suffix.toUpperCase()) {
            case "K" -> BigDecimal.valueOf(1_000L);
            case "M" -> BigDecimal.valueOf(1_000_000L);
            case "B" -> BigDecimal.valueOf(1_000_000_000L);
            default -> BigDecimal.ONE;
        };
    }
}
