package net.pistonmaster.encryptedchat.util;

import net.pistonmaster.encryptedchat.packet.Packet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BusHelper {
    public static void handlePacket(Packet packet, Object bus) {
        for (Method declaredMethod : bus.getClass().getDeclaredMethods()) {
            if (declaredMethod.getParameterCount() != 1) {
                continue;
            }

            Class<?> parameter = declaredMethod.getParameterTypes()[0];
            if (parameter.isAssignableFrom(packet.getClass())) {
                try {
                    declaredMethod.invoke(bus, packet);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
