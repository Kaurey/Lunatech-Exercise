package lunatech;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * CRUD for TodoEntity
 */
@Path("/api/todos")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
public class TodoResource {
    private static final Logger logger = Logger.getLogger(TodoResource.class);

    @Inject
    Validator validator;

    @GET
    public Response todos(
            @QueryParam("tag") Set<String> tags,
            @Context SecurityContext securityContext) {

        String username = securityContext.getUserPrincipal().getName();
        UserEntity user = UserEntity.findByUsername(username);

        Set<String> formattedTags = new HashSet<String>();
        
        for (String tag : tags) {
            formattedTags.add(FormattedTags.tagsWithoutCaseAndAccent(tag));
        }

        if (!formattedTags.isEmpty()) {
            return Response.ok(
                TodoEntity.list("{ $and: [ { tags: { $all: [?1] } }, { idUser: { $eq: ?2} } ] }", 
                                        formattedTags, user.id.toString())   
            )
            .build();
        } else {
            return Response.ok(
                TodoEntity.list("{ idUser: { $eq: ?1 } }", user.id.toString())
            )
            .build();
        }
       
    }

    @DELETE
    @Path("/{id}")
    public Response deleteTodo(
            @PathParam("id") String id,
            @Context SecurityContext securityContext) {

        TodoEntity todoe = TodoEntity.findById(new ObjectId(id));

        if (todoe == null) {
            logger.warn(String.format("Todo with id [%s] could not be deleted because it does not exists", id));
            return Response.status(Response.Status.NOT_FOUND).entity("Todo does not exists").build();
        }

        String username = securityContext.getUserPrincipal().getName();
        UserEntity user = UserEntity.findByUsername(username);

        if (!user.id.toString().equals(todoe.idUser)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("You can't delete a someone else's todo").build();
        }

        todoe.delete();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    public Response addTodo(
            TodoEntity todo,
            @Context SecurityContext securityContext) {
        var existingTodo = TodoEntity.findById(todo.id);

        if (existingTodo != null) {
            logger.warn(String.format("Todo with id [%s] could not be created because it already exists", todo.id));
            return Response.status(Response.Status.BAD_REQUEST).entity("Todo already exists").build();
        }

        var violations = validator.validate(todo);
        if (!violations.isEmpty()) {
            var messages = violations.stream().map(ConstraintViolation::getMessage);
            return Response.status(Response.Status.BAD_REQUEST).entity(messages).build();
        }

        String username = securityContext.getUserPrincipal().getName();
        UserEntity user = UserEntity.findByUsername(username);

        todo.setIdUser(user.id.toString());
        todo.persist();
        var location = URI.create(String.format("/api/todos/%s", todo.id));
        return Response.created(location).entity(todo).build();
    }

    @PUT
    public Response updateTodo(
            TodoEntity todo,
            @Context SecurityContext securityContext) {

        var existingTodo = TodoEntity.findById(todo.id);

        if (existingTodo == null) {
            logger.warn(String.format("Todo with id [%s] could not be updated because it does not exists", todo.id));
            return Response.status(Response.Status.NOT_FOUND).entity("Todo does not exists").build();
        }

        String username = securityContext.getUserPrincipal().getName();
        UserEntity user = UserEntity.findByUsername(username);

        if (!user.id.toString().equals(todo.idUser)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("You can't modify a someone else's todo").build();
        }

        var violations = validator.validate(todo);
        if (!violations.isEmpty()) {
            var messages = violations.stream().map(ConstraintViolation::getMessage);
            return Response.status(Response.Status.BAD_REQUEST).entity(messages).build();
        }

        todo.update();
        return Response.ok(todo).build();
    }
}
