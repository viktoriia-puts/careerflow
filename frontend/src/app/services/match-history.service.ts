import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  MatchHistoryRunDetail,
  MatchHistoryRunSummary
} from '../models/match-history.model';

@Injectable({
  providedIn: 'root'
})
export class MatchHistoryService {
  private readonly apiUrl = 'http://localhost:8081/api/job-search/match-history';

  constructor(private http: HttpClient) { }

  getRuns(profileId: number): Observable<MatchHistoryRunSummary[]> {
    const params = new HttpParams().set('profileId', profileId.toString());
    return this.http.get<MatchHistoryRunSummary[]>(this.apiUrl, { params });
  }

  getRun(profileId: number, runId: number): Observable<MatchHistoryRunDetail> {
    const params = new HttpParams().set('profileId', profileId.toString());
    return this.http.get<MatchHistoryRunDetail>(`${this.apiUrl}/${runId}`, { params });
  }

  deleteResult(profileId: number, resultId: number): Observable<void> {
    const params = new HttpParams().set('profileId', profileId.toString());
    return this.http.delete<void>(`${this.apiUrl}/results/${resultId}`, { params });
  }
}
