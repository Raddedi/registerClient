package tn.iteam.api;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.iteam.model.NewUserRecord;
import tn.iteam.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UsersApi {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody NewUserRecord newUserRecord) {

        userService.createUser(newUserRecord);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PostMapping("/client")
    public ResponseEntity<?> createClient(@RequestBody NewUserRecord newUserRecord) {

        userService.createUser(newUserRecord);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}/send-verification-email")
    public ResponseEntity<?> sendVerificationEmail(@PathVariable String id) {

        userService.sendVerificationEmail(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {

        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @PutMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String username) {

        userService.forgotPassword(username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @PutMapping("/{userId}/roles")
    public ResponseEntity<?> getUserRoles(@PathVariable String userId) {

            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserRols(userId));
    }
    /*@GetMapping
    public ResponseEntity<List<UserRepresentation>> getAllUsers() {
        List<UserRepresentation> users = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }*/
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        int firstResult = page * size;
        List<UserRepresentation> users = userService.getAllUsers(firstResult, size, search);

        // Si vous pouvez obtenir le nombre total d'utilisateurs (cela dépend de votre implémentation Keycloak)
        int totalUsers = userService.getUsersCount(search);
        int totalPages = (int) Math.ceil((double) totalUsers / size);

        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("currentPage", page);
        response.put("totalItems", totalUsers);
        response.put("totalPages", totalPages);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /*@GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        UserResource user = userService.getUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }*/
    @GetMapping("/{userId}")
    public ResponseEntity<UserRepresentation> getUserById(@PathVariable String userId) {
        UserRepresentation user = userService.getUserById(userId);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable String userId,@RequestBody NewUserRecord updateUserRecord) {
        userService.updateUser(userId, updateUserRecord);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @PutMapping("/{userId}/categorie")
    public ResponseEntity<String> updateCategorie(@PathVariable String userId) {
        userService.updateCategorie(userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
