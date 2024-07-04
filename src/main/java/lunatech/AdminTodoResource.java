package lunatech;

import java.net.URI;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/admin/todos")
@RolesAllowed("admin")
@Consumes(MediaType.APPLICATION_JSON)
public class AdminTodoResource {
    private static final Logger logger = Logger.getLogger(TodoResource.class);

    @Inject
    Validator validator;

    @GET
    public Response todosAdmin(
            @QueryParam("tag") Set<String> tags) {

        Set<String> formattedTags = new HashSet<String>();
        
        for (String tag : tags) {
            formattedTags.add(stringWithoutCaseAndAccent(tag));
        }

        if (!formattedTags.isEmpty()) {
            return Response.ok(
                TodoEntity.list("{ tags: { $all: [?1] } }", formattedTags)   
            )
            .build();
        } else {
            return Response.ok(TodoEntity.listAll()).build();
        }
       
    }

    @GET
    @Path("/{id}")
    public Response todoAdmin(
            @PathParam("id") String id) {
        var todo = TodoEntity.findById(new ObjectId(id));

        if (todo == null) {
            logger.warn(String.format("Todo with id [%s] could not be retrieved because it does not exists", id));
            return Response.status(Response.Status.NO_CONTENT).entity("Todo does not exists").build();
        }

        return Response.ok(todo).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteTodoAdmin(
            @PathParam("id") String id) {
        var todoe = TodoEntity.findById(new ObjectId(id));
        if (todoe == null) {
            logger.warn(String.format("Todo with id [%s] could not be deleted because it already exists", id));
            return Response.status(Response.Status.BAD_REQUEST).entity("Todo already exists").build();
        }

        todoe.delete();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    public Response addTodoAdmin(
            TodoEntity todo) {
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

        todo.persist();
        var location = URI.create(String.format("/api/todos/%s", todo.id));
        return Response.created(location).entity(todo).build();
    }

    @PUT
    public Response updateTodoAdmin(
            TodoEntity todo) {
        var existingTodo = TodoEntity.findById(todo.id);

        if (existingTodo == null) {
            logger.warn(String.format("Todo with id [%s] could not be updated because it does not exists", todo.id));
            return Response.status(Response.Status.BAD_REQUEST).entity("Todo does not exists").build();
        }

        var violations = validator.validate(todo);
        if (!violations.isEmpty()) {
            var messages = violations.stream().map(ConstraintViolation::getMessage);
            return Response.status(Response.Status.BAD_REQUEST).entity(messages).build();
        }

        todo.update();
        return Response.ok(todo).build();
    }

    public String stringWithoutCaseAndAccent(String tag) {
        String formattedString = 
                    Normalizer
                        .normalize(tag, Normalizer.Form.NFD)
                        .replaceAll("[^\\p{ASCII}]", "")
                        .toLowerCase();
        return formattedString;
    }
}
