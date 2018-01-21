package net.bdew.wurm.taxidermy;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.*;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillList;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPropagation;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class PreserveAction implements ActionPerformer {
    private final int aux;
    private final String suffix;
    private final int karmaCost;

    final ActionEntry actionEntry;

    public PreserveAction(int aux, String name, String suffix, int karmaCost) {
        this.aux = aux;
        this.suffix = suffix;
        this.karmaCost = karmaCost;
        actionEntry = ActionEntry.createEntry((short) ModActions.getNextActionId(), name + (karmaCost > 0 ? String.format(" (%d karma)", karmaCost) : ""), "preserving", new int[]{
                48 /* ACTION_TYPE_ENEMY_ALWAYS */,
                36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */
        });
        ModActions.registerAction(actionEntry);
        ModActions.registerActionPerformer(this);
    }

    @Override
    public short getActionId() {
        return actionEntry.getNumber();
    }

    static boolean canUse(Creature performer, Item source, Item target) {
        return (performer.isPlayer() &&
                (source != null) && (source.getTopParentOrNull() == performer.getInventory()) && (source.getTemplateId() == CustomItems.taxidermyKitId) &&
                (target != null) && (target.getTopParentOrNull() == performer.getInventory()) && (target.getTemplateId() == ItemList.corpse));
    }

    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        Communicator comm = performer.getCommunicator();
        if (!canUse(performer, source, target)) {
            comm.sendAlertServerMessage("You aren't allowed to do that.");
            return propagate(action, ActionPropagation.FINISH_ACTION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.NO_SERVER_PROPAGATION);
        }

        if (performer.getKarma() < karmaCost) {
            if (performer.getPower() > 0) {
                comm.sendNormalServerMessage("You don't have enough karma to transfer to the body... but since you are a GM who cares, right?");
            } else {
                comm.sendAlertServerMessage("You don't have enough karma to transfer to the body.");
                return propagate(action, ActionPropagation.FINISH_ACTION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.NO_SERVER_PROPAGATION);
            }
        }

        Skill fineCarp = performer.getSkills().getSkillOrLearn(SkillList.CARPENTRY_FINE);
        CreatureTemplate tpl;

        try {
            tpl = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
        } catch (NoSuchCreatureTemplateException e) {
            comm.sendNormalServerMessage("This body is unrecognizable, you will not be able to preserve it.");
            return propagate(action, ActionPropagation.FINISH_ACTION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.NO_SERVER_PROPAGATION);
        }

        if (counter == 1.0f) {
            comm.sendNormalServerMessage(String.format("You start preserving the %s.", target.getName()));
            action.setTimeLeft(Actions.getStandardActionTime(performer, fineCarp, source, 0.0D));
            performer.sendActionControl("preserving", true, action.getTimeLeft());
        } else if (counter * 10.0F > action.getTimeLeft()) {
            double power = fineCarp.skillCheck(tpl.baseCombatRating, source, 0.0D, false, counter);
            if (power > 0) {
                try {
                    Item created = ItemFactory.createItem(CustomItems.stuffedCorpseId, (float) power, (byte) Math.max(performer.getRarity(), source.getRarity()), performer.getName());
                    if (created.getRarity() > 0)
                        performer.playPersonalSound("sound.fx.drumroll");
                    created.setData1(target.getData1());
                    created.setAuxData((byte) aux);
                    created.setFemale(target.female);

                    float scale = target.getSizeY() / target.getTemplate().getSizeY();

                    int color = 0;
                    try {
                        if (tpl.isBlackOrWhite || (boolean) ReflectionUtil.getPrivateField(tpl, ReflectionUtil.getField(CreatureTemplate.class, "isHorse"))) {
                            switch (target.getDescription()) {
                                case "brown":
                                    color = 1;
                                    break;
                                case "gold":
                                    color = 2;
                                    break;
                                case "black":
                                    color = 3;
                                    break;
                                case "white":
                                    color = 4;
                                    break;
                                case "piebaldPinto":
                                    color = 5;
                                    break;
                                case "bloodBay":
                                    color = 6;
                                    break;
                                case "ebonyBlack":
                                    color = 7;
                                    break;
                            }
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        TaxidermyMod.logException("Error getting isHorse", e);
                    }

                    created.setData2(target.getAuxData() | ((int) (scale * 20f) << 8) | (color << 16));
                    created.setName("preserved " + (target.getName().toLowerCase().replace("corpse of ", "").replace("The ", "")) + " body");
                    if (suffix.length() > 0)
                        created.setDescription(suffix);
                    created.setMaterial(source.getMaterial());
                    performer.getInventory().insertItem(created, true);
                    if (karmaCost > 0) {
                        performer.getCommunicator().sendNormalServerMessage(String.format("You channel your energy into the %s!", target.getName()));
                        if (performer.getPower() == 0)
                            performer.setKarma(performer.getKarma() - karmaCost);
                    }
                } catch (FailedException | NoSuchTemplateException e) {
                    TaxidermyMod.logException("Error preserving corpse", e);
                    comm.sendAlertServerMessage("Something went wrong, try again later or contact staff.");
                    return propagate(action, ActionPropagation.FINISH_ACTION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.NO_SERVER_PROPAGATION);
                }

                try {
                    target.getParent().dropItem(target.getWurmId(), false);
                    source.getParent().dropItem(source.getWurmId(), false);
                } catch (NoSuchItemException ignored) {
                }

                Items.destroyItem(target.getWurmId());
                Items.destroyItem(source.getWurmId());

                if (aux == 2 && tpl.getModelName().equals("model.creature.humanoid.human.player")) {
                    comm.sendNormalServerMessage(String.format("You manage to preserve the %s... but something isn't quite right!", target.getName()));
                } else {
                    comm.sendNormalServerMessage(String.format("You manage to preserve the %s in perfect condition!", target.getName()));
                }
            } else {
                if (target.setDamage((float) (target.getDamage() - power / 10))) {
                    comm.sendNormalServerMessage(String.format("You fail to preserve and destroy the %s!", target.getName()));
                } else {
                    comm.sendNormalServerMessage(String.format("You fail to preserve and damage the %s.", target.getName()));
                }
            }
            return propagate(action, ActionPropagation.FINISH_ACTION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.NO_SERVER_PROPAGATION);
        }
        return propagate(action, ActionPropagation.CONTINUE_ACTION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.NO_SERVER_PROPAGATION);
    }

}
