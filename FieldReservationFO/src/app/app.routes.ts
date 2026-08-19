import { Routes } from '@angular/router';
import { TerrainListComponent } from './components/terrain-list/terrain-list.component';
import { TerrainFormComponent } from './components/terrain-form/terrain-form.component';

export const routes: Routes = [
  { path: 'terrains', component: TerrainListComponent },
  { path: 'terrains/new', component: TerrainFormComponent },
  { path: 'terrains/edit/:id', component: TerrainFormComponent },
  { path: '', redirectTo: '/terrains', pathMatch: 'full' }
];
