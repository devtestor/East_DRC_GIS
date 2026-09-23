"use client";

import { FormEvent, useState } from "react";

type ApplicationResponse = {
  id: string;
  parcelId: string;
  status: string;
  generatedDocumentId?: string;
  decisionReason?: string;
  purpose: string;
  invoice: {
    invoiceNumber: string;
    amount: string;
    currency: string;
    status: string;
    provider: string;
  };
};

const defaultApiUrl = "http://localhost:8080";

export function ParcelInformationApplication() {
  const [apiUrl, setApiUrl] = useState(defaultApiUrl);
  const [email, setEmail] = useState("phase2.staff@example.test");
  const [password, setPassword] = useState("ChangeMe-Phase2-Local!");
  const [parcelId, setParcelId] = useState("");
  const [purpose, setPurpose] = useState("Verifier les informations publiques de la parcelle");
  const [application, setApplication] = useState<ApplicationResponse | null>(null);
  const [history, setHistory] = useState<ApplicationResponse[]>([]);
  const [message, setMessage] = useState("");

  async function call(path: string, method: string, payload?: unknown) {
    const response = await fetch(`${apiUrl}${path}`, {
      method,
      headers: {
        Authorization: `Basic ${btoa(`${email}:${password}`)}`,
        "Content-Type": "application/json",
        "X-Correlation-Id": crypto.randomUUID()
      },
      body: payload ? JSON.stringify(payload) : undefined
    });
    const body = await response.text();
    if (!response.ok) {
      throw new Error(`Service indisponible (${response.status}): ${body}`);
    }
    return JSON.parse(body) as ApplicationResponse;
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    try {
      setApplication(await call("/api/v1/applications/parcel-information-requests", "POST", { parcelId, purpose }));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Demande impossible");
    }
  }

  async function confirmSandboxPayment() {
    if (!application) return;
    try {
      setApplication(await call(`/api/v1/applications/${application.id}/payments/sandbox-confirmation`, "POST", {
        paymentReference: `SANDBOX-${application.id}`
      }));
      setMessage("Paiement sandbox confirme. La demande est en attente de revue humaine.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Paiement impossible");
    }
  }

  async function viewGeneratedReport() {
    if (!application?.generatedDocumentId) return;
    try {
      const response = await fetch(`${apiUrl}/api/v1/documents/${application.generatedDocumentId}/content`, {
        headers: {
          Authorization: `Basic ${btoa(`${email}:${password}`)}`,
          "X-Correlation-Id": crypto.randomUUID()
        }
      });
      if (!response.ok) throw new Error(`Rapport indisponible (${response.status})`);
      const blob = await response.blob();
      const downloadUrl = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = downloadUrl;
      link.download = "parcel-information-report-sandbox.txt";
      link.click();
      URL.revokeObjectURL(downloadUrl);
      setMessage("Rapport operationnel telecharge. Ce document n'est pas un titre officiel.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Rapport indisponible");
    }
  }

  async function loadHistory() {
    try {
      const response = await fetch(`${apiUrl}/api/v1/applications`, {
        headers: {
          Authorization: `Basic ${btoa(`${email}:${password}`)}`,
          "X-Correlation-Id": crypto.randomUUID()
        }
      });
      if (!response.ok) throw new Error(`Historique indisponible (${response.status})`);
      setHistory((await response.json()) as ApplicationResponse[]);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Historique indisponible");
    }
  }

  async function requestCorrection(item: ApplicationResponse) {
    try {
      const response = await fetch(`${apiUrl}/api/v1/applications/${item.id}/correction-requests`, {
        method: "POST",
        headers: {
          Authorization: `Basic ${btoa(`${email}:${password}`)}`,
          "Content-Type": "application/json",
          "X-Correlation-Id": crypto.randomUUID()
        },
        body: JSON.stringify({ reason: "Le demandeur sollicite une nouvelle verification." })
      });
      if (!response.ok) throw new Error(`Correction impossible (${response.status})`);
      setMessage("Demande de correction envoyee.");
      await loadHistory();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Correction impossible");
    }
  }

  return (
    <section className="public-search" aria-labelledby="application-heading">
      <h2 id="application-heading">Demande d&apos;information parcellaire</h2>
      <p>
        Cette demande produit un dossier de service et non un titre foncier officiel. Le paiement ci-dessous est
        un adaptateur sandbox local.
      </p>
      <form onSubmit={submit}>
        <label>API URL<input value={apiUrl} onChange={(event) => setApiUrl(event.target.value)} /></label>
        <label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
        <label>Mot de passe<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
        <label>UUID parcelle<input required value={parcelId} onChange={(event) => setParcelId(event.target.value)} /></label>
        <label>Motif<input required value={purpose} onChange={(event) => setPurpose(event.target.value)} /></label>
        <button type="submit">Soumettre la demande</button>
      </form>
      {message ? <p className="message">{message}</p> : null}
      {application ? (
        <article className="parcel-result">
          <h3>Demande {application.id}</h3>
          <p>Statut: {application.status}</p>
          <p>Facture: {application.invoice.invoiceNumber} · {application.invoice.amount} {application.invoice.currency} · {application.invoice.status}</p>
          {application.invoice.status === "PENDING" ? (
            <button type="button" onClick={confirmSandboxPayment}>Confirmer paiement sandbox</button>
          ) : null}
          {application.generatedDocumentId ? (
            <button type="button" onClick={viewGeneratedReport}>Voir le rapport autorise</button>
          ) : null}
        </article>
      ) : null}
      <button type="button" onClick={loadHistory}>Charger mon historique</button>
      {history.length > 0 ? (
        <ul className="notification-list" aria-label="Historique des demandes">
          {history.map((item) => (
            <li className="notification read" key={item.id}>
              <strong>{item.id}</strong>
              <span>Statut: {item.status}</span>
              {item.decisionReason ? <span>Motif: {item.decisionReason}</span> : null}
              {item.status === "REJECTED" ? (
                <button type="button" onClick={() => requestCorrection(item)}>Demander une correction</button>
              ) : null}
            </li>
          ))}
        </ul>
      ) : null}
    </section>
  );
}
