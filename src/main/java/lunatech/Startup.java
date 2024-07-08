package lunatech;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import io.quarkus.runtime.StartupEvent;

@Singleton
public class Startup {
    @Transactional
    public void loadUsers(@Observes StartupEvent evt) {
        UserEntity.deleteAll();
        UserEntity.add("Ewen", "mj3Ed78GXtQ83g", false);
        UserEntity.add("Sébastien", "2UBks5AGpcj856", false);
        UserEntity.add("Nicolas", "2R76q7wXQCki3v", true);
    }
}