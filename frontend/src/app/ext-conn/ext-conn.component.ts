import { Component, OnInit, ChangeDetectorRef } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { DatePipe, DecimalPipe } from "@angular/common";
import { Subject } from "rxjs";
import { debounceTime, distinctUntilChanged } from "rxjs/operators";
import { ExtConn } from "./ext-conn.model";
import { ExtConnService } from "./ext-conn.service";

@Component({
  selector: "app-ext-conn",
  standalone: true,
  imports: [FormsModule, DatePipe, DecimalPipe],
  templateUrl: "./ext-conn.component.html",
  styleUrl: "./ext-conn.component.scss"
})
export class ExtConnComponent implements OnInit {

  items: ExtConn[] = [];
  expandedId: number | null = null;
  editCopy: Partial<ExtConn> = {};
  authValueCleared = false;
  saving = false;
  errorMsg = "";
  deleteConfirmId: number | null = null;
  private pendingExpandId: number | null = null;

  currentPage = 0;
  pageSize = 25;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = [5, 10, 25, 50, 100];

  searchTerm = "";
  private searchSubject = new Subject<string>();

  constructor(private svc: ExtConnService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(term => {
      this.searchTerm = term;
      this.currentPage = 0;
      this.load();
    });
    this.load();
  }

  load(): void {
    this.svc.getAll(this.currentPage, this.pageSize, this.searchTerm).subscribe({
      next: page => {
        this.items = [...page.content];
        this.totalElements = page.totalElements;
        this.totalPages = page.totalPages;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMsg = "Failed to load connections.";
        this.cdr.detectChanges();
      }
    });
  }

  onSearch(term: string): void { this.searchSubject.next(term); }

  onPageSizeChange(newSize: number): void {
    this.pageSize = +newSize;
    this.currentPage = 0;
    this.load();
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.load();
  }

  get pages(): number[] {
    const delta = 2;
    const start = Math.max(0, this.currentPage - delta);
    const end = Math.min(this.totalPages - 1, this.currentPage + delta);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  get rangeStart(): number { return this.currentPage * this.pageSize + 1; }
  get rangeEnd(): number { return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements); }

  isExpanded(item: ExtConn): boolean { return this.expandedId === item.id; }

  toggle(item: ExtConn): void {
    if (this.expandedId === item.id) {
      this.pendingExpandId = null;
      this.autoSave();
    } else if (this.expandedId !== null) {
      this.pendingExpandId = item.id!;
      this.autoSave();
    } else {
      this.openRow(item);
    }
  }

  private autoSave(): void {
    if (this.expandedId === null) return;
    const id = this.expandedId;
    this.saving = true;
    this.errorMsg = "";
    this.expandedId = null;
    this.svc.update(id, this.editCopy as ExtConn).subscribe({
      next: updated => {
        const idx = this.items.findIndex(i => i.id === updated.id);
        if (idx > -1) this.items[idx] = updated;
        this.saving = false;
        this.editCopy = {};
        this.authValueCleared = false;
        if (this.pendingExpandId !== null) {
          const next = this.items.find(i => i.id === this.pendingExpandId);
          if (next) this.openRow(next);
          this.pendingExpandId = null;
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMsg = "Auto-save failed. Please save manually.";
        this.saving = false;
        this.pendingExpandId = null;
        this.cdr.detectChanges();
      }
    });
  }

  private openRow(item: ExtConn): void {
    this.expandedId = item.id!;
    this.editCopy = { ...item };
    this.authValueCleared = false;
    this.cdr.detectChanges();
  }

  clearAuthValue(): void {
    this.editCopy.authValue = '';
    this.authValueCleared = true;
    this.cdr.detectChanges();
  }

  save(item: ExtConn): void {
    this.pendingExpandId = null;
    this.autoSave();
  }

  cancel(): void {
    this.expandedId = null;
    this.editCopy = {};
    this.errorMsg = "";
    this.authValueCleared = false;
    this.pendingExpandId = null;
  }

  addNew(): void {
    const blank: ExtConn = { name: "New connection" };
    this.svc.create(blank).subscribe({
      next: created => {
        this.items.unshift(created);
        this.totalElements++;
        this.openRow(created);
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMsg = "Failed to create connection.";
        this.cdr.detectChanges();
      }
    });
  }

  clone(item: ExtConn, event: Event): void {
    event.stopPropagation();
    const cloned: ExtConn = { ...item, name: item.name + " (copy)", id: undefined };
    this.svc.create(cloned).subscribe({
      next: created => {
        const idx = this.items.findIndex(i => i.id === item.id);
        this.items.splice(idx + 1, 0, created);
        this.totalElements++;
        this.openRow(created);
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMsg = "Failed to clone connection.";
        this.cdr.detectChanges();
      }
    });
  }

  confirmDelete(item: ExtConn, event: Event): void {
    event.stopPropagation();
    this.deleteConfirmId = item.id!;
    this.cdr.detectChanges();
  }

  cancelDelete(): void { this.deleteConfirmId = null; }

  deleteItem(item: ExtConn): void {
    this.svc.delete(item.id!).subscribe({
      next: () => {
        this.items = this.items.filter(i => i.id !== item.id);
        this.totalElements--;
        if (this.expandedId === item.id) {
          this.expandedId = null;
          this.editCopy = {};
          this.authValueCleared = false;
        }
        this.deleteConfirmId = null;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMsg = "Delete failed. Please try again.";
        this.deleteConfirmId = null;
        this.cdr.detectChanges();
      }
    });
  }
}
