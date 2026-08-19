package com.example.fieldreservation;

import java.util.List;

import retrofit2.Call;

public class TerrainRepository {
    private TerrainApi terrainApi;

    public TerrainRepository() {
        terrainApi = RetrofitClient.getClient().create(TerrainApi.class);
    }

    public Call<List<Terrain>> getAllTerrains() {
        return terrainApi.getTerrains();
    }

    public Call<Terrain> addTerrain(Terrain terrain) {
        return terrainApi.addTerrain(terrain);
    }

    public Call<Terrain> updateTerrain(long id, Terrain terrain) {
        return terrainApi.updateTerrain(id, terrain);
    }

    public Call<Void> deleteTerrain(long id) {
        return terrainApi.deleteTerrain(id);

    }
}

