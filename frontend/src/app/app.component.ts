import { Component } from '@angular/core';
import { ExtConnComponent } from './ext-conn/ext-conn.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ExtConnComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'frontend';
}

