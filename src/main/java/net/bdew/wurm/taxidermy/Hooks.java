package net.bdew.wurm.taxidermy;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.SkillList;

public class Hooks {
    public static float sizeMod(Item item) {
        if (item.getTemplateId() == CustomItems.stuffedCorpseId) {
            if (item.getData2() == -1) return 1f;
            int sf = (item.getData2() >> 8) & 0xFF;
            if (sf > 0)
                return (float) (sf) / 20f;
            else
                return 1f;
        } else {
            return 1f;
        }
    }

    public static int getImproveSkill(Item item) {
        if (item.getTemplateId() == CustomItems.stuffedCorpseId)
            return SkillList.CARPENTRY_FINE;
        else
            return -10;
    }

    public static boolean sendItemHook(Communicator comm, Item item) {
        if ((item.getTemplateId() == CustomItems.stuffedCorpseId) && (item.getAuxData() == 2)) {
            Animated.sendAnimated(comm, item, item.getPosX(), item.getPosY(), item.getPosZ(), item.getRotation());
            return false;
        } else {
            return true;
        }
    }

    public static boolean removeItemHook(Communicator comm, Item item) {
        if ((item.getTemplateId() == CustomItems.stuffedCorpseId) && (item.getAuxData() == 2)) {
            Animated.removeAnimated(comm, item);
            return false;
        } else {
            return true;
        }
    }
}
