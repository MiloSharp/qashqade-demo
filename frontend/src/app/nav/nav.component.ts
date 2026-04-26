import { Component } from "@angular/core";
import { RouterLink, RouterLinkActive } from "@angular/router";

@Component({
  selector: "app-nav",
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="app-nav">
      <span class="app-nav__brand">qashqade</span>
      <a class="app-nav__link" routerLink="/connections" routerLinkActive="app-nav__link--active">
        External Connections
      </a>
      <a class="app-nav__link" routerLink="/planners" routerLinkActive="app-nav__link--active">
        Planners
      </a>
    </nav>
  `,
  styles: [`
    @import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap");

    .app-nav {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 0 32px;
      height: 52px;
      background: #0F1923;
      border-bottom: 1px solid #1E2D3D;
      font-family: "Inter", sans-serif;
    }
    .app-nav__brand {
      font-size: 15px;
      font-weight: 700;
      color: #FFFFFF;
      letter-spacing: -0.3px;
      margin-right: 16px;
    }
    .app-nav__link {
      font-size: 13px;
      font-weight: 500;
      color: #8A9BB0;
      text-decoration: none;
      padding: 6px 12px;
      border-radius: 6px;
      transition: color 0.15s, background 0.15s;
    }
    .app-nav__link:hover {
      color: #FFFFFF;
      background: rgba(255,255,255,0.07);
    }
    .app-nav__link--active {
      color: #FFFFFF;
      background: rgba(0,87,255,0.25);
    }
  `]
})
export class NavComponent {}
