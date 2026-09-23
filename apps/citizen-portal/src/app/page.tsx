import { PublicParcelSearch } from "./public-parcel-search";
import { ParcelInformationApplication } from "./parcel-information-application";
import { NotificationInbox } from "./notification-inbox";

export default function Home() {
  return (
    <main className="shell">
      <section className="notice" aria-labelledby="service-title">
        <p className="eyebrow">Phase 2 foundation</p>
        <h1 id="service-title">Services fonciers proposes pour l'Est de la RDC</h1>
        <p>
          Ce portail prepare les demandes, le suivi et la consultation publique autorisee. Il ne
          constitue pas un registre foncier officiel ni un titre legal.
        </p>
      </section>
      <PublicParcelSearch />
      <ParcelInformationApplication />
      <NotificationInbox />
    </main>
  );
}
