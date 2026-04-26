import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, OnChanges, SimpleChanges, ChangeDetectorRef, Inject, ElementRef, HostListener, NgZone } from "@angular/core";
import { FormsModule } from "@angular/forms";

export interface TypeaheadOption {
  id: number;
  label: string;
}

@Component({
  selector: "app-typeahead",
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="ta-wrapper" [class.ta-open]="isOpen">
      <input
        class="ta-input"
        type="text"
        [placeholder]="placeholder"
        [(ngModel)]="searchText"
        (ngModelChange)="onType($event)"
        (focus)="onFocus()"
        (keydown)="onKeyDown($event)"
        [disabled]="disabled"
        autocomplete="off" />
      @if (searchText && !disabled) {
        <button class="ta-clear" type="button" (click)="clear()">
          <svg viewBox="0 0 16 16" fill="none" style="width:12px;height:12px">
            <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
      }
      @if (isOpen && (options.length > 0 || (allowCreate && searchText.trim().length > 0))) {
        <ul class="ta-dropdown">
          @if (!resetAfterSelect) {
            <li class="ta-option ta-option--none"
                [class.ta-option--active]="activeIndex === -1"
                (mousedown)="select(null)">
              -- None --
            </li>
          }
          @for (opt of options; track opt.id; let i = $index) {
            <li class="ta-option"
                [class.ta-option--active]="i === activeIndex"
                [class.ta-option--selected]="opt.id === selectedId"
                (mousedown)="select(opt)">
              {{ opt.label }}
            </li>
          }
          @if (allowCreate && searchText.trim().length > 0 && !exactMatch) {
            <li class="ta-option ta-option--create"
                [class.ta-option--active]="activeIndex === options.length"
                (mousedown)="createNew()">
              + Create "{{ searchText.trim() }}"
            </li>
          }
        </ul>
      }
      @if (isOpen && options.length === 0 && searchText.length > 0 && !allowCreate) {
        <ul class="ta-dropdown">
          <li class="ta-option ta-option--empty">No results found</li>
        </ul>
      }
    </div>
  `,
  styles: [`
    .ta-wrapper { position: relative; width: 100%; }
    .ta-input {
      font-family: system-ui, sans-serif;
      font-size: 13px;
      color: #0F1923;
      background: #FFFFFF;
      border: 1px solid #E2E5EB;
      border-radius: 6px;
      padding: 6px 28px 6px 10px;
      width: 100%;
      box-sizing: border-box;
      transition: border-color 0.15s, box-shadow 0.15s;
    }
    .ta-input:focus {
      outline: none;
      border-color: #0057FF;
      box-shadow: 0 0 0 3px rgba(0,87,255,0.1);
    }
    .ta-input:disabled {
      background: #F0F2F5;
      color: #9AA3AF;
      cursor: not-allowed;
      border-color: #E2E5EB;
    }
    .ta-clear {
      position: absolute;
      right: 6px;
      top: 50%;
      transform: translateY(-50%);
      background: none;
      border: none;
      cursor: pointer;
      color: #6B7585;
      padding: 2px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .ta-clear:hover { color: #0F1923; }
    .ta-dropdown {
      position: absolute;
      top: calc(100% + 4px);
      left: 0;
      right: 0;
      background: #FFFFFF;
      border: 1px solid #E2E5EB;
      border-radius: 6px;
      box-shadow: 0 4px 16px rgba(0,0,0,0.1);
      list-style: none;
      margin: 0;
      padding: 4px 0;
      z-index: 1000;
      max-height: 220px;
      overflow-y: auto;
    }
    .ta-option {
      font-size: 13px;
      padding: 7px 12px;
      cursor: pointer;
      color: #0F1923;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .ta-option:hover, .ta-option--active { background: #F0F4FF; }
    .ta-option--selected { font-weight: 500; color: #0057FF; }
    .ta-option--none { color: #6B7585; font-style: italic; }
    .ta-option--empty { color: #6B7585; font-style: italic; cursor: default; }
    .ta-option--create { color: #0057FF; font-style: italic; border-top: 1px solid #E2E5EB; }
    .ta-option--create:hover, .ta-option--create.ta-option--active { background: #EEF4FF; }
  `]
})
export class TypeaheadComponent implements OnInit, OnDestroy, OnChanges {

  @Input() placeholder = "Search...";
  @Input() disabled = false;
  @Input() selectedId: number | undefined = undefined;
  @Input() selectedLabel = "";
  @Input() fetchFn!: (search: string) => Promise<TypeaheadOption[]>;
  @Input() resetAfterSelect = false;
  @Input() allowCreate = false;  // Shows "+ Create" option when no exact match
  @Input() createFn?: (label: string) => Promise<TypeaheadOption>; // Called when user clicks Create

  @Output() selected = new EventEmitter<number | null>();

  searchText = "";
  options: TypeaheadOption[] = [];
  isOpen = false;
  activeIndex = -1;

  private debounceTimer: any = null;

  constructor(
    @Inject(ChangeDetectorRef) private cdr: ChangeDetectorRef,
    @Inject(ElementRef) private el: ElementRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    if (this.selectedLabel && !this.resetAfterSelect) {
      this.searchText = this.selectedLabel;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["selectedLabel"] && !this.resetAfterSelect) {
      this.searchText = this.selectedLabel || "";
      this.cdr.detectChanges();
    }
  }

  ngOnDestroy(): void {
    if (this.debounceTimer) clearTimeout(this.debounceTimer);
  }

  get exactMatch(): boolean {
    return this.options.some(o => o.label.toLowerCase() === this.searchText.trim().toLowerCase());
  }

  @HostListener("document:click", ["$event"])
  onDocumentClick(event: MouseEvent): void {
    if (!this.el.nativeElement.contains(event.target)) {
      this.close();
    }
  }

  onFocus(): void {
    this.searchText = "";
    this.search("");
  }

  onType(value: string): void {
    if (this.debounceTimer) clearTimeout(this.debounceTimer);
    this.debounceTimer = setTimeout(() => this.search(value), 250);
  }

  private async search(term: string): Promise<void> {
    if (!this.fetchFn) return;
    const results = await this.fetchFn(term);
    this.zone.run(() => {
      this.options = results;
      this.isOpen = true;
      this.activeIndex = -1;
      this.cdr.detectChanges();
    });
  }

  onKeyDown(event: KeyboardEvent): void {
    if (!this.isOpen) return;
    // Total includes the Create option if visible
    const createVisible = this.allowCreate && this.searchText.trim().length > 0 && !this.exactMatch;
    const total = this.options.length + (createVisible ? 1 : 0);
    if (event.key === "ArrowDown") {
      event.preventDefault();
      this.activeIndex = Math.min(this.activeIndex + 1, total - 1);
      this.cdr.detectChanges();
    } else if (event.key === "ArrowUp") {
      event.preventDefault();
      this.activeIndex = Math.max(this.activeIndex - 1, -1);
      this.cdr.detectChanges();
    } else if (event.key === "Enter") {
      event.preventDefault();
      if (this.activeIndex === -1) {
        this.select(null);
      } else if (createVisible && this.activeIndex === this.options.length) {
        this.createNew();
      } else if (this.activeIndex >= 0 && this.activeIndex < this.options.length) {
        this.select(this.options[this.activeIndex]);
      }
    } else if (event.key === "Escape") {
      this.close();
    }
  }

  select(opt: TypeaheadOption | null): void {
    this.zone.run(() => {
      if (opt) {
        this.selectedId = opt.id;
        this.selected.emit(opt.id);
        if (this.resetAfterSelect) {
          this.searchText = "";
          this.selectedId = undefined;
        } else {
          this.searchText = opt.label;
          this.selectedLabel = opt.label;
        }
      } else {
        this.searchText = "";
        this.selectedId = undefined;
        this.selected.emit(null);
      }
      this.isOpen = false;
      this.options = [];
      this.activeIndex = -1;
      this.cdr.detectChanges();
    });
  }

  async createNew(): Promise<void> {
    if (!this.createFn) return;
    const label = this.searchText.trim();
    const newOpt = await this.createFn(label);
    this.zone.run(() => {
      this.select(newOpt);
    });
  }

  clear(): void {
    this.zone.run(() => {
      this.searchText = "";
      this.selectedId = undefined;
      this.selected.emit(null);
      this.options = [];
      this.isOpen = false;
      this.cdr.detectChanges();
    });
  }

  private close(): void {
    this.zone.run(() => {
      this.isOpen = false;
      if (!this.resetAfterSelect && this.selectedLabel) {
        this.searchText = this.selectedLabel;
      } else if (this.resetAfterSelect) {
        this.searchText = "";
      }
      this.cdr.detectChanges();
    });
  }
}
