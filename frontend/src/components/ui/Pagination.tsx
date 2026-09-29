import { cn } from '@/utils/cn';
import { ChevronLeft, ChevronRight, MoreHorizontal } from 'lucide-react';

export interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  pageSize?: number;
  onPageSizeChange?: (size: number) => void;
  pageSizeOptions?: number[];
  className?: string;
}

export const Pagination = ({
  currentPage,
  totalPages,
  onPageChange,
  pageSize,
  // onPageSizeChange,
  // pageSizeOptions = [10, 20, 50],
  className,
}: PaginationProps) => {
  // Generate page numbers
  const getPages = () => {
    const pages = [];
    if (totalPages <= 5) {
      for (let i = 1; i <= totalPages; i++) pages.push(i);
    } else {
      if (currentPage <= 3) {
        pages.push(1, 2, 3, 4, '...', totalPages);
      } else if (currentPage >= totalPages - 2) {
        pages.push(1, '...', totalPages - 3, totalPages - 2, totalPages - 1, totalPages);
      } else {
        pages.push(1, '...', currentPage - 1, currentPage, currentPage + 1, '...', totalPages);
      }
    }
    return pages;
  };

  if (totalPages <= 1 && !pageSize) return null;

  return (
    <div className={cn("flex flex-col sm:flex-row items-center justify-center gap-4 w-full", className)}>
      {/* <div className="flex items-center text-sm text-gray-500">
        {onPageSizeChange && pageSize && (
          <div className="flex items-center gap-2">
            <span>Show</span>
            <select
              value={pageSize}
              onChange={(e) => onPageSizeChange(Number(e.target.value))}
              className="bg-transparent border border-gray-500/30 outline-none rounded-full py-1 px-3 text-xs focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500/20"
            >
              {pageSizeOptions.map(size => (
                <option key={size} value={size}>{size}</option>
              ))}
            </select>
            <span>items / page</span>
          </div>
        )}
      </div> */}

      <div className="flex items-center justify-center gap-1">
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage <= 1}
          className="p-1 rounded-full text-gray-500 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
          aria-label="Prev"
        >
          <ChevronLeft className="w-5 h-5" />
        </button>

        {/* Desktop pages */}
        <div className="hidden sm:flex items-center gap-1">
          {getPages().map((page, index) => {
            if (page === '...') {
              return (
                <span key={`dots-${index}`} className="w-8 flex justify-center text-gray-400">
                  <MoreHorizontal className="w-4 h-4" />
                </span>
              );
            }
            const isCurrent = page === currentPage;
            return (
              <button
                key={page}
                onClick={() => onPageChange(page as number)}
                className={cn(
                  "w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium transition-colors",
                  isCurrent 
                    ? "bg-indigo-500 text-white" 
                    : "text-gray-700 hover:bg-gray-100"
                )}
                aria-current={isCurrent ? 'page' : undefined}
              >
                {page}
              </button>
            );
          })}
        </div>

        {/* Mobile simple info */}
        <div className="sm:hidden text-sm font-medium text-gray-700 mx-2">
          {currentPage} / {totalPages}
        </div>

        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage >= totalPages}
          className="p-1 rounded-full text-gray-500 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
          aria-label="Next"
        >
          <ChevronRight className="w-5 h-5" />
        </button>
      </div>
    </div>
  );
};
