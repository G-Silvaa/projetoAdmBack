"use client";

import { useMemo, useState } from "react";
import { ChevronLeft, ChevronRight, Loader2, Search } from "lucide-react";

import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { cn } from "@/lib/utils";

interface DataTableProps<TRow extends Record<string, unknown>> {
  data: TRow[];
  columns?: Array<keyof TRow & string>;
  excludeColumns?: Array<keyof TRow & string>;
  isLoading?: boolean;
  pageSize?: number;
  emptyMessage?: string;
  searchLabel?: string;
  searchPlaceholder?: string;
  showSearch?: boolean;
  rowActions?: (row: TRow) => React.ReactNode;
  onRowClick?: (row: TRow) => void;
  className?: string;
}

export function DataTable<TRow extends Record<string, unknown>>({
  data,
  columns,
  excludeColumns = [],
  isLoading,
  pageSize = 10,
  emptyMessage = "Nenhum registro encontrado.",
  searchLabel = "Busca rápida",
  searchPlaceholder = "Filtre por qualquer coluna...",
  showSearch = true,
  rowActions,
  onRowClick,
  className,
}: DataTableProps<TRow>) {
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);

  const resolvedColumns = useMemo<string[]>(() => {
    if (columns?.length) return columns;
    const first = data[0];
    if (!first) return [];
    return Object.keys(first).filter(
      (k) => !k.startsWith("_") && k !== "id" && !excludeColumns.includes(k),
    );
  }, [columns, data, excludeColumns]);

  const filtered = useMemo(() => {
    if (!search.trim()) return data;
    const term = search.trim().toLowerCase();
    return data.filter((row) =>
      resolvedColumns.some((col) => String(row[col] ?? "").toLowerCase().includes(term)),
    );
  }, [data, search, resolvedColumns]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const currentPage = Math.min(page, totalPages - 1);
  const start = currentPage * pageSize;
  const pageRows = filtered.slice(start, start + pageSize);

  return (
    <div className={cn("space-y-3", className)}>
      {showSearch && (
        <div className="space-y-1.5 max-w-sm">
          <Label htmlFor="dt-search" className="text-xs font-medium text-text-list">
            {searchLabel}
          </Label>
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              id="dt-search"
              placeholder={searchPlaceholder}
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              className="pl-9"
            />
          </div>
        </div>
      )}

      <div className="surface-card overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-secondary/10 hover:bg-secondary/10">
              {resolvedColumns.map((col) => (
                <TableHead key={col} className="text-primary font-semibold whitespace-nowrap">
                  {col}
                </TableHead>
              ))}
              {rowActions && <TableHead className="text-right text-primary">Ações</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell
                  colSpan={resolvedColumns.length + (rowActions ? 1 : 0)}
                  className="py-10 text-center"
                >
                  <Loader2 className="mx-auto size-5 animate-spin text-primary" />
                </TableCell>
              </TableRow>
            ) : pageRows.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={resolvedColumns.length + (rowActions ? 1 : 0)}
                  className="py-10 text-center text-sm text-muted-foreground"
                >
                  {emptyMessage}
                </TableCell>
              </TableRow>
            ) : (
              pageRows.map((row, idx) => (
                <TableRow
                  key={idx}
                  className={cn(onRowClick && "cursor-pointer")}
                  onClick={() => onRowClick?.(row)}
                >
                  {resolvedColumns.map((col) => (
                    <TableCell key={col} className="whitespace-nowrap text-text-list">
                      {String(row[col] ?? "")}
                    </TableCell>
                  ))}
                  {rowActions && (
                    <TableCell className="text-right" onClick={(e) => e.stopPropagation()}>
                      <div className="inline-flex items-center gap-1">{rowActions(row)}</div>
                    </TableCell>
                  )}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {filtered.length > pageSize && (
        <div className="flex items-center justify-between text-sm text-muted-foreground">
          <span>
            Mostrando {start + 1}–{Math.min(start + pageSize, filtered.length)} de {filtered.length}
          </span>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={currentPage === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              <ChevronLeft className="size-4" />
            </Button>
            <span>
              {currentPage + 1} / {totalPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              disabled={currentPage >= totalPages - 1}
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            >
              <ChevronRight className="size-4" />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
