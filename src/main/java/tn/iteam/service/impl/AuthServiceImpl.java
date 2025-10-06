package tn.iteam.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tn.iteam.model.LoginRequest;
import tn.iteam.model.LoginResponse;
import tn.iteam.model.UserInfo;
import tn.iteam.service.AuthService;

import java.util.*;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Value("${app.keycloak.realm}")
    private String realm;

    @Value("${app.keycloak.serverUrl}")
    private String serverUrl;

    @Value("${app.keycloak.admin.clientId}")
    private String clientId;

    @Value("${app.keycloak.admin.clientSecret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isEmpty()) {
            body.add("client_secret", clientSecret);
        }
        body.add("username", loginRequest.username());
        body.add("password", loginRequest.password());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            String accessToken = (String) responseBody.get("access_token");

            // Décoder le JWT pour extraire les rôles et informations utilisateur
            List<String> roles = extractRolesFromToken(accessToken);
            UserInfo userInfo = extractUserInfoFromToken(accessToken);

            return new LoginResponse(
                    accessToken,
                    (String) responseBody.get("refresh_token"),
                    (String) responseBody.get("token_type"),
                    ((Number) responseBody.get("expires_in")).longValue(),
                    roles,
                    userInfo
            );

        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.username(), e);
            throw new RuntimeException("Authentication failed", e);
        }
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isEmpty()) {
            body.add("client_secret", clientSecret);
        }
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            String accessToken = (String) responseBody.get("access_token");

            // Décoder le JWT pour extraire les rôles et informations utilisateur
            List<String> roles = extractRolesFromToken(accessToken);
            UserInfo userInfo = extractUserInfoFromToken(accessToken);

            return new LoginResponse(
                    accessToken,
                    (String) responseBody.get("refresh_token"),
                    (String) responseBody.get("token_type"),
                    ((Number) responseBody.get("expires_in")).longValue(),
                    roles,
                    userInfo
            );

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new RuntimeException("Token refresh failed", e);
        }
    }

    @Override
    public void logout(String refreshToken) {
        String logoutUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isEmpty()) {
            body.add("client_secret", clientSecret);
        }
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(
                    logoutUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            log.info("User logged out successfully");
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw new RuntimeException("Logout failed", e);
        }
    }

    /**
     * Extrait les rôles du JWT token
     */
    private List<String> extractRolesFromToken(String accessToken) {
        try {
            Map<String, Object> claims = decodeJwtPayload(accessToken);
            List<String> allRoles = new ArrayList<>();

            // Extraire les rôles du realm
            Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> realmRoles = (List<String>) realmAccess.get("roles");
                allRoles.addAll(realmRoles);
            }

            // Extraire les rôles des clients
            Map<String, Object> resourceAccess = (Map<String, Object>) claims.get("resource_access");
            if (resourceAccess != null) {
                for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                    Map<String, Object> clientAccess = (Map<String, Object>) entry.getValue();
                    if (clientAccess != null && clientAccess.containsKey("roles")) {
                        List<String> clientRoles = (List<String>) clientAccess.get("roles");
                        allRoles.addAll(clientRoles);
                    }
                }
            }

            return allRoles.stream().distinct().toList();
        } catch (Exception e) {
            log.error("Failed to extract roles from token", e);
            return new ArrayList<>();
        }
    }

    /**
     * Extrait les informations utilisateur du JWT token
     */
    private UserInfo extractUserInfoFromToken(String accessToken) {
        try {
            Map<String, Object> claims = decodeJwtPayload(accessToken);

            return new UserInfo(
                    (String) claims.get("sub"),
                    (String) claims.get("username"),
                    (String) claims.get("preferred_username"),
                    (String) claims.get("email"),
                    (Boolean) claims.get("email_verified"),
                    (String) claims.get("given_name"),
                    (String) claims.get("family_name"),
                    (String) claims.get("name"),
                    (String) claims.get("role")
            );
        } catch (Exception e) {
            log.error("Failed to extract user info from token", e);
            return null;
        }
    }

    /**
     * Décode le payload d'un JWT token
     */
    private Map<String, Object> decodeJwtPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }

            String payload = parts[1];
            byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            return objectMapper.readValue(decodedPayload, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to decode JWT payload", e);
            throw new RuntimeException("Failed to decode JWT token", e);
        }
    }
}