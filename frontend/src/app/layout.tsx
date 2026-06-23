import type { Metadata } from "next";
import { Barlow } from "next/font/google";
import { Toaster } from "@/components/ui/toaster";
import { Toaster as ToasterSonner } from "sonner";
import "./globals.css";

const barlow = Barlow({
  subsets: ["latin"],
  weight: ["300", "400", "500", "600", "700"],
  variable: "--font-sans",
  display: "swap",
});

export const metadata: Metadata = {
  title: {
    default: "Arctech — Painel administrativo",
    template: "%s · Arctech",
  },
  description:
    "Plataforma administrativa Arctech: gestão de clientes, processos, contratos e finanças da assistência previdenciária.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR" className={barlow.variable}>
      <body>
        {children}
        <Toaster />
        <ToasterSonner richColors position="top-right" />
      </body>
    </html>
  );
}
