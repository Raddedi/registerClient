package tn.iteam.service;

import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import tn.iteam.model.NewUserRecord;

import java.util.List;

public interface UserService {

    void createUser(NewUserRecord newUserRecord);
    void sendVerificationEmail(String userId);//vqlu fesx fgro bczs
    void deleteUser(String userId);
    void forgotPassword(String username);
    UserResource getUser(String userId);
    UserRepresentation getUserById(String userId);
    void updateUser(String userId, NewUserRecord updateUserRecord);
    List<RoleRepresentation> getUserRols(String userId);
    List<UserRepresentation> getAllUsers();
    List<UserRepresentation> getAllUsers(int firstResult, int maxResults, String search);
    int getUsersCount(String search);
    void updateCategorie(String userId);

}
