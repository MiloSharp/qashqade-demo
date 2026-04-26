import { Component, OnInit, ChangeDetectorRef, Inject, NgZone } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { FormsModule } from "@angular/forms";
import { DatePipe, DecimalPipe } from "@angular/common";
import { Subject, firstValueFrom } from "rxjs";
import { debounceTime, distinctUntilChanged } from "rxjs/operators";
import { Planner, PlannerType, ReportType, Employee, Fund, FundAlias, PlannerFund, Source, Run, Report } from "./planner.model";
import { PlannerService } from "./planner.service";
import { ExtConn } from "../ext-conn/ext-conn.model";
import { ExtConnService } from "../ext-conn/ext-conn.service";
import { IconComponent } from "../shared/icon.component";
import { TypeaheadComponent, TypeaheadOption } from "../shared/typeahead.component";

@Component({
  selector: "app-planner",
  standalone: true,
  imports: [FormsModule, DatePipe, DecimalPipe, IconComponent, TypeaheadComponent],
  templateUrl: "./planner.component.html",
  styleUrl: "./planner.component.scss",
  providers: [PlannerService, ExtConnService]
})
export class PlannerComponent implements OnInit {

  items: Planner[] = [];
  plannerTypes: PlannerType[] = [];
  reportTypes: ReportType[] = [];
  expandedId: number | null = null;
  editCopy: Partial<Planner> = {};
  plannerFunds: PlannerFund[] = [];
  saving = false;
  errorMsg = "";
  deleteConfirmId: number | null = null;
  private pendingExpandId: number | null = null;

  currentPage = 0;
  pageSize = 25;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = [10, 25, 50, 100];

  sortBy = "name";
  sortDir = "asc";
  statusOptions = ["", "Finished", "Failed"];
  searchTerm = "";
  private searchSubject = new Subject<string>();

  constructor(
    @Inject(PlannerService) private svc: PlannerService,
    @Inject(ExtConnService) private extConnSvc: ExtConnService,
    @Inject(ChangeDetectorRef) private cdr: ChangeDetectorRef,
    @Inject(HttpClient) private http: HttpClient,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    this.searchSubject.pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(term => {
        this.searchTerm = term;
        this.currentPage = 0;
        this.loadWith(this.sortBy, this.sortDir);
        this.cdr.detectChanges();
      });
    this.loadWith(this.sortBy, this.sortDir);
    this.loadLookups();
  }

  private loadWith(sortBy: string, sortDir: string): void {
    this.svc.getAll(this.currentPage, this.pageSize, this.searchTerm, sortBy, sortDir).subscribe({
      next: (page) => {
        this.items = [...page.content];
        this.totalElements = page.totalElements;
        this.totalPages = page.totalPages;
        this.cdr.detectChanges();
      },
      error: () => { this.errorMsg = "Failed to load planners."; this.cdr.detectChanges(); }
    });
  }

  load(): void { this.loadWith(this.sortBy, this.sortDir); }

  loadLookups(): void {
    this.svc.getPlannerTypes().subscribe({ next: (t) => { this.plannerTypes = t; this.cdr.detectChanges(); } });
    this.svc.getReportTypes().subscribe({ next: (t) => { this.reportTypes = t; this.cdr.detectChanges(); } });
  }

  loadPlannerFunds(plannerId: number): void {
    this.svc.getPlannerFunds(plannerId).subscribe({
      next: (funds) => { this.plannerFunds = [...funds]; this.cdr.detectChanges(); },
      error: () => { this.plannerFunds = []; this.cdr.detectChanges(); }
    });
  }

  // -- Typeahead fetch functions ------------------------------

  fetchExtConns = async (search: string): Promise<TypeaheadOption[]> => {
    const page = await firstValueFrom(this.extConnSvc.getAll(0, 10, search, "name"));
    return page.content.map((ec: ExtConn) => ({ id: ec.id!, label: ec.name }));
  }

  fetchEmployees = async (search: string): Promise<TypeaheadOption[]> => {
    const params = new HttpParams().set("search", search).set("size", "10");
    const page = await firstValueFrom(this.http.get<any>("http://localhost:8080/api/employee", { params }));
    return (page.content as Employee[]).map(e => ({ id: e.id, label: e.fullName }));
  }

  createEmployee = async (fullName: string): Promise<TypeaheadOption> => {
    const emp = await firstValueFrom(
      this.http.post<Employee>("http://localhost:8080/api/employee", { fullName })
    );
    return { id: emp.id, label: emp.fullName };
  }

