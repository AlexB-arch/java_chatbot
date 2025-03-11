FROM openjdk:23-slim

WORKDIR /app

# Install SQLite
RUN apt-get update && apt-get install -y sqlite3 && apt-get clean

# Copy SQL files and create an initialization script
COPY src/main/resources/db.sql /app/db.sql
COPY src/main/resources/inserts.sql /app/inserts.sql
COPY src/main/resources/views.sql /app/views.sql

# Create initialization script
RUN echo "#!/bin/sh\n\
sqlite3 chatbot.db < db.sql\n\
sqlite3 chatbot.db < inserts.sql\n\
sqlite3 chatbot.db < views.sql\n\
echo 'Database initialized'\n\
tail -f /dev/null" > /app/init.sh && chmod +x /app/init.sh

CMD ["/app/init.sh"]