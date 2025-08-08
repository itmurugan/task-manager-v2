# Multi-stage Dockerfile for serving static frontend
FROM node:18-alpine as builder

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies if package.json exists, otherwise skip
RUN if [ -f package.json ]; then npm ci --only=production; fi

# Copy source files
COPY . .

# Build step (if needed) - for now, just copy files
RUN mkdir -p dist && cp -r * dist/ 2>/dev/null || true

# Production stage with nginx
FROM nginx:alpine

# Install security updates
RUN apk update && apk upgrade

# Remove default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy static files from builder
COPY --from=builder /app/*.html /app/*.css /app/*.js /usr/share/nginx/html/

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Create nginx user if not exists and set permissions
RUN addgroup -g 1001 -S nginx-app || true && \
    adduser -S -D -H -u 1001 -h /usr/share/nginx/html -s /sbin/nologin -G nginx-app nginx-app || true && \
    chown -R nginx-app:nginx-app /usr/share/nginx/html && \
    chown -R nginx-app:nginx-app /var/cache/nginx && \
    chown -R nginx-app:nginx-app /var/log/nginx && \
    chown -R nginx-app:nginx-app /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown -R nginx-app:nginx-app /var/run/nginx.pid

# Switch to non-root user
USER nginx-app

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8081/ || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]