package com.jobfinder.auth;

import java.util.Map;

public class MicrosoftOAuth2UserInfo extends OAuth2UserInfo {

    public MicrosoftOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        String name = (String) attributes.get("name");
        if (name != null) return name;
        String first = getFirstName();
        String last = getLastName();
        if (first != null && last != null) return first + " " + last;
        return getEmail();
    }

    @Override
    public String getEmail() {
        String email = (String) attributes.get("email");
        if (email == null) email = (String) attributes.get("preferred_username");
        return email;
    }

    @Override
    public String getFirstName() {
        return (String) attributes.get("given_name");
    }

    @Override
    public String getLastName() {
        return (String) attributes.get("family_name");
    }

    @Override
    public String getImageUrl() {
        return null; // MS Graph photo requires separate API call
    }

    @Override
    public String getProvider() {
        return "MICROSOFT";
    }
}
