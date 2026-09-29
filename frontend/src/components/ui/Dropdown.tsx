import { cn } from '@/utils/cn';
import { useEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { MoreVertical } from 'lucide-react';

export interface DropdownItem {
  id: string;
  label: React.ReactNode;
  icon?: React.ReactNode;
  danger?: boolean;
  disabled?: boolean;
  onClick?: () => void;
  href?: string;
}

interface DropdownProps {
  trigger?: React.ReactNode;
  items: DropdownItem[];
  align?: 'left' | 'right';
  className?: string;
}

export const Dropdown = ({ trigger, items, align = 'right', className }: DropdownProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);
  const [coords, setCoords] = useState({ top: 0, left: 0, right: 0 });

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current && 
        !dropdownRef.current.contains(event.target as Node) &&
        menuRef.current &&
        !menuRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    };
    
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && isOpen) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      document.addEventListener('keydown', handleKeyDown);
    }
    
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen]);

  useEffect(() => {
    const handleScroll = () => {
      if (isOpen && dropdownRef.current) {
        const rect = dropdownRef.current.getBoundingClientRect();
        setCoords({
          top: rect.bottom + window.scrollY,
          left: rect.left + window.scrollX,
          right: window.innerWidth - rect.right - window.scrollX,
        });
      }
    };
    
    if (isOpen) {
      window.addEventListener('scroll', handleScroll, true); // true for capturing phase to detect scroll in nested containers
      window.addEventListener('resize', handleScroll);
    }
    
    return () => {
      window.removeEventListener('scroll', handleScroll, true);
      window.removeEventListener('resize', handleScroll);
    };
  }, [isOpen]);

  const toggleDropdown = () => {
    if (!isOpen && dropdownRef.current) {
      const rect = dropdownRef.current.getBoundingClientRect();
      setCoords({
        top: rect.bottom + window.scrollY,
        left: rect.left + window.scrollX,
        right: window.innerWidth - rect.right - window.scrollX,
      });
    }
    setIsOpen(!isOpen);
  };

  return (
    <div className="relative inline-block text-left" ref={dropdownRef}>
      <div onClick={toggleDropdown} className="cursor-pointer">
        {trigger || (
          <button className="p-1.5 rounded-full hover:bg-gray-100 text-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/40">
            <MoreVertical className="w-5 h-5" />
          </button>
        )}
      </div>

      {isOpen && createPortal(
        <div
          ref={menuRef}
          className={cn(
            "absolute z-50 mt-2 w-48 rounded-xl bg-white shadow-[0px_4px_24px_0px] shadow-black/15 ring-1 ring-black/5 focus:outline-none py-1 animate-in fade-in zoom-in-95 duration-150",
            align === 'right' ? "origin-top-right" : "origin-top-left",
            className
          )}
          style={{
            top: coords.top,
            ...(align === 'left' ? { left: coords.left } : { right: coords.right })
          }}
          role="menu"
          aria-orientation="vertical"
        >
          {items.map((item) => (
            <div key={item.id}>
              {item.id === 'divider' ? (
                <div className="h-px bg-gray-500/10 my-1" />
              ) : (
                <button
                  className={cn(
                    "w-full text-left flex items-center px-4 py-2 text-sm",
                    item.disabled ? "opacity-50 cursor-not-allowed text-gray-400" : (
                      item.danger 
                        ? "text-red-600 hover:bg-red-50 focus:bg-red-50" 
                        : "text-gray-700 hover:bg-gray-50 focus:bg-gray-50"
                    )
                  )}
                  role="menuitem"
                  disabled={item.disabled}
                  onClick={() => {
                    if (!item.disabled) {
                      item.onClick?.();
                      setIsOpen(false);
                    }
                  }}
                >
                  {item.icon && (
                    <span className="mr-2 shrink-0 flex items-center justify-center [&_svg]:w-4 [&_svg]:h-4">
                      {item.icon}
                    </span>
                  )}
                  <span className="truncate">{item.label}</span>
                </button>
              )}
            </div>
          ))}
        </div>,
        document.body
      )}
    </div>
  );
};
