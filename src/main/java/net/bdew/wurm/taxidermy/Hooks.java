package net.bdew.wurm.taxidermy;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.SkillList;

public class Hooks {
    public static float sizeMod(Item item) {
        if (item.getTemplateId() == CustomItems.stuffedCorpseId) {
            int sf = item.getData2() >> 8;
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

}
