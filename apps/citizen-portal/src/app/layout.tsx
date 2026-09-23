import type { Metadata } from "next";
import "./styles.css";

export const metadata: Metadata = {
  title: "Eastern DRC Land Services",
  description: "Proposed public land-service portal for Eastern DRC"
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="fr">
      <body>{children}</body>
    </html>
  );
}
