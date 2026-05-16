import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SearchQueryStateService } from '../../services/search-query-state.service';

@Component({
  selector: 'app-search-queries',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './search-queries.component.html',
  styleUrls: ['./search-queries.component.css']
})
export class SearchQueriesComponent {
  data: any = null;

  constructor(private state: SearchQueryStateService, private router: Router) {
    this.data = this.state.getCleanedAnalysis();
  }

  goBack() {
    this.router.navigate(['/cv']);
  }
}


