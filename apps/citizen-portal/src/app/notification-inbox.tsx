"use client";

import { useState } from "react";

type Notification = {
  id: string;
  notificationType: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
};

const defaultApiUrl = "http://localhost:8080";

export function NotificationInbox() {
  const [apiUrl, setApiUrl] = useState(defaultApiUrl);
  const [email, setEmail] = useState("phase2.staff@example.test");
  const [password, setPassword] = useState("ChangeMe-Phase2-Local!");
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [message, setMessage] = useState("");

  function headers() {
    return {
      Authorization: `Basic ${btoa(`${email}:${password}`)}`,
      "X-Correlation-Id": crypto.randomUUID()
    };
  }

  async function loadNotifications() {
    setMessage("");
    try {
      const response = await fetch(`${apiUrl}/api/v1/notifications`, { headers: headers() });
      if (!response.ok) throw new Error(`Notifications indisponibles (${response.status})`);
      setNotifications((await response.json()) as Notification[]);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Notifications indisponibles");
    }
  }

  async function markRead(notificationId: string) {
    try {
      const response = await fetch(`${apiUrl}/api/v1/notifications/${notificationId}/read`, {
        method: "POST",
        headers: headers()
      });
      if (!response.ok) throw new Error(`Lecture impossible (${response.status})`);
      setNotifications((current) => current.map((notification) =>
        notification.id === notificationId ? { ...notification, read: true } : notification
      ));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Lecture impossible");
    }
  }

  return (
    <section className="public-search" aria-labelledby="notification-heading">
      <h2 id="notification-heading">Boite de reception</h2>
      <p>Les messages indiquent l&apos;avancement de vos demandes. Aucun detail protege n&apos;est envoye dans ces messages.</p>
      <div className="notification-credentials">
        <label>API URL<input value={apiUrl} onChange={(event) => setApiUrl(event.target.value)} /></label>
        <label>Compte<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
        <label>Mot de passe<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
      </div>
      <button type="button" onClick={loadNotifications}>Charger mes notifications</button>
      {message ? <p className="message" aria-live="polite">{message}</p> : null}
      {notifications.length === 0 ? <p>Aucune notification chargee.</p> : (
        <ul className="notification-list" aria-label="Notifications">
          {notifications.map((notification) => (
            <li className={notification.read ? "notification read" : "notification"} key={notification.id}>
              <div>
                <strong>{notification.title}</strong>
                <small>{new Date(notification.createdAt).toLocaleString("fr-FR")}</small>
              </div>
              <p>{notification.message}</p>
              {!notification.read ? (
                <button type="button" onClick={() => markRead(notification.id)}>Marquer comme lu</button>
              ) : <span className="notification-state">Lu</span>}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
