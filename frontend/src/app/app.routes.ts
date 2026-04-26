import { Routes } from "@angular/router";
import { ExtConnComponent } from "./ext-conn/ext-conn.component";
import { PlannerComponent } from "./planner/planner.component";
import { ErrorComponent } from "./error/error.component";

export const routes: Routes = [
  { path: "",            redirectTo: "connections", pathMatch: "full" },
  { path: "connections", component: ExtConnComponent },
  { path: "planners",    component: PlannerComponent },
  { path: "**",          component: ErrorComponent }
];
