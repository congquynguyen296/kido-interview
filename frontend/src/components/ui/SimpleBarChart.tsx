import { cn } from '@/utils/cn';

interface DataPoint {
  label: string;
  value: number;
}

interface SimpleBarChartProps {
  data: DataPoint[];
  height?: number;
  className?: string;
}

export const SimpleBarChart = ({ data, height = 200, className }: SimpleBarChartProps) => {
  if (!data || data.length === 0) return null;

  const maxValue = Math.max(...data.map(d => d.value), 1); // Avoid div by 0

  return (
    <div className={cn("w-full", className)}>
      <div 
        className="flex items-end gap-2 overflow-x-auto hide-scrollbar pb-2 pt-6"
        style={{ height: `${height}px` }}
      >
        {data.map((item, i) => {
          const percentage = (item.value / maxValue) * 100;
          return (
            <div key={i} className="flex flex-col items-center flex-1 min-w-[24px] group relative h-full justify-end">
              {/* Tooltip */}
              <div className="opacity-0 group-hover:opacity-100 transition-opacity absolute -top-8 bg-gray-800 text-white text-xs px-2 py-1 rounded whitespace-nowrap z-10 pointer-events-none">
                {item.label}: {item.value}
              </div>
              
              {/* Bar */}
              <div 
                className="w-full bg-indigo-500 hover:bg-indigo-400 rounded-t-sm transition-all duration-300 relative"
                style={{ height: `${Math.max(percentage, 2)}%` }} // At least 2% to show something
              >
              </div>
              
              {/* X-axis Label (only show some if many) */}
              <span className="text-[10px] text-gray-500 mt-2 truncate max-w-full block">
                {data.length > 15 ? (i % Math.ceil(data.length / 7) === 0 ? item.label : '') : item.label}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
};
