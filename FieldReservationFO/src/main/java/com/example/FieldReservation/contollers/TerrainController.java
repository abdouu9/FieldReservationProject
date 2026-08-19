package com.example.FieldReservation.contollers;

import com.example.FieldReservation.entities.Terrain;
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

    @GetMapping
    public List<Terrain> getAllTerrains() {
        return terrainService.getAllTerrains();
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
    public Terrain addTerrain(@RequestBody Terrain terrain) {
        return terrainService.addTerrain(terrain);
    }

    @PutMapping("/{id}")
    public Terrain updateTerrain(@PathVariable Long id, @RequestBody Terrain terrain) {
        return terrainService.updateTerrain(id, terrain);
    }

    @DeleteMapping("/{id}")
    public void deleteTerrain(@PathVariable Long id) {
        terrainService.deleteTerrain(id);
    }
} 