  fetchFunds = async (search: string): Promise<TypeaheadOption[]> => {
    const params = new HttpParams().set("search", search).set("size", "10");
    const page = await firstValueFrom(this.http.get<any>("http://localhost:8080/api/fund", { params }));
    return (page.content as Fund[]).map(f => ({ id: f.id, label: f.fundName }));
  }

  createFund = async (fundName: string): Promise<TypeaheadOption> => {
    const fund = await firstValueFrom(
      this.http.post<Fund>("http://localhost:8080/api/fund", { fundName })
    );
    return { id: fund.id, label: fund.fundName };
  }

  // Per-row alias fetch — fetches aliases for a specific fund
  fetchAliasesForFund(fundId: number) {
    return async (search: string): Promise<TypeaheadOption[]> => {
      const aliases = await firstValueFrom(
        this.http.get<FundAlias[]>("http://localhost:8080/api/fund/" + fundId + "/aliases")
      );
      const filtered = search
        ? aliases.filter(a => a.aliasName.toLowerCase().includes(search.toLowerCase()))
        : aliases;
      return [
        { id: 0, label: "No Alias" },
        ...filtered.map(a => ({ id: a.id, label: a.aliasName }))
      ];
    };
  }

  createAliasForFund(fundId: number) {
    return async (aliasName: string): Promise<TypeaheadOption> => {
      const alias = await firstValueFrom(
        this.http.post<FundAlias>(
          "http://localhost:8080/api/fund/" + fundId + "/aliases",
          { aliasName }
        )
      );
      return { id: alias.id, label: alias.aliasName };
    };
  }

  fetchSources = async (search: string): Promise<TypeaheadOption[]> => {
    const params = new HttpParams().set("search", search).set("size", "10");
    const page = await firstValueFrom(this.http.get<any>("http://localhost:8080/api/source", { params }));
    return (page.content as Source[]).map(s => ({ id: s.id, label: s.sourceName }));
  }

  fetchRuns = async (search: string): Promise<TypeaheadOption[]> => {
    const params = new HttpParams().set("search", search).set("size", "10");
    const page = await firstValueFrom(this.http.get<any>("http://localhost:8080/api/run", { params }));
    return (page.content as Run[]).map(r => ({ id: r.id, label: r.runName }));
  }

  fetchReports = async (search: string): Promise<TypeaheadOption[]> => {
    const params = new HttpParams().set("search", search).set("size", "10");
    const page = await firstValueFrom(this.http.get<any>("http://localhost:8080/api/report", { params }));
    return (page.content as Report[]).map(r => ({ id: r.id, label: r.reportType + " - " + r.reportName }));
  }

  extConnLabel(item: Planner): string { return item.extConn?.name ?? ""; }
  ownerLabel(item: Planner): string   { return item.ownerEmployee?.fullName ?? item.owner ?? ""; }

  onExtConnSelected(id: number | null): void { this.editCopy.extConnId = id ?? undefined; }
  onOwnerSelected(id: number | null): void   { this.editCopy.ownerEmployeeId = id ?? undefined; }

  // -- Fund association handlers ------------------------------

  addFund(id: number | null): void {
    if (!id || this.expandedId === null) return;
    this.svc.addPlannerFund(this.expandedId, id).subscribe({
      next: (pf: PlannerFund) => { this.zone.run(() => {
        this.plannerFunds = [...this.plannerFunds, pf];
        this.cdr.detectChanges();
      }); },
      error: () => { this.errorMsg = "Failed to add fund."; this.cdr.detectChanges(); }
    });
  }

  updateFundAlias(plannerFundId: number, aliasId: number | null): void {
    if (this.expandedId === null) return;
    // aliasId of 0 means "No Alias"
    const resolvedAliasId = aliasId === 0 ? null : aliasId;
    this.svc.updatePlannerFundAlias(this.expandedId, plannerFundId, resolvedAliasId).subscribe({
      next: (updated: PlannerFund) => { this.zone.run(() => {
        const idx = this.plannerFunds.findIndex(pf => pf.id === updated.id);
        if (idx > -1) {
          this.plannerFunds = [
            ...this.plannerFunds.slice(0, idx),
            updated,
            ...this.plannerFunds.slice(idx + 1)
          ];
        }
        this.cdr.detectChanges();
      }); },
      error: () => { this.errorMsg = "Failed to update alias."; this.cdr.detectChanges(); }
    });
  }

  removeFund(plannerFundId: number): void {
    if (this.expandedId === null) return;
    this.svc.removePlannerFund(this.expandedId, plannerFundId).subscribe({
      next: () => { this.zone.run(() => {
        this.plannerFunds = this.plannerFunds.filter(pf => pf.id !== plannerFundId);
        this.cdr.detectChanges();
      }); },
      error: () => { this.errorMsg = "Failed to remove fund."; this.cdr.detectChanges(); }
    });
  }

