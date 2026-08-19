package com.example.fieldreservation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int VIEW_MY_TERRAINS = 1;
    private static final int VIEW_AVAILABLE_TERRAINS = 2;

    private RecyclerView recyclerView;
    private TerrainAdapter adapter;
    private Button btnAdd, btnLogout;
    private FirebaseAuth mAuth;
    private User currentUser;
    private FirebaseFirestore db;
    private TerrainApi terrainApi;
    private int currentViewMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        terrainApi = RetrofitClient.getClient().create(TerrainApi.class);

        // Check if user is logged in
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        }

        db.collection("users")
            .document(firebaseUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String role = documentSnapshot.getString("role");
                if (role == null || role.isEmpty()) {
                    role = "user";
                }
                currentUser = new User(firebaseUser.getUid(), firebaseUser.getEmail());
                currentUser.setRole(role);
                initializeUI();
            })
            .addOnFailureListener(e -> {
        currentUser = new User(firebaseUser.getUid(), firebaseUser.getEmail());
            currentUser.setRole("user");
                initializeUI();
            });
    }

    private void initializeUI() {

        recyclerView = findViewById(R.id.recyclerView);
        btnAdd = findViewById(R.id.btnAdd);
        btnLogout = findViewById(R.id.btnLogout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TerrainAdapter(new ArrayList<>(), new TerrainAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Terrain terrain) {
                    showTerrainForm(terrain);
            }

            @Override
            public void onDeleteClick(Terrain terrain) {
                    deleteTerrain(terrain.getId());
            }
        });
        recyclerView.setAdapter(adapter);

        if (currentUser != null) {
            if (currentUser.isManager()) {
                currentViewMode = VIEW_MY_TERRAINS;
                btnAdd.setVisibility(View.VISIBLE);
                adapter.setManagementEnabled(true);
            } else {
                currentViewMode = VIEW_AVAILABLE_TERRAINS;
                btnAdd.setVisibility(View.GONE);
                adapter.setManagementEnabled(false);
            }
        }

        btnAdd.setOnClickListener(view -> showTerrainForm(null));
        btnLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });

        updateUIForUserRole();
        getTerrains();
    }

            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleApiError(int errorCode, String errorMessage) {
        String finalMessage;
        
        switch (errorCode) {
            case 401:
                finalMessage = "Authentication required";
                break;
            case 403:
                finalMessage = "Access denied";
                break;
            case 404:
                finalMessage = "Resource not found";
                break;
            case 500:
                finalMessage = "Server error";
                break;
            default:
                finalMessage = errorMessage;
        }
        
        Toast.makeText(this, finalMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "API Error: " + finalMessage);
    }

    private void updateUIForUserRole() {
        Log.d(TAG, "Updating UI for user role. Is manager: " + currentUser.isManager() + 
              ", View mode: " + currentViewMode);
        if (currentUser.isManager() && currentViewMode == VIEW_MY_TERRAINS) {
            Log.d(TAG, "Showing management options for manager");
            btnAdd.setVisibility(View.VISIBLE);
            adapter.setManagementEnabled(true);
        } else {
            Log.d(TAG, "Hiding management options");
            btnAdd.setVisibility(View.GONE);
            adapter.setManagementEnabled(false);
        }
    }

    private void getTerrains() {
        if (currentViewMode == VIEW_MY_TERRAINS && currentUser.isManager()) {
            terrainApi.getManagerTerrains(currentUser.getUid()).enqueue(new Callback<List<Terrain>>() {
                @Override
                public void onResponse(Call<List<Terrain>> call, Response<List<Terrain>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.updateData(response.body());
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to load terrains", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Terrain>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            terrainApi.getTerrains().enqueue(new Callback<List<Terrain>>() {
                @Override
                public void onResponse(Call<List<Terrain>> call, Response<List<Terrain>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.updateData(response.body());
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to load terrains", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Terrain>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showTerrainForm(Terrain terrain) {
        Intent intent = new Intent(this, TerrainFormActivity.class);
        if (terrain != null) {
            intent.putExtra("terrain", terrain);
        }
        startActivityForResult(intent, 1);
    }

    private void deleteTerrain(String terrainId) {
        terrainApi.deleteTerrain(terrainId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Terrain deleted successfully", Toast.LENGTH_SHORT).show();
                    getTerrains();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to delete terrain", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTerrain(Terrain terrain) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        
        terrainApi.addTerrain(terrain).enqueue(new Callback<Terrain>() {
            @Override
            public void onResponse(Call<Terrain> call, Response<Terrain> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this, "Terrain added successfully", Toast.LENGTH_SHORT).show();
                    getTerrains();
                } else {
                    handleApiError(response.code(), "Failed to add terrain");
                }
            }

            @Override
            public void onFailure(Call<Terrain> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                handleApiError(0, "Network error: " + t.getMessage());
            }
        });
    }

    private void updateTerrain(Terrain terrain) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        
        terrainApi.updateTerrain(terrain.getId(), terrain).enqueue(new Callback<Terrain>() {
            @Override
            public void onResponse(Call<Terrain> call, Response<Terrain> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Log.d(TAG, "updateTerrain: Successfully updated");
                    getTerrains();
                } else {
                    handleApiError(response.code(), "Failed to update terrain");
                    Log.e(TAG, "updateTerrain: Error - Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Terrain> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "updateTerrain: Failure - " + t.getMessage(), t);
                handleApiError(0, "Network error: " + t.getMessage());
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            getTerrains();
        }
    }
}
package com.example.fieldreservation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int VIEW_MY_TERRAINS = 1;
    private static final int VIEW_AVAILABLE_TERRAINS = 2;

    private RecyclerView recyclerView;
    private TerrainAdapter adapter;
    private Button btnAdd, btnLogout;
    private FirebaseAuth mAuth;
    private User currentUser;
    private FirebaseFirestore db;
    private TerrainApi terrainApi;
    private int currentViewMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        terrainApi = RetrofitClient.getClient().create(TerrainApi.class);

        // Check if user is logged in
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
                        return;
                    }

        // Get user data from Firestore
        db.collection("users")
            .document(firebaseUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String role = documentSnapshot.getString("role");
                if (role == null || role.isEmpty()) {
                    role = "user";
                }
                currentUser = new User(firebaseUser.getUid(), firebaseUser.getEmail());
                currentUser.setRole(role);
                initializeUI();
            })
            .addOnFailureListener(e -> {
                currentUser = new User(firebaseUser.getUid(), firebaseUser.getEmail());
                currentUser.setRole("user");
                initializeUI();
            });
    }

    private void initializeUI() {
        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        btnAdd = findViewById(R.id.btnAdd);
        btnLogout = findViewById(R.id.btnLogout);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TerrainAdapter(new ArrayList<>(), new TerrainAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Terrain terrain) {
                showTerrainForm(terrain);
            }

            @Override
            public void onDeleteClick(Terrain terrain) {
                deleteTerrain(terrain.getId());
            }
        });
        recyclerView.setAdapter(adapter);

        // Setup UI based on user role
        if (currentUser != null) {
            if (currentUser.isManager()) {
                currentViewMode = VIEW_MY_TERRAINS;
                btnAdd.setVisibility(View.VISIBLE);
                adapter.setManagementEnabled(true);
            } else {
                currentViewMode = VIEW_AVAILABLE_TERRAINS;
                btnAdd.setVisibility(View.GONE);
                adapter.setManagementEnabled(false);
            }
        }

        // Set button click listeners
        btnAdd.setOnClickListener(view -> showTerrainForm(null));
        btnLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });

        updateUIForUserRole();
        getTerrains();
    }

    private void handleApiError(int errorCode, String errorMessage) {
        String finalMessage;
        
        switch (errorCode) {
            case 401:
                finalMessage = "Authentication required";
                break;
            case 403:
                finalMessage = "Access denied";
                break;
            case 404:
                finalMessage = "Resource not found";
                break;
            case 500:
                finalMessage = "Server error";
                break;
            default:
                finalMessage = errorMessage;
        }
        
        Toast.makeText(this, finalMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "API Error: " + finalMessage);
    }

    private void updateUIForUserRole() {
        Log.d(TAG, "Updating UI for user role. Is manager: " + currentUser.isManager() + 
              ", View mode: " + currentViewMode);
        if (currentUser.isManager() && currentViewMode == VIEW_MY_TERRAINS) {
            Log.d(TAG, "Showing management options for manager");
            btnAdd.setVisibility(View.VISIBLE);
            adapter.setManagementEnabled(true);
        } else {
            Log.d(TAG, "Hiding management options");
            btnAdd.setVisibility(View.GONE);
            adapter.setManagementEnabled(false);
        }
    }

    private void getTerrains() {
        if (currentViewMode == VIEW_MY_TERRAINS && currentUser.isManager()) {
            terrainApi.getManagerTerrains(currentUser.getUid()).enqueue(new Callback<List<Terrain>>() {
                @Override
                public void onResponse(Call<List<Terrain>> call, Response<List<Terrain>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.updateData(response.body());
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to load terrains", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Terrain>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            terrainApi.getTerrains().enqueue(new Callback<List<Terrain>>() {
                @Override
                public void onResponse(Call<List<Terrain>> call, Response<List<Terrain>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.updateData(response.body());
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to load terrains", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Terrain>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showTerrainForm(Terrain terrain) {
        Intent intent = new Intent(this, TerrainFormActivity.class);
        if (terrain != null) {
            intent.putExtra("terrain", terrain);
        }
        startActivityForResult(intent, 1);
    }

    private void deleteTerrain(String terrainId) {
        terrainApi.deleteTerrain(terrainId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Terrain deleted successfully", Toast.LENGTH_SHORT).show();
                    getTerrains();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to delete terrain", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTerrain(Terrain terrain) {
        terrainApi.addTerrain(terrain).enqueue(new Callback<Terrain>() {
            @Override
            public void onResponse(Call<Terrain> call, Response<Terrain> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Terrain added successfully", Toast.LENGTH_SHORT).show();
                    getTerrains();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to add terrain", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Terrain> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTerrain(Terrain terrain) {
        terrainApi.updateTerrain(terrain.getId(), terrain).enqueue(new Callback<Terrain>() {
            @Override
            public void onResponse(Call<Terrain> call, Response<Terrain> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Terrain updated successfully", Toast.LENGTH_SHORT).show();
                    getTerrains();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to update terrain", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Terrain> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Terrain terrain = (Terrain) data.getSerializableExtra("terrain");
            if (terrain.getId() == null || terrain.getId().isEmpty()) {
                addTerrain(terrain);
            } else {
                updateTerrain(terrain);
            }
        }
    }
}package com.example.fieldreservation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int VIEW_MY_TERRAINS = 1;
    private static final int VIEW_AVAILABLE_TERRAINS = 2;

    private RecyclerView recyclerView;
    private TerrainAdapter adapter;
    private Button btnAdd, btnLogout;
    private FirebaseAuth mAuth;
    private User currentUser;
    private FirebaseFirestore db;
    private TerrainApi terrainApi;
    private int currentViewMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        terrainApi = RetrofitClient.getClient().create(TerrainApi.class);

        // Check if user is logged in
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        }

        // Get user data from Firestore
        db.collection("users")
            .document(firebaseUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String role = documentSnapshot.getString("role");
                if (role == null || role.isEmpty()) {
                    role = "user";
                }
                currentUser = new User(firebaseUser.getUid(), firebaseUser.getEmail());
                currentUser.setRole(role);
                initializeUI();
            })
            .addOnFailureListener(e -> {
                currentUser = new User(firebaseUser.getUid(), firebaseUser.getEmail());
                currentUser.setRole("user");
                initializeUI();
            });
    }

    private void initializeUI() {
        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        btnAdd = findViewById(R.id.btnAdd);
        btnLogout = findViewById(R.id.btnLogout);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TerrainAdapter(new ArrayList<>(), new TerrainAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Terrain terrain) {
                showTerrainForm(terrain);
            }

            @Override
            public void onDeleteClick(Terrain terrain) {
                deleteTerrain(terrain.getId());
            }
        });
        recyclerView.setAdapter(adapter);

        // Setup UI based on user role
        if (currentUser != null) {
            if (currentUser.isManager()) {
                currentViewMode = VIEW_MY_TERRAINS;
                btnAdd.setVisibility(View.VISIBLE);
                adapter.setManagementEnabled(true);
            } else {
                currentViewMode = VIEW_AVAILABLE_TERRAINS;
                btnAdd.setVisibility(View.GONE);
                adapter.setManagementEnabled(false);
            }
        }

        // Set button click listeners
        btnAdd.setOnClickListener(view -> showTerrainForm(null));
        btnLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });

        updateUIForUserRole();
        getTerrains();
    }

    private void handleApiError(int errorCode, String errorMessage) {
        String finalMessage;
        
        switch (errorCode) {
            case 401:
                finalMessage = "Authentication required";
                break;
            case 403:
                finalMessage = "Access denied";
                break;
            case 404:
                finalMessage = "Resource not found";
                break;
            case 500:
                finalMessage = "Server error";
                break;
            default:
                finalMessage = errorMessage;
        }
        
        Toast.makeText(this, finalMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "API Error: " + finalMessage);
    }

    private void updateUIForUserRole() {
        Log.d(TAG, "Updating UI for user role. Is manager: " + currentUser.isManager() + 
              ", View mode: " + currentViewMode);
        if (currentUser.isManager() && currentViewMode == VIEW_MY_TERRAINS) {
            Log.d(TAG, "Showing management options for manager");
            btnAdd.setVisibility(View.VISIBLE);
            adapter.setManagementEnabled(true);
        } else {
            Log.d(TAG, "Hiding management options");
            btnAdd.setVisibility(View.GONE);
            adapter.setManagementEnabled(false);
        }
    }

    private void getTerrains() {
        if (currentViewMode == VIEW_MY_TERRAINS && currentUser.isManager()) {
            terrainApi.getManagerTerrains(currentUser.getUid()).enqueue(new Callback<List<Terrain>>() {
                @Override
                public void onResponse(Call<List<Terrain>> call, Response<List<Terrain>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.updateData(response.body());
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to load terrains", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Terrain>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            terrainApi.getTerrains().enqueue(new Callback<List<Terrain>>() {
                @Override
                public void onResponse(Call<List<Terrain>> call, Response<List<Terrain>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.updateData(response.body());
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to load terrains", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Terrain>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showTerrainForm(Terrain terrain) {
        Intent intent = new Intent(this, TerrainFormActivity.class);
        if (terrain != null) {
            intent.putExtra("terrain", terrain);
        }
        startActivityForResult(intent, 1);
    }

    private void deleteTerrain(String terrainId) {
        terrainApi.deleteTerrain(terrainId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Terrain deleted successfully", Toast.LENGTH_SHORT).show();
                    getTerrains();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to delete terrain", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTerrain(Terrain terrain) {
        terrainApi.addTerrain(terrain).enqueue(new Callback<Terrain>() {
            @Override
            public void onResponse(Call<Terrain> call, Response<Terrain> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Terrain added successfully", Toast.LENGTH_SHORT).show();
                    getTerrains();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to add terrain", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Terrain> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTerrain(Terrain terrain) {
        terrainApi.updateTerrain(terrain.getId(), terrain).enqueue(new Callback<Terrain>() {
            @Override
            public void onResponse(Call<Terrain> call, Response<Terrain> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Terrain updated successfully", Toast.LENGTH_SHORT).show();
                    getTerrains();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to update terrain", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Terrain> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Terrain terrain = (Terrain) data.getSerializableExtra("terrain");
            if (terrain.getId() == null || terrain.getId().isEmpty()) {
                addTerrain(terrain);
            } else {
                updateTerrain(terrain);
            }
        }
    }
}