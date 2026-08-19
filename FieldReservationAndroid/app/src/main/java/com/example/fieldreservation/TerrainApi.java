package com.example.fieldreservation;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface TerrainApi {
    @GET("api/terrains")
    Call<List<Terrain>> getTerrains();

    @GET("api/terrains/manager/{managerId}")
    Call<List<Terrain>> getManagerTerrains(@Path("managerId") String managerId);

    @POST("api/terrains")
    Call<Terrain> addTerrain(@Body Terrain terrain);

    @PUT("api/terrains/{id}")
    Call<Terrain> updateTerrain(@Path("id") Long id, @Body Terrain terrain);

    @DELETE("api/terrains/{id}")
    Call<Void> deleteTerrain(@Path("id") Long id);
}

