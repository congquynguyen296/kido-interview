import { cn } from '@/utils/cn';

interface DonutSegment {
  label: string;
  value: number;
  color: string;
}

interface DonutChartProps {
  data: DonutSegment[];
  size?: number;
  thickness?: number;
  className?: string;
}

export const DonutChart = ({ data, size = 160, thickness = 24, className }: DonutChartProps) => {
  if (!data || data.length === 0) return null;

  const total = data.reduce((acc, curr) => acc + curr.value, 0) || 1;
  const center = size / 2;
  const radius = center - thickness / 2;
  const circumference = 2 * Math.PI * radius;

  return (
    <div className={cn("flex items-center gap-6", className)}>
      <div className="relative" style={{ width: size, height: size }}>
        <svg width={size} height={size} className="transform -rotate-90">
          <circle
            cx={center}
            cy={center}
            r={radius}
            fill="transparent"
            stroke="#f3f4f6"
            strokeWidth={thickness}
          />
          {data.map((item, i) => {
            const dashArray = (item.value / total) * circumference;
            const prevSum = data.slice(0, i).reduce((acc, curr) => acc + curr.value, 0);
            const dashOffset = (prevSum / total) * circumference;
            
            if (item.value === 0) return null;

            return (
              <circle
                key={i}
                cx={center}
                cy={center}
                r={radius}
                fill="transparent"
                stroke={item.color}
                strokeWidth={thickness}
                strokeDasharray={`${dashArray} ${circumference}`}
                strokeDashoffset={-dashOffset}
                className="transition-all duration-1000 ease-out"
              />
            );
          })}
        </svg>
        <div className="absolute inset-0 flex items-center justify-center flex-col">
          <span className="text-2xl font-semibold text-gray-800">{total}</span>
          <span className="text-xs text-gray-500">Tổng</span>
        </div>
      </div>
      
      {/* Legend */}
      <div className="flex flex-col gap-2">
        {data.map((item, i) => (
          <div key={i} className="flex items-center gap-2 text-sm">
            <span className="w-3 h-3 rounded-full shrink-0" style={{ backgroundColor: item.color }} />
            <span className="text-gray-600">{item.label}</span>
            <span className="font-medium ml-auto text-gray-800">{item.value}</span>
          </div>
        ))}
      </div>
    </div>
  );
};
