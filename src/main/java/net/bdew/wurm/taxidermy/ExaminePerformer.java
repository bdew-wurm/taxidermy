package net.bdew.wurm.taxidermy;

import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPropagation;

public class ExaminePerformer implements ActionPerformer {
    @Override
    public short getActionId() {
        return Actions.EXAMINE;
    }

    @Override
    public boolean action(Action action, Creature performer, Item target, short num, float counter) {
        if (target.getTemplateId() == CustomItems.stuffedCorpseId) {
            String bodyName;
            try {
                CreatureTemplate tpl = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
                bodyName = tpl.getName().toLowerCase();
            } catch (NoSuchCreatureTemplateException e) {
                bodyName = "creature";
            }

            boolean animated = target.getAuxData() == 2;

            String res = String.format("%s of a %s that was preserved using a taxidermy kit%s.", animated ? "An animated body" : "A body", bodyName, animated ? " and some magic" : "");

            if (target.creator != null && target.creator.length() > 0) {
                res += String.format(" It was preserved by %s on %s", target.creator, WurmCalendar.getDateFor(target.creationDate));
            }

            if (target.isPlanted()) {
                PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(target.lastOwner);
                String planter = "someone";
                if (pInfo != null) {
                    planter = pInfo.getName();
                }
                res += " It has been firmly secured to the ground by " + planter + ".";
            }

            if (target.getRarity() > 0) {
                res += MethodsItems.getRarityDesc(target.rarity);
            }

            performer.getCommunicator().sendNormalServerMessage(res);
            return propagate(action, ActionPropagation.FINISH_ACTION, ActionPropagation.NO_SERVER_PROPAGATION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION);
        } else {
            return propagate(action, ActionPropagation.CONTINUE_ACTION, ActionPropagation.SERVER_PROPAGATION, ActionPropagation.ACTION_PERFORMER_PROPAGATION);
        }

    }
}
