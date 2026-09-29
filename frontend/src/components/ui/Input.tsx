import { cn } from '@/utils/cn';
import type { InputHTMLAttributes } from 'react';
import { forwardRef, useId } from 'react';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  showLabel?: boolean;
  error?: string;
  hint?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  (
    {
      className,
      label,
      showLabel = false,
      error,
      hint,
      leftIcon,
      rightIcon,
      id: providedId,
      ...props
    },
    ref
  ) => {
    const generatedId = useId();
    const id = providedId || generatedId;
    const errorId = `${id}-error`;
    const hintId = `${id}-hint`;

    return (
      <div className="w-full">
        <label
          htmlFor={id}
          className={cn(
            'mb-1.5 block text-sm font-medium text-gray-800',
            !showLabel && 'sr-only'
          )}
        >
          {label}
        </label>
        <div className="relative">
          {leftIcon && (
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-4 text-gray-400">
              {leftIcon}
            </div>
          )}
          <input
            id={id}
            ref={ref}
            className={cn(
              'w-full bg-transparent border border-gray-500/30 outline-none rounded-full py-2.5 px-4 text-sm text-gray-800 placeholder:text-gray-400 transition-colors',
              'focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20',
              'disabled:opacity-50 disabled:bg-gray-50',
              error && 'border-red-500 focus:border-red-500 focus:ring-red-500/20',
              leftIcon && 'pl-10',
              rightIcon && 'pr-10',
              className
            )}
            aria-invalid={!!error}
            aria-describedby={
              error ? errorId : hint ? hintId : undefined
            }
            {...props}
          />
          {rightIcon && (
            <div className="absolute inset-y-0 right-0 flex items-center pr-4 text-gray-400">
              {rightIcon}
            </div>
          )}
        </div>
        {error && (
          <p id={errorId} className="text-xs text-red-500 mt-1 px-4">
            {error}
          </p>
        )}
        {!error && hint && (
          <p id={hintId} className="text-xs text-gray-500 mt-1 px-4">
            {hint}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
