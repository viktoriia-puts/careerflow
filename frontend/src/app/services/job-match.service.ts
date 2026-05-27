import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JobMatchAnalysisRequest, JobMatchAnalysisResponse } from '../models/job-match.model';
import { RankedJobSearchResult } from '../models/ranked-job.model';

@Injectable({
  providedIn: 'root'
})
export class JobMatchService {
  private apiUrl = 'http://localhost:8081/api/job-matches';
  private rankedApiUrl = 'http://localhost:8081/api/job-search/ranked';

  constructor(private http: HttpClient) { }

  analyzeJobMatch(request: JobMatchAnalysisRequest): Observable<JobMatchAnalysisResponse> {
    return this.http.post<JobMatchAnalysisResponse>(`${this.apiUrl}/analyze`, request);
  }

  getRankedJobs(
    profileId: number,
    location: string,
    targetPerProvider: number = 25,
    jobLevel: string = 'JUNIOR'
  ): Observable<RankedJobSearchResult[]> {
    const params = new HttpParams()
      .set('profileId', profileId.toString())
      .set('location', location)
      .set('targetPerProvider', targetPerProvider.toString())
      .set('jobLevel', jobLevel);

    return this.http.get<RankedJobSearchResult[]>(this.rankedApiUrl, { params });
  }
}
