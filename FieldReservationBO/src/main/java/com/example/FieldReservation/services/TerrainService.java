package com.example.FieldReservation.services;

import com.example.FieldReservation.entities.Terrain;
import com.example.FieldReservation.repositories.TerrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TerrainService {
    @Autowired
    private TerrainRepository terrainRepository;

    public List<Terrain> getAllTerrains() {
        return terrainRepository.findAll();
    }

    public List<Terrain> getTerrainsByManagerId(String managerId) {
        return terrainRepository.findByManagerId(managerId);
    }

    public Terrain getTerrainById(Long id) {
        return terrainRepository.findById(id)
                .orElse(null);
    }

    public Terrain addTerrain(Terrain terrain) {
        return terrainRepository.save(terrain);
    }

    public Terrain updateTerrain(Long id, Terrain newTerrain) {
        return terrainRepository.findById(id)
                .map(terrain -> {
                    terrain.setName(newTerrain.getName());
                    terrain.setLocation(newTerrain.getLocation());
                    terrain.setPrice(newTerrain.getPrice());
                    return terrainRepository.save(terrain);
                }).orElseThrow(() -> new RuntimeException("Terrain not found"));
    }

    public void deleteTerrain(Long id) {
        terrainRepository.deleteById(id);
    }
}