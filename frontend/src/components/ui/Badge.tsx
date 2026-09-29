import { cn } from '@/utils/cn';
import type { HTMLAttributes } from 'react';
import { forwardRef } from 'react';

type BadgeTone = 'gray' | 'indigo' | 'emerald' | 'amber' | 'red';

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  tone?: BadgeTone;
}

const toneClasses: Record<BadgeTone, string> = {
  gray: 'bg-gray-100 text-gray-700',
  indigo: 'bg-indigo-50 text-indigo-700',
  emerald: 'bg-emerald-50 text-emerald-700',
  amber: 'bg-amber-50 text-amber-700',
  red: 'bg-red-50 text-red-700',
};

export const Badge = forwardRef<HTMLSpanElement, BadgeProps>(
  ({ className, tone = 'gray', ...props }, ref) => {
    return (
      <span
        ref={ref}
        className={cn(
          'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
          toneClasses[tone],
          className
        )}
        {...props}
      />
    );
  }
);
Badge.displayName = 'Badge';
