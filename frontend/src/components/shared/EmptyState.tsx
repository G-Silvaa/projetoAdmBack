interface EmptyStateProps {
  title: string;
  description?: string;
  icon?: React.ReactNode;
  action?: React.ReactNode;
}

export function EmptyState({ title, description, icon, action }: EmptyStateProps) {
  return (
    <div className="surface-card flex flex-col items-center gap-3 p-10 text-center">
      {icon && <div className="text-primary opacity-80">{icon}</div>}
      <h3 className="font-display text-2xl text-primary">{title}</h3>
      {description && (
        <p className="max-w-md text-sm text-muted-foreground">{description}</p>
      )}
      {action}
    </div>
  );
}
