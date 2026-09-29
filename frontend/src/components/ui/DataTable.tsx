import type { ReactNode } from 'react';
import { EmptyState } from './EmptyState';
import { SkeletonTableRow, SkeletonCard } from './Skeleton';
import { Search } from 'lucide-react';
import { cn } from '@/utils/cn';

export interface Column<T> {
  key: string;
  header: ReactNode;
  render: (item: T) => ReactNode;
  sortable?: boolean;
}

export interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  keyExtractor: (item: T) => string;
  isLoading?: boolean;
  emptyState?: ReactNode;
  renderMobileCard?: (item: T) => ReactNode;
  onSort?: (key: string, direction: 'asc' | 'desc') => void;
  sortKey?: string;
  sortDirection?: 'asc' | 'desc';
}

export function DataTable<T>({
  columns,
  data,
  keyExtractor,
  isLoading,
  emptyState,
  renderMobileCard,
  onSort,
  sortKey,
  sortDirection,
}: DataTableProps<T>) {
  
  if (isLoading) {
    return (
      <div className="w-full">
        <div className="hidden md:block">
          <div className="border border-gray-500/10 rounded-xl overflow-hidden bg-white">
            <div className="bg-gray-50 px-4 py-3 border-b border-gray-500/10">
              <div className="flex space-x-4">
                {columns.map((c, i) => (
                  <div key={c.key} className={cn("text-xs font-medium text-gray-500", i === 0 ? "flex-1" : "w-24")}>
                    {c.header}
                  </div>
                ))}
              </div>
            </div>
            {Array.from({ length: 5 }).map((_, i) => (
              <SkeletonTableRow key={i} />
            ))}
          </div>
        </div>
        <div className="md:hidden space-y-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <div className="w-full border border-gray-500/10 rounded-xl bg-white overflow-hidden">
        {emptyState || (
          <EmptyState
            icon={<Search className="text-gray-300" />}
            title="Không tìm thấy dữ liệu"
          />
        )}
      </div>
    );
  }

  const handleSort = (key: string) => {
    if (!onSort) return;
    if (sortKey === key) {
      onSort(key, sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      onSort(key, 'desc'); // default new sort to desc, or asc depending on preference
    }
  };

  return (
    <div className="w-full">
      {/* Desktop View */}
      <div className="hidden md:block overflow-x-auto rounded-xl border border-gray-500/10 bg-white">
        <table className="w-full text-left text-sm text-gray-700">
          <thead className="bg-gray-50 text-xs text-gray-500 uppercase border-b border-gray-500/10">
            <tr>
              {columns.map((col) => (
                <th
                  key={col.key}
                  className={cn(
                    "px-4 py-3 font-medium whitespace-nowrap",
                    col.sortable && "cursor-pointer hover:bg-gray-100 select-none"
                  )}
                  onClick={() => col.sortable && handleSort(col.key)}
                >
                  <div className="flex items-center gap-1">
                    {col.header}
                    {col.sortable && sortKey === col.key && (
                      <span className="text-indigo-500 text-[10px]">
                        {sortDirection === 'asc' ? '▲' : '▼'}
                      </span>
                    )}
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-500/10">
            {data.map((item) => (
              <tr key={keyExtractor(item)} className="hover:bg-gray-50/50 transition-colors">
                {columns.map((col) => (
                  <td key={col.key} className="px-4 py-3">
                    {col.render(item)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile View */}
      <div className="md:hidden space-y-4">
        {data.map((item) => (
          <div key={keyExtractor(item)}>
            {renderMobileCard ? (
              renderMobileCard(item)
            ) : (
              <div className="bg-white p-4 rounded-xl shadow-[0px_0px_10px_0px] shadow-black/10">
                {columns.map((col, index) => (
                  <div key={col.key} className={cn("py-2", index !== columns.length - 1 && "border-b border-gray-100")}>
                    <div className="text-xs text-gray-500 mb-1">{col.header}</div>
                    <div>{col.render(item)}</div>
                  </div>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
