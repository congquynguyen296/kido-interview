import { cn } from '@/utils/cn';
import { Info, CheckCircle2, AlertTriangle, XCircle } from 'lucide-react';
import type { HTMLAttributes } from 'react';
import { forwardRef } from 'react';

type AlertTone = 'info' | 'success' | 'warning' | 'error';

export interface AlertProps extends HTMLAttributes<HTMLDivElement> {
  tone?: AlertTone;
  title?: string;
}

const toneStyles: Record<AlertTone, { bg: string; border: string; icon: string; title: string; text: string; Icon: any }> = {
  info: { bg: 'bg-indigo-50', border: 'border-indigo-200', icon: 'text-indigo-500', title: 'text-indigo-800', text: 'text-indigo-700', Icon: Info },
  success: { bg: 'bg-emerald-50', border: 'border-emerald-200', icon: 'text-emerald-500', title: 'text-emerald-800', text: 'text-emerald-700', Icon: CheckCircle2 },
  warning: { bg: 'bg-amber-50', border: 'border-amber-200', icon: 'text-amber-500', title: 'text-amber-800', text: 'text-amber-700', Icon: AlertTriangle },
  error: { bg: 'bg-red-50', border: 'border-red-200', icon: 'text-red-500', title: 'text-red-800', text: 'text-red-700', Icon: XCircle },
};

export const Alert = forwardRef<HTMLDivElement, AlertProps>(
  ({ className, tone = 'info', title, children, ...props }, ref) => {
    const styles = toneStyles[tone];
    const Icon = styles.Icon;

    return (
      <div
        ref={ref}
        className={cn(
          'rounded-xl border p-4 flex gap-3',
          styles.bg,
          styles.border,
          className
        )}
        role="alert"
        {...props}
      >
        <Icon className={cn('h-5 w-5 shrink-0 mt-0.5', styles.icon)} />
        <div className="flex-1">
          {title && <h3 className={cn('text-sm font-medium mb-1', styles.title)}>{title}</h3>}
          <div className={cn('text-sm', styles.text)}>{children}</div>
        </div>
      </div>
    );
  }
);
Alert.displayName = 'Alert';
