package tn.iteam.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record LoginResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Long expiresIn,

        List<String> roles,

        @JsonProperty("user_info")
        UserInfo userInfo,

        String error
) {
        // Constructeur pour les réponses réussies
        public LoginResponse(String accessToken, String refreshToken, String tokenType, Long expiresIn, List<String> roles, UserInfo userInfo) {
                this(accessToken, refreshToken, tokenType, expiresIn, roles, userInfo, null);
        }

        // Constructeur pour les erreurs
        public LoginResponse(String error) {
                this(null, null, null, null, null, null, error);
        }
}