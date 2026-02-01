"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";

const menuItems = [
  { label: "대시보드", href: "/", disabled: true },
  { label: "뉴스 매핑", href: "/mappings", disabled: false },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="w-56 border-r bg-muted/40 p-4 flex flex-col gap-1">
      <h1 className="text-lg font-bold mb-4 px-2">Stocker Admin</h1>
      <nav className="flex flex-col gap-1">
        {menuItems.map((item) => (
          <Link
            key={item.href}
            href={item.disabled ? "#" : item.href}
            className={cn(
              "rounded-md px-3 py-2 text-sm transition-colors",
              pathname === item.href || pathname.startsWith(item.href + "/")
                ? "bg-primary text-primary-foreground"
                : "hover:bg-accent",
              item.disabled && "opacity-50 pointer-events-none"
            )}
          >
            {item.label}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
