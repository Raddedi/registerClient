package tn.iteam.service;


import java.util.List;

public interface RoleService {

    String assignRole(String userId, List<String> roleName);
    void deleteRoleFromUser(String userId,String roleName);

}
