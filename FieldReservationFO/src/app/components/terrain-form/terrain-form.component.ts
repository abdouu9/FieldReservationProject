import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TerrainService } from '../../services/terrain.service';
import { Terrain } from '../../models/terrain.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-terrain-form',
  templateUrl: './terrain-form.component.html',
  styleUrls: ['./terrain-form.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class TerrainFormComponent implements OnInit {
  terrainForm: FormGroup;
  isEditMode = false;
  terrainId?: number;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    public router: Router,
    private terrainService: TerrainService
  ) {
    this.terrainForm = this.fb.group({
      name: ['', Validators.required],
      location: ['', Validators.required],
      price: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.terrainId = +params['id'];
        this.loadTerrain();
      }
    });
  }

  loadTerrain(): void {
    if (this.terrainId) {
      this.loading = true;
      this.terrainService.getTerrainById(this.terrainId).subscribe({
        next: (terrain) => {
          this.terrainForm.patchValue(terrain);
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Error loading terrain';
          this.loading = false;
          console.error(err);
        }
      });
    }
  }

  onSubmit(): void {
    if (this.terrainForm.valid) {
      this.loading = true;
      const terrainData = this.terrainForm.value;

      if (this.isEditMode && this.terrainId) {
        this.terrainService.updateTerrain(this.terrainId, terrainData).subscribe({
          next: () => {
            this.router.navigate(['/terrains']);
          },
          error: (err) => {
            this.error = 'Error updating terrain';
            this.loading = false;
            console.error(err);
          }
        });
      } else {
        this.terrainService.createTerrain(terrainData).subscribe({
          next: () => {
            this.router.navigate(['/terrains']);
          },
          error: (err) => {
            this.error = 'Error creating terrain';
            this.loading = false;
            console.error(err);
          }
        });
      }
    }
  }
}
