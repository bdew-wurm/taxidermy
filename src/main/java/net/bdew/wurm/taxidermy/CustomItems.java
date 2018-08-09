package net.bdew.wurm.taxidermy;

import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.items.*;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.shared.constants.ItemMaterials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.items.ModItems;
import org.gotti.wurmunlimited.modsupport.items.ModelNameProvider;

import java.io.IOException;

public class CustomItems {
    public static ItemTemplate taxidermyKit, stuffedCorpse;
    public static int taxidermyKitId, stuffedCorpseId;
    public static ModelNameProvider corpseModelNameProvider;

    public static void registerCorpse() throws IOException {
        stuffedCorpse = new ItemTemplateBuilder("bdew.taxidermy.body")
                .name("preserved body", "preserved bodies", "A body of a creature that was preserved using a taxidermy kit.")
                .imageNumber((short) 40)
                .weightGrams(20000)
                .dimensions(100, 100, 100)
                .decayTime(Long.MAX_VALUE)
                .primarySkill(TaxidermyMod.preserveSkill)
                .difficulty(30)
                .value(1000)
                .itemTypes(new short[]{
                        ItemTypes.ITEM_TYPE_WOOD,
                        ItemTypes.ITEM_TYPE_HASDATA,
                        ItemTypes.ITEM_TYPE_PLANTABLE,
                        ItemTypes.ITEM_TYPE_DECORATION,
                        ItemTypes.ITEM_TYPE_NAMED,
                        ItemTypes.ITEM_TYPE_NORENAME,
                        ItemTypes.ITEM_TYPE_REPAIRABLE,
                        ItemTypes.ITEM_TYPE_TURNABLE,
                        ItemTypes.ITEM_TYPE_COLORABLE
                })
                .material(ItemMaterials.MATERIAL_WOOD_BIRCH)
                .modelName("model.corpse.")
                .behaviourType((short) 1)
                .build();

        stuffedCorpseId = stuffedCorpse.getTemplateId();

        corpseModelNameProvider = item -> {
            try {
                int tplId = item.getData1();
                int colorId = (item.getData2() >> 16) & 0x0F;

                CreatureTemplate tpl = CreatureTemplateFactory.getInstance().getTemplate(tplId);

                String colorText = ColorNames.getLivingName(tplId, colorId);
                if (!colorText.isEmpty()) colorText += ".";

                String model = "model.corpse.";
                switch (item.getAuxData()) {
                    case 0:
                        model += tpl.getCorpsename() + ColorNames.getCorpseName(tplId, colorId);
                        break;
                    case 1:
                        model += tpl.getCorpsename() + ColorNames.getCorpseName(tplId, colorId) + "butchered.";
                        break;
                    case 2:
                        if (tpl.isBlackOrWhite) {
                            model = tpl.getModelName();
                            if (item.female) {
                                model += ".female";
                            } else if (item.getAuxData() == 2) {
                                model += ".male";
                            }
                            model += "." + ColorNames.getLivingName(tplId, colorId);
                        } else {
                            model = tpl.getModelName() + "." + ColorNames.getLivingName(tplId, colorId);
                            if (model.equals("model.creature.humanoid.human.player."))
                                model = "model.creature.humanoid.human.player.zombie.";
                        }
                        break;
                }

                if (!tpl.isBlackOrWhite || item.getAuxData() != 2) {
                    if (item.female) {
                        model += "female.";
                    } else if (item.getAuxData() == 2) {
                        model += "male.";
                    }
                }

                byte kingdom = (byte) (item.getData2() & 0xFF);

                Kingdom k = Kingdoms.getKingdom(kingdom);
                if (k != null && k.getTemplate() != kingdom) {
                    model += (Kingdoms.getSuffixFor(k.getTemplate()));
                }

                model += Kingdoms.getSuffixFor(kingdom);
                return model;
            } catch (NoSuchCreatureTemplateException e) {
                return "model.corpse.";
            }
        };

        ModItems.addModelNameProvider(stuffedCorpseId, corpseModelNameProvider);
    }

    public static void registerKit() throws IOException {
        taxidermyKit = new ItemTemplateBuilder("bdew.taxidermy.kit")
                .name("taxidermy kit", "taxidermy kits", "A kit that can be used to preserve the body of a dead creature.")
                .imageNumber((short) 244)
                .weightGrams(500)
                .dimensions(20, 20, 30)
                .decayTime(3024000L)
                .difficulty(50)
                .value(1000)
                .primarySkill(SkillList.CARPENTRY_FINE)
                .itemTypes(new short[]{
                        ItemTypes.ITEM_TYPE_WOOD,
                        ItemTypes.ITEM_TYPE_REPAIRABLE,
                })
                .material(ItemMaterials.MATERIAL_WOOD_BIRCH)
                .modelName("model.container.chest.small.")
                .behaviourType((short) 1)
                .build();

        taxidermyKitId = taxidermyKit.getTemplateId();
    }

    public static void registerRecipes() {
        CreationEntryCreator.createAdvancedEntry(TaxidermyMod.preserveSkill, ItemList.shaft, ItemList.clothYard, taxidermyKitId, false, false, 0.0f, true, false, CreationCategories.TOOLS)
                .addRequirement(new CreationRequirement(1, ItemList.clothYard, 10, true))
                .addRequirement(new CreationRequirement(2, ItemList.shaft, 5, true))
                .addRequirement(new CreationRequirement(3, ItemList.scrapwood, 20, true));

    }
}
