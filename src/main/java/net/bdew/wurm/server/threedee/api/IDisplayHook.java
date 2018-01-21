package net.bdew.wurm.server.threedee.api;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.Item;

public interface IDisplayHook {
    /**
     * Handle sending item to client
     *
     * @param comm Client communicator
     * @param item Item to send
     * @param x    x position override
     * @param y    y position override
     * @param z    z position override
     * @param rot  rotation override
     * @return true if handled sending, false if it should be sent normally
     */
    boolean addItem(Communicator comm, Item item, float x, float y, float z, float rot);

    /**
     * Handle sending remove item to client
     *
     * @param comm Client communicator
     * @param item Item to remove
     * @return true if handled removing, false if it should be removed normally
     */
    boolean removeItem(Communicator comm, Item item);
}
