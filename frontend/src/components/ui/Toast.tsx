import { Toaster, resolveValue } from 'react-hot-toast';
import { cn } from '@/utils/cn';
import { CheckCircle2, XCircle, Info, AlertTriangle } from 'lucide-react';

export const ToastProvider = () => {
  return (
    <Toaster
      position="top-right"
      toastOptions={{
        className: '',
        style: {
          padding: 0,
          background: 'transparent',
          boxShadow: 'none',
        },
      }}
    >
      {(t) => {
        const isError = t.type === 'error';
        const isSuccess = t.type === 'success';
        const isWarning = t.type === 'custom' && (t as any).tone === 'warning';
        
        let Icon = Info;
        let toneClass = 'bg-indigo-50 border-indigo-200 text-indigo-700';
        let iconClass = 'text-indigo-500';

        if (isError) {
          Icon = XCircle;
          toneClass = 'bg-red-50 border-red-200 text-red-700';
          iconClass = 'text-red-500';
        } else if (isSuccess) {
          Icon = CheckCircle2;
          toneClass = 'bg-emerald-50 border-emerald-200 text-emerald-700';
          iconClass = 'text-emerald-500';
        } else if (isWarning) {
          Icon = AlertTriangle;
          toneClass = 'bg-amber-50 border-amber-200 text-amber-700';
          iconClass = 'text-amber-500';
        }

        return (
          <div
            className={cn(
              "flex items-start gap-3 rounded-xl border p-4 shadow-[0px_4px_24px_0px] shadow-black/15 max-w-sm w-full",
              toneClass,
              t.visible ? "animate-in fade-in slide-in-from-top-2 sm:slide-in-from-right-full" : "animate-out fade-out slide-out-to-right-full"
            )}
          >
            <Icon className={cn("h-5 w-5 shrink-0 mt-0.5", iconClass)} />
            <div className="flex-1 text-sm font-medium">
              {resolveValue(t.message, t)}
            </div>
          </div>
        );
      }}
    </Toaster>
  );
};
