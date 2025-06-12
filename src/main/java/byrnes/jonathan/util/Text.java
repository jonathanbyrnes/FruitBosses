package byrnes.jonathan.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Text {
    public static Component translate(String text) {
        return MiniMessage.miniMessage().deserialize(text != null ? text : "<red>Missing message.");
    }
}
