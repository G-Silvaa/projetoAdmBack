import { cn } from "@/lib/utils";

interface StatCardProps {
  label: string;
  value: string | number;
  meta?: string;
  className?: string;
  highlight?: boolean;
}

export function StatCard({ label, value, meta, className, highlight }: StatCardProps) {
  return (
    <div className={cn("stat-card", highlight && "ring-1 ring-secondary/40", className)}>
      <p className="stat-label">{label}</p>
      <p className="stat-value">{value}</p>
      {meta && <p className="stat-meta">{meta}</p>}
    </div>
  );
}
