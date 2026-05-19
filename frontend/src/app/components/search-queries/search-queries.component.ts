import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SearchQueryStateService } from '../../services/search-query-state.service';
import { CvAnalysisResponse } from '../../models/cv-analysis.model';
import { SearchQueryGenerationResponse } from '../../models/search-query-generation.model';
// ...existing imports...

@Component({
  selector: 'app-search-queries',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-queries.component.html',
  styleUrls: ['./search-queries.component.css']
})
export class SearchQueriesComponent {
  cleanedAnalysis = signal<CvAnalysisResponse | null>(null);
  generatedQueries = signal<SearchQueryGenerationResponse | null>(null);

  // Temporary input signals for adding new queries
  newRoleTitleQuery = signal('');
  newRequirementBasedQuery = signal('');
  newAlternativeDirectionQuery = signal('');
  // ...existing signals...

  constructor(private state: SearchQueryStateService, private router: Router) {
    this.cleanedAnalysis.set(this.state.getCleanedAnalysis());
    this.generatedQueries.set(this.state.getGeneratedQueries());
  }

  // Remove methods
  removeRoleTitleQuery(query: string) {
    const current = this.generatedQueries();
    if (!current) return;
    const updated = {
      ...current,
      roleTitleQueries: current.roleTitleQueries.filter(q => q !== query)
    };
    this.generatedQueries.set(updated);
  }

  removeRequirementBasedQuery(query: string) {
    const current = this.generatedQueries();
    if (!current) return;
    const updated = {
      ...current,
      requirementBasedQueries: current.requirementBasedQueries.filter(q => q !== query)
    };
    this.generatedQueries.set(updated);
  }

  removeAlternativeDirectionQuery(query: string) {
    const current = this.generatedQueries();
    if (!current) return;
    const updated = {
      ...current,
      alternativeDirectionQueries: current.alternativeDirectionQueries.filter(q => q !== query)
    };
    this.generatedQueries.set(updated);
  }

  // Add methods
  addRoleTitleQuery() {
    const value = this.newRoleTitleQuery().trim();
    if (!value) return;

    const current = this.generatedQueries();
    if (!current) return;

    // Avoid duplicates
    if (current.roleTitleQueries.includes(value)) {
      this.newRoleTitleQuery.set('');
      return;
    }

    const updated = {
      ...current,
      roleTitleQueries: [...current.roleTitleQueries, value]
    };
    this.generatedQueries.set(updated);
    this.newRoleTitleQuery.set('');
  }

  addRequirementBasedQuery() {
    const value = this.newRequirementBasedQuery().trim();
    if (!value) return;

    const current = this.generatedQueries();
    if (!current) return;

    // Avoid duplicates
    if (current.requirementBasedQueries.includes(value)) {
      this.newRequirementBasedQuery.set('');
      return;
    }

    const updated = {
      ...current,
      requirementBasedQueries: [...current.requirementBasedQueries, value]
    };
    this.generatedQueries.set(updated);
    this.newRequirementBasedQuery.set('');
  }

  addAlternativeDirectionQuery() {
    const value = this.newAlternativeDirectionQuery().trim();
    if (!value) return;

    const current = this.generatedQueries();
    if (!current) return;

    // Avoid duplicates
    if (current.alternativeDirectionQueries.includes(value)) {
      this.newAlternativeDirectionQuery.set('');
      return;
    }

    const updated = {
      ...current,
      alternativeDirectionQueries: [...current.alternativeDirectionQueries, value]
    };
    this.generatedQueries.set(updated);
    this.newAlternativeDirectionQuery.set('');
  }

  goBack() {
    this.router.navigate(['/cv']);
  }

  // Job match functionality moved to CvInputComponent
}




