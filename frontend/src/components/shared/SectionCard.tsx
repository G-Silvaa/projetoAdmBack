import { cn } from "@/lib/utils";

interface SectionCardProps {
  title?: string;
  description?: string;
  actions?: React.ReactNode;
  className?: string;
  children: React.ReactNode;
}

export function SectionCard({
  title,
  description,
  actions,
  className,
  children,
}: SectionCardProps) {
  return (
    <section className={cn("surface-card p-5 md:p-6", className)}>
      {(title || actions) && (
        <div className="mb-4 flex flex-col gap-2 md:flex-row md:items-start md:justify-between">
          <div>
            {title && <h2 className="text-base font-semibold text-primary">{title}</h2>}
            {description && (
              <p className="mt-1 text-sm text-muted-foreground">{description}</p>
            )}
          </div>
          {actions && <div className="flex flex-wrap gap-2">{actions}</div>}
        </div>
      )}
      {children}
    </section>
  );
}
