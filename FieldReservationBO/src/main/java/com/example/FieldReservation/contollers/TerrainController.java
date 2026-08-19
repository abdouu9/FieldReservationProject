package com.example.FieldReservation.contollers;

import com.example.FieldReservation.entities.Terrain;
import com.example.FieldReservation.entities.User;
import com.example.FieldReservation.repositories.UserRepository;
import com.example.FieldReservation.services.TerrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/terrains")
public class TerrainController {
    @Autowired
    private TerrainService terrainService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Terrain> getAllTerrains() {
        return terrainService.getAllTerrains();
    }

    @GetMapping("/manager/{managerId}")
    public List<Terrain> getManagerTerrains(@PathVariable String managerId) {
        return terrainService.getTerrainsByManagerId(managerId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Terrain> getTerrainById(@PathVariable Long id) {
        Terrain terrain = terrainService.getTerrainById(id);
        if (terrain != null) {
            return ResponseEntity.ok(terrain);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Terrain> addTerrain(@RequestBody Terrain terrain, @RequestHeader("Authorization") String authHeader) {
        try {
            String uid = authHeader.replace("Bearer ", "");
            User user = userRepository.findById(uid).orElse(null);
            
            if (user != null && "manager".equals(user.getRole())) {
                terrain.setManagerId(uid);
                return ResponseEntity.ok(terrainService.addTerrain(terrain));
            } else {
                return ResponseEntity.status(403).build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Terrain> updateTerrain(@PathVariable Long id, @RequestBody Terrain terrain, @RequestHeader("Authorization") String authHeader) {
        try {
            String uid = authHeader.replace("Bearer ", "");
            Terrain existingTerrain = terrainService.getTerrainById(id);
            
            if (existingTerrain != null && uid.equals(existingTerrain.getManagerId())) {
                return ResponseEntity.ok(terrainService.updateTerrain(id, terrain));
            } else {
                return ResponseEntity.status(403).build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTerrain(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            String uid = authHeader.replace("Bearer ", "");
            Terrain terrain = terrainService.getTerrainById(id);
            
            if (terrain != null && uid.equals(terrain.getManagerId())) {
                terrainService.deleteTerrain(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(403).build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}