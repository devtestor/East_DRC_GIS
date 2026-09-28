import type { FormEvent } from "react";

import { documentExportBlockers } from "./document-governance";
import { Result, type ApiResult } from "./staff-console-components";

type StaffConsoleCopy = Record<string, string>;

export type DocumentVersionResponse = {
  id: string;
  documentId: string;
  versionNumber: number;
  objectStorageKey: string;
  originalFilename: string;
  mediaType: string;
  sizeBytes: number;
  checksumSha256: string;
  malwareScanStatus: string;
  digitalSignatureStatus: string;
  safetyStatus: string;
  safetyReason: string | null;
  safetyReviewedByUserId: string | null;
  safetyReviewedBy: string | null;
  safetyReviewedAt: string | null;
  uploadedByUserId: string;
  uploadedBy: string;
  uploadedAt: string;
};

export type DocumentResponse = {
  id: string;
  documentType: string;
  ownerType: string;
  ownerId: string;
  title: string;
  classification: string;
  retentionCategory: string;
  accessPolicy: string;
  custodianOrganizationId: string | null;
  custodianRoleCode: string | null;
  legalHold: boolean;
  createdByUserId: string;
  createdBy: string;
  createdAt: string;
  effectiveAt: string | null;
  versions: DocumentVersionResponse[];
};

export type DocumentGovernanceSummary = {
  total: number;
  pendingScan: number;
  failedScan: number;
  invalidSignature: number;
  quarantined: number;
};

