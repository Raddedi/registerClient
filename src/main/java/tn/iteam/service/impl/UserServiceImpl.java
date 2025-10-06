package tn.iteam.service.impl;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.iteam.model.NewUserRecord;
import tn.iteam.service.UserService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${app.keycloak.realm}")
    private String realm;
    private final Keycloak keycloak;

    @Override
    public void createUser(NewUserRecord newUserRecord) {

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setFirstName(newUserRecord.firstName());
        userRepresentation.setLastName(newUserRecord.lastName());
        userRepresentation.setUsername(newUserRecord.username());
        userRepresentation.setEmail(newUserRecord.username());
        userRepresentation.setEmailVerified(false);

        // Ajouter le rôle comme attribut personnalisé
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("role", List.of(newUserRecord.role()));
        userRepresentation.setAttributes(attributes);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(newUserRecord.password());
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

        userRepresentation.setCredentials(List.of(credentialRepresentation));

        UsersResource usersResource = getUsersResource();

        Response response = usersResource.create(userRepresentation);

        log.info("Status Code " + response.getStatus());

        if (!Objects.equals(201, response.getStatus())) {
            throw new RuntimeException("Status code " + response.getStatus());
        }

        log.info("New user has been created");

        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(newUserRecord.username(), true);
        UserRepresentation userRepresentation1 = userRepresentations.get(0);

        sendVerificationEmail(userRepresentation1.getId());
    }

    @Override
    public void sendVerificationEmail(String userId) {
        UsersResource usersResource = getUsersResource();
        UserRepresentation user = usersResource.get(userId).toRepresentation();
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        usersResource.get(userId).sendVerifyEmail();

    }

    @Override
    public void deleteUser(String userId) {
        UsersResource usersResource = getUsersResource();
        usersResource.delete(userId);


    }

    @Override
    public void forgotPassword(String username) {
        UsersResource usersResource = getUsersResource();

        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(username, true);
        UserRepresentation userRepresentation1 = userRepresentations.get(0);

        UserResource userResource = usersResource.get(userRepresentation1.getId());

        userResource.executeActionsEmail(List.of("UPDATE_PASSWORD"));

    }

    @Override
    public UserResource getUser(String userId) {

        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);

    }
    @Override
    public UserRepresentation getUserById(String userId) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        return userResource.toRepresentation();
    }

    @Override
    public List<RoleRepresentation> getUserRols(String userId) {


        return getUser(userId).roles().realmLevel().listAll();
    }


    public UsersResource getUsersResource(){
        return keycloak.realm(realm).users();
    }
    @Override
    public List<UserRepresentation> getAllUsers() {
        UsersResource usersResource = getUsersResource();
        return usersResource.list();
    }
    @Override
    public List<UserRepresentation> getAllUsers(int firstResult, int maxResults, String search) {
        UsersResource usersResource = getUsersResource();

        // Ajouter les paramètres de pagination et recherche
        if (search != null && !search.isEmpty()) {
            return usersResource.search(search, firstResult, maxResults);
        } else {
            return usersResource.list(firstResult, maxResults);
        }
    }
    @Override
    public int getUsersCount(String search) {
        UsersResource usersResource = getUsersResource();
        if (search != null && !search.isEmpty()) {
            return usersResource.search(search).size();
        } else {
            return usersResource.count();
        }
    }
    @Override
    public void updateUser(String userId, NewUserRecord updateUserRecord) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        UserRepresentation user = userResource.toRepresentation();

        // Mise à jour des champs modifiables
        if (updateUserRecord.firstName() != null) {
            user.setFirstName(updateUserRecord.firstName());
        }
        if (updateUserRecord.lastName() != null) {
            user.setLastName(updateUserRecord.lastName());
        }
        if (updateUserRecord.username() != null) {
            user.setEmail(updateUserRecord.username());
        }
        if (updateUserRecord.username() != null) {
            user.setUsername(updateUserRecord.username());
        }

        userResource.update(user);
    }
    @Override
    public void updateCategorie(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            if (user == null) {
                throw new RuntimeException("Utilisateur non trouvé");
            }

            // Approche minimaliste
            UserRepresentation updateRequest = new UserRepresentation();
            updateRequest.singleAttribute("categorie", "java,angular"); // Alternative

            // Ou pour les listes :
            Map<String, List<String>> attrs = new HashMap<>();
            attrs.put("categorie", Arrays.asList("java", "angular"));
            updateRequest.setAttributes(attrs);

            userResource.update(updateRequest);

        } catch (BadRequestException e) {
            // Loguer le détail de l'erreur
            System.err.println("BadRequest detail: " + e.getResponse().readEntity(String.class));
            throw new RuntimeException("Requête invalide vers Keycloak: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur Keycloak: " + e.getMessage(), e);
        }
    }

}
