"use client";

import { FormEvent, useState } from "react";

type PublicParcelSummary = {
  parcelId: string;
  status: string;
  proposedUpi: string;
  administrativeUnitName: string;
  administrativeUnitType: string;
  landUse: string | null;
  tenureClassification: string | null;
};

const defaultApiUrl = "http://localhost:8080";

export function PublicParcelSearch() {
  const [apiUrl, setApiUrl] = useState(defaultApiUrl);
  const [upi, setUpi] = useState("NK-DEM-000001");
  const [result, setResult] = useState<PublicParcelSummary | null>(null);
  const [message, setMessage] = useState("");

  async function search(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    setResult(null);
    const response = await fetch(`${apiUrl}/api/v1/public/parcels/search?upi=${encodeURIComponent(upi)}`, {
      headers: {
        "X-Correlation-Id": crypto.randomUUID()
      }
    });

    if (!response.ok) {
      setMessage(`Recherche indisponible (${response.status})`);
      return;
    }

    const data = (await response.json()) as PublicParcelSummary | null;
    if (!data) {
      setMessage("Aucune information publique autorisee trouvee pour cet identifiant.");
      return;
    }

    setResult(data);
  }

  return (
    <section className="public-search" aria-labelledby="public-search-heading">
      <h2 id="public-search-heading">Recherche publique autorisee</h2>
      <form onSubmit={search}>
        <label>
          API URL
          <input value={apiUrl} onChange={(event) => setApiUrl(event.target.value)} />
        </label>
        <label>
          UPI propose
          <input value={upi} onChange={(event) => setUpi(event.target.value.toUpperCase())} />
        </label>
        <button type="submit">Rechercher</button>
      </form>

      {message ? <p className="message">{message}</p> : null}

      {result ? (
        <article className="parcel-result">
          <h3>{result.proposedUpi}</h3>
          <dl>
            <div>
              <dt>Statut</dt>
              <dd>{result.status}</dd>
            </div>
            <div>
              <dt>Unite administrative</dt>
              <dd>
                {result.administrativeUnitName} ({result.administrativeUnitType})
              </dd>
            </div>
            <div>
              <dt>Usage</dt>
              <dd>{result.landUse ?? "Non publie"}</dd>
            </div>
            <div>
              <dt>Tenure</dt>
              <dd>{result.tenureClassification ?? "Non publie"}</dd>
            </div>
          </dl>
        </article>
      ) : null}
    </section>
  );
}
