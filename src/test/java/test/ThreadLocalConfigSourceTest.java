package test;

import org.eclipse.microprofile.config.ConfigProvider;
import org.github.t1.ThreadLocalConfigSource;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.BDDAssertions.then;

class ThreadLocalConfigSourceTest {
    String key = UUID.randomUUID().toString();

    @Test
    void shouldOverwriteSystemPropertyConfig() {
        System.setProperty(key, "sys-prop");

        ThreadLocalConfigSource.set(key, "thread-local-config");

        then(configValue()).isEqualTo("thread-local-config");
    }

    @Test
    void shouldNotLeakToOtherThread() throws Throwable {
        System.setProperty(key, "sys-prop");

        ThreadLocalConfigSource.set(key, "outer-thread");

        var thread = new Thread(() -> {
            then(configValue()).isEqualTo("sys-prop");
            ThreadLocalConfigSource.set(key, "inner-thread");
            then(configValue()).isEqualTo("inner-thread");
        });
        var exception = new AtomicReference<Throwable>();
        thread.setUncaughtExceptionHandler((t, e) -> exception.set(e));
        thread.start();
        thread.join(100);

        then(configValue()).isEqualTo("outer-thread");
        if (exception.get() != null) throw exception.get();
    }

    @Test
    void shouldClearConfig() {
        System.setProperty(key, "sys-prop");
        ThreadLocalConfigSource.set(key, "thread-local-config");

        ThreadLocalConfigSource.remove(key);

        then(configValue()).isEqualTo("sys-prop");
    }

    @Test
    void shouldRunWith() {
        System.setProperty(key, "sys-prop");

        ThreadLocalConfigSource.with(key, "thread-local-config", () ->
                then(configValue()).isEqualTo("thread-local-config"));

        then(configValue()).isEqualTo("sys-prop");
    }

    @Test
    void shouldReturnPreviousConfig() {
        ThreadLocalConfigSource.set(key, "old");

        var oldValue = ThreadLocalConfigSource.set(key, "new");

        then(oldValue).isEqualTo("old");
        then(configValue()).isEqualTo("new");
    }

    private String configValue() {
        return ConfigProvider.getConfig().getValue(key, String.class);
    }
}
