package org.dimasik.liteauction.api.events;

import java.lang.reflect.Method;
import java.util.*;
import org.bukkit.plugin.java.JavaPlugin;

public class EventManager {
    private final Map<JavaPlugin, Set<Object>> listeners = new HashMap<>();
    private final Map<Class<? extends Event>, Map<Object, List<Method>>> handlerCache = new HashMap<>();

    public void register(JavaPlugin plugin, Object listener) {
        if (plugin == null || !plugin.isEnabled()) {
            return;
        }

        if (!listener.getClass().isAnnotationPresent(Subscribed.class)) {
            throw new IllegalArgumentException("Listener must be annotated with @Subscribed");
        }

        unregister(plugin, listener);
        listeners.computeIfAbsent(plugin, k -> new HashSet<>()).add(listener);
        buildHandlerCache(listener);
    }

    public void unregister(JavaPlugin plugin) {
        if (plugin == null) return;

        Set<Object> pluginListeners = listeners.remove(plugin);
        if (pluginListeners != null) {
            for (Object listener : pluginListeners) {
                removeFromHandlerCache(listener);
            }
        }
    }

    public void unregister(JavaPlugin plugin, Object listener) {
        if (plugin == null) return;

        Set<Object> pluginListeners = listeners.get(plugin);
        if (pluginListeners != null) {
            pluginListeners.remove(listener);
            removeFromHandlerCache(listener);
        }
    }

    public void triggerEvent(Event event) {
        Class<? extends Event> eventClass = event.getClass();
        Map<Object, List<Method>> handlers = handlerCache.get(eventClass);

        if (handlers != null) {
            for (Map.Entry<Object, List<Method>> entry : handlers.entrySet()) {
                Object listener = entry.getKey();
                JavaPlugin plugin = getPluginForListener(listener);

                if (plugin == null || !plugin.isEnabled()) {
                    continue;
                }

                for (Method method : entry.getValue()) {
                    try {
                        method.invoke(listener, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private JavaPlugin getPluginForListener(Object listener) {
        for (Map.Entry<JavaPlugin, Set<Object>> entry : listeners.entrySet()) {
            if (entry.getValue().contains(listener)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void buildHandlerCache(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 1 && Event.class.isAssignableFrom(parameters[0])) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) parameters[0];

                    handlerCache.computeIfAbsent(eventClass, k -> new HashMap<>())
                            .computeIfAbsent(listener, k -> new ArrayList<>())
                            .add(method);
                }
            }
        }
    }

    private void removeFromHandlerCache(Object listener) {
        for (Map<Object, List<Method>> handlers : handlerCache.values()) {
            handlers.remove(listener);
        }
    }
}