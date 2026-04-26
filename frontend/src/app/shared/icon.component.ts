import { Component, Input } from "@angular/core";

const ICONS: Record<string, string> = {
  "sort-both":  "M8 3v10M5 6l3-3 3 3M5 10l3 3 3-3",
  "sort-asc":   "M8 13V3M5 6l3-3 3 3",
  "sort-desc":  "M8 3v10M5 10l3 3 3-3",
  "chevron":    "M4 6l4 4 4-4",
  "run":        "M4 2l10 6-10 6V2z",
  "download":   "M8 2v8M5 7l3 3 3-3M2 12v1a1 1 0 0 0 1 1h10a1 1 0 0 0 1-1v-1",
  "clone-back": "M3 11H2.5A1.5 1.5 0 0 1 1 9.5v-7A1.5 1.5 0 0 1 2.5 1h7A1.5 1.5 0 0 1 11 2.5V3",
  "delete":     "M2 4h12M5 4V2.5A.5.5 0 0 1 5.5 2h5a.5.5 0 0 1 .5.5V4M6 7v5M10 7v5M3 4l1 9.5A.5.5 0 0 0 4.5 14h7a.5.5 0 0 0 .5-.5L13 4",
  "nav-first":  "M3 3v10M13 4L7 8l6 4V4z",
  "nav-prev":   "M10 4L6 8l4 4",
  "nav-next":   "M6 4l4 4-4 4",
  "nav-last":   "M13 3v10M3 4l6 4-6 4V4z",
  "lock-body":  "M5 7V5a3 3 0 0 1 6 0v2",
};

@Component({
  selector: "app-icon",
  standalone: true,
  template: `
    <svg [attr.viewBox]="viewBox"
         fill="none"
         [attr.class]="cls"
         [style.width.px]="size"
         [style.height.px]="size"
         [style.display]="inline ? 'inline-block' : 'block'"
         style="flex-shrink:0;vertical-align:middle">
      @if (name === 'clone') {
        <rect x="5" y="5" width="9" height="9" rx="1.5"
              stroke="currentColor" stroke-width="1.5"/>
        <path [attr.d]="ICONS['clone-back']"
              stroke="currentColor" stroke-width="1.5"
              stroke-linecap="round" stroke-linejoin="round"/>
      } @else if (name === 'lock') {
        <rect x="3" y="7" width="10" height="8" rx="1.5"
              stroke="currentColor" stroke-width="1.5"/>
        <path [attr.d]="ICONS['lock-body']"
              stroke="currentColor" stroke-width="1.5"
              stroke-linecap="round"/>
      } @else {
        <path [attr.d]="path"
              stroke="currentColor"
              [attr.stroke-width]="strokeWidth"
              stroke-linecap="round"
              stroke-linejoin="round"/>
      }
    </svg>
  `
})
export class IconComponent {
  @Input() name = "";
  @Input() cls = "";
  @Input() size = 16;
  @Input() inline = false;
  @Input() viewBox = "0 0 16 16";
  @Input() strokeWidth = "1.5";

  readonly ICONS = ICONS;

  get path(): string {
    return ICONS[this.name] ?? "";
  }
}
