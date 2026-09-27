import {
  supportedLanguages,
  type StaffConsoleLanguage
} from "./staff-console-i18n";

export type ApiResult = {
  ok: boolean;
  status?: number;
  body: string;
};

export type NotificationResponse = {
  id: string;
  notificationType: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
};

type StaffConsoleCopy = Record<string, string>;

export function StaffConsoleHero({
  copy,
  documentBlockerCount,
  documentCount,
  language,
  notificationCount,
  onLanguageChange,
  workflowTaskCount
}: Readonly<{
  copy: StaffConsoleCopy;
  documentBlockerCount: number;
  documentCount: number;
  language: StaffConsoleLanguage;
  notificationCount: number;
  onLanguageChange: (language: StaffConsoleLanguage) => void;
  workflowTaskCount: number;
}>) {
  return (
    <header className="hero">
      <nav className="topbar" aria-label={copy.quickNav}>
        <a className="brand-mark" href="#top" aria-label={copy.appName}>
          <span>ED</span>
          <strong>{copy.appName}</strong>
        </a>
        <div className="topbar-links">
          <a href="#connection-heading">{copy.connection}</a>
          <a href="#document-intake-heading">{copy.documents}</a>
          <a href="#export-governance-heading">{copy.exports}</a>
          <a href="#pilot-readiness-heading">{copy.pilot}</a>
          <a href="#foundation-flows-heading">{copy.parcels}</a>
          <a href="#workflow-heading">{copy.workflow}</a>
        </div>
        <label className="language-switcher">
          <span>{copy.language}</span>
          <select value={language} onChange={(event) => onLanguageChange(event.target.value as StaffConsoleLanguage)}>
            {supportedLanguages.map((option) => (
              <option key={option.code} value={option.code}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
      </nav>

      <div className="hero-grid" id="top">
        <div className="hero-copy">
          <p className="eyebrow">{copy.staffConsole}</p>
          <h1>{copy.heroTitle}</h1>
          <p>{copy.heroBody}</p>
          <aside className="legal-boundary" aria-label={copy.productBoundary}>
            <strong>{copy.productBoundary}</strong>
            <span>{copy.legalNotice}</span>
          </aside>
        </div>

        <section className="overview-card" aria-labelledby="overview-heading">
          <p className="eyebrow">{copy.operationsOverview}</p>
          <h2 id="overview-heading">{copy.operationsOverview}</h2>
          <div className="metric-grid">
            <article>
              <strong>{documentCount}</strong>
              <span>{copy.loadedDocuments}</span>
            </article>
            <article>
              <strong>{workflowTaskCount}</strong>
              <span>{copy.openTasks}</span>
            </article>
            <article>
              <strong>{notificationCount}</strong>
              <span>{copy.notifications}</span>
            </article>
            <article className={documentBlockerCount > 0 ? "attention" : ""}>
              <strong>{documentBlockerCount}</strong>
              <span>{copy.documentBlockers}</span>
            </article>
          </div>
        </section>
      </div>
    </header>
  );
}

export function Result({ result }: Readonly<{ result: ApiResult | null }>) {
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

export function ConnectionPanel({
  apiUrl,
  copy,
  email,
  onApiUrlChange,
  onEmailChange,
  onPasswordChange,
  password
}: Readonly<{
  apiUrl: string;
  copy: StaffConsoleCopy;
  email: string;
  onApiUrlChange: (value: string) => void;
  onEmailChange: (value: string) => void;
  onPasswordChange: (value: string) => void;
  password: string;
}>) {
  return (
    <section className="panel connection-panel" aria-labelledby="connection-heading">
      <div>
        <p className="eyebrow">{copy.connection}</p>
        <h2 id="connection-heading">{copy.apiConnection}</h2>
        <p className="hint">{copy.apiConnectionHint}</p>
      </div>
      <div className="form-grid">
        <label>
          {copy.apiUrl}
          <input value={apiUrl} onChange={(event) => onApiUrlChange(event.target.value)} />
        </label>
        <label>
          {copy.staffEmail}
          <input value={email} onChange={(event) => onEmailChange(event.target.value)} />
        </label>
        <label>
          {copy.password}
          <input type="password" value={password} onChange={(event) => onPasswordChange(event.target.value)} />
        </label>
      </div>
    </section>
  );
}

export function NotificationPanel({
  copy,
  locale,
  notifications,
  onMarkRead,
  onRefresh,
  result
}: Readonly<{
  copy: StaffConsoleCopy;
  locale: string;
  notifications: NotificationResponse[];
  onMarkRead: (notificationId: string) => void;
  onRefresh: () => void;
  result: ApiResult | null;
}>) {
  return (
    <section className="panel notification-panel" aria-labelledby="notification-heading">
      <div className="section-row">
        <div>
          <h2 id="notification-heading">{copy.operationalNotifications}</h2>
          <p className="hint">{copy.notificationHint}</p>
        </div>
        <button type="button" onClick={onRefresh}>{copy.refresh}</button>
      </div>
      <Result result={result} />
      {notifications.length === 0 ? <p className="hint">{copy.noneLoaded}</p> : (
        <ul className="notification-list" aria-label={copy.operationalNotifications}>
          {notifications.map((notification) => (
            <li className={notification.read ? "notification read" : "notification"} key={notification.id}>
              <div className="section-row">
                <strong>{notification.title}</strong>
                <small>{new Date(notification.createdAt).toLocaleString(locale)}</small>
              </div>
              <p>{notification.message}</p>
              {!notification.read ? (
                <button type="button" onClick={() => onMarkRead(notification.id)}>{copy.markRead}</button>
              ) : <span className="notification-state">{copy.read}</span>}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

export function GeometryPreview({ wkt }: Readonly<{ wkt: string }>) {
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

export function formatBody(text: string) {
  try {
    return JSON.stringify(JSON.parse(text), null, 2);
  } catch {
    return text;
  }
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
