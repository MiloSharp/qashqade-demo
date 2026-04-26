import { Component } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { NavComponent } from "./nav/nav.component";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterOutlet, NavComponent],
  template: `
    <app-nav></app-nav>
    <router-outlet></router-outlet>
  `
})
export class App {
  title = "frontend";
}
