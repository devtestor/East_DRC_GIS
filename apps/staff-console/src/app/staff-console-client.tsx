"use client";

import { FormEvent, useMemo, useState } from "react";

type ApiResult = {
  ok: boolean;
  status?: number;
  body: string;
};

type AdministrativeUnitResponse = {
  id: string;
  code: string;
  name: string;
};

type ParcelResponse = {
  id: string;
  status: string;
  administrativeUnitId: string;
  proposedUpi: string | null;
};

type ParcelGeometryVersionResponse = {
  id: string;
  parcelId: string;
  geometryWkt: string;
  geometryStatus: string;
  calculatedAreaSquareMeters: string | null;
  source: string;
  effectiveFrom: string | null;
  effectiveTo: string | null;
  approvedBy: string | null;
  approvedAt: string | null;
  createdAt: string;
};

type ParcelGeometryApprovalResponse = {
  geometry: ParcelGeometryVersionResponse;
  requestedAction: string;
  workflowStatus: string;
  taskId: string;
};

type PartyResponse = {
  id: string;
  partyType: string;
  displayName: string;
  dataConfidence: string;
  verificationStatus: string;
  createdAt: string;
};

type OwnershipInterestResponse = {
  id: string;
  parcelId: string;
  partyId: string;
  partyDisplayName: string;
  interestType: string;
  interestStatus: string;
  sharePercent: string | null;
  dataConfidence: string;
  source: string;
  createdAt: string;
};

type OwnershipInterestReviewResponse = {
  interest: OwnershipInterestResponse;
  requestedAction: string;
  workflowStatus: string;
  taskId: string;
};

type OwnershipConflictResponse = {
  parcelId: string;
  conflictType: string;
  severity: string;
  affectedInterestCount: number;
  summary: string;
};

type DisputeCaseResponse = {
  id: string;
  parcelId: string;
  caseType: string;
  status: string;
  summary: string;
  source: string;
  authorityReference: string | null;
  openedBy: string;
  openedAt: string;
  updatedAt: string;
};

type DisputeCaseReviewResponse = {
  disputeCase: DisputeCaseResponse;
  requestedAction: string;
  workflowStatus: string;
  taskId: string;
};

type ParcelRestrictionResponse = {
  id: string;
  parcelId: string;
  restrictionType: string;
  status: string;
  source: string;
  authorityReference: string | null;
  summary: string;
  blocksOwnershipChanges: boolean;
  effectiveFrom: string;
  effectiveTo: string | null;
  createdBy: string;
  createdAt: string;
};

type WorkflowTaskResponse = {
  id: string;
  workflowType: string;
  targetType: string;
  targetId: string;
  requestedAction: string;
  requestedByUserId: string;
  requestedBy: string;
  status: string;
  priority: string;
  createdAt: string;
  assignedToActor: string | null;
  assignedToUserId: string | null;
  assignedToRole: string | null;
  claimedAt: string | null;
};

type WorkflowTaskEvidenceResponse = {
  id: string;
  taskId: string;
  evidenceType: string;
  referenceType: string;
  referenceId: string | null;
  externalReference: string | null;
  summary: string;
  addedByUserId: string;
  addedBy: string;
  addedAt: string;
};

type RegisteredDeviceResponse = {
  id: string;
  deviceId: string;
  assignedUserId: string | null;
  organizationId: string | null;
  deviceType: string;
  status: string;
  enrolledAt: string;
  expiresAt: string | null;
  revokedAt: string | null;
};

type RegisteredDeviceLifecycleResponse = {
  device: RegisteredDeviceResponse;
  requestedAction: string;
  workflowStatus: string;
  taskId: string;
};

type SurveyResponse = {
  id: string;
  parcelId: string;
  assignedToUserId: string;
  status: string;
  purpose: string;
  observationCount: number;
};

type NotificationResponse = {
  id: string;
  notificationType: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
};

const defaultApiUrl = "http://localhost:8080";

