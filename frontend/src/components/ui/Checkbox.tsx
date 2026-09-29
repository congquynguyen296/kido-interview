import { cn } from '@/utils/cn';
import type { InputHTMLAttributes } from 'react';
import { forwardRef, useId } from 'react';

export interface CheckboxProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label: React.ReactNode;
  description?: React.ReactNode;
  error?: string;
}

export const Checkbox = forwardRef<HTMLInputElement, CheckboxProps>(
  ({ className, label, description, error, id: providedId, ...props }, ref) => {
    const generatedId = useId();
    const id = providedId || generatedId;
    const errorId = `${id}-error`;

    return (
      <div className="relative flex items-start">
        <div className="flex h-6 items-center">
          <input
            id={id}
            ref={ref}
            type="checkbox"
            className={cn(
              "h-4 w-4 rounded border-gray-500/30 text-indigo-500 focus:ring-indigo-500 focus:ring-offset-0 bg-transparent transition-colors",
              error && "border-red-500 text-red-500 focus:ring-red-500",
              className
            )}
            aria-invalid={!!error}
            aria-describedby={error ? errorId : undefined}
            {...props}
          />
        </div>
        <div className="ml-3 text-sm leading-6">
          <label htmlFor={id} className="font-medium text-gray-800 select-none">
            {label}
          </label>
          {description && (
            <p className="text-gray-500">{description}</p>
          )}
          {error && (
            <p id={errorId} className="text-red-500 text-xs mt-1">{error}</p>
          )}
        </div>
      </div>
    );
  }
);
Checkbox.displayName = 'Checkbox';
