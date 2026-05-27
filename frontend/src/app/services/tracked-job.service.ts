import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  TrackedJobCreateRequest,
  TrackedJobResponse,
  TrackedJobUpdateRequest
} from '../models/tracked-job.model';

@Injectable({
  providedIn: 'root'
})
export class TrackedJobService {
  private apiUrl = 'http://localhost:8081/api/tracked-jobs';

  constructor(private http: HttpClient) { }

  getTrackedJobs(profileId: number | null = null): Observable<TrackedJobResponse[]> {
    let params = new HttpParams();

    if (profileId !== null) {
      params = params.set('profileId', profileId.toString());
    }

    return this.http.get<TrackedJobResponse[]>(this.apiUrl, { params });
  }

  createTrackedJob(request: TrackedJobCreateRequest): Observable<TrackedJobResponse> {
    return this.http.post<TrackedJobResponse>(this.apiUrl, request);
  }

  updateTrackedJob(
    id: number,
    request: TrackedJobUpdateRequest
  ): Observable<TrackedJobResponse> {
    return this.http.put<TrackedJobResponse>(`${this.apiUrl}/${id}`, request);
  }

  deleteTrackedJob(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
