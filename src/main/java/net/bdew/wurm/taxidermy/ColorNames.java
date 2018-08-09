package net.bdew.wurm.taxidermy;

import java.util.HashMap;
import java.util.Map;

public class ColorNames {
    private static class CreatureMapping {
        private final Map<String, Integer> corpseToId = new HashMap<>();
        private final Map<Integer, String> idToCorpse = new HashMap<>();
        private final Map<Integer, String> idToLiving = new HashMap<>();
    }

    private static Map<Integer, CreatureMapping> creatures = new HashMap<>();

    public static void addMapping(int template, int colorId, String corpse, String living) {
        if (!creatures.containsKey(template))
            creatures.put(template, new CreatureMapping());
        CreatureMapping mapping = creatures.get(template);
        mapping.corpseToId.put(corpse, colorId);
        mapping.idToCorpse.put(colorId, corpse.replace(" ", ""));
        mapping.idToLiving.put(colorId, living);
    }

    public static String getLivingName(int template, int colorId) {
        if (creatures.containsKey(template))
            return creatures.get(template).idToLiving.getOrDefault(colorId, "") + ".";
        else return "";
    }

    public static String getCorpseName(int template, int colorId) {
        if (creatures.containsKey(template))
            return creatures.get(template).idToCorpse.getOrDefault(colorId, "") + ".";
        else return "";
    }


    public static int getIdFromCorpse(int template, String name) {
        if (creatures.containsKey(template))
            return creatures.get(template).corpseToId.getOrDefault(name, 0);
        else return 0;
    }

    public static boolean hasColors(int template) {
        return creatures.containsKey(template);
    }
}
