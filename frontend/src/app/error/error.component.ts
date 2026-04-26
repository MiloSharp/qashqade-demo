import { Component } from "@angular/core";
import { RouterLink } from "@angular/router";

@Component({
  selector: "app-error",
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="error-page">
      <div class="error-card">
        <div class="error-code">404</div>
        <h1 class="error-title">Page not found</h1>
        <p class="error-message">
          The page you are looking for does not exist or has been moved.
        </p>
        <a routerLink="/connections" class="error-btn">
          Back to External Connections
        </a>
      </div>
    </div>
  `,
  styles: [`
    .error-page {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: calc(100vh - 52px);
      background: #F7F8FA;
      font-family: system-ui, sans-serif;
    }
    .error-card {
      text-align: center;
      padding: 48px 40px;
      background: #FFFFFF;
      border: 1px solid #E2E5EB;
      border-radius: 12px;
      max-width: 420px;
      width: 100%;
    }
    .error-code {
      font-size: 80px;
      font-weight: 700;
      color: #0057FF;
      line-height: 1;
      margin-bottom: 16px;
      letter-spacing: -4px;
    }
    .error-title {
      font-size: 22px;
      font-weight: 600;
      color: #0F1923;
      margin: 0 0 10px;
    }
    .error-message {
      font-size: 14px;
      color: #6B7585;
      margin: 0 0 28px;
      line-height: 1.6;
    }
    .error-btn {
      display: inline-block;
      background: #0057FF;
      color: #fff;
      font-size: 14px;
      font-weight: 500;
      padding: 10px 24px;
      border-radius: 8px;
      text-decoration: none;
      transition: background 0.15s;
    }
    .error-btn:hover { background: #0044CC; }
  `]
})
export class ErrorComponent {}
