package com.example.fieldreservation;





import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserApi {
    @POST("api/users/sync")
    Call<User> syncUser();  // token is injected by interceptor
    @POST("/api/users")
    Call<Void> createUser(@Body User user);
}


