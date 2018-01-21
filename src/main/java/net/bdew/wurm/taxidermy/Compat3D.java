package net.bdew.wurm.taxidermy;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.Item;
import net.bdew.wurm.server.threedee.api.IDisplayHook;

import java.lang.reflect.InvocationTargetException;

public class Compat3D {
    static void installDisplayHook() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            Class.forName("net.bdew.wurm.server.threedee.api.DisplayHookRegistry")
                    .getMethod("add", int.class, IDisplayHook.class)
                    .invoke(null, CustomItems.stuffedCorpseId, new IDisplayHook() {
                                @Override
                                public boolean addItem(Communicator comm, Item item, float x, float y, float z, float rot) {
                                    if (item.getAuxData() == 2) {
                                        Animated.sendAnimated(comm, item, x, y, z, rot);
                                        return true;
                                    } else return false;
                                }

                                @Override
                                public boolean removeItem(Communicator comm, Item item) {
                                    if (item.getAuxData() == 2) {
                                        Animated.removeAnimated(comm, item);
                                        return true;
                                    } else return false;
                                }
                            }

                    );
            TaxidermyMod.logInfo("3D Stuff mod loaded - added compatibility hook");
        } catch (ClassNotFoundException e) {
            TaxidermyMod.logInfo("3D Stuff mod doesn't seem to be loaded");
        }
    }
}
