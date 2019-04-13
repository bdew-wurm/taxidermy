package net.bdew.wurm.taxidermy;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.IdFactory;
import org.gotti.wurmunlimited.modsupport.IdType;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.gotti.wurmunlimited.modsupport.creatures.CreatureTemplateParser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TaxidermyMod implements WurmServerMod, Initable, PreInitable, Configurable, ItemTemplatesCreatedListener, ServerStartedListener {
    private static final Logger logger = Logger.getLogger("TaxidermyMod");

    public static void logException(String msg, Throwable e) {
        if (logger != null)
            logger.log(Level.SEVERE, msg, e);
    }

    public static void logWarning(String msg) {
        if (logger != null)
            logger.log(Level.WARNING, msg);
    }

    public static void logInfo(String msg) {
        if (logger != null)
            logger.log(Level.INFO, msg);
    }

    private boolean addRecipes = true;
    private int normalCost = 0;
    private int animatedCost = 0;
    static int preserveSkill = 0;
    private Map<String, String> colorMappings;

    @Override
    public void configure(Properties properties) {
        addRecipes = Boolean.parseBoolean(properties.getProperty("addRecipes", "true"));
        normalCost = Integer.parseInt(properties.getProperty("normalCost", "0"), 10);
        animatedCost = Integer.parseInt(properties.getProperty("animatedCost", "0"), 10);
        preserveSkill = Integer.parseInt(properties.getProperty("preserveSkill", "10044"), 10);

        colorMappings = properties.stringPropertyNames().stream()
                .filter(x -> x.startsWith("color@"))
                .collect(Collectors.toMap(Function.identity(), properties::getProperty));
    }

    @Override
    public void preInit() {
        if (addRecipes) ModActions.init();
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();

            classPool.getCtClass("com.wurmonline.server.items.Item")
                    .getMethod("getSizeMod", "()F")
                    .insertAfter("$_ = $_ * net.bdew.wurm.taxidermy.Hooks.sizeMod(this);");

            classPool.getCtClass("com.wurmonline.server.behaviours.MethodsItems")
                    .getMethod("getImproveSkill", "(Lcom/wurmonline/server/items/Item;)I")
                    .insertAfter("if ($_ <= 0) $_ = net.bdew.wurm.taxidermy.Hooks.getImproveSkill($1);");

            CtClass ctCommunicator = classPool.getCtClass("com.wurmonline.server.creatures.Communicator");
            ctCommunicator.getMethod("sendItem", "(Lcom/wurmonline/server/items/Item;JZ)V")
                    .insertBefore("if (!net.bdew.wurm.taxidermy.Hooks.sendItemHook(this, $1)) return;");
            ctCommunicator.getMethod("sendRemoveItem", "(Lcom/wurmonline/server/items/Item;)V")
                    .insertAfter("net.bdew.wurm.taxidermy.Hooks.removeItemHook(this, $1);");


        } catch (CannotCompileException | NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void onItemTemplatesCreated() {
        try {
            CustomItems.registerCorpse();
            if (addRecipes) {
                CustomItems.registerKit();
                CustomItems.registerRecipes();
            }
            Compat3D.installDisplayHook();
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onServerStarted() {
        CreatureTemplateParser cidParser = new CreatureTemplateParser() {
            @Override
            protected int unparsable(String name) {
                return IdFactory.getIdFor(name, IdType.CREATURETEMPLATE);
            }
        };

        colorMappings.forEach((key, valz) -> {
            String[] keyparts = key.split("@");
            String[] valparts = valz.split(":");
            if (keyparts.length != 3 || valparts.length != 2) {
                logWarning(String.format("Invalid color mapping: %s => %s", Arrays.toString(keyparts), Arrays.toString(valparts)));
            } else {
                int tpl = cidParser.parse(keyparts[1]);
                int col;
                if (keyparts[2].equals("*"))
                    col = -1;
                else
                    col = Integer.parseInt(keyparts[2], 10);
                logInfo(String.format("Adding color mapping for %d: color=%d corpse=%s living=%s", tpl, col, valparts[0], valparts[1]));
                ColorNames.addMapping(tpl, col, valparts[0], valparts[1]);
            }
        });

        ModActions.registerActionPerformer(new ExaminePerformer());
        if (addRecipes) {
            ModActions.registerBehaviourProvider(new PreserveBehaviourProvider(normalCost, animatedCost));
        }
    }
}
