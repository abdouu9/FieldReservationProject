package com.example.FieldReservation.contollers;
import com.example.FieldReservation.entities.User;
import com.example.FieldReservation.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserRepository repo;

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody User user) {
        try {
            // Vérifier si l'utilisateur existe déjà
            if (repo.existsById(user.getUid())) {
                return ResponseEntity.badRequest().build();
            }

            // Sauvegarder le nouvel utilisateur
            repo.save(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<User> syncUser(@RequestHeader("Authorization") String authHeader)
            throws Exception {
        String idToken = authHeader.replace("Bearer ","");
        FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid   = decoded.getUid();
        String email = decoded.getEmail();
        String name  = decoded.getName();

        User u = repo.findById(uid)
                .orElseGet(() -> repo.save(new User(uid, email, name)));
        return ResponseEntity.ok(u);
    }
}