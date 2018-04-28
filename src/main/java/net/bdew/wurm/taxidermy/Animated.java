package net.bdew.wurm.taxidermy;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.WurmColor;

public class Animated {
    static void sendAnimated(Communicator comm, Item item, float x, float y, float z, float rot) {
        comm.sendNewCreature(
                item.getWurmId(),
                item.getName(),
                CustomItems.corpseModelNameProvider.getModelName(item),
                x,
                y,
                z,
                item.getBridgeId(),
                rot,
                (byte) (item.isOnSurface() ? 0 : -1),
                false,
                false,
                false,
                (byte) (item.getData2() & 0xFF),
                0L,
                (byte) 0,
                false,
                false,
                (byte) 0
        );
        if (item.getRarity() != 0)
            comm.updateCreatureRarity(item.getWurmId(), item.rarity);

        comm.setCreatureDamage(item.getWurmId(), 100f);

        if (item.getColor() != -1)
            comm.sendRepaint(item.getWurmId(), (byte) WurmColor.getColorRed(item.getColor()), (byte) WurmColor.getColorGreen(item.getColor()), (byte) WurmColor.getColorBlue(item.getColor()), (byte) -1, (byte) 0);

        if (item.isLight()) {
            int lightStrength;
            if (item.color != -1) {
                lightStrength = Math.max(WurmColor.getColorRed(item.color), WurmColor.getColorGreen(item.color));
                lightStrength = Math.max(1, Math.max(lightStrength, WurmColor.getColorBlue(item.color)));
                byte r = (byte) (WurmColor.getColorRed(item.color) * 128 / lightStrength);
                byte g = (byte) (WurmColor.getColorGreen(item.color) * 128 / lightStrength);
                byte b = (byte) (WurmColor.getColorBlue(item.color) * 128 / lightStrength);
                comm.sendAttachEffect(item.getWurmId(), (byte) 0, r, g, b, item.getRadius());
            } else {
                comm.sendAttachEffect(item.getWurmId(), (byte) 0, Item.getRLight(80), Item.getGLight(80), Item.getBLight(80), item.getRadius());
            }
        }

        int sz = (int) (Math.min(Hooks.sizeMod(item) * 64f, 255f));

        comm.sendResize(item.getWurmId(), (byte) sz, (byte) sz, (byte) sz);
    }

    static void removeAnimated(Communicator comm, Item item) {
        comm.sendDeleteCreature(item.getWurmId());
    }
}
