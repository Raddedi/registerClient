package tn.iteam.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.iteam.service.RoleService;
import tn.iteam.service.UserService;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final UserService userService;

    @Value("${app.keycloak.realm}")
    private String realm;
    private final Keycloak keycloak;
    @Override
    public String assignRole(String userId, List<String> roles) {
        UsersResource usersResource = keycloak.realm(realm).users();
        UserResource userResource = usersResource.get(userId);
        List<RoleRepresentation> currentRoles = userResource.roles().realmLevel().listAll()
                .stream().filter(role -> !role.getName().equals("default-roles-bancassurance")).toList();

        if (!currentRoles.isEmpty()) {
            userResource.roles().realmLevel().remove(currentRoles);
        }

        List<RoleRepresentation> rolesToAdd = keycloak.realm(realm).roles().list().stream()
                .filter(r -> roles.contains(r.getName()))
                .toList();

        if (!rolesToAdd.isEmpty()) {
            userResource.roles().realmLevel().add(rolesToAdd);
            return "Roles updated successfully: ";
        } else {
            return "No valid roles selected.";
        }

    }

    @Override
    public void deleteRoleFromUser(String userId, String roleName) {
        UserResource user =userService.getUser(userId);

        RolesResource rolesResource= getRolesResourece();
        RoleRepresentation roleRepresentation = rolesResource.get(roleName).toRepresentation();

        user.roles().realmLevel().remove(Collections.singletonList(roleRepresentation));

    }

    private RolesResource getRolesResourece(){

        return keycloak.realm(realm).roles();
    }
}
