FROM node:22-alpine AS build

WORKDIR /workspace
COPY package.json package-lock.json ./
COPY apps/citizen-portal/package.json apps/citizen-portal/package.json
COPY apps/staff-console/package.json apps/staff-console/package.json
RUN npm ci
COPY . .
RUN npm run build --workspace @edrc-land-gis/staff-console

FROM node:22-alpine

WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build --chown=appuser:appgroup /workspace/apps/staff-console/.next/standalone ./
COPY --from=build --chown=appuser:appgroup /workspace/apps/staff-console/.next/static ./apps/staff-console/.next/static
USER appuser

ENV NODE_ENV=production
ENV PORT=3002
EXPOSE 3002
CMD ["node", "apps/staff-console/server.js"]
