package lunatech;

import io.quarkus.elytron.security.common.BcryptUtil;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.validation.constraints.*;

@MongoEntity(collection = "users")
public class UserEntity extends PanacheMongoEntity{
    
    @NotNull(message = "Name should be set")
    @Size(min = 3, max = 30, message="Title length should be between 3 and 30 characters")
    public String username;
  
    @NotNull(message = "Password should be set")
    @NotEmpty(message = "password must not be empty")
    public String password;

    @NotNull(message = "Role should be set")
    public Boolean isAdmin;

    public static void add(String username, String password, Boolean isAdmin) { 
        UserEntity user = new UserEntity();
        user.username = username;
        user.password = BcryptUtil.bcryptHash(password);
        user.isAdmin = isAdmin;
        user.persist();
    }

    public static UserEntity findByUsername(String username) {
        return find("username", username).firstResult();
    }
}

