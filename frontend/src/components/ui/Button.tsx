import { cn } from '@/utils/cn';
import type { ButtonHTMLAttributes } from 'react';
import { forwardRef } from 'react';
import { Spinner } from './Spinner';

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'dark' | 'danger' | 'ghost' | 'link';
  size?: 'sm' | 'md';
  loading?: boolean;
  fullWidth?: boolean;
  leftIcon?: React.ReactNode;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      className,
      variant = 'primary',
      size = 'md',
      loading = false,
      fullWidth = false,
      leftIcon,
      children,
      disabled,
      ...props
    },
    ref
  ) => {
    const baseClasses = 'inline-flex items-center justify-center rounded-full font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500/40 focus-visible:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none';
    
    const variants = {
      primary: 'bg-indigo-500 text-white hover:bg-indigo-600 active:bg-indigo-700',
      secondary: 'bg-white text-gray-800 border border-gray-500/30 hover:bg-gray-50',
      dark: 'bg-black text-white hover:bg-gray-900',
      danger: 'bg-red-500 text-white hover:bg-red-600',
      ghost: 'bg-transparent text-gray-800 hover:bg-gray-100',
      link: 'bg-transparent text-blue-600 hover:underline p-0 h-auto rounded-none focus-visible:ring-0',
    };

    const sizes = {
      sm: 'py-1.5 px-3 text-xs min-h-8',
      md: 'py-2.5 px-4 text-sm min-h-11',
    };

    return (
      <button
        ref={ref}
        className={cn(
          baseClasses,
          variant !== 'link' && sizes[size],
          variants[variant],
          fullWidth && 'w-full',
          className
        )}
        disabled={disabled || loading}
        {...props}
      >
        {loading && <Spinner size="sm" className="mr-2" />}
        {!loading && leftIcon && <span className="mr-2">{leftIcon}</span>}
        {children}
      </button>
    );
  }
);

Button.displayName = 'Button';
