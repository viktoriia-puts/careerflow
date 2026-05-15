import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CvAnalysisRequest, CvAnalysisResponse } from '../models/cv-analysis.model';

@Injectable({
  providedIn: 'root'
})
export class CvAnalysisService {
  private readonly apiUrl = 'http://localhost:8081/api/cv/analyze';

  constructor(private http: HttpClient) { }

  /**
   * Sends CV text to the backend for analysis
   * @param cvText the CV text to analyze
   * @returns Observable<CvAnalysisResponse> with the analysis results
   */
  analyzeCv(cvText: string): Observable<CvAnalysisResponse> {
    const request: CvAnalysisRequest = { cvText };
    return this.http.post<CvAnalysisResponse>(this.apiUrl, request);
  }
}

