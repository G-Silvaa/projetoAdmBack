import { AuthProvider } from "@/core/auth/AuthProvider";
import { Sidenav } from "@/components/layout/Sidenav";

export default function PrivateLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthProvider>
      <div className="min-h-screen lg:pl-64">
        <Sidenav />
        <main className="mx-auto max-w-7xl px-4 py-6 md:px-8 md:py-8">{children}</main>
      </div>
    </AuthProvider>
  );
}
