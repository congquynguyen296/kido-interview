import { cn } from '@/utils/cn';

interface TabItem {
  id: string;
  label: string;
  icon?: React.ReactNode;
}

interface TabsProps {
  tabs: TabItem[];
  activeId: string;
  onChange: (id: string) => void;
  className?: string;
}

export const Tabs = ({ tabs, activeId, onChange, className }: TabsProps) => {
  return (
    <div className={cn("relative", className)}>
      <div className="flex overflow-x-auto hide-scrollbar gap-2 p-1">
        {tabs.map((tab) => {
          const isActive = tab.id === activeId;
          return (
            <button
              key={tab.id}
              onClick={() => onChange(tab.id)}
              className={cn(
                "flex items-center gap-2 whitespace-nowrap rounded-full px-4 py-2 text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500/40",
                isActive
                  ? "bg-indigo-500 text-white"
                  : "bg-transparent text-gray-500 hover:bg-gray-100 hover:text-gray-900"
              )}
              aria-selected={isActive}
              role="tab"
            >
              {tab.icon && <span className={cn("w-4 h-4", isActive ? "text-white" : "text-gray-400")}>{tab.icon}</span>}
              {tab.label}
            </button>
          );
        })}
      </div>
    </div>
  );
};
