import type { FieldErrorResponse } from '@/types/api';

export class ApiError extends Error {
  public readonly code: number;
  public readonly httpStatus?: number;
  public readonly fieldErrors?: FieldErrorResponse[];

  constructor(
    code: number,
    message: string,
    httpStatus?: number,
    fieldErrors?: FieldErrorResponse[]
  ) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.httpStatus = httpStatus;
    this.fieldErrors = fieldErrors;
  }
}
