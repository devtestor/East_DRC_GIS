FROM node:22-alpine AS build

WORKDIR /workspace
COPY package.json package-lock.json ./
COPY apps/citizen-portal/package.json apps/citizen-portal/package.json
COPY apps/staff-console/package.json apps/staff-console/package.json
RUN npm ci
COPY . .
RUN npm run build --workspace @edrc-land-gis/citizen-portal

FROM node:22-alpine

WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build --chown=appuser:appgroup /workspace/apps/citizen-portal/.next/standalone ./
COPY --from=build --chown=appuser:appgroup /workspace/apps/citizen-portal/.next/static ./apps/citizen-portal/.next/static
USER appuser

ENV NODE_ENV=production
ENV PORT=3001
EXPOSE 3001
CMD ["node", "apps/citizen-portal/server.js"]