export function DocumentIntakePanel({
  appendDocumentVersion,
  copy,
  createDocumentEvidence,
  decideDocumentVersionQuarantine,
  documentDownloadResult,
  documentGovernanceSummary,
  documentOwnerId,
  documentOwnerType,
  documentQuarantineResult,
  documentResult,
  documents,
  documentSafetyResult,
  documentVersionResult,
  downloadDocumentContent,
  loadDocumentMetadata,
  loadDocumentsByOwner,
  parcelId,
  selectedDocumentId,
  selectedDocumentLatestVersion,
  setDocumentOwnerId,
  setDocumentOwnerType,
  setSelectedDocumentId,
  updateDocumentVersionSafetyStatus
}: Readonly<{
  appendDocumentVersion: (event: FormEvent<HTMLFormElement>) => void;
  copy: StaffConsoleCopy;
  createDocumentEvidence: (event: FormEvent<HTMLFormElement>) => void;
  decideDocumentVersionQuarantine: (event: FormEvent<HTMLFormElement>) => void;
  documentDownloadResult: ApiResult | null;
  documentGovernanceSummary: DocumentGovernanceSummary;
  documentOwnerId: string;
  documentOwnerType: string;
  documentQuarantineResult: ApiResult | null;
  documentResult: ApiResult | null;
  documents: DocumentResponse[];
  documentSafetyResult: ApiResult | null;
  documentVersionResult: ApiResult | null;
  downloadDocumentContent: () => void;
  loadDocumentMetadata: () => void;
  loadDocumentsByOwner: () => void;
  parcelId: string;
  selectedDocumentId: string;
  selectedDocumentLatestVersion: DocumentVersionResponse | null;
  setDocumentOwnerId: (value: string) => void;
  setDocumentOwnerType: (value: string) => void;
  setSelectedDocumentId: (value: string) => void;
  updateDocumentVersionSafetyStatus: (event: FormEvent<HTMLFormElement>) => void;
}>) {
  const ownerIdValue = documentOwnerId || parcelId;

  return (
    <section className="panel" aria-labelledby="document-intake-heading">
      <div className="section-row">
        <div>
          <p className="eyebrow">{copy.documentEvidence}</p>
          <h2 id="document-intake-heading">{copy.documentIntake}</h2>
          <p className="hint">{copy.documentIntakeHint}</p>
        </div>
        <button type="button" onClick={loadDocumentMetadata}>
          {copy.refreshDocument}
        </button>
      </div>

      <form className="nested-form" onSubmit={createDocumentEvidence}>
        <h3>Creer une preuve documentaire</h3>
        <div className="form-grid">
          <label>
            Type document
            <select name="documentType" defaultValue="SURVEY_PLAN">
              <option value="IDENTITY_EVIDENCE">Preuve identite</option>
              <option value="SURVEY_PLAN">Plan de bornage</option>
              <option value="TRANSFER_AGREEMENT">Accord de transfert</option>
              <option value="COURT_ORDER">Decision judiciaire</option>
              <option value="VALUATION_REPORT">Rapport de valeur</option>
              <option value="SERVICE_RECORD">Dossier de service</option>
            </select>
          </label>
          <label>
            Proprietaire logique
            <select
              name="ownerType"
              value={documentOwnerType}
              onChange={(event) => setDocumentOwnerType(event.target.value)}
            >
              <option value="parcel">Parcelle</option>
              <option value="party">Partie</option>
              <option value="application">Application</option>
              <option value="workflow-task">Tache workflow</option>
              <option value="dispute-case">Dossier litige</option>
            </select>
          </label>
          <label>
            UUID proprietaire
            <input
              name="ownerId"
              value={ownerIdValue}
              onChange={(event) => setDocumentOwnerId(event.target.value)}
              placeholder="UUID parcelle, partie, application ou tache"
              required
            />
          </label>
          <label>
            Titre
            <input name="title" defaultValue="Plan de bornage fictif - preuve locale" required />
          </label>
          <label>
            Classification
            <select name="classification" defaultValue="LEGAL_EVIDENCE">
              <option value="STAFF_OPERATIONAL">Operationnel staff</option>
              <option value="PROTECTED_PERSONAL">Personnel protege</option>
              <option value="LEGAL_EVIDENCE">Preuve legale</option>
              <option value="FINANCIAL">Financier</option>
              <option value="SECURITY">Securite</option>
              <option value="PUBLIC">Public approuve</option>
            </select>
          </label>
          <label>
            Retention
            <input name="retentionCategory" defaultValue="LEGAL_RECORD" />
          </label>
          <label>
            Politique d&apos;acces
            <select name="accessPolicy" defaultValue="workflow-task-and-authorized-staff">
              <option value="workflow-task-and-authorized-staff">Workflow + agents autorises</option>
              <option value="restricted-staff">Agents restreints</option>
              <option value="application-applicant-and-authorized-staff">Demandeur + agents autorises</option>
            </select>
          </label>
          <label>
            Organisation gardienne
            <input name="custodianOrganizationId" placeholder="UUID optionnel" />
          </label>
          <label>
            Role gardien
            <input name="custodianRoleCode" defaultValue="CADASTRAL_OFFICER" />
          </label>
          <label>
            Date effective
            <input name="effectiveAt" type="datetime-local" />
          </label>
          <label className="checkbox-label">
            <input name="legalHold" type="checkbox" />
            Conservation legale active
          </label>
          <label>
            Cle objet chiffree
            <input name="objectStorageKey" defaultValue="documents/fictional/survey-plan-v1.pdf" />
          </label>
          <label>
            Nom fichier
            <input name="originalFilename" defaultValue="survey-plan-v1.pdf" />
          </label>
          <label>
            Type media
            <input name="mediaType" defaultValue="application/pdf" />
          </label>
          <label>
            Taille octets
            <input name="sizeBytes" type="number" min="0" defaultValue="128" />
          </label>
          <label>
            SHA-256
            <input
              name="checksumSha256"
              defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
              pattern="[A-Fa-f0-9]{64}"
            />
          </label>
          <label>
            Scan malware
            <select name="malwareScanStatus" defaultValue="PENDING">
              <option value="PENDING">En attente</option>
              <option value="PASSED">Valide</option>
              <option value="FAILED">Echec</option>
              <option value="NOT_REQUIRED">Non requis</option>
            </select>
          </label>
          <label>
            Signature numerique
            <select name="digitalSignatureStatus" defaultValue="UNSIGNED">
              <option value="UNSIGNED">Non signe</option>
              <option value="VALID">Valide</option>
              <option value="INVALID">Invalide</option>
              <option value="UNKNOWN">Inconnue</option>
            </select>
          </label>
        </div>
        <button type="submit">Creer document/version 1</button>
        <Result result={documentResult} />
      </form>

      <div className="form-grid">
        <label>
          Document selectionne
          <input
            value={selectedDocumentId}
            onChange={(event) => setSelectedDocumentId(event.target.value)}
            placeholder="UUID document"
          />
        </label>
        <label>
          Recherche owner type
          <input value={documentOwnerType} onChange={(event) => setDocumentOwnerType(event.target.value)} />
        </label>
        <label>
          Recherche owner UUID
          <input value={ownerIdValue} onChange={(event) => setDocumentOwnerId(event.target.value)} />
        </label>
      </div>
      <div className="decision-form">
        <button type="button" onClick={loadDocumentsByOwner}>
          Charger documents du proprietaire
        </button>
        <button type="button" onClick={downloadDocumentContent}>
          Telecharger contenu sandbox
        </button>
        <Result result={documentDownloadResult} />
      </div>

      <DocumentGovernanceSummaryGrid summary={documentGovernanceSummary} />

      <form className="nested-form" onSubmit={appendDocumentVersion}>
        <h3>Ajouter une version immutable</h3>
        <p className="hint">
          Une nouvelle version conserve les versions precedentes et met a jour les statuts de scan/signature sans
          remplacer l&apos;historique.
        </p>
        <div className="form-grid">
          <input type="hidden" name="documentId" value={selectedDocumentId} />
          <label>
            Cle objet v2
            <input name="objectStorageKey" defaultValue="documents/fictional/survey-plan-v2.pdf" />
          </label>
          <label>
            Nom fichier v2
            <input name="originalFilename" defaultValue="survey-plan-v2.pdf" />
          </label>
          <label>
            Type media
            <input name="mediaType" defaultValue="application/pdf" />
          </label>
          <label>
            Taille octets
            <input name="sizeBytes" type="number" min="0" defaultValue="192" />
          </label>
          <label>
            SHA-256 v2
            <input
              name="checksumSha256"
              defaultValue="bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
              pattern="[A-Fa-f0-9]{64}"
            />
          </label>
          <label>
            Scan malware
            <select name="malwareScanStatus" defaultValue="PASSED">
              <option value="PENDING">En attente</option>
              <option value="PASSED">Valide</option>
              <option value="FAILED">Echec</option>
              <option value="NOT_REQUIRED">Non requis</option>
            </select>
          </label>
          <label>
            Signature numerique
            <select name="digitalSignatureStatus" defaultValue="UNKNOWN">
              <option value="UNSIGNED">Non signe</option>
              <option value="VALID">Valide</option>
              <option value="INVALID">Invalide</option>
              <option value="UNKNOWN">Inconnue</option>
            </select>
          </label>
        </div>
        <button type="submit">Ajouter version</button>
        <Result result={documentVersionResult} />
      </form>

      <form
        className="nested-form"
        key={`${selectedDocumentId}-${selectedDocumentLatestVersion?.id ?? "no-version"}`}
        onSubmit={updateDocumentVersionSafetyStatus}
      >
        <h3>Revue scan/signature de version</h3>
        <p className="hint">
          Met a jour seulement les statuts de securite de la version selectionnee. Le fichier, le checksum et les
          versions precedentes restent immuables; l&apos;API enregistre un evenement d&apos;audit.
        </p>
        <div className="form-grid">
          <label>
            Document UUID
            <input name="documentId" defaultValue={selectedDocumentId} placeholder="UUID document" />
          </label>
          <label>
            Version UUID
            <input
              name="versionId"
              defaultValue={selectedDocumentLatestVersion?.id ?? ""}
              placeholder="UUID version"
            />
          </label>
          <label>
            Scan malware
            <select name="malwareScanStatus" defaultValue={selectedDocumentLatestVersion?.malwareScanStatus ?? "PASSED"}>
              <option value="PENDING">En attente</option>
              <option value="PASSED">Valide</option>
              <option value="FAILED">Echec</option>
              <option value="NOT_REQUIRED">Non requis</option>
            </select>
          </label>
          <label>
            Signature numerique
            <select
              name="digitalSignatureStatus"
              defaultValue={selectedDocumentLatestVersion?.digitalSignatureStatus ?? "VALID"}
            >
              <option value="UNSIGNED">Non signe</option>
              <option value="VALID">Valide</option>
              <option value="INVALID">Invalide</option>
              <option value="UNKNOWN">Inconnue</option>
            </select>
          </label>
          <label>
            Motif de revue
            <input name="reason" defaultValue="Scan sandbox termine et verification documentaire effectuee" />
          </label>
        </div>
        <button type="submit">Mettre a jour les statuts</button>
        <Result result={documentSafetyResult} />
      </form>

      <form
        className="nested-form"
        key={`quarantine-${selectedDocumentId}-${selectedDocumentLatestVersion?.id ?? "no-version"}`}
        onSubmit={decideDocumentVersionQuarantine}
      >
        <h3>Quarantaine / liberation de version</h3>
        <p className="hint">
          Controle operationnel distinct du scan: une version en quarantaine reste conservee mais bloque les exports
          gouvernes jusqu&apos;a liberation explicite apres revue humaine.
        </p>
        <div className="form-grid">
          <label>
            Document UUID
            <input name="documentId" defaultValue={selectedDocumentId} placeholder="UUID document" />
          </label>
          <label>
            Version UUID
            <input
              name="versionId"
              defaultValue={selectedDocumentLatestVersion?.id ?? ""}
              placeholder="UUID version"
            />
          </label>
          <label>
            Decision
            <select
              name="action"
              defaultValue={selectedDocumentLatestVersion?.safetyStatus === "QUARANTINED" ? "release" : "quarantine"}
            >
              <option value="quarantine">Mettre en quarantaine</option>
              <option value="release">Liberer apres revue</option>
            </select>
          </label>
          <label>
            Motif obligatoire
            <input name="reason" defaultValue="Revue humaine documentee par l&apos;agent securite documentaire" />
          </label>
        </div>
        <button type="submit">Enregistrer decision de securite</button>
        <Result result={documentQuarantineResult} />
      </form>

      <DocumentEvidenceList documents={documents} onSelectDocument={setSelectedDocumentId} />
    </section>
  );
}

