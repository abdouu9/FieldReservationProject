import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { Terrain } from '../models/terrain.model';

@Injectable({
  providedIn: 'root'
})
export class TerrainService {
  private apiUrl = 'http://localhost:8080/api/terrains';

  constructor(private http: HttpClient) { }

  private handleError(error: HttpErrorResponse) {
    console.error('API Error:', error);
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      console.error('Client error:', error.error.message);
    } else {
      // Server-side error
      console.error(`Server returned code ${error.status}, body was:`, error.error);
      console.error('Error details:', {
        status: error.status,
        statusText: error.statusText,
        url: error.url,
        headers: error.headers
      });
    }
    return throwError(() => new Error(`Error: ${error.status} - ${error.statusText}`));
  }

  getAllTerrains(): Observable<Terrain[]> {
    console.log('Fetching terrains from:', this.apiUrl);
    return this.http.get<Terrain[]>(this.apiUrl)
      .pipe(
        catchError(this.handleError)
      );
  }

  getTerrainById(id: number): Observable<Terrain> {
    const url = `${this.apiUrl}/${id}`;
    console.log('Fetching terrain from:', url);
    return this.http.get<Terrain>(url)
      .pipe(
        catchError(this.handleError)
      );
  }

  createTerrain(terrain: Terrain): Observable<Terrain> {
    return this.http.post<Terrain>(this.apiUrl, terrain)
      .pipe(
        catchError(this.handleError)
      );
  }

  updateTerrain(id: number, terrain: Terrain): Observable<Terrain> {
    return this.http.put<Terrain>(`${this.apiUrl}/${id}`, terrain)
      .pipe(
        catchError(this.handleError)
      );
  }

  deleteTerrain(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }
} 