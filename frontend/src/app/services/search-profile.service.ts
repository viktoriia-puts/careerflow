import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SearchProfileCreateRequest, SearchProfileResponse } from '../models/search-profile.model';

@Injectable({
  providedIn: 'root'
})
export class SearchProfileService {
  private readonly apiUrl = 'http://localhost:8081/api/search-profiles';

  constructor(private http: HttpClient) { }

  /**
   * Create and save a search profile
   * @param request the search profile data to save
   * @returns Observable with the saved SearchProfileResponse including id
   */
  createSearchProfile(request: SearchProfileCreateRequest): Observable<SearchProfileResponse> {
    return this.http.post<SearchProfileResponse>(this.apiUrl, request);
  }

  getSearchProfiles(): Observable<SearchProfileResponse[]> {
    return this.http.get<SearchProfileResponse[]>(this.apiUrl);
  }

  getSearchProfile(id: number): Observable<SearchProfileResponse> {
    return this.http.get<SearchProfileResponse>(`${this.apiUrl}/${id}`);
  }

  deleteSearchProfile(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

