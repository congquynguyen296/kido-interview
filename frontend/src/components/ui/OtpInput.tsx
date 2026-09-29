import { cn } from '@/utils/cn';
import type { InputHTMLAttributes, KeyboardEvent, ClipboardEvent } from 'react';
import { forwardRef, useRef, useImperativeHandle } from 'react';

export interface OtpInputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'value' | 'onChange'> {
  length?: number;
  value: string;
  onChange: (value: string) => void;
  error?: boolean;
}

export const OtpInput = forwardRef<HTMLInputElement, OtpInputProps>(
  ({ className, length = 6, value, onChange, error, ...props }, ref) => {
    const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

    useImperativeHandle(ref, () => inputRefs.current[0] as HTMLInputElement);

    const handleChange = (index: number, val: string) => {
      const char = val.slice(-1);
      if (!/^\d*$/.test(char)) return; // Only numeric

      const newValue = value.split('');
      newValue[index] = char;
      const combined = newValue.join('');
      onChange(combined);

      // Auto focus next
      if (char && index < length - 1) {
        inputRefs.current[index + 1]?.focus();
      }
    };

    const handleKeyDown = (index: number, e: KeyboardEvent<HTMLInputElement>) => {
      if (e.key === 'Backspace' && !value[index] && index > 0) {
        // Delete previous and focus
        const newValue = value.split('');
        newValue[index - 1] = '';
        onChange(newValue.join(''));
        inputRefs.current[index - 1]?.focus();
      }
    };

    const handlePaste = (e: ClipboardEvent<HTMLInputElement>) => {
      e.preventDefault();
      const pastedData = e.clipboardData.getData('text').slice(0, length).replace(/\D/g, '');
      if (pastedData) {
        onChange(pastedData);
        // Focus the next empty input or the last one
        const focusIndex = Math.min(pastedData.length, length - 1);
        inputRefs.current[focusIndex]?.focus();
      }
    };

    return (
      <div className={cn("flex gap-2 justify-between max-w-sm mx-auto", className)}>
        {Array.from({ length }).map((_, index) => (
          <input
            key={index}
            ref={(el) => { inputRefs.current[index] = el; }}
            type="text"
            inputMode="numeric"
            autoComplete="one-time-code"
            maxLength={2} // Allow 2 to catch the latest char in onChange
            className={cn(
              "w-12 h-14 text-center text-xl font-semibold bg-transparent border border-gray-500/30 rounded-lg outline-none transition-colors",
              "focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20",
              error && "border-red-500 focus:border-red-500 focus:ring-red-500/20 animate-shake"
            )}
            value={value[index] || ''}
            onChange={(e) => handleChange(index, e.target.value)}
            onKeyDown={(e) => handleKeyDown(index, e)}
            onPaste={handlePaste}
            {...props}
          />
        ))}
      </div>
    );
  }
);
OtpInput.displayName = 'OtpInput';
