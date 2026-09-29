import { cn } from '@/utils/cn';
import { X } from 'lucide-react';
import { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';

export interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: React.ReactNode;
  children: React.ReactNode;
  className?: string;
  hideCloseButton?: boolean;
}

export const Modal = ({
  isOpen,
  onClose,
  title,
  children,
  className,
  hideCloseButton = false,
}: ModalProps) => {
  const modalRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };

    if (isOpen) {
      document.body.style.overflow = 'hidden';
      document.addEventListener('keydown', handleKeyDown);
    } else {
      document.body.style.overflow = '';
    }

    return () => {
      document.body.style.overflow = '';
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center">
      <div 
        className="fixed inset-0 bg-black/40 transition-opacity" 
        onClick={onClose}
        aria-hidden="true"
      />
      <div
        ref={modalRef}
        role="dialog"
        aria-modal="true"
        className={cn(
          "relative w-full sm:max-w-lg max-h-[90vh] bg-white sm:rounded-xl rounded-t-xl sm:rounded-t-xl shadow-[0px_4px_24px_0px] shadow-black/15 overflow-hidden flex flex-col",
          "animate-in slide-in-from-bottom-full sm:zoom-in-95 duration-200",
          className
        )}
      >
        {title && (
          <div className="flex items-center justify-between px-4 py-4 md:px-6 border-b border-gray-500/10 shrink-0">
            <h2 className="text-lg font-semibold text-gray-800">{title}</h2>
            {!hideCloseButton && (
              <button
                onClick={onClose}
                className="p-1 -mr-1 rounded-full text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500/40"
              >
                <X className="w-5 h-5" />
                <span className="sr-only">Đóng</span>
              </button>
            )}
          </div>
        )}
        <div className="p-4 md:p-6 overflow-y-auto">
          {children}
        </div>
      </div>
    </div>,
    document.body
  );
};
