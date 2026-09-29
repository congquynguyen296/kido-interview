import { cn } from '@/utils/cn';
import { Card, CardBody } from './Card';
import type { ReactNode } from 'react';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: ReactNode;
  iconTone?: 'indigo' | 'emerald' | 'amber' | 'red' | 'gray';
  trend?: {
    value: string | number;
    isPositive: boolean;
  };
  className?: string;
}

const toneStyles = {
  indigo: 'bg-indigo-50 text-indigo-500',
  emerald: 'bg-emerald-50 text-emerald-500',
  amber: 'bg-amber-50 text-amber-500',
  red: 'bg-red-50 text-red-500',
  gray: 'bg-gray-50 text-gray-500',
};

export const StatCard = ({ title, value, icon, iconTone = 'indigo', trend, className }: StatCardProps) => {
  return (
    <Card className={className}>
      <CardBody className="p-5 flex items-center gap-4">
        <div className={cn("w-12 h-12 rounded-full flex items-center justify-center shrink-0", toneStyles[iconTone])}>
          {icon}
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm text-gray-500 truncate mb-1">{title}</p>
          <div className="flex items-baseline gap-2">
            <h3 className="text-2xl font-semibold text-gray-800">{value}</h3>
            {trend && (
              <span className={cn(
                "text-xs font-medium",
                trend.isPositive ? "text-emerald-500" : "text-red-500"
              )}>
                {trend.isPositive ? '+' : '-'}{trend.value}
              </span>
            )}
          </div>
        </div>
      </CardBody>
    </Card>
  );
};
