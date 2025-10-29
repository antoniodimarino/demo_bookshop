# --- FASE 1: Build (Node.js) ---
# Usa un'immagine Node.js per installare le dipendenze e costruire l'app
FROM node:22-alpine AS builder

# Imposta la directory di lavoro nel container
WORKDIR /app

# Copia package.json e package-lock.json (o yarn.lock)
COPY package*.json ./

# Installa le dipendenze
RUN npm install

# Copia tutto il resto del codice sorgente
COPY . .

# --- ARGOMENTO DI BUILD ---
# Dichiara un argomento per l'URL dell'API. 
# Verrà passato dal BuildConfig di OpenShift.
ARG VITE_API_URL
# Imposta la variabile d'ambiente per il comando di build
ENV VITE_API_URL=${VITE_API_URL}

# Esegui la build di produzione
# Vite leggerà VITE_API_URL e lo inserirà nel codice JS finale
RUN npm run build

# --- FASE 2: Serve (Nginx) ---
# Usa un'immagine Nginx leggera per servire i file statici
FROM nginx:1.25-alpine

# Copia i file statici costruiti (dalla directory 'dist' di Vite)
# nella directory di default di Nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# Copia una configurazione Nginx personalizzata (vedi sotto)
COPY nginx.conf /etc/nginx/conf.d/default.conf

RUN chgrp -R 0 /var/cache/nginx /var/run && \
    chmod -R g+w /var/cache/nginx /var/run
# Esponi la porta 8080 (standard OpenShift per Nginx)
EXPOSE 8080

# Comando di avvio di Nginx
CMD ["nginx", "-g", "daemon off;"]