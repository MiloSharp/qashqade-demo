import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { Planner, PlannerType, ReportType, PlannerFund } from "./planner.model";
import { PageResponse } from "../ext-conn/ext-conn.service";

@Injectable({ providedIn: "root" })
export class PlannerService {

  private readonly apiUrl  = "http://localhost:8080/api/planner";
  private readonly fundUrl = "http://localhost:8080/api/fund";

  constructor(private http: HttpClient) {}

  getAll(page = 0, size = 25, search = "", sort = "name", dir = "asc"): Observable<PageResponse<Planner>> {
    const params = new HttpParams()
      .set("page", page).set("size", size)
      .set("search", search).set("sort", sort).set("dir", dir);
    return this.http.get<PageResponse<Planner>>(this.apiUrl, { params });
  }

  getPlannerTypes(): Observable<PlannerType[]> {
    return this.http.get<PlannerType[]>(this.apiUrl + "/planner-types");
  }

  getReportTypes(): Observable<ReportType[]> {
    return this.http.get<ReportType[]>(this.apiUrl + "/report-types");
  }

  create(planner: Planner): Observable<Planner> {
    return this.http.post<Planner>(this.apiUrl, planner);
  }

  update(id: number, planner: Planner): Observable<Planner> {
    const body = {
      name:            planner.name,
      description:     planner.description,
      triggerSources:  planner.triggerSources  ?? false,
      triggerRuns:     planner.triggerRuns     ?? false,
      triggerReports:  planner.triggerReports  ?? false,
      reportName:      planner.reportName,
      status:          planner.status          ?? "",
      extConnId:       planner.extConnId       ?? null,
      plannerTypeId:   planner.plannerTypeId   ?? null,
      outputFormatId:  planner.outputFormatId  ?? null,
      ownerEmployeeId: planner.ownerEmployeeId ?? null
    };
    return this.http.put<Planner>(this.apiUrl + "/" + id, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.apiUrl + "/" + id);
  }

  run(id: number): Observable<Planner> {
    return this.http.post<Planner>(this.apiUrl + "/" + id + "/run", {});
  }

  downloadLog(planner: Planner): void {
    if (!planner.logFile) return;
    const blob = new Blob([planner.logFile], { type: "text/plain" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = (planner.name + "-log.txt").replace(/\s+/g, "-");
    a.click();
    URL.revokeObjectURL(url);
  }

  // Source/Run/Report associations
  addSource(plannerId: number, sourceId: number): Observable<Planner> {
    return this.http.post<Planner>(this.apiUrl + "/" + plannerId + "/sources/" + sourceId, {});
  }
  removeSource(plannerId: number, sourceId: number): Observable<Planner> {
    return this.http.delete<Planner>(this.apiUrl + "/" + plannerId + "/sources/" + sourceId);
  }
  addRun(plannerId: number, runId: number): Observable<Planner> {
    return this.http.post<Planner>(this.apiUrl + "/" + plannerId + "/runs/" + runId, {});
  }
  removeRun(plannerId: number, runId: number): Observable<Planner> {
    return this.http.delete<Planner>(this.apiUrl + "/" + plannerId + "/runs/" + runId);
  }
  addReport(plannerId: number, reportId: number): Observable<Planner> {
    return this.http.post<Planner>(this.apiUrl + "/" + plannerId + "/reports/" + reportId, {});
  }
  removeReport(plannerId: number, reportId: number): Observable<Planner> {
    return this.http.delete<Planner>(this.apiUrl + "/" + plannerId + "/reports/" + reportId);
  }

  // Fund associations
  getPlannerFunds(plannerId: number): Observable<PlannerFund[]> {
    return this.http.get<PlannerFund[]>(this.fundUrl + "/planner/" + plannerId);
  }

  addPlannerFund(plannerId: number, fundId: number, aliasId?: number): Observable<PlannerFund> {
    return this.http.post<PlannerFund>(this.fundUrl + "/planner/" + plannerId,
      { fundId, aliasId: aliasId ?? null });
  }

  updatePlannerFundAlias(plannerId: number, plannerFundId: number, aliasId: number | null): Observable<PlannerFund> {
    return this.http.put<PlannerFund>(
      this.fundUrl + "/planner/" + plannerId + "/row/" + plannerFundId,
      { aliasId });
  }

  removePlannerFund(plannerId: number, plannerFundId: number): Observable<void> {
    return this.http.delete<void>(
      this.fundUrl + "/planner/" + plannerId + "/row/" + plannerFundId);
  }
}
