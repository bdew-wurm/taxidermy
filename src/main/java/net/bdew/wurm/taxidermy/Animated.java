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
                false
        );
        if (item.getRarity() != 0)
            comm.updateCreatureRarity(item.getWurmId(), item.rarity);

        comm.setCreatureDamage(item.getWurmId(), 100f);

        if (item.getColor() != -1)
            comm.sendRepaint(item.getWurmId(), (byte) WurmColor.getColorRed(item.getColor()), (byte) WurmColor.getColorGreen(item.getColor()), (byte) WurmColor.getColorBlue(item.getColor()), (byte) -1, (byte) 0);

        int sz = (int) (Math.min(Hooks.sizeMod(item) * 64f, 255f));

        comm.sendResize(item.getWurmId(), (byte) sz, (byte) sz, (byte) sz);
    }

    static void removeAnimated(Communicator comm, Item item) {
        comm.sendDeleteCreature(item.getWurmId());
    }
}
