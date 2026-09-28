import { FormEvent } from "react";

import { Result, type ApiResult } from "./staff-console-components";

export type DocumentExportRequestResponse = {
  id: string;
  documentId: string;
  purpose: string;
  redactionPlanned: boolean;
  status: string;
  redactionRequired: boolean;
  approvalRequired: boolean;
  blockers: string[];
  workflowTaskId: string | null;
  requestedByUserId: string;
  requestedBy: string;
  requestedAt: string;
  decidedByUserId: string | null;
  decidedBy: string | null;
  decidedAt: string | null;
  decisionReason: string | null;
};

export type DocumentExportPackageResponse = {
  id: string;
  exportRequestId: string;
  documentId: string;
  documentVersionId: string;
  objectStorageKey: string;
  manifestSha256: string;
  packageSha256: string;
  packageSizeBytes: number;
  expiresAt: string;
  generatedByUserId: string;
  generatedBy: string;
  generatedAt: string;
  downloadedAt: string | null;
  downloadCount: number;
  deliveryToken: string | null;
};

type ControlledExportPanelProps = {
  copy: Record<string, string>;
  createDocumentExportRequest: (event: FormEvent<HTMLFormElement>) => Promise<void>;
  loadDocumentExportRequest: () => Promise<void>;
  completeDocumentExportReview: (event: FormEvent<HTMLFormElement>) => Promise<void>;
  generateDocumentExportPackage: (event: FormEvent<HTMLFormElement>) => Promise<void>;
  loadDocumentExportPackage: () => Promise<void>;
  downloadDocumentExportPackage: () => Promise<void>;
  exportRequestId: string;
  setExportRequestId: (value: string) => void;
  exportPackageId: string;
  setExportPackageId: (value: string) => void;
  exportDeliveryToken: string;
  setExportDeliveryToken: (value: string) => void;
  exportRequest: DocumentExportRequestResponse | null;
  exportPackage: DocumentExportPackageResponse | null;
  exportRequestResult: ApiResult | null;
  exportReviewResult: ApiResult | null;
  exportPackageResult: ApiResult | null;
  exportDownloadResult: ApiResult | null;
};

export function ControlledExportPanel({
  copy,
  createDocumentExportRequest,
  loadDocumentExportRequest,
  completeDocumentExportReview,
  generateDocumentExportPackage,
  loadDocumentExportPackage,
  downloadDocumentExportPackage,
  exportRequestId,
  setExportRequestId,
  exportPackageId,
  setExportPackageId,
  exportDeliveryToken,
  setExportDeliveryToken,
  exportRequest,
  exportPackage,
  exportRequestResult,
  exportReviewResult,
  exportPackageResult,
  exportDownloadResult
}: ControlledExportPanelProps) {
  return (
    <section className="panel" aria-labelledby="export-governance-heading">
      <div className="section-row">
        <div>
          <p className="eyebrow">{copy.dataGovernance}</p>
          <h2 id="export-governance-heading">{copy.controlledExports}</h2>
          <p className="hint">{copy.controlledExportsHint}</p>
        </div>
        <button type="button" onClick={loadDocumentExportRequest}>
          {copy.refresh}
        </button>
      </div>

      <form className="nested-form" onSubmit={createDocumentExportRequest}>
        <h3>{copy.createExportRequest}</h3>
        <div className="form-grid">
          <label>
            {copy.sourceDocumentUuid}
            <input name="documentId" required placeholder={copy.documentUuidPlaceholder} />
          </label>
          <label>
            {copy.exportPurpose}
            <input name="purpose" defaultValue={copy.defaultExportPurpose} required />
          </label>
          <label className="checkbox-label">
            <input name="redactionPlanned" type="checkbox" defaultChecked />
            {copy.redactionPlanConfirmed}
          </label>
        </div>
        <button type="submit">{copy.submitRequest}</button>
        <Result result={exportRequestResult} />
      </form>

      <div className="form-grid">
        <label>
          {copy.exportRequestUuid}
          <input value={exportRequestId} onChange={(event) => setExportRequestId(event.target.value)} />
        </label>
        <label>
          {copy.packageUuid}
          <input value={exportPackageId} onChange={(event) => setExportPackageId(event.target.value)} />
        </label>
        <label>
          {copy.deliveryToken}
          <input
            value={exportDeliveryToken}
            onChange={(event) => setExportDeliveryToken(event.target.value)}
            placeholder={copy.deliveryTokenPlaceholder}
          />
        </label>
      </div>

      {exportRequest ? (
        <article className="record-card">
          <strong>
            {copy.requestLabel} {exportRequest.status}
          </strong>
          <span>
            {copy.documentLabel} {exportRequest.documentId}
          </span>
          <small>
            {copy.redactionRequired}: {exportRequest.redactionRequired ? copy.yes : copy.no} ·{" "}
            {copy.approvalRequired}: {exportRequest.approvalRequired ? copy.yes : copy.no} · {copy.task}:{" "}
            {exportRequest.workflowTaskId ?? copy.none}
          </small>
          {exportRequest.blockers.length > 0 ? (
            <small>
              {copy.blockers}: {exportRequest.blockers.join(", ")}
            </small>
          ) : null}
        </article>
      ) : (
        <p className="hint">{copy.noExportRequestLoaded}</p>
      )}

      <form className="nested-form" onSubmit={completeDocumentExportReview}>
        <h3>{copy.closeGovernanceReview}</h3>
        <p className="hint">{copy.closeGovernanceReviewHint}</p>
        <div className="form-grid">
          <input type="hidden" name="exportRequestId" value={exportRequestId} />
          <label>
            {copy.decision}
            <select name="decision" defaultValue="APPROVE">
              <option value="APPROVE">{copy.approveRedactedExport}</option>
              <option value="REJECT">{copy.rejectExport}</option>
            </select>
          </label>
          <label>
            {copy.reason}
            <input name="reason" defaultValue={copy.defaultExportReviewReason} />
          </label>
        </div>
        <button type="submit">{copy.recordExportReview}</button>
        <Result result={exportReviewResult} />
      </form>

      <form className="nested-form" onSubmit={generateDocumentExportPackage}>
        <h3>{copy.generateSecurePackage}</h3>
        <div className="form-grid">
          <input type="hidden" name="exportRequestId" value={exportRequestId} />
          <label>
            {copy.tokenExpiryMinutes}
            <input name="expiresInMinutes" type="number" min="5" max="1440" defaultValue="30" />
          </label>
        </div>
        <button type="submit">{copy.generatePackage}</button>
        <button type="button" onClick={loadDocumentExportPackage}>
          {copy.loadPackage}
        </button>
        <Result result={exportPackageResult} />
      </form>

      {exportPackage ? (
        <article className="record-card">
          <strong>
            {copy.packageLabel} {exportPackage.id}
          </strong>
          <span>SHA-256: {exportPackage.packageSha256}</span>
          <small>
            {copy.manifest}: {exportPackage.manifestSha256} · {copy.size}: {exportPackage.packageSizeBytes}{" "}
            {copy.bytes} · {copy.expiresAt} {new Date(exportPackage.expiresAt).toLocaleString("fr-CD")}
          </small>
          <small>
            {copy.downloads}: {exportPackage.downloadCount} · {copy.tokenDisplayed}:{" "}
            {exportPackage.deliveryToken ? copy.yesSaveNow : copy.no}
          </small>
        </article>
      ) : null}

      <div className="decision-form">
        <button type="button" onClick={downloadDocumentExportPackage}>
          {copy.downloadWithToken}
        </button>
        <Result result={exportDownloadResult} />
      </div>
    </section>
  );
}
