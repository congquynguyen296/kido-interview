import { cn } from '@/utils/cn';
import type { TextareaHTMLAttributes } from 'react';
import { forwardRef, useId } from 'react';

export interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label: string;
  showLabel?: boolean;
  error?: string;
  hint?: string;
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
  (
    {
      className,
      label,
      showLabel = false,
      error,
      hint,
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
        <textarea
          id={id}
          ref={ref}
          className={cn(
            'w-full bg-transparent border border-gray-500/30 outline-none rounded-xl py-2.5 px-4 text-sm text-gray-800 placeholder:text-gray-400 transition-colors resize-y min-h-[80px]',
            'focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20',
            'disabled:opacity-50 disabled:bg-gray-50',
            error && 'border-red-500 focus:border-red-500 focus:ring-red-500/20',
            className
          )}
          aria-invalid={!!error}
          aria-describedby={error ? errorId : hint ? hintId : undefined}
          {...props}
        />
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
Textarea.displayName = 'Textarea';