function DocumentGovernanceSummaryGrid({ summary }: Readonly<{ summary: DocumentGovernanceSummary }>) {
  return (
    <div className="readiness-grid" aria-label="Synthese de gouvernance documentaire">
      <article className="readiness-check ready">
        <strong>{summary.total}</strong>
        <span>Documents charges</span>
        <small>Metadonnees accessibles selon les politiques serveur.</small>
      </article>
      <article className={`readiness-check ${summary.pendingScan > 0 ? "attention" : "ready"}`}>
        <strong>{summary.pendingScan}</strong>
        <span>Scan malware en attente</span>
        <small>Bloque tout export jusqu&apos;au resultat du scan.</small>
      </article>
      <article className={`readiness-check ${summary.failedScan > 0 ? "attention" : "ready"}`}>
        <strong>{summary.failedScan}</strong>
        <span>Scan malware echoue</span>
        <small>Blocage dur: nouvelle version saine ou decision de quarantaine requise.</small>
      </article>
      <article className={`readiness-check ${summary.invalidSignature > 0 ? "attention" : "ready"}`}>
        <strong>{summary.invalidSignature}</strong>
        <span>Signature invalide</span>
        <small>Blocage dur pour les exports gouvernes.</small>
      </article>
      <article className={`readiness-check ${summary.quarantined > 0 ? "attention" : "ready"}`}>
        <strong>{summary.quarantined}</strong>
        <span>Versions en quarantaine</span>
        <small>Export bloque jusqu&apos;a liberation humaine documentee.</small>
      </article>
    </div>
  );
}

