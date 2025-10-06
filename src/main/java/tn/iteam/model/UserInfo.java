package tn.iteam.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserInfo(
        String sub,
        String username,

        @JsonProperty("preferred_username")
        String preferredUsername,

        String email,

        @JsonProperty("email_verified")
        Boolean emailVerified,

        @JsonProperty("given_name")
        String givenName,

        @JsonProperty("family_name")
        String familyName,

        String name,
        String role
) {}
