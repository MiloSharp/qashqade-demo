import { ExtConn } from "../ext-conn/ext-conn.model";

export interface PlannerType {
  id: number;
  name: string;
  sortOrder?: number;
}

export interface ReportType {
  id: number;
  name: string;
  sortOrder?: number;
}

export interface Employee {
  id: number;
  employeeId?: string;
  fullName: string;
  email?: string;
  title?: string;
  department?: string;
  dateOfBirth?: string;
}

export interface FundAlias {
  id: number;
  aliasName: string;
}

export interface Fund {
  id: number;
  fundName: string;
  aliases?: FundAlias[];
}

export interface PlannerFund {
  id: number;
  fund: Fund;
  alias?: FundAlias;
}

export interface Source {
  id: number;
  sourceName: string;
}

export interface Run {
  id: number;
  runName: string;
}

export interface Report {
  id: number;
  reportType: string;
  reportName: string;
  label?: string;
}

export interface Planner {
  id?: number;
  name: string;
  description?: string;
  plannerType?: PlannerType;
  plannerTypeId?: number;
  extConn?: ExtConn;
  extConnId?: number;
  fundName?: string;
  fundAlias?: string;
  triggerSources?: boolean;
  triggerRuns?: boolean;
  triggerReports?: boolean;
  outputFormat?: ReportType;
  outputFormatId?: number;
  reportName?: string;
  owner?: string;
  ownerEmployee?: Employee;
  ownerEmployeeId?: number;
  status?: string;
  statusAt?: string;
  logFile?: string;
  createdAt?: string;
  updatedAt?: string;
  sources?: Source[];
  runs?: Run[];
  reports?: Report[];
  plannerFunds?: PlannerFund[];
}