  sortBy$(col: string): void {
    const newDir = this.sortBy === col ? (this.sortDir === "asc" ? "desc" : "asc") : "asc";
    this.sortBy = col; this.sortDir = newDir; this.currentPage = 0;
    this.loadWith(col, newDir);
  }

  isSortedBy(col: string): boolean { return this.sortBy === col; }
  isSortedAsc(): boolean { return this.sortDir === "asc"; }
  onSearch(term: string): void { this.searchSubject.next(term); }

  onPageSizeChange(newSize: number): void {
    this.pageSize = +newSize; this.currentPage = 0; this.load();
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page; this.load();
  }

  get pages(): number[] {
    const delta = 2;
    const start = Math.max(0, this.currentPage - delta);
    const end = Math.min(this.totalPages - 1, this.currentPage + delta);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  get rangeStart(): number { return this.currentPage * this.pageSize + 1; }
  get rangeEnd(): number { return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements); }

  isItemLocked(item: Planner): boolean { return item.status === "Finished"; }
  isEditCopyLocked(): boolean { return this.editCopy.status === "Finished"; }
  isExpanded(item: Planner): boolean { return this.expandedId === item.id; }

  statusClass(status?: string): string {
    if (status === "Finished") return "status--finished";
    if (status === "Failed")   return "status--failed";
    return "status--empty";
  }

  formatStatus(item: Planner): string {
    if (!item.status) return "";
    const dt = item.statusAt ? new Date(item.statusAt) : null;
    if (!dt) return item.status;
    return dt.toLocaleDateString("en-GB") + " " +
      dt.toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit", second: "2-digit", hour12: true }) +
      " " + item.status;
  }

  toggle(item: Planner): void {
    if (this.expandedId === item.id) { this.pendingExpandId = null; this.autoSave(); }
    else if (this.expandedId !== null) { this.pendingExpandId = item.id!; this.autoSave(); }
    else { this.openRow(item); }
  }

