import type { ReactNode } from 'react';

interface AuthLayoutProps {
  children: ReactNode;
}

export const AuthLayout = ({ children }: AuthLayoutProps) => {
  const currentYear = new Date().getFullYear();

  return (
    <div className="min-h-dvh flex flex-col justify-center items-center px-4 py-8 bg-gray-50 relative">
      <div className="absolute inset-0 bg-[radial-gradient(#cbd5e1_1px,transparent_1px)] bg-[size:20px_20px] pointer-events-none z-0"></div>
      <div className="w-full max-w-md flex flex-col items-center relative z-10">
        {/* <div className="mb-6">
          <Logo className="scale-90" />
        </div> */}
        
        <div className="w-full">
          {children}
        </div>
        
        <footer className="mt-8 text-center text-xs text-gray-400">
          <p>&copy; {currentYear} {import.meta.env.VITE_APP_NAME || 'MINI AUTH'}. All rights reserved.</p>
        </footer>
      </div>
    </div>
  );
};
