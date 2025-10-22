package it.example.bookshop.api.gateway.clients;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import it.example.bookshop.common.dto.AuthRequest;
import it.example.bookshop.common.dto.AuthResponse;
import it.example.bookshop.common.dto.RegisterRequest;
import it.example.bookshop.common.dto.UserDTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "usr")
@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UserClient {

    @POST
    @Path("/register")
    Response register(RegisterRequest registerRequest);

    @POST
    @Path("/login")
    AuthResponse login(AuthRequest authRequest);

    @GET
    @Path("/me")
    UserDTO getMe(@HeaderParam("Authorization") String authorizationHeader);

    @GET
    List<UserDTO> list(@QueryParam("page") int page, @QueryParam("size") int size, @HeaderParam("Authorization") String authorizationHeader);

    @GET
    @Path("/{id}")
    UserDTO get(@PathParam("id") String id, @HeaderParam("Authorization") String authorizationHeader);

    /*
    @POST
    String create(String userJson);
    */
}
