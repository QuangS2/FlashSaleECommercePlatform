# Multi-stage Build cho Frontend React.js

# Stage 1: Build static bundle
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci || npm install
COPY . .
ARG VITE_KEYCLOAK_URL
ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL
RUN npm run build

# Stage 2: Serve qua Nginx nhẹ
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
