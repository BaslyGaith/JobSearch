package com.jobfinder.auth;

import com.jobfinder.user.User;

/**
 * Implemented by every authenticated principal this application produces, so
 * controllers can reach the local {@link User} without caring whether the
 * provider used plain OAuth2 (Microsoft) or OpenID Connect (Google).
 */
public interface AppUserPrincipal {

    User getUser();
}
