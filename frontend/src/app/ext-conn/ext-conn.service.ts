import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { ExtConn } from "./ext-conn.model";

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

@Injectable({ providedIn: "root" })
export class ExtConnService {

  private readonly apiUrl = "/api/ext-conn";

  constructor(private http: HttpClient) {}

  getAll(page = 0, size = 25, search = "", sort = "name"): Observable<PageResponse<ExtConn>> {
    const params = new HttpParams()
      .set("page", page)
      .set("size", size)
      .set("search", search)
      .set("sort", sort);
    return this.http.get<PageResponse<ExtConn>>(this.apiUrl, { params });
  }

  create(extConn: ExtConn): Observable<ExtConn> {
    return this.http.post<ExtConn>(this.apiUrl, extConn);
  }

  update(id: number, extConn: ExtConn): Observable<ExtConn> {
    return this.http.put<ExtConn>(this.apiUrl + "/" + id, extConn);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.apiUrl + "/" + id);
  }
}
