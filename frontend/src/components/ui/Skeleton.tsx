import { cn } from '@/utils/cn';
import type { HTMLAttributes } from 'react';

export const Skeleton = ({ className, ...props }: HTMLAttributes<HTMLDivElement>) => {
  return (
    <div
      className={cn("animate-pulse bg-gray-200 rounded-md", className)}
      {...props}
    />
  );
};

export const SkeletonText = ({ lines = 3, className }: { lines?: number; className?: string }) => {
  return (
    <div className={cn("space-y-2 flex-1 w-full", className)}>
      {Array.from({ length: lines }).map((_, i) => (
        <Skeleton 
          key={i} 
          className={cn("h-4", i === lines - 1 && lines > 1 ? "w-2/3" : "w-full")} 
        />
      ))}
    </div>
  );
};

export const SkeletonCard = () => {
  return (
    <div className="bg-white rounded-xl shadow-[0px_0px_10px_0px] shadow-black/10 overflow-hidden p-6 space-y-4 w-full">
      <div className="flex items-center gap-4">
        <Skeleton className="w-12 h-12 rounded-full shrink-0" />
        <SkeletonText lines={2} className="w-1/2" />
      </div>
      <SkeletonText lines={4} />
      <div className="flex justify-end gap-2 pt-2">
        <Skeleton className="w-20 h-10 rounded-full" />
        <Skeleton className="w-20 h-10 rounded-full" />
      </div>
    </div>
  );
};

export const SkeletonTableRow = () => {
  return (
    <div className="flex items-center space-x-4 p-4 border-b border-gray-500/10">
      <Skeleton className="h-4 w-1/4" />
      <Skeleton className="h-4 w-1/4" />
      <Skeleton className="h-4 w-1/4" />
      <Skeleton className="h-8 w-8 rounded-full ml-auto" />
    </div>
  );
};
