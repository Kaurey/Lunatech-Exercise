package lunatech;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.validation.constraints.*;

@MongoEntity(collection = "users")
public class UserEntity extends PanacheMongoEntity{
    
    @NotNull(message = "Name should be set")
    @Size(min = 3, max = 30, message="Title length should be between 3 and 30 characters")
    public String username;
  
    @NotNull(message = "Password should be set")
    public String password;

    @NotNull(message = "Role should be set")
    public String role;

    public static UserEntity findByUsername(String username) {
        return find("username", username).firstResult();
    }
}

