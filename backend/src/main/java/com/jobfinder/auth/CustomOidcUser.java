package com.jobfinder.auth;

import com.jobfinder.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Collection;
import java.util.List;

public class CustomOidcUser extends DefaultOidcUser implements AppUserPrincipal {

    private final User user;

    public CustomOidcUser(User user, OidcIdToken idToken, OidcUserInfo userInfo) {
        super(List.of(new SimpleGrantedAuthority("ROLE_USER")), idToken, userInfo);
        this.user = user;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return user.getEmail();
    }
}
