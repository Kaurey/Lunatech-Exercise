package lunatech;

import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BasicAuthenticator implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request,
                                              AuthenticationRequestContext authenticationRequestContext) {
        String username = request.getUsername();
        String password = new String(request.getPassword().getPassword());

        UserEntity user = UserEntity.findByUsername(username);

        if (user != null && BcryptUtil.matches(password, user.password)) {
            String role = "";

            if (user.isAdmin) {
                role = "admin";
            } else {
                role = "user";
            }

            return Uni.createFrom().item(QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal(request.getUsername()))
                .addCredential(request.getPassword())
                .setAnonymous(false)
                .addRole(role)
                .build());
        }

        throw new AuthenticationFailedException("password invalid or user not found");
    }
}