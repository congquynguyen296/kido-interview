import { cn } from '@/utils/cn';
import { Search, X } from 'lucide-react';
import type { InputHTMLAttributes } from 'react';
import { forwardRef, useEffect, useState } from 'react';

export interface SearchInputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange'> {
  value?: string;
  onSearch?: (value: string) => void;
  debounceMs?: number;
}

export const SearchInput = forwardRef<HTMLInputElement, SearchInputProps>(
  ({ className, value = '', onSearch, debounceMs = 400, ...props }, ref) => {
    const [localValue, setLocalValue] = useState(value);
    const [prevValue, setPrevValue] = useState(value);

    if (value !== prevValue) {
      setPrevValue(value);
      setLocalValue(value);
    }

    useEffect(() => {
      if (!onSearch) return;

      const timer = setTimeout(() => {
        onSearch(localValue);
      }, debounceMs);

      return () => clearTimeout(timer);
    }, [localValue, onSearch, debounceMs]);

    return (
      <div className="relative w-full sm:max-w-xs">
        <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-4 text-gray-400">
          <Search className="h-4 w-4" />
        </div>
        <input
          ref={ref}
          type="text"
          className={cn(
            'w-full bg-transparent border border-gray-500/30 outline-none rounded-full py-2 pl-10 pr-10 text-sm text-gray-800 placeholder:text-gray-400 transition-colors',
            'focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20',
            className
          )}
          value={localValue}
          onChange={(e) => setLocalValue(e.target.value)}
          placeholder="Tìm kiếm..."
          {...props}
        />
        {localValue && (
          <button
            type="button"
            className="absolute inset-y-0 right-0 flex items-center pr-4 text-gray-400 hover:text-gray-600 focus:outline-none"
            onClick={() => setLocalValue('')}
          >
            <X className="h-4 w-4" />
          </button>
        )}
      </div>
    );
  }
);
SearchInput.displayName = 'SearchInput';
