import { cn } from '@/utils/cn';

interface PasswordStrengthMeterProps {
  score: number; // 0 to 4
  className?: string;
}

const labels = ['Rất yếu', 'Yếu', 'Trung bình', 'Khá', 'Mạnh'];
const colors = ['bg-red-500', 'bg-red-400', 'bg-amber-400', 'bg-emerald-400', 'bg-emerald-500'];

export const PasswordStrengthMeter = ({ score, className }: PasswordStrengthMeterProps) => {
  const safeScore = Math.max(0, Math.min(4, Math.floor(score)));

  return (
    <div className={cn("mt-2 space-y-1", className)}>
      <div className="flex gap-1 h-1.5">
        {[0, 1, 2, 3].map((index) => {
          const isActive = safeScore > 0 && index < safeScore;
          const colorClass = isActive ? colors[safeScore] : 'bg-gray-200';
          return (
            <div
              key={index}
              className={cn("flex-1 rounded-full transition-colors", colorClass)}
            />
          );
        })}
      </div>
      <p className={cn(
        "text-xs text-right",
        safeScore === 0 ? "text-gray-500" : colors[safeScore].replace('bg-', 'text-')
      )}>
        {safeScore > 0 ? labels[safeScore] : 'Mật khẩu trống'}
      </p>
    </div>
  );
};
