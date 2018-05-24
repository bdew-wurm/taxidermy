package net.bdew.wurm.taxidermy;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;

import java.util.LinkedList;
import java.util.List;

public class PreserveBehaviourProvider implements BehaviourProvider {
    private List<ActionEntry> menu;

    public PreserveBehaviourProvider(int normalCost, int animatedCost) {
        menu = new LinkedList<>();
        menu.add(new ActionEntry((short) -3, "Preserve", ""));
        menu.add(new PreserveAction(0, "Lying", "", normalCost).actionEntry);
        menu.add(new PreserveAction(1, "Butchered", "butchered", normalCost).actionEntry);
        menu.add(new PreserveAction(2, "Animated", "animated", animatedCost).actionEntry);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        if (PreserveAction.canUse(performer, source, target))
            return menu;
        else
            return null;
    }

}
