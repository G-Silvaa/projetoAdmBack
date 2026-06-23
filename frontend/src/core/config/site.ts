export const site = {
  apiUrl:
    process.env.NEXT_PUBLIC_API_URL ?? "https://liv-api-demo.onrender.com/liv-api/",
  apiServerUrl:
    process.env.API_URL ??
    process.env.NEXT_PUBLIC_API_URL ??
    "https://liv-api-demo.onrender.com/liv-api/",
  name: "Arctech",
  fullName: "Arctech — Assistência Previdenciária",
};
