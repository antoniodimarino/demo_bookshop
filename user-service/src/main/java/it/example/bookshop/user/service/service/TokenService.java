package it.example.bookshop.user.service.service;

import io.smallrye.jwt.build.Jwt;
import it.example.bookshop.user.service.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    public String generateToken(User user) {
        Set<String> roles = new HashSet<>();
        roles.add(user.role.name());

        return Jwt.issuer("bookshop-user-service")
                .upn(user.email)
                .groups(roles)
                .claim("userId", user.id)
                .sign();
    }
}