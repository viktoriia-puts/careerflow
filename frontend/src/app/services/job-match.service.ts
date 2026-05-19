import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JobMatchAnalysisRequest, JobMatchAnalysisResponse } from '../models/job-match.model';

@Injectable({
  providedIn: 'root'
})
export class JobMatchService {
  private apiUrl = 'http://localhost:8081/api/job-matches';

  constructor(private http: HttpClient) { }

  analyzeJobMatch(request: JobMatchAnalysisRequest): Observable<JobMatchAnalysisResponse> {
	return this.http.post<JobMatchAnalysisResponse>(`${this.apiUrl}/analyze`, request);
  }
}