export function StaffConsoleClient() {
  const [apiUrl, setApiUrl] = useState(defaultApiUrl);
  const [email, setEmail] = useState("phase2.staff@example.test");
  const [password, setPassword] = useState("ChangeMe-Phase2-Local!");
  const [adminUnitId, setAdminUnitId] = useState("");
  const [adminUnits, setAdminUnits] = useState<AdministrativeUnitResponse[]>([]);
  const [adminResult, setAdminResult] = useState<ApiResult | null>(null);
  const [parcelResult, setParcelResult] = useState<ApiResult | null>(null);
  const [parcelSearchResult, setParcelSearchResult] = useState<ApiResult | null>(null);
  const [parcelId, setParcelId] = useState("");
  const [geometryVersions, setGeometryVersions] = useState<ParcelGeometryVersionResponse[]>([]);
  const [geometryResult, setGeometryResult] = useState<ApiResult | null>(null);
  const [geometryApprovalResult, setGeometryApprovalResult] = useState<ApiResult | null>(null);
  const [parties, setParties] = useState<PartyResponse[]>([]);
  const [partyResult, setPartyResult] = useState<ApiResult | null>(null);
  const [selectedPartyId, setSelectedPartyId] = useState("");
  const [ownershipInterests, setOwnershipInterests] = useState<OwnershipInterestResponse[]>([]);
  const [ownershipInterestResult, setOwnershipInterestResult] = useState<ApiResult | null>(null);
  const [ownershipConflicts, setOwnershipConflicts] = useState<OwnershipConflictResponse[]>([]);
  const [disputeCases, setDisputeCases] = useState<DisputeCaseResponse[]>([]);
  const [disputeCaseResult, setDisputeCaseResult] = useState<ApiResult | null>(null);
  const [parcelRestrictions, setParcelRestrictions] = useState<ParcelRestrictionResponse[]>([]);
  const [parcelRestrictionResult, setParcelRestrictionResult] = useState<ApiResult | null>(null);
  const [transitionResult, setTransitionResult] = useState<ApiResult | null>(null);
  const [unitLoadResult, setUnitLoadResult] = useState<ApiResult | null>(null);
  const [workflowTasks, setWorkflowTasks] = useState<WorkflowTaskResponse[]>([]);
  const [workflowTaskId, setWorkflowTaskId] = useState("");
  const [workflowLoadResult, setWorkflowLoadResult] = useState<ApiResult | null>(null);
  const [workflowClaimResult, setWorkflowClaimResult] = useState<ApiResult | null>(null);
  const [workflowEvidenceLoadResult, setWorkflowEvidenceLoadResult] = useState<ApiResult | null>(null);
  const [workflowEvidenceResult, setWorkflowEvidenceResult] = useState<ApiResult | null>(null);
  const [workflowDecisionResult, setWorkflowDecisionResult] = useState<ApiResult | null>(null);
  const [workflowEvidence, setWorkflowEvidence] = useState<WorkflowTaskEvidenceResponse[]>([]);
  const [devices, setDevices] = useState<RegisteredDeviceResponse[]>([]);
  const [deviceResult, setDeviceResult] = useState<ApiResult | null>(null);
  const [deviceLifecycleResult, setDeviceLifecycleResult] = useState<ApiResult | null>(null);
  const [surveys, setSurveys] = useState<SurveyResponse[]>([]);
  const [surveyResult, setSurveyResult] = useState<ApiResult | null>(null);
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [notificationResult, setNotificationResult] = useState<ApiResult | null>(null);
  const authHeader = useMemo(() => `Basic ${btoa(`${email}:${password}`)}`, [email, password]);
  const selectedWorkflowTask = useMemo(
    () => workflowTasks.find((task) => task.id === workflowTaskId) ?? null,
    [workflowTaskId, workflowTasks]
  );
  const selectedTaskRequiresEvidence = selectedWorkflowTask?.status === "CLAIMED" || selectedWorkflowTask?.status === "OPEN";

  async function callApi(path: string, options: { method?: string; payload?: unknown } = {}): Promise<ApiResult> {
    const response = await fetch(`${apiUrl}${path}`, {
      method: options.method ?? "GET",
      headers: {
        Authorization: authHeader,
        "Content-Type": "application/json",
        "X-Correlation-Id": crypto.randomUUID()
      },
      body: options.payload ? JSON.stringify(options.payload) : undefined
    });
    const text = await response.text();
    return {
      ok: response.ok,
      status: response.status,
      body: formatBody(text)
    };
  }

  async function loadNotifications() {
    const result = await callApi("/api/v1/notifications");
    setNotificationResult(result);
    if (result.ok) {
      setNotifications(JSON.parse(result.body) as NotificationResponse[]);
    }
  }

  async function markNotificationRead(notificationId: string) {
    const result = await callApi(`/api/v1/notifications/${notificationId}/read`, { method: "POST" });
    setNotificationResult(result);
    if (result.ok) {
      setNotifications((current) => current.map((notification) =>
        notification.id === notificationId ? { ...notification, read: true } : notification
      ));
    }
  }

  async function createAdministrativeUnit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const result = await callApi("/api/v1/administrative-units", {
      method: "POST",
      payload: {
      parentId: null,
      unitType: data.get("unitType"),
      code: data.get("code"),
      name: data.get("name"),
      validFrom: data.get("validFrom")
      }
    });
    setAdminResult(result);

    if (result.ok) {
      const parsed = JSON.parse(result.body) as AdministrativeUnitResponse;
      setAdminUnitId(parsed.id);
      setAdminUnits((current) => [parsed, ...current.filter((unit) => unit.id !== parsed.id)]);
    }
  }

  async function loadAdministrativeUnits() {
    const result = await callApi("/api/v1/administrative-units");
    setUnitLoadResult(result);
    if (result.ok) {
      const parsed = JSON.parse(result.body) as AdministrativeUnitResponse[];
      setAdminUnits(parsed);
      if (!adminUnitId && parsed.length > 0) {
        setAdminUnitId(parsed[0].id);
      }
    }
  }

  async function createParcel(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const result = await callApi("/api/v1/parcels", {
      method: "POST",
      payload: {
      administrativeUnitId: data.get("administrativeUnitId"),
      proposedUpi: data.get("proposedUpi"),
      landUse: data.get("landUse"),
      tenureClassification: data.get("tenureClassification")
      }
    });
    setParcelResult(result);
    if (result.ok) {
      const parsed = JSON.parse(result.body) as ParcelResponse;
      setParcelId(parsed.id);
    }
  }

  async function searchParcelByUpi(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const upi = String(data.get("upi") ?? "").trim();
    const result = await callApi(`/api/v1/parcels/search?upi=${encodeURIComponent(upi)}`);
    setParcelSearchResult(result);
    if (result.ok && result.body && result.body !== "null") {
      const parsed = JSON.parse(result.body) as ParcelResponse | null;
      if (parsed?.id) {
        setParcelId(parsed.id);
      }
    }
  }

  async function transitionParcel(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const targetParcelId = String(data.get("parcelId") ?? "").trim();
    const result = await callApi(`/api/v1/parcels/${targetParcelId}/status`, {
      method: "PATCH",
      payload: {
        targetStatus: data.get("targetStatus")
      }
    });
    setTransitionResult(result);
    if (result.ok) {
      const parsed = JSON.parse(result.body) as { workflowTaskId?: string | null };
      if (parsed.workflowTaskId) {
        setWorkflowTaskId(parsed.workflowTaskId);
      }
    }
  }

  async function createDraftGeometry(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const targetParcelId = String(data.get("parcelId") ?? "").trim();
    const result = await callApi(`/api/v1/parcels/${targetParcelId}/geometries`, {
      method: "POST",
      payload: {
        geometryWkt: data.get("geometryWkt"),
        source: data.get("source")
      }
    });
    setGeometryResult(result);
    if (result.ok) {
      await loadGeometryVersions(targetParcelId);
    }
  }

  async function loadGeometryVersions(targetParcelId = parcelId) {
    const normalizedParcelId = targetParcelId.trim();
    if (!normalizedParcelId) {
      setGeometryVersions([]);
      return;
    }
    const result = await callApi(`/api/v1/parcels/${normalizedParcelId}/geometries`);
    setGeometryResult(result);
    if (result.ok) {
      setGeometryVersions(JSON.parse(result.body) as ParcelGeometryVersionResponse[]);
    }
  }

  async function requestGeometryApproval(targetParcelId: string, geometryVersionId: string) {
    const result = await callApi(
      `/api/v1/parcels/${targetParcelId}/geometries/${geometryVersionId}/approval-requests`,
      { method: "POST" }
    );
    setGeometryApprovalResult(result);
    if (result.ok) {
      const parsed = JSON.parse(result.body) as ParcelGeometryApprovalResponse;
      selectWorkflowTask(parsed.taskId);
      await loadWorkflowTasks();
    }
  }

  async function createParty(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const result = await callApi("/api/v1/parties", {
      method: "POST",
      payload: {
        partyType: data.get("partyType"),
        displayName: data.get("displayName"),
        dataConfidence: data.get("dataConfidence")
      }
    });
    setPartyResult(result);
    if (result.ok) {
      const parsed = JSON.parse(result.body) as PartyResponse;
      setSelectedPartyId(parsed.id);
      setParties((current) => [parsed, ...current.filter((party) => party.id !== parsed.id)]);
    }
  }

  async function searchParties(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const query = String(data.get("query") ?? "").trim();
    const result = await callApi(`/api/v1/parties?query=${encodeURIComponent(query)}`);
    setPartyResult(result);
    if (result.ok) {
      const parsed = JSON.parse(result.body) as PartyResponse[];
      setParties(parsed);
      if (!selectedPartyId && parsed.length > 0) {
        setSelectedPartyId(parsed[0].id);
      }
    }
  }

  async function createOwnershipInterest(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const targetParcelId = String(data.get("parcelId") ?? "").trim();
    const sharePercent = String(data.get("sharePercent") ?? "").trim();
    const result = await callApi(`/api/v1/parcels/${targetParcelId}/ownership-interests`, {
      method: "POST",
      payload: {
        partyId: data.get("partyId"),
        interestType: data.get("interestType"),
        sharePercent: sharePercent ? Number(sharePercent) : null,
        dataConfidence: data.get("dataConfidence"),
        source: data.get("source")
      }
    });
    setOwnershipInterestResult(result);
    if (result.ok) {
      await loadOwnershipInterests(targetParcelId);
    }
  }

  async function loadOwnershipInterests(targetParcelId = parcelId) {
    const normalizedParcelId = targetParcelId.trim();
    if (!normalizedParcelId) {
      setOwnershipInterests([]);
      setOwnershipConflicts([]);
      return;
    }
    const result = await callApi(`/api/v1/parcels/${normalizedParcelId}/ownership-interests`);
    setOwnershipInterestResult(result);
    if (result.ok) {
      setOwnershipInterests(JSON.parse(result.body) as OwnershipInterestResponse[]);
    }
    await loadOwnershipConflicts(normalizedParcelId);
  }

  async function loadOwnershipConflicts(targetParcelId = parcelId) {
    const normalizedParcelId = targetParcelId.trim();
    if (!normalizedParcelId) {
      setOwnershipConflicts([]);
      return;
    }
    const result = await callApi(`/api/v1/parcels/${normalizedParcelId}/ownership-interests/conflicts`);
    if (result.ok) {
      setOwnershipConflicts(JSON.parse(result.body) as OwnershipConflictResponse[]);
    }
  }

  async function createDisputeCase(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const targetParcelId = String(data.get("parcelId") ?? "").trim();
    const result = await callApi(`/api/v1/parcels/${targetParcelId}/disputes`, {
      method: "POST",
      payload: {
        caseType: data.get("caseType"),
        summary: data.get("summary"),
        source: data.get("source"),
        authorityReference: data.get("authorityReference")
      }
    });
    setDisputeCaseResult(result);
    if (result.ok) {
      await loadDisputeCases(targetParcelId);
    }
  }

  async function loadDisputeCases(targetParcelId = parcelId) {
    const normalizedParcelId = targetParcelId.trim();
    if (!normalizedParcelId) {
      setDisputeCases([]);
      return;
    }
    const result = await callApi(`/api/v1/parcels/${normalizedParcelId}/disputes`);
    setDisputeCaseResult(result);
    if (result.ok) {
      setDisputeCases(JSON.parse(result.body) as DisputeCaseResponse[]);
    }
  }

  async function requestDisputeReview(targetParcelId: string, caseId: string) {
    const result = await callApi(`/api/v1/parcels/${targetParcelId.trim()}/disputes/${caseId}/review-requests`, {
      method: "POST"
    });
    setDisputeCaseResult(result);
    if (result.ok) {
      const parsed = JSON.parse(result.body) as DisputeCaseReviewResponse;
      selectWorkflowTask(parsed.taskId);
      await loadDisputeCases(targetParcelId);
      await loadWorkflowTasks();
    }
  }

  async function requestOwnershipInterestReview(targetParcelId: string, interestId: string) {
    const result = await callApi(
      `/api/v1/parcels/${targetParcelId}/ownership-interests/${interestId}/review-requests`,
      { method: "POST" }
    );
    setOwnershipInterestResult(result);
    if (result.ok) {
      const parsed = JSON.parse(result.body) as OwnershipInterestReviewResponse;
      selectWorkflowTask(parsed.taskId);
      await loadOwnershipInterests(targetParcelId);
      await loadWorkflowTasks();
    }
  }

  async function createParcelRestriction(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const targetParcelId = String(data.get("parcelId") ?? "").trim();
    const result = await callApi(`/api/v1/parcels/${targetParcelId}/restrictions`, {
      method: "POST",
      payload: {
        restrictionType: data.get("restrictionType"),
        source: data.get("source"),
        authorityReference: data.get("authorityReference"),
        summary: data.get("summary"),
        blocksOwnershipChanges: data.get("blocksOwnershipChanges") === "on",
        effectiveTo: null
      }
    });
    setParcelRestrictionResult(result);
    if (result.ok) {
      await loadParcelRestrictions(targetParcelId);
    }
  }

  async function loadParcelRestrictions(targetParcelId = parcelId) {
    const normalizedParcelId = targetParcelId.trim();
    if (!normalizedParcelId) {
      setParcelRestrictions([]);
      return;
    }
    const result = await callApi(`/api/v1/parcels/${normalizedParcelId}/restrictions`);
    setParcelRestrictionResult(result);
    if (result.ok) {
      setParcelRestrictions(JSON.parse(result.body) as ParcelRestrictionResponse[]);
    }
  }

  async function requestParcelRestrictionRelease(targetParcelId: string, restrictionId: string) {
    const result = await callApi(
      `/api/v1/parcels/${targetParcelId.trim()}/restrictions/${restrictionId}/release-requests`,
      { method: "POST" }
    );
    setParcelRestrictionResult(result);
    if (result.ok) {
      const parsed = JSON.parse(result.body) as { taskId: string };
      selectWorkflowTask(parsed.taskId);
      await loadWorkflowTasks();
      await loadParcelRestrictions(targetParcelId);
    }
  }

  async function loadWorkflowTasks() {
    const result = await callApi("/api/v1/workflow/tasks");
    setWorkflowLoadResult(result);
    if (result.ok) {
      const parsed = JSON.parse(result.body) as WorkflowTaskResponse[];
      setWorkflowTasks(parsed);
      if (!workflowTaskId && parsed.length > 0) {
        selectWorkflowTask(parsed[0].id);
      }
    }
  }

  async function loadWorkflowEvidenceForTask(taskId: string) {
    const targetTaskId = taskId.trim();
    if (!targetTaskId) {
      setWorkflowEvidence([]);
      return;
    }
    const result = await callApi(`/api/v1/workflow/tasks/${targetTaskId}/evidence`);
    setWorkflowEvidenceLoadResult(result);
    if (result.ok) {
      setWorkflowEvidence(JSON.parse(result.body) as WorkflowTaskEvidenceResponse[]);
    }
  }

  function selectWorkflowTask(taskId: string) {
    setWorkflowTaskId(taskId);
    void loadWorkflowEvidenceForTask(taskId);
  }

  async function decideWorkflowTask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const targetTaskId = String(data.get("workflowTaskId") ?? "").trim();
    const selectedTask = workflowTasks.find((task) => task.id === targetTaskId);
    const decisionPath =
      selectedTask?.workflowType === "REGISTERED_DEVICE_LIFECYCLE"
        ? `/api/v1/devices/tasks/${targetTaskId}/decisions`
        : selectedTask?.workflowType === "PARCEL_GEOMETRY_APPROVAL"
          ? `/api/v1/parcels/geometries/tasks/${targetTaskId}/decisions`
          : selectedTask?.workflowType === "OWNERSHIP_INTEREST_REVIEW"
            ? `/api/v1/parcels/${parcelId.trim()}/ownership-interests/tasks/${targetTaskId}/decisions`
            : selectedTask?.workflowType === "PARCEL_RESTRICTION_RELEASE"
              ? `/api/v1/parcels/${parcelId.trim()}/restrictions/tasks/${targetTaskId}/decisions`
            : selectedTask?.workflowType === "DISPUTE_CASE_REVIEW"
              ? `/api/v1/parcels/${parcelId.trim()}/disputes/tasks/${targetTaskId}/decisions`
              : selectedTask?.workflowType === "PARCEL_INFORMATION_REQUEST_REVIEW"
                ? `/api/v1/applications/tasks/${targetTaskId}/decisions`
                : `/api/v1/workflow/tasks/${targetTaskId}/decisions`;
    const result = await callApi(decisionPath, {
      method: "POST",
      payload: {
        decision: data.get("decision"),
        reason: data.get("reason")
      }
    });
    setWorkflowDecisionResult(result);
    if (result.ok) {
      await loadWorkflowTasks();
      setWorkflowEvidence([]);
    }
  }

  async function claimWorkflowTask() {
    const targetTaskId = workflowTaskId.trim();
    const result = await callApi(`/api/v1/workflow/tasks/${targetTaskId}/claim`, {
      method: "POST"
    });
    setWorkflowClaimResult(result);
    if (result.ok) {
      await loadWorkflowTasks();
      await loadWorkflowEvidenceForTask(targetTaskId);
    }
  }

  async function addWorkflowEvidence(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const targetTaskId = String(data.get("workflowTaskId") ?? "").trim();
    const result = await callApi(`/api/v1/workflow/tasks/${targetTaskId}/evidence`, {
      method: "POST",
      payload: {
        evidenceType: data.get("evidenceType"),
        referenceType: data.get("referenceType"),
        externalReference: data.get("externalReference"),
        summary: data.get("summary")
      }
    });
    setWorkflowEvidenceResult(result);
    if (result.ok) {
      await loadWorkflowEvidenceForTask(targetTaskId);
    }
  }

  async function enrollDevice(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const assignedUserId = String(data.get("assignedUserId") ?? "").trim();
    const organizationId = String(data.get("organizationId") ?? "").trim();
    const expiresAt = String(data.get("expiresAt") ?? "").trim();
    const result = await callApi("/api/v1/devices", {
      method: "POST",
      payload: {
        deviceId: data.get("deviceId"),
        assignedUserId: assignedUserId || null,
        organizationId: organizationId || null,
        deviceType: data.get("deviceType"),
        expiresAt: expiresAt ? new Date(expiresAt).toISOString() : null
      }
    });
    setDeviceResult(result);
    if (result.ok) {
      const parsed = JSON.parse(result.body) as RegisteredDeviceResponse;
      setDevices((current) => [parsed, ...current.filter((device) => device.id !== parsed.id)]);
    }
  }

  async function loadRegisteredDevices() {
    const result = await callApi("/api/v1/devices");
    setDeviceResult(result);
    if (result.ok) {
      setDevices(JSON.parse(result.body) as RegisteredDeviceResponse[]);
    }
  }

  async function requestDeviceLifecycle(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const targetDeviceId = String(data.get("deviceId") ?? "").trim();
    const action = String(data.get("requestedAction") ?? "").toLowerCase();
    const result = await callApi(`/api/v1/devices/${encodeURIComponent(targetDeviceId)}/${action}`, {
      method: "POST"
    });
    setDeviceLifecycleResult(result);
    if (result.ok) {
      const parsed = JSON.parse(result.body) as RegisteredDeviceLifecycleResponse;
      selectWorkflowTask(parsed.taskId);
      await loadWorkflowTasks();
    }
  }

  async function createSurvey(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const result = await callApi("/api/v1/surveys", {
      method: "POST",
      payload: {
        parcelId: data.get("surveyParcelId"),
        assignedToUserId: String(data.get("assignedToUserId") ?? "").trim() || null,
        purpose: data.get("surveyPurpose")
      }
    });
    setSurveyResult(result);
    if (result.ok) setSurveys((current) => [JSON.parse(result.body) as SurveyResponse, ...current]);
  }

  async function loadSurveys() {
    const result = await callApi("/api/v1/surveys");
    setSurveyResult(result);
    if (result.ok) setSurveys(JSON.parse(result.body) as SurveyResponse[]);
  }

  async function addSurveyObservation(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const surveyId = String(data.get("observationSurveyId") ?? "").trim();
    const result = await callApi(`/api/v1/surveys/${surveyId}/observations`, {
      method: "POST",
      payload: {
        observationType: data.get("observationType"),
        latitude: data.get("latitude") ? Number(data.get("latitude")) : null,
        longitude: data.get("longitude") ? Number(data.get("longitude")) : null,
        accuracyMeters: data.get("accuracyMeters") ? Number(data.get("accuracyMeters")) : null,
        note: data.get("observationNote"),
        clientObservationId: crypto.randomUUID(),
        deviceId: data.get("deviceId"),
        captureSource: data.get("captureSource"),
        gnssFixQuality: data.get("gnssFixQuality"),
        photoObjectKey: data.get("photoObjectKey") || null,
        signatureStatus: data.get("signatureStatus")
      }
    });
    setSurveyResult(result);
    if (result.ok) await loadSurveys();
  }

  async function acknowledgeBoundary(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const surveyId = String(data.get("ackSurveyId") ?? "").trim();
    const result = await callApi(`/api/v1/surveys/${surveyId}/boundary-acknowledgements`, {
      method: "POST",
      payload: { neighborName: data.get("neighborName"), status: data.get("ackStatus"), note: data.get("ackNote") }
    });
    setSurveyResult(result);
  }

  async function submitSurvey(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const surveyId = String(data.get("submitSurveyId") ?? "").trim();
    const result = await callApi(`/api/v1/surveys/${surveyId}/submit`, {
      method: "POST",
      payload: { geometryWkt: data.get("geometryWkt") }
    });
    setSurveyResult(result);
    if (result.ok) await loadSurveys();
  }

  return (
    <main className="shell">
      <header>
        <p className="eyebrow">Console agents</p>
        <h1>Operations foncieres controlees</h1>
        <p>
          Creation controlee des unites administratives et des brouillons de parcelles. Les
          geometries approuvees, droits legaux et titres officiels restent hors de ce flux.
        </p>
      </header>

      <section className="panel" aria-labelledby="connection-heading">
        <h2 id="connection-heading">Connexion locale API</h2>
        <div className="form-grid">
          <label>
            API URL
            <input value={apiUrl} onChange={(event) => setApiUrl(event.target.value)} />
          </label>
          <label>
            Email agent
            <input value={email} onChange={(event) => setEmail(event.target.value)} />
          </label>
          <label>
            Mot de passe
            <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} />
          </label>
        </div>
      </section>

      <section className="panel notification-panel" aria-labelledby="notification-heading">
        <div className="section-row">
          <div>
            <h2 id="notification-heading">Notifications operationnelles</h2>
            <p className="hint">Messages de service destines uniquement a l&apos;agent authentifie.</p>
          </div>
          <button type="button" onClick={loadNotifications}>Actualiser</button>
        </div>
        <Result result={notificationResult} />
        {notifications.length === 0 ? <p className="hint">Aucune notification chargee.</p> : (
          <ul className="notification-list" aria-label="Notifications operationnelles">
            {notifications.map((notification) => (
              <li className={notification.read ? "notification read" : "notification"} key={notification.id}>
                <div className="section-row">
                  <strong>{notification.title}</strong>
                  <small>{new Date(notification.createdAt).toLocaleString("fr-FR")}</small>
                </div>
                <p>{notification.message}</p>
                {!notification.read ? (
                  <button type="button" onClick={() => markNotificationRead(notification.id)}>Marquer comme lu</button>
                ) : <span className="notification-state">Lu</span>}
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="workbench" aria-label="Flux Phase 2">
        <form className="panel" onSubmit={createAdministrativeUnit}>
          <h2>Unite administrative</h2>
          <div className="form-grid">
            <label>
              Type
              <select name="unitType" defaultValue="COMMUNE">
                <option value="PROVINCE">Province</option>
                <option value="CITY">Ville</option>
                <option value="TERRITORY">Territoire</option>
                <option value="COMMUNE">Commune</option>
                <option value="SECTOR">Secteur</option>
                <option value="CHIEFDOM">Chefferie</option>
                <option value="GROUPEMENT">Groupement</option>
                <option value="QUARTIER">Quartier</option>
                <option value="LOCALITY">Localite</option>
                <option value="AVENUE">Avenue</option>
                <option value="VILLAGE">Village</option>
              </select>
            </label>
            <label>
              Code
              <input name="code" defaultValue="GOMA-COMMUNE-DEMO" pattern="[A-Z0-9.-]{2,64}" />
            </label>
            <label>
              Nom
              <input name="name" defaultValue="Commune fictive de Goma" />
            </label>
            <label>
              Valide depuis
              <input name="validFrom" type="date" defaultValue="2026-01-01" />
            </label>
          </div>
          <button type="submit">Creer l'unite</button>
          <Result result={adminResult} />
        </form>

        <form className="panel" onSubmit={createParcel}>
          <h2>Brouillon de parcelle</h2>
          <button type="button" onClick={loadAdministrativeUnits}>
            Charger les unites actives
          </button>
          <Result result={unitLoadResult} />
          <div className="form-grid">
            <label>
              Unite administrative
              <select
                name="administrativeUnitId"
                value={adminUnitId}
                onChange={(event) => setAdminUnitId(event.target.value)}
              >
                <option value="">Selectionner une unite</option>
                {adminUnits.map((unit) => (
                  <option key={unit.id} value={unit.id}>
                    {unit.name} ({unit.code})
                  </option>
                ))}
              </select>
            </label>
            <label>
              UPI propose
              <input name="proposedUpi" defaultValue="NK-DEM-000001" pattern="[A-Z0-9.-]{6,64}" />
            </label>
            <label>
              Usage
              <input name="landUse" defaultValue="Residential" />
            </label>
            <label>
              Tenure
              <input name="tenureClassification" defaultValue="Customary claim - unverified" />
            </label>
          </div>
          <button type="submit">Creer le brouillon</button>
          <Result result={parcelResult} />
        </form>

        <section className="panel" aria-labelledby="parcel-transition-heading">
          <h2 id="parcel-transition-heading">Transition de parcelle</h2>
          <form className="nested-form" onSubmit={searchParcelByUpi}>
            <h3>Recherche rapide</h3>
            <div className="form-grid">
              <label>
                UPI propose
                <input name="upi" defaultValue="NK-DEM-000001" />
              </label>
            </div>
            <button type="submit">Chercher par UPI</button>
            <Result result={parcelSearchResult} />
          </form>
          <form className="nested-form" onSubmit={transitionParcel}>
            <div className="form-grid">
              <label>
                Parcelle
                <input
                  name="parcelId"
                  value={parcelId}
                  onChange={(event) => setParcelId(event.target.value)}
                  placeholder="UUID de la parcelle"
                />
              </label>
              <label>
                Prochain statut
                <select name="targetStatus" defaultValue="UNDER_SURVEY">
                  <option value="UNDER_SURVEY">Sous enquete - application immediate depuis brouillon</option>
                  <option value="UNDER_REVIEW">Sous revue - cree une tache</option>
                  <option value="ACTIVE">Active - cree une tache</option>
                  <option value="DISPUTED">Contestee - cree une tache</option>
                  <option value="RESTRICTED">Restreinte - cree une tache</option>
                </select>
              </label>
            </div>
            <button type="submit">Appliquer la transition</button>
            <Result result={transitionResult} />
          </form>
        </section>

        <section className="panel" aria-labelledby="geometry-heading">
          <h2 id="geometry-heading">Geometrie de parcelle</h2>
          <p className="hint">
            Ce flux enregistre une geometrie brouillon uniquement. Il ne publie pas de geometrie
            cadastrale courante et ne cree pas de droit legal.
          </p>

          <form className="nested-form" onSubmit={createDraftGeometry}>
            <div className="form-grid">
              <label>
                Parcelle
                <input
                  name="parcelId"
                  value={parcelId}
                  onChange={(event) => setParcelId(event.target.value)}
                  placeholder="UUID de la parcelle"
                />
              </label>
              <label>
                Source
                <input name="source" defaultValue="survey-plan-demo" />
              </label>
              <label>
                Geometrie WKT MultiPolygon
                <textarea
                  name="geometryWkt"
                  defaultValue="MULTIPOLYGON(((29.1 -1.6,29.2 -1.6,29.2 -1.5,29.1 -1.5,29.1 -1.6)))"
                  rows={5}
                />
              </label>
            </div>
            <button type="submit">Enregistrer la geometrie brouillon</button>
          </form>

          <button type="button" onClick={() => loadGeometryVersions()}>
            Charger l'historique des geometries
          </button>
          <Result result={geometryResult} />

          {geometryVersions.length > 0 && (
            <div className="evidence-list" aria-label="Versions de geometrie">
              {geometryVersions.map((geometry) => (
                <article className="record-card" key={geometry.id}>
                  <strong>{geometry.geometryStatus}</strong>
                  <span>
                    Source: {geometry.source} · aire calculee:{" "}
                    {geometry.calculatedAreaSquareMeters ?? "non calculee"} m²
                  </span>
                  <GeometryPreview wkt={geometry.geometryWkt} />
                  <small>
                    Cree le {new Date(geometry.createdAt).toLocaleString("fr-CD")} · {geometry.geometryWkt}
                  </small>
                  {geometry.geometryStatus === "DRAFT" || geometry.geometryStatus === "VALIDATED" ? (
                    <button type="button" onClick={() => requestGeometryApproval(geometry.parcelId, geometry.id)}>
                      Soumettre a l'approbation cadastrale
                    </button>
                  ) : null}
                </article>
              ))}
            </div>
          )}
          <Result result={geometryApprovalResult} />
        </section>

        <section className="panel" aria-labelledby="survey-heading">
          <h2 id="survey-heading">Operations de terrain</h2>
          <p className="hint">Les observations restent des preuves de travail. La geometrie ne devient courante qu'apres approbation cadastrale.</p>
          <form className="nested-form" onSubmit={createSurvey}>
            <h3>Assigner une mission</h3>
            <div className="form-grid">
              <label>UUID parcelle<input name="surveyParcelId" required /></label>
              <label>UUID agent assigne<input name="assignedToUserId" placeholder="Optionnel: agent courant" /></label>
              <label>Objet<input name="surveyPurpose" defaultValue="Leve cadastral de controle" /></label>
            </div>
            <button type="submit">Creer la mission</button>
          </form>
          <button type="button" onClick={loadSurveys}>Charger les missions</button>
          {surveys.map((survey) => <article className="record-card" key={survey.id}><strong>{survey.id}</strong><span>{survey.status} · parcelle {survey.parcelId} · observations {survey.observationCount}</span></article>)}
          <form className="nested-form" onSubmit={addSurveyObservation}>
            <h3>Capturer une observation</h3>
            <div className="form-grid">
              <label>UUID mission<input name="observationSurveyId" required /></label>
              <label>Appareil actif<input name="deviceId" required placeholder="FIELD-TABLET-010" /></label>
              <label>Type<select name="observationType" defaultValue="BOUNDARY_POINT"><option>BOUNDARY_POINT</option><option>BOUNDARY_MARKER</option><option>WITNESS_NOTE</option><option>PHOTO_REFERENCE</option></select></label>
              <label>Source<select name="captureSource" defaultValue="GNSS"><option>GNSS</option><option>MANUAL</option><option>IMPORTED</option></select></label>
              <label>Latitude<input name="latitude" type="number" step="0.0000001" /></label>
              <label>Longitude<input name="longitude" type="number" step="0.0000001" /></label>
              <label>Precision metres<input name="accuracyMeters" type="number" step="0.001" /></label>
              <label>Qualite GNSS<input name="gnssFixQuality" defaultValue="ACCURATE" /></label>
              <label>Reference photo<input name="photoObjectKey" placeholder="object-key apres upload securise" /></label>
              <label>Signature<select name="signatureStatus" defaultValue="NOT_CAPTURED"><option>NOT_CAPTURED</option><option>CAPTURED</option><option>VERIFIED</option></select></label>
              <label>Note<textarea name="observationNote" rows={2} /></label>
            </div>
            <button type="submit">Enregistrer l'observation</button>
          </form>
          <form className="nested-form" onSubmit={acknowledgeBoundary}>
            <h3>Accuse voisin</h3>
            <div className="form-grid"><label>UUID mission<input name="ackSurveyId" required /></label><label>Nom du voisin<input name="neighborName" required /></label><label>Statut<select name="ackStatus" defaultValue="ACKNOWLEDGED"><option>ACKNOWLEDGED</option><option>DECLINED</option><option>UNAVAILABLE</option></select></label><label>Note<textarea name="ackNote" rows={2} /></label></div>
            <button type="submit">Enregistrer l'accuse</button>
          </form>
          <form className="nested-form" onSubmit={submitSurvey}>
            <h3>Soumettre la geometrie proposee</h3>
            <div className="form-grid"><label>UUID mission<input name="submitSurveyId" required /></label><label>WKT MultiPolygon<textarea name="geometryWkt" rows={4} defaultValue="MULTIPOLYGON(((29.1 -1.6,29.2 -1.6,29.2 -1.5,29.1 -1.5,29.1 -1.6)))" /></label></div>
            <button type="submit">Soumettre a la revue cadastrale</button>
          </form>
          <Result result={surveyResult} />
        </section>

        <section className="panel" aria-labelledby="device-heading">
          <h2 id="device-heading">Appareils enregistres</h2>
          <p className="hint">
            Les appareils servent a tracer les operations terrain et audit. La suspension,
            revocation ou expiration ouvre une tache de securite; elle ne modifie pas l'appareil
            sans approbation humaine.
          </p>

          <form className="nested-form" onSubmit={enrollDevice}>
            <h3>Enroler un appareil</h3>
            <div className="form-grid">
              <label>
                Identifiant appareil
                <input name="deviceId" defaultValue="FIELD-TABLET-010" pattern="[A-Z0-9._:-]{4,96}" />
              </label>
              <label>
                Type
                <select name="deviceType" defaultValue="FIELD_MOBILE">
                  <option value="FIELD_MOBILE">Mobile terrain</option>
                  <option value="STAFF_WORKSTATION">Poste agent</option>
                  <option value="SERVICE_ACCOUNT">Compte service</option>
                  <option value="OTHER">Autre</option>
                </select>
              </label>
              <label>
                Utilisateur assigne optionnel
                <input name="assignedUserId" placeholder="UUID utilisateur" />
              </label>
              <label>
                Organisation optionnelle
                <input name="organizationId" placeholder="UUID organisation" />
              </label>
              <label>
                Expire a optionnel
                <input name="expiresAt" type="datetime-local" />
              </label>
            </div>
            <button type="submit">Enroler l'appareil</button>
          </form>

          <button type="button" onClick={loadRegisteredDevices}>
            Charger les appareils
          </button>
          <Result result={deviceResult} />

          {devices.length > 0 && (
            <div className="task-list" aria-label="Appareils enregistres">
              {devices.map((device) => (
                <article className="record-card" key={device.id}>
                  <strong>{device.deviceId}</strong>
                  <small>
                    {device.deviceType} · {device.status}
                    {device.revokedAt ? ` · revoque le ${new Date(device.revokedAt).toLocaleString("fr-CD")}` : ""}
                  </small>
                </article>
              ))}
            </div>
          )}

          <form className="nested-form" onSubmit={requestDeviceLifecycle}>
            <h3>Demander une action sensible</h3>
            <div className="form-grid">
              <label>
                Identifiant appareil
                <input name="deviceId" defaultValue="FIELD-TABLET-010" />
              </label>
              <label>
                Action demandee
                <select name="requestedAction" defaultValue="revoke">
                  <option value="suspend">Suspendre</option>
                  <option value="revoke">Revoquer</option>
                  <option value="expire">Expirer</option>
                </select>
              </label>
            </div>
            <button type="submit">Creer la tache d'approbation</button>
            <Result result={deviceLifecycleResult} />
          </form>
        </section>

        <section className="panel" aria-labelledby="parties-heading">
          <h2 id="parties-heading">Parties et interets declares</h2>
          <p className="hint">
            Ces donnees sont protegees. Elles representent des parties et revendications
            operationnelles; elles ne constituent pas un titre officiel.
          </p>

          <form className="nested-form" onSubmit={createParty}>
            <h3>Creer une partie</h3>
            <div className="form-grid">
              <label>
                Type
                <select name="partyType" defaultValue="INDIVIDUAL">
                  <option value="INDIVIDUAL">Individu</option>
                  <option value="ORGANIZATION">Organisation</option>
                  <option value="GOVERNMENT_INSTITUTION">Institution publique</option>
                  <option value="ESTATE">Succession</option>
                  <option value="COOPERATIVE">Cooperative</option>
                </select>
              </label>
              <label>
                Nom d'affichage protege
                <input name="displayName" defaultValue="Personne fictive - revendication locale" />
              </label>
              <label>
                Confiance donnees
                <select name="dataConfidence" defaultValue="SELF_DECLARED">
                  <option value="UNVERIFIED">Non verifie</option>
                  <option value="SELF_DECLARED">Declare par l'interesse</option>
                  <option value="DOCUMENT_SUPPORTED">Appuye par document</option>
                  <option value="INSTITUTION_VERIFIED">Verifie institutionnellement</option>
                </select>
              </label>
            </div>
            <button type="submit">Creer la partie</button>
          </form>

          <form className="nested-form" onSubmit={searchParties}>
            <h3>Rechercher parties</h3>
            <div className="form-grid">
              <label>
                Recherche
                <input name="query" defaultValue="fictive" />
              </label>
            </div>
            <button type="submit">Chercher</button>
            <Result result={partyResult} />
          </form>

          {parties.length > 0 && (
            <div className="task-list" aria-label="Parties protegees">
              {parties.map((party) => (
                <button
                  className={party.id === selectedPartyId ? "task-card selected" : "task-card"}
                  key={party.id}
                  type="button"
                  onClick={() => setSelectedPartyId(party.id)}
                >
                  <span>{party.displayName}</span>
                  <small>
                    {party.partyType} · {party.verificationStatus} · {party.dataConfidence}
                  </small>
                </button>
              ))}
            </div>
          )}

          <form className="nested-form" onSubmit={createOwnershipInterest}>
            <h3>Ajouter une revendication/interet sur parcelle</h3>
            <div className="form-grid">
              <label>
                Parcelle
                <input
                  name="parcelId"
                  value={parcelId}
                  onChange={(event) => setParcelId(event.target.value)}
                  placeholder="UUID de la parcelle"
                />
              </label>
              <label>
                Partie
                <input
                  name="partyId"
                  value={selectedPartyId}
                  onChange={(event) => setSelectedPartyId(event.target.value)}
                  placeholder="UUID de la partie"
                />
              </label>
              <label>
                Type d'interet
                <select name="interestType" defaultValue="CUSTOMARY_CLAIM">
                  <option value="OWNERSHIP_CLAIM">Revendication propriete</option>
                  <option value="OCCUPANCY_CLAIM">Occupation</option>
                  <option value="USE_RIGHT_CLAIM">Droit d'usage</option>
                  <option value="CUSTOMARY_CLAIM">Revendication coutumiere</option>
                  <option value="LEASEHOLD_CLAIM">Bail revendique</option>
                </select>
              </label>
              <label>
                Part declaree optionnelle (%)
                <input name="sharePercent" type="number" min="0.01" max="100" step="0.01" defaultValue="100" />
              </label>
              <label>
                Confiance donnees
                <select name="dataConfidence" defaultValue="SELF_DECLARED">
                  <option value="UNVERIFIED">Non verifie</option>
                  <option value="SELF_DECLARED">Declare</option>
                  <option value="DOCUMENT_SUPPORTED">Documente</option>
                  <option value="INSTITUTION_VERIFIED">Verifie institutionnellement</option>
                </select>
              </label>
              <label>
                Source
                <input name="source" defaultValue="entretien-local-fictif" />
              </label>
            </div>
            <button type="submit">Enregistrer la revendication</button>
          </form>

          <button type="button" onClick={() => loadOwnershipInterests()}>
            Charger les interets de la parcelle
          </button>
          <Result result={ownershipInterestResult} />

          <section className="nested-form" aria-labelledby="conflicts-heading">
            <div className="section-row">
              <h3 id="conflicts-heading">Signaux de conflit</h3>
              <button type="button" onClick={() => loadOwnershipConflicts()}>
                Actualiser les conflits
              </button>
            </div>
            <p className="hint">
              Ces signaux identifient des revendications concurrentes ou des parts superieures a 100 %. Ils
              exigent une revue humaine et ne determinent pas la propriete legale.
            </p>
            {ownershipConflicts.length === 0 ? (
              <p className="hint">Aucun signal de conflit detecte pour cette parcelle.</p>
            ) : (
              <div className="evidence-list" aria-label="Signaux de conflit foncier">
                {ownershipConflicts.map((conflict) => (
                  <article className="record-card warning-text" key={`${conflict.parcelId}-${conflict.conflictType}`}>
                    <strong>{conflict.conflictType} · {conflict.severity}</strong>
                    <span>{conflict.summary}</span>
                    <small>{conflict.affectedInterestCount} interet(s) concerne(s)</small>
                  </article>
                ))}
              </div>
            )}
          </section>

          <section className="nested-form" aria-labelledby="disputes-heading">
            <h3 id="disputes-heading">Dossiers de litige</h3>
            <p className="hint">
              Un dossier conserve le signalement et ses preuves. Son ouverture ne constitue ni une decision de
              justice ni une determination de propriete.
            </p>
            <form onSubmit={createDisputeCase}>
              <div className="form-grid">
                <label>
                  Parcelle
                  <input name="parcelId" value={parcelId} onChange={(event) => setParcelId(event.target.value)} />
                </label>
                <label>
                  Type de litige
                  <select name="caseType" defaultValue="COMPETING_CLAIM">
                    <option value="BOUNDARY_DISPUTE">Litige de limite</option>
                    <option value="OWNERSHIP_DISPUTE">Litige de propriete</option>
                    <option value="COMPETING_CLAIM">Revendications concurrentes</option>
                    <option value="FRAUD_ALLEGATION">Allegation de fraude</option>
                    <option value="ADMINISTRATIVE_APPEAL">Recours administratif</option>
                    <option value="OTHER">Autre</option>
                  </select>
                </label>
                <label>
                  Source
                  <input name="source" defaultValue="signalement-local-fictif" />
                </label>
                <label>
                  Reference autorite
                  <input name="authorityReference" defaultValue="" />
                </label>
                <label>
                  Resume
                  <input name="summary" defaultValue="Revendications concurrentes a examiner" />
                </label>
              </div>
              <button type="submit">Ouvrir le dossier</button>
            </form>
            <button type="button" onClick={() => loadDisputeCases()}>
              Charger les dossiers
            </button>
            <Result result={disputeCaseResult} />
            {disputeCases.length > 0 && (
              <div className="evidence-list" aria-label="Dossiers de litige">
                {disputeCases.map((disputeCase) => (
                  <article className="record-card" key={disputeCase.id}>
                    <strong>{disputeCase.caseType} · {disputeCase.status}</strong>
                    <span>{disputeCase.summary}</span>
                    <small>
                      source: {disputeCase.source} · ouvert par {disputeCase.openedBy} le {new Date(disputeCase.openedAt).toLocaleString("fr-CD")}
                    </small>
                    {disputeCase.status === "OPEN" && (
                      <button type="button" onClick={() => requestDisputeReview(disputeCase.parcelId, disputeCase.id)}>
                        Demander la revue avec preuve
                      </button>
                    )}
                  </article>
                ))}
              </div>
            )}
          </section>

          {ownershipInterests.length > 0 && (
            <div className="evidence-list" aria-label="Interets fonciers declares">
              {ownershipInterests.map((interest) => (
                <article className="record-card" key={interest.id}>
                  <strong>{interest.partyDisplayName}</strong>
                  <span>
                    {interest.interestType} · {interest.interestStatus} · {interest.dataConfidence}
                  </span>
                  <small>
                    Part: {interest.sharePercent ?? "non precisee"}% · source: {interest.source} · cree le{" "}
                    {new Date(interest.createdAt).toLocaleString("fr-CD")}
                  </small>
                  {interest.interestStatus === "CLAIMED" && (
                    <button
                      type="button"
                      onClick={() => requestOwnershipInterestReview(interest.parcelId, interest.id)}
                    >
                      Demander la revue avec preuve
                    </button>
                  )}
                </article>
              ))}
            </div>
          )}

          <section className="nested-form" aria-labelledby="restrictions-heading">
            <h3 id="restrictions-heading">Restrictions et cautions sur parcelle</h3>
            <p className="hint">
              Une restriction active qui bloque les changements de droits empeche l'API et la base de donnees de
              modifier les interets/proprietes declares.
            </p>
            <form onSubmit={createParcelRestriction}>
              <div className="form-grid">
                <label>
                  Parcelle
                  <input
                    name="parcelId"
                    value={parcelId}
                    onChange={(event) => setParcelId(event.target.value)}
                    placeholder="UUID de la parcelle"
                  />
                </label>
                <label>
                  Type restriction
                  <select name="restrictionType" defaultValue="COURT_CAUTION">
                    <option value="BOUNDARY_DISPUTE">Litige limite</option>
                    <option value="OWNERSHIP_DISPUTE">Litige propriete</option>
                    <option value="COURT_CAUTION">Caution judiciaire</option>
                    <option value="FRAUD_ALLEGATION">Allegation fraude</option>
                    <option value="ADMINISTRATIVE_FREEZE">Gel administratif</option>
                    <option value="EXPROPRIATION_NOTICE">Avis expropriation</option>
                    <option value="OTHER">Autre</option>
                  </select>
                </label>
                <label>
                  Source
                  <input name="source" defaultValue="decision-judiciaire-fictive" />
                </label>
                <label>
                  Reference autorite
                  <input name="authorityReference" defaultValue="COURT-2026-FICTIVE-001" />
                </label>
                <label>
                  Resume
                  <input name="summary" defaultValue="Caution fictive: examen requis avant changement de droits" />
                </label>
                <label className="checkbox-label">
                  <input name="blocksOwnershipChanges" type="checkbox" defaultChecked />
                  Bloquer les changements de droits/interets
                </label>
              </div>
              <button type="submit">Appliquer la restriction</button>
            </form>

            <button type="button" onClick={() => loadParcelRestrictions()}>
              Charger les restrictions
            </button>
            <Result result={parcelRestrictionResult} />

            {parcelRestrictions.length > 0 && (
              <div className="evidence-list" aria-label="Restrictions de parcelle">
                {parcelRestrictions.map((restriction) => (
                  <article className="record-card" key={restriction.id}>
                    <strong>
                      {restriction.restrictionType} · {restriction.status}
                    </strong>
                    <span>{restriction.summary}</span>
                    <small>
                      {restriction.blocksOwnershipChanges ? "Bloque les changements de droits" : "Information seulement"} ·{" "}
                      source: {restriction.source}
                      {restriction.authorityReference ? ` · ref: ${restriction.authorityReference}` : ""} · cree par{" "}
                      {restriction.createdBy} le {new Date(restriction.createdAt).toLocaleString("fr-CD")}
                    </small>
                    {restriction.status === "ACTIVE" && (
                      <button
                        type="button"
                        onClick={() => requestParcelRestrictionRelease(restriction.parcelId, restriction.id)}
                      >
                        Demander la levee avec preuve
                      </button>
                    )}
                  </article>
                ))}
              </div>
            )}
          </section>
        </section>

        <section className="panel workflow-panel" aria-labelledby="workflow-heading">
          <h2 id="workflow-heading">Taches de workflow</h2>
          <p className="hint">
            Les transitions sensibles restent en attente jusqu'a validation humaine. Cette console
            applique une decision explicite et auditee.
          </p>
          <button type="button" onClick={loadWorkflowTasks}>
            Charger les taches ouvertes
          </button>
          <Result result={workflowLoadResult} />

          {workflowTasks.length > 0 && (
            <div className="task-list" aria-label="Taches ouvertes">
              {workflowTasks.map((task) => (
                <button
                  className={task.id === workflowTaskId ? "task-card selected" : "task-card"}
                  key={task.id}
                  type="button"
                  onClick={() => selectWorkflowTask(task.id)}
                >
                  <span>{task.requestedAction.replace("_TO_", " -> ")}</span>
                  <small>
                    {task.workflowType} · {task.targetType} {task.targetId} · demande par {task.requestedBy} ·{" "}
                    {task.assignedToActor ? `assignee a ${task.assignedToActor}` : task.assignedToRole ?? "Non assigne"}
                  </small>
                </button>
              ))}
            </div>
          )}

          {selectedWorkflowTask && (
            <aside className="safety-note" aria-label="Controle avant decision">
              <strong>Controle avant decision</strong>
              <span>
                Endpoint de decision:{" "}
                {selectedWorkflowTask.workflowType === "REGISTERED_DEVICE_LIFECYCLE"
                  ? "cycle de vie appareil"
                  : selectedWorkflowTask.workflowType === "PARCEL_GEOMETRY_APPROVAL"
                    ? "approbation geometrie cadastrale"
                    : selectedWorkflowTask.workflowType === "OWNERSHIP_INTEREST_REVIEW"
                      ? "revue interet/propriete declaree"
                      : selectedWorkflowTask.workflowType === "PARCEL_RESTRICTION_RELEASE"
                        ? "levee d'une restriction de parcelle"
                        : selectedWorkflowTask.workflowType === "DISPUTE_CASE_REVIEW"
                          ? "revue d'un dossier de litige"
                          : selectedWorkflowTask.workflowType === "PARCEL_INFORMATION_REQUEST_REVIEW"
                            ? "revue d'une demande d'information parcellaire"
                            : "transition parcelle"}
              </span>
              <span>
                Role attendu: {selectedWorkflowTask.assignedToRole ?? "non defini"} · statut:{" "}
                {selectedWorkflowTask.status}
              </span>
              <span>
                Preuves liees: {workflowEvidence.length}. Une approbation est refusee par l'API si aucune preuve
                n'est liee.
              </span>
            </aside>
          )}

          <section className="evidence-panel" aria-labelledby="evidence-heading">
            <div className="section-row">
              <h3 id="evidence-heading">Preuves de la tache selectionnee</h3>
              <button type="button" onClick={() => loadWorkflowEvidenceForTask(workflowTaskId)}>
                Actualiser les preuves
              </button>
            </div>
            <Result result={workflowEvidenceLoadResult} />
            {workflowEvidence.length === 0 ? (
              <p className={selectedTaskRequiresEvidence ? "warning-text" : "hint"}>
                Aucune preuve chargee pour cette tache. Ajoutez ou actualisez les preuves avant toute approbation.
              </p>
            ) : (
              <div className="evidence-list" aria-label="Preuves liees">
                {workflowEvidence.map((evidence) => (
                  <article className="record-card" key={evidence.id}>
                    <strong>{evidence.evidenceType}</strong>
                    <span>{evidence.summary}</span>
                    <small>
                      {evidence.referenceType}
                      {evidence.referenceId ? ` · ${evidence.referenceId}` : ""}
                      {evidence.externalReference ? ` · ${evidence.externalReference}` : ""} · ajoute par{" "}
                      {evidence.addedBy} le {new Date(evidence.addedAt).toLocaleString("fr-CD")}
                    </small>
                  </article>
                ))}
              </div>
            )}
          </section>

          <form className="decision-form" onSubmit={addWorkflowEvidence}>
            <div className="form-grid">
              <input type="hidden" name="workflowTaskId" value={workflowTaskId} />
              <label>
                Type de preuve
                <select name="evidenceType" defaultValue="NOTE">
                  <option value="DOCUMENT">Document</option>
                  <option value="SURVEY_OBSERVATION">Observation terrain</option>
                  <option value="FIELD_PHOTO">Photo terrain</option>
                  <option value="BOUNDARY_MARKER">Borne limite</option>
                  <option value="NEIGHBOR_ACKNOWLEDGEMENT">Reconnaissance voisin</option>
                  <option value="COURT_ORDER">Decision judiciaire</option>
                  <option value="NOTE">Note</option>
                  <option value="OTHER">Autre</option>
                </select>
              </label>
              <label>
                Reference
                <input name="referenceType" defaultValue="manual-note" />
              </label>
              <label>
                Identifiant externe
                <input name="externalReference" defaultValue="local-evidence-note" />
              </label>
              <label>
                Resume
                <input name="summary" defaultValue="Preuve operationnelle fictive pour validation locale" />
              </label>
            </div>
            <button type="submit">Ajouter une preuve</button>
            <Result result={workflowEvidenceResult} />
          </form>

          <form className="decision-form" onSubmit={decideWorkflowTask}>
            <div className="form-grid">
              <label>
                Tache
                <input
                  name="workflowTaskId"
                  value={workflowTaskId}
                  onChange={(event) => setWorkflowTaskId(event.target.value)}
                  placeholder="UUID de la tache"
                />
              </label>
              <label>
                Decision
                <select name="decision" defaultValue="APPROVE">
                  <option value="APPROVE">Approuver et appliquer</option>
                  <option value="REJECT">Rejeter sans changer la cible</option>
                </select>
              </label>
              <label>
                Motif
                <input name="reason" defaultValue="Verification humaine effectuee" />
              </label>
            </div>
            <button type="button" onClick={claimWorkflowTask}>
              Revendiquer la tache
            </button>
            <Result result={workflowClaimResult} />
            <button type="submit">Enregistrer la decision</button>
            <Result result={workflowDecisionResult} />
          </form>
        </section>
      </section>
    </main>
  );
}

function Result({ result }: Readonly<{ result: ApiResult | null }>) {
  if (!result) {
    return null;
  }

  return (
    <output className={result.ok ? "result success" : "result error"}>
      <span>{result.ok ? "Succes" : `Erreur ${result.status ?? ""}`}</span>
      <pre>{result.body}</pre>
    </output>
  );
}

function GeometryPreview({ wkt }: Readonly<{ wkt: string }>) {
  const points = parseFirstWktRing(wkt);
  if (points.length < 3) {
    return <span className="hint">Apercu indisponible pour cette geometrie.</span>;
  }

  const xs = points.map((point) => point[0]);
  const ys = points.map((point) => point[1]);
  const minX = Math.min(...xs);
  const maxX = Math.max(...xs);
  const minY = Math.min(...ys);
  const maxY = Math.max(...ys);
  const width = Math.max(maxX - minX, 0.000001);
  const height = Math.max(maxY - minY, 0.000001);
  const projected = points
    .map(([x, y]) => {
      const svgX = 10 + ((x - minX) / width) * 180;
      const svgY = 10 + (1 - (y - minY) / height) * 120;
      return `${svgX.toFixed(2)},${svgY.toFixed(2)}`;
    })
    .join(" ");

  return (
    <svg className="geometry-preview" role="img" aria-label="Apercu geometrique brouillon" viewBox="0 0 200 140">
      <rect x="1" y="1" width="198" height="138" rx="6" />
      <polyline points={projected} />
    </svg>
  );
}

function parseFirstWktRing(wkt: string): Array<[number, number]> {
  const match = wkt.match(/MULTIPOLYGON\s*\(\s*\(\s*\(([^)]+)\)/i) ?? wkt.match(/POLYGON\s*\(\s*\(([^)]+)\)/i);
  if (!match) {
    return [];
  }
  return match[1]
    .split(",")
    .map((coordinate) => coordinate.trim().split(/\s+/).map(Number))
    .filter((coordinate) => coordinate.length >= 2 && Number.isFinite(coordinate[0]) && Number.isFinite(coordinate[1]))
    .map((coordinate) => [coordinate[0], coordinate[1]]);
}

function formatBody(text: string) {
  try {
    return JSON.stringify(JSON.parse(text), null, 2);
  } catch {
    return text;
  }
}
