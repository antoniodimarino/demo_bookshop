package it.example.bookshop.user.service;

import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import it.example.bookshop.common.dto.AddressDTO;
import it.example.bookshop.common.dto.AuthRequest;
import it.example.bookshop.common.dto.AuthResponse;
import it.example.bookshop.common.dto.RegisterRequest;
import it.example.bookshop.common.dto.UserDTO;
import it.example.bookshop.user.service.dto.UserUpsert;
import it.example.bookshop.user.service.model.Role;
import it.example.bookshop.user.service.model.User;
import it.example.bookshop.user.service.service.PasswordService;
import it.example.bookshop.user.service.service.TokenService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    PasswordService passwordService;

    @Inject
    TokenService tokenService;

    @Inject
    SecurityIdentity securityIdentity;

    private UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        List<AddressDTO> addressDTOs = user.getAddresses().stream()
                .map(a -> new AddressDTO(a.id, a.label, a.street, a.city, a.country, a.zip, a.primaryAddress))
                .collect(Collectors.toList());

        return new UserDTO(user.id, user.email, user.firstName, user.lastName, user.getRole().name(), addressDTOs);
    }

    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterRequest request) {
        if (User.find("email", request.email()).firstResult() != null) {
            return Response.status(Response.Status.CONFLICT).entity("Email già in uso").build();
        }
        User user = new User();
        user.email = request.email();
        user.password = passwordService.hash(request.password());
        user.firstName = request.firstName();
        user.lastName = request.lastName();
        user.persist();
        return Response.status(Response.Status.CREATED).entity(toUserDTO(user)).build();
    }

    @POST
    @Path("/login")
    public Response login(AuthRequest request) {
        User user = User.find("email", request.email()).firstResult();
        if (user == null || !passwordService.check(request.password(), user.password)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Credenziali non valide").build();
        }
        String token = tokenService.generateToken(user);
        return Response.ok(new AuthResponse(token)).build();
    }

    @GET
    @Path("/me")
    @Authenticated // Richiede un token JWT valido
    public UserDTO getMe() {
        String email = securityIdentity.getPrincipal().getName();
        User user = User.find("email", email).firstResult();
        if (user == null) {
            throw new NotFoundException();
        }
        return toUserDTO(user);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public UserDTO getById(@PathParam("id") Long id) {
        User user = User.findById(id);
        if (user == null) {
            throw new NotFoundException();
        }
        return toUserDTO(user);
    }

    @GET
    @RolesAllowed("ADMIN")
    public List<UserDTO> list(@QueryParam("page") @DefaultValue("0") int page, @QueryParam("size") @DefaultValue("50") int size) {
        // Fetch a typed list of users
        List<User> userList = User.<User>findAll().page(page, size).list();

        // Map each User entity to a UserDTO using the existing helper method
        return userList.stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    @PUT
    @Transactional
    @Path("/{id}")
    public void update(@PathParam("id") Long id, UserUpsert req) {
        User u = User.findById(id);
        if (u == null) {
            throw new NotFoundException();
        }
        if (req.firstName() != null) {
            u.firstName = req.firstName();

        }
        if (req.lastName() != null) {
            u.lastName = req.lastName();

        }
        if (req.phone() != null) {
            u.phone = req.phone();

        }
        if (req.role() != null) {
            u.role = Role.valueOf(req.role());
        }
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        User u = User.findById(id);
        if (u != null) {
            u.delete();

        }
    }
}
