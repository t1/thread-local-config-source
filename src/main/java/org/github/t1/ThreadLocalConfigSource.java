package org.github.t1;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * A simple config source for <a href="https://microprofile.io/project/eclipse/microprofile-config">Microprofile Config</a>,
 * which keeps the config in a {@link ThreadLocal} variable.
 * You may need this, e.g., for tests running in parallel.
 * <p>
 * The ordinal of this config source is {@value #ORDINAL}, so it's picked up before system properties,
 * which are ordinal 400.
 *
 * @see #set(String, String)
 * @see #remove(String)
 * @see #with(String, String, Runnable)
 */
public class ThreadLocalConfigSource implements ConfigSource {
    /**
     * Run the {@code runnable} with that {@code key} and {@code value}, and cleanup thereafter.
     */
    public static void with(String key, String value, Runnable runnable) {
        set(key, value);
        try {
            runnable.run();
        } finally {
            remove(key);
        }
    }

    /**
     * Set that config {@code key} and {@code value}. Return the previous value, if there is one.
     */
    public static String set(String key, String value) {return CONFIGS.get().put(key, value);}

    /**
     * Remove that config {@code key}. Return the previous value, if there is one.
     */
    public static String remove(String key) {return CONFIGS.get().remove(key);}


    private static final int ORDINAL = 500;

    private static final ThreadLocal<Map<String, String>> CONFIGS = ThreadLocal.withInitial(LinkedHashMap::new);

    @Override public String getName() {
        return "thread-local-config-source for thread [" + Thread.currentThread().getName() + "]";
    }

    @Override public int getOrdinal() {
        return ORDINAL;
    }

    @Override public Map<String, String> getProperties() {
        return unmodifiableMap(CONFIGS.get());
    }

    @Override public Set<String> getPropertyNames() {
        return unmodifiableSet(CONFIGS.get().keySet());
    }

    @Override public String getValue(String propertyName) {
        return CONFIGS.get().get(propertyName);
    }
}
