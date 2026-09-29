import { cn } from '@/utils/cn';
import type { ButtonHTMLAttributes } from 'react';
import { forwardRef, useId, useState } from 'react';

export interface SwitchProps extends Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'onChange' | 'value'> {
  label?: React.ReactNode;
  description?: React.ReactNode;
  checked?: boolean;
  defaultChecked?: boolean;
  onChange?: (checked: boolean) => void;
}

export const Switch = forwardRef<HTMLButtonElement, SwitchProps>(
  (
    {
      className,
      label,
      description,
      checked: controlledChecked,
      defaultChecked = false,
      onChange,
      id: providedId,
      disabled,
      ...props
    },
    ref
  ) => {
    const generatedId = useId();
    const id = providedId || generatedId;
    const isControlled = controlledChecked !== undefined;
    const [uncontrolledChecked, setUncontrolledChecked] = useState(defaultChecked);
    
    const checked = isControlled ? controlledChecked : uncontrolledChecked;

    const toggle = () => {
      if (disabled) return;
      const newValue = !checked;
      if (!isControlled) {
        setUncontrolledChecked(newValue);
      }
      onChange?.(newValue);
    };

    return (
      <div className="relative flex items-center">
        <button
          id={id}
          ref={ref}
          type="button"
          role="switch"
          aria-checked={checked}
          disabled={disabled}
          onClick={toggle}
          className={cn(
            "relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2",
            checked ? "bg-indigo-500" : "bg-gray-200",
            disabled && "opacity-50 cursor-not-allowed",
            className
          )}
          {...props}
        >
          <span className="sr-only">Use setting</span>
          <span
            aria-hidden="true"
            className={cn(
              "pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out",
              checked ? "translate-x-5" : "translate-x-0"
            )}
          />
        </button>
        {(label || description) && (
          <div className="ml-3 text-sm leading-6">
            {label && (
              <label htmlFor={id} className="font-medium text-gray-800 cursor-pointer" onClick={toggle}>
                {label}
              </label>
            )}
            {description && (
              <p className="text-gray-500">{description}</p>
            )}
          </div>
        )}
      </div>
    );
  }
);
Switch.displayName = 'Switch';