  private autoSave(): void {
    if (this.expandedId === null) return;
    const id = this.expandedId;
    this.saving = true; this.errorMsg = "";
    const pending = this.pendingExpandId;
    this.expandedId = null; this.pendingExpandId = null;
    this.plannerFunds = [];
    this.svc.update(id, this.editCopy as Planner).subscribe({
      next: (updated: Planner) => {
        const idx = this.items.findIndex(i => i.id === updated.id);
        if (idx > -1) this.items[idx] = updated;
        this.saving = false; this.editCopy = {};
        if (pending !== null) {
          const next = this.items.find(i => i.id === pending);
          if (next) this.openRow(next);
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMsg = "Auto-save failed."; this.saving = false;
        const item = this.items.find(i => i.id === id);
        if (item) this.openRow(item);
        this.cdr.detectChanges();
      }
    });
  }

  private openRow(item: Planner): void {
    this.expandedId = item.id!;
    this.editCopy = {
      name:            item.name,
      description:     item.description,
      triggerSources:  item.triggerSources ?? false,
      triggerRuns:     item.triggerRuns    ?? false,
      triggerReports:  item.triggerReports ?? false,
      reportName:      item.reportName,
      owner:           item.owner,
      ownerEmployeeId: item.ownerEmployee?.id ?? item.ownerEmployeeId,
      status:          item.status,
      statusAt:        item.statusAt,
      logFile:         item.logFile,
      plannerTypeId:   item.plannerType?.id  ?? item.plannerTypeId,
      extConnId:       item.extConn?.id      ?? item.extConnId,
      outputFormatId:  item.outputFormat?.id ?? item.outputFormatId,
      sources:         item.sources  ?? [],
      runs:            item.runs     ?? [],
      reports:         item.reports  ?? []
    };
    setTimeout(() => this.loadPlannerFunds(item.id!), 0);
    this.cdr.detectChanges();
  }

  save(item: Planner): void { this.pendingExpandId = null; this.autoSave(); }

  cancel(): void {
    this.expandedId = null; this.editCopy = {};
    this.errorMsg = ""; this.pendingExpandId = null;
    this.plannerFunds = [];
  }

  addSource(id: number | null): void {
    if (!id || this.expandedId === null) return;
    this.svc.addSource(this.expandedId, id).subscribe({
      next: (updated: Planner) => { this.updateExpandedItem(updated); },
      error: () => { this.errorMsg = "Failed to add source."; this.cdr.detectChanges(); }
    });
  }

  removeSource(sourceId: number): void {
    if (this.expandedId === null) return;
    this.svc.removeSource(this.expandedId, sourceId).subscribe({
      next: (updated: Planner) => { this.updateExpandedItem(updated); },
      error: () => { this.errorMsg = "Failed to remove source."; this.cdr.detectChanges(); }
    });
  }

  addRun(id: number | null): void {
    if (!id || this.expandedId === null) return;
    this.svc.addRun(this.expandedId, id).subscribe({
      next: (updated: Planner) => { this.updateExpandedItem(updated); },
      error: () => { this.errorMsg = "Failed to add run."; this.cdr.detectChanges(); }
    });
  }

  removeRun(runId: number): void {
    if (this.expandedId === null) return;
    this.svc.removeRun(this.expandedId, runId).subscribe({
      next: (updated: Planner) => { this.updateExpandedItem(updated); },
      error: () => { this.errorMsg = "Failed to remove run."; this.cdr.detectChanges(); }
    });
  }

  addReport(id: number | null): void {
    if (!id || this.expandedId === null) return;
    this.svc.addReport(this.expandedId, id).subscribe({
      next: (updated: Planner) => { this.updateExpandedItem(updated); },
      error: () => { this.errorMsg = "Failed to add report."; this.cdr.detectChanges(); }
    });
  }

  removeReport(reportId: number): void {
    if (this.expandedId === null) return;
    this.svc.removeReport(this.expandedId, reportId).subscribe({
      next: (updated: Planner) => { this.updateExpandedItem(updated); },
      error: () => { this.errorMsg = "Failed to remove report."; this.cdr.detectChanges(); }
    });
  }

  private updateExpandedItem(updated: Planner): void {
    const idx = this.items.findIndex(i => i.id === updated.id);
    if (idx > -1) this.items[idx] = { ...updated };
    this.editCopy = {
      ...this.editCopy,
      sources: [...(updated.sources ?? [])],
      runs:    [...(updated.runs    ?? [])],
      reports: [...(updated.reports ?? [])]
    };
    this.cdr.markForCheck();
    this.cdr.detectChanges();
  }

  addNew(): void {
    const blank: Planner = { name: "New planner", owner: "", triggerSources: false, triggerRuns: false, triggerReports: false };
    this.svc.create(blank).subscribe({
      next: (created: Planner) => {
        this.items.unshift(created); this.totalElements++;
        this.openRow(created); this.cdr.detectChanges();
      },
      error: () => { this.errorMsg = "Failed to create planner."; this.cdr.detectChanges(); }
    });
  }

  clone(item: Planner, event: Event): void {
    event.stopPropagation();
    const cloned: Planner = {
      ...item, name: item.name + " (copy)",
      id: undefined, status: "", statusAt: undefined, logFile: undefined
    };
    this.svc.create(cloned).subscribe({
      next: (created: Planner) => {
        const idx = this.items.findIndex(i => i.id === item.id);
        this.items.splice(idx + 1, 0, created);
        this.totalElements++; this.openRow(created); this.cdr.detectChanges();
      },
      error: () => { this.errorMsg = "Failed to clone planner."; this.cdr.detectChanges(); }
    });
  }

  run(item: Planner, event: Event): void {
    event.stopPropagation();
    this.svc.run(item.id!).subscribe({
      next: (updated: Planner) => {
        const idx = this.items.findIndex(i => i.id === updated.id);
        if (idx > -1) this.items[idx] = updated;
        if (this.expandedId === updated.id) {
          this.editCopy = { ...this.editCopy, status: updated.status, statusAt: updated.statusAt, logFile: updated.logFile };
        }
        this.cdr.detectChanges();
      },
      error: () => { this.errorMsg = "Run failed."; this.cdr.detectChanges(); }
    });
  }

  download(item: Planner, event: Event): void { event.stopPropagation(); this.svc.downloadLog(item); }

  confirmDelete(item: Planner, event: Event): void {
    event.stopPropagation(); this.deleteConfirmId = item.id!; this.cdr.detectChanges();
  }

  cancelDelete(): void { this.deleteConfirmId = null; }

  deleteItem(item: Planner): void {
    this.svc.delete(item.id!).subscribe({
      next: () => {
        this.items = this.items.filter(i => i.id !== item.id);
        this.totalElements--;
        if (this.expandedId === item.id) {
          this.expandedId = null; this.editCopy = {}; this.plannerFunds = [];
        }
        this.deleteConfirmId = null; this.cdr.detectChanges();
      },
      error: () => { this.errorMsg = "Delete failed."; this.deleteConfirmId = null; this.cdr.detectChanges(); }
    });
  }
}
