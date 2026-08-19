import  { Component, OnInit } from '@angular/core';
import { TerrainService } from '../../services/terrain.service';
import { Terrain } from '../../models/terrain.model';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-terrain-list',
  templateUrl: './terrain-list.component.html',
  styleUrls: ['./terrain-list.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class TerrainListComponent implements OnInit {
  terrains: Terrain[] = [];
  loading = true;
  error = '';

  constructor(
    private terrainService: TerrainService,
    public router: Router
  ) { }

  ngOnInit(): void {
    console.log('TerrainListComponent initialized');
    this.loadTerrains();
  }

  loadTerrains(): void {
    console.log('Loading terrains...');
    this.terrainService.getAllTerrains().subscribe({
      next: (data) => {
        console.log('Terrains loaded:', data);
        this.terrains = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading terrains:', err);
        this.error = 'Error loading terrains: ' + err.message;
        this.loading = false;
      }
    });
  }

  editTerrain(id: number): void {
    this.router.navigate(['/terrains/edit', id]);
  }

  deleteTerrain(id: number): void {
    if (confirm('Are you sure you want to delete this terrain?')) {
      this.terrainService.deleteTerrain(id).subscribe({
        next: () => {
          this.terrains = this.terrains.filter(t => t.id !== id);
        },
        error: (err) => {
          console.error('Error deleting terrain:', err);
          this.error = 'Error deleting terrain: ' + err.message;
        }
      });
    }
  }
}
