import { NextResponse, type NextRequest } from "next/server";
import { ACCESS_TOKEN_KEY } from "@/core/auth/storage-keys";

// Páginas de autenticação: acessíveis sem login, mas redirecionam para /home
// quando o usuário já está logado.
const AUTH_PATHS = new Set(["/login", "/cadastro"]);

// Páginas de marketing (landing/vendas): sempre acessíveis, com ou sem login.
const MARKETING_PATHS = new Set(["/"]);

export function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;

  if (
    pathname.startsWith("/_next") ||
    pathname.startsWith("/api") ||
    pathname.startsWith("/favicon") ||
    pathname.startsWith("/assets") ||
    pathname === "/manifest.webmanifest" ||
    pathname === "/robots.txt"
  ) {
    return NextResponse.next();
  }

  // A landing pública fica liberada para todo mundo.
  if (MARKETING_PATHS.has(pathname)) {
    return NextResponse.next();
  }

  const token = req.cookies.get(ACCESS_TOKEN_KEY)?.value;
  const isPublic = AUTH_PATHS.has(pathname);

  if (!token && !isPublic) {
    const url = req.nextUrl.clone();
    url.pathname = "/login";
    url.searchParams.set("redirectTo", pathname);
    return NextResponse.redirect(url);
  }

  if (token && isPublic) {
    const url = req.nextUrl.clone();
    url.pathname = "/home";
    url.search = "";
    return NextResponse.redirect(url);
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};
