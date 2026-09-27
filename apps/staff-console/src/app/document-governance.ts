export type GovernedDocumentVersion = {
  versionNumber: number;
  malwareScanStatus: string;
  digitalSignatureStatus: string;
  safetyStatus: string;
};

export type GovernedDocument = {
  versions: GovernedDocumentVersion[];
};

export function latestDocumentVersion<TVersion extends GovernedDocumentVersion>(
  document: { versions: TVersion[] },
): TVersion | null {
  if (document.versions.length === 0) {
    return null;
  }

  return document.versions.reduce((latest, candidate) =>
    candidate.versionNumber > latest.versionNumber ? candidate : latest,
  );
}

export function documentExportBlockers(document: GovernedDocument): string[] {
  const version = latestDocumentVersion(document);
  if (!version) {
    return ["DOCUMENT_VERSION_MISSING"];
  }

  const blockers: string[] = [];
  if (version.safetyStatus === "QUARANTINED") {
    blockers.push("DOCUMENT_VERSION_QUARANTINED");
  }
  if (version.malwareScanStatus === "PENDING") {
    blockers.push("MALWARE_SCAN_PENDING");
  }
  if (version.malwareScanStatus === "FAILED") {
    blockers.push("MALWARE_SCAN_FAILED");
  }
  if (version.digitalSignatureStatus === "INVALID") {
    blockers.push("DIGITAL_SIGNATURE_INVALID");
  }

  return blockers;
}

export function summarizeDocumentGovernance(documents: GovernedDocument[]) {
  return documents.reduce(
    (summary, document) => {
      const blockers = documentExportBlockers(document);
      return {
        total: summary.total + 1,
        pendingScan: summary.pendingScan + (blockers.includes("MALWARE_SCAN_PENDING") ? 1 : 0),
        failedScan: summary.failedScan + (blockers.includes("MALWARE_SCAN_FAILED") ? 1 : 0),
        invalidSignature: summary.invalidSignature + (blockers.includes("DIGITAL_SIGNATURE_INVALID") ? 1 : 0),
        quarantined: summary.quarantined + (blockers.includes("DOCUMENT_VERSION_QUARANTINED") ? 1 : 0),
      };
    },
    { total: 0, pendingScan: 0, failedScan: 0, invalidSignature: 0, quarantined: 0 },
  );
}
