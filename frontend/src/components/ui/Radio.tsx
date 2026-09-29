import { cn } from '@/utils/cn';
import type { InputHTMLAttributes } from 'react';
import { forwardRef, useId } from 'react';

export interface RadioProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label: React.ReactNode;
  description?: React.ReactNode;
}

export const Radio = forwardRef<HTMLInputElement, RadioProps>(
  ({ className, label, description, id: providedId, ...props }, ref) => {
    const generatedId = useId();
    const id = providedId || generatedId;

    return (
      <div className="relative flex items-start">
        <div className="flex h-6 items-center">
          <input
            id={id}
            ref={ref}
            type="radio"
            className={cn(
              "h-4 w-4 border-gray-500/30 text-indigo-500 focus:ring-indigo-500 bg-transparent transition-colors",
              className
            )}
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
        </div>
      </div>
    );
  }
);
Radio.displayName = 'Radio';
