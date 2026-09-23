import type { Metadata } from "next";
import "./styles.css";

export const metadata: Metadata = {
  title: "EDRC Land GIS Staff Console",
  description: "Staff console foundation for controlled land workflows"
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="fr">
      <body>{children}</body>
    </html>
  );
}
