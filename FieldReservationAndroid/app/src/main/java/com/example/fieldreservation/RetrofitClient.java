package com.example.fieldreservation;

// TEST COMMENT 2 - Si vous voyez ce commentaire, les changements sont appliqués
import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    
    // Multiple options for connecting to your local server
    private static final String LOCALHOST_EMULATOR = "http://10.0.2.2:8080/";
    private static final String LOCALHOST_DIRECT = "http://localhost:8080/";
    private static final String LOCAL_IP = "http://192.168.11.200:8080/"; // Your actual IP address
    
    // Select which URL to use - try changing this if connection fails
    private static final String BASE_URL = LOCAL_IP;
    
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            try {
                // Créer un intercepteur de logging
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                // Configurer OkHttpClient avec des timeouts plus longs
                OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor())
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

                Log.d(TAG, "Initializing Retrofit with BASE_URL: " + BASE_URL);
                Log.d(TAG, "Available URLs:");
                Log.d(TAG, "Emulator URL: " + LOCALHOST_EMULATOR);
                Log.d(TAG, "Local URL: " + LOCALHOST_DIRECT);
                Log.d(TAG, "IP URL: " + LOCAL_IP);

                retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
                    
                Log.d(TAG, "Retrofit instance created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating Retrofit instance: " + e.getMessage(), e);
            }
        }
        return retrofit;
    }
}

class AuthInterceptor implements okhttp3.Interceptor {
    private static final String TAG = "AuthInterceptor";

    @Override
    public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
        okhttp3.Request original = chain.request();
        Log.d(TAG, "Making request to: " + original.url());
        Log.d(TAG, "Request method: " + original.method());
        Log.d(TAG, "Request headers: " + original.headers());
        
        okhttp3.Request request = original.newBuilder()
            .header("Content-Type", "application/json")
            .method(original.method(), original.body())
            .build();

        try {
            Log.d(TAG, "Executing request to: " + request.url());
            try {
                okhttp3.Response response = chain.proceed(request);
                Log.d(TAG, "Response received with code: " + response.code());
                Log.d(TAG, "Response headers: " + response.headers());
                return response;
            } catch (java.net.ConnectException e) {
                Log.e(TAG, "Connection failed: " + e.getMessage());
                throw e;
            } catch (java.net.SocketTimeoutException e) {
                Log.e(TAG, "Connection timed out: " + e.getMessage());
                throw e;
            } catch (Exception e) {
                Log.e(TAG, "Request failed: " + e.getMessage());
                throw e;
            }
            return response;
        } catch (Exception e) {
            Log.e(TAG, "Error during request: " + e.getMessage(), e);
            Log.e(TAG, "Error type: " + e.getClass().getName());
            if (e.getCause() != null) {
                Log.e(TAG, "Caused by: " + e.getCause().getMessage());
            }
            throw e;
        }
    }
}
