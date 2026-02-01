# syntax=docker/dockerfile:1
FROM node:22-alpine AS builder
WORKDIR /app

# 빌드 시 환경변수 주입 (NEXT_PUBLIC_* 는 빌드 시점에 번들됨)
ARG NEXT_PUBLIC_API_URL=http://localhost:8080
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL

COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY . .
RUN --mount=type=cache,target=/app/.next/cache npm run build

FROM node:22-alpine
WORKDIR /app
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public
EXPOSE 3000
CMD ["node", "server.js"]
