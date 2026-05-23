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
  private rankedApiUrl = 'http://localhost:8081/api/job-search/test/arbeitnow/ranked';

  constructor(private http: HttpClient) { }

  analyzeJobMatch(request: JobMatchAnalysisRequest): Observable<JobMatchAnalysisResponse> {
    return this.http.post<JobMatchAnalysisResponse>(`${this.apiUrl}/analyze`, request);
  }

  getRankedArbeitnowJobs(
    profileId: number,
    location: string,
    target: number = 10
  ): Observable<RankedJobSearchResult[]> {
    const params = new HttpParams()
      .set('profileId', profileId.toString())
      .set('location', location)
      .set('target', target.toString());

    return this.http.get<RankedJobSearchResult[]>(this.rankedApiUrl, { params });
  }
}
