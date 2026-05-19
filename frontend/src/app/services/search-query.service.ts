import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SearchQueryGenerationResponse } from '../models/search-query-generation.model';

@Injectable({
  providedIn: 'root'
})
export class SearchQueryService {
  private apiUrl = 'http://localhost:8081/api/search-profiles';

  constructor(private http: HttpClient) { }

  generateQueries(profileId: number): Observable<SearchQueryGenerationResponse> {
    return this.http.post<SearchQueryGenerationResponse>(
      `${this.apiUrl}/${profileId}/generate-queries`,
      {}
    );
  }
}

