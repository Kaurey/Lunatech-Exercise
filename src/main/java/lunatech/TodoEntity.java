package lunatech;

import java.util.Set;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.validation.constraints.*;

@MongoEntity(collection = "todos")
public class TodoEntity extends PanacheMongoEntity {

    /**
     * Title of the todo
     * 
     * @example "Brush my teeth"
     */
    @NotNull(message = "Title should be set")
    @Size(min = 3, max = 50, message = "Title length should be between 3 and 50 characters")
    public String title;

    /**
     * List of tags
     * 
     * @example [ "healthcare", "work", "sport" ]
     */
    @NotNull(message = "Tags should be set")
    @NotEmpty(message = "At least one tag should be specified")
    public Set<@Pattern(regexp = "[a-z]+", message = "Tags should be only in minuscule without accents") 
               @NotNull String> tags;

    /**
     * True if the task is done, false otherwise
     * 
     * @example true
     */
    @NotNull
    public Boolean done;

    public String idUser;

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }
}