function DocumentEvidenceList({
  documents,
  onSelectDocument
}: Readonly<{
  documents: DocumentResponse[];
  onSelectDocument: (documentId: string) => void;
}>) {
  if (documents.length === 0) {
    return <p className="hint">Aucun document charge.</p>;
  }

  return (
    <div className="evidence-list" aria-label="Documents controles">
      {documents.map((item) => {
        const blockers = documentExportBlockers(item);
        return (
          <article className="record-card" key={item.id}>
            <div className="section-row">
              <strong>{item.title}</strong>
              <button type="button" onClick={() => onSelectDocument(item.id)}>
                Selectionner
              </button>
            </div>
            <span>
              {item.documentType} · {item.classification} · {item.accessPolicy}
            </span>
            <small>
              Retention: {item.retentionCategory} · Legal hold: {item.legalHold ? "oui" : "non"} · Gardien:{" "}
              {item.custodianRoleCode ?? "non defini"} · Cree par {item.createdBy}
            </small>
            <small>
              Owner: {item.ownerType}/{item.ownerId} · Document UUID: {item.id}
            </small>
            {blockers.length > 0 ? (
              <small className="warning-text">
                Export bloque: {blockers.join(", ")}
              </small>
            ) : (
              <small>Export documentaire: aucun bloqueur de scan/signature sur la derniere version.</small>
            )}
            {item.versions.map((version) => (
              <small key={version.id}>
                v{version.versionNumber}: {version.originalFilename} · {version.malwareScanStatus} ·{" "}
                {version.digitalSignatureStatus} · {version.safetyStatus} · {version.checksumSha256}
                {version.safetyReason ? ` · Motif securite: ${version.safetyReason}` : ""}
              </small>
            ))}
          </article>
        );
      })}
    </div>
  );
}
