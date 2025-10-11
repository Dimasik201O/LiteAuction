package org.dimasik.liteauction.backend.utils.nms;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

@UtilityClass
public class NBTUtil {
    public static String getNBTAsString(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return "";
        }

        try {
            Class<?> craftItemStackClass = VersionUtil.getCraftItemStackClass();
            Method asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Object nmsItem = asNMSCopyMethod.invoke(null, itemStack);

            Method getTagMethod = nmsItem.getClass().getMethod("getTag");
            Object tag = getTagMethod.invoke(nmsItem);

            if (tag == null) {
                return "";
            }
            return tag.toString();
        } catch (Exception e) { return ""; }
    }
}