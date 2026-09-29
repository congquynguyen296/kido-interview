export interface ApiResponse<T> {
  code: number;
  message?: string;
  result?: T;
}

export interface FieldErrorResponse {
  field: string;
  message: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
