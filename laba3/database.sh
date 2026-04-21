#!/bin/sh

start_db() {
    if [ "$(docker ps -aq -f name=^postgresql$)" ]; then
        echo "Error: The 'postgresql' container already exists"
        echo "Please stop and remove it first (use command: ./start_db.sh -d)."
        exit 1
    fi

    echo "Starting a new PostgreSQL container with automatic migration..."
    docker run --name postgresql \
      -e POSTGRES_USER=postgres \
      -e POSTGRES_PASSWORD=postgres \
      -p 5432:5432 \
      -v "$(pwd)/init.sql:/docker-entrypoint-initdb.d/init.sql" \
      -d postgres

    echo "Waiting for the database to be ready..."

    while ! curl -s --connect-timeout 2 telnet://localhost:5432 </dev/null >/dev/null 2>&1; do
        echo "The database is still initializing, please wait..."
        sleep 2
    done

    echo "The database has successfully started, tables are created, and it is ready for connections!"
}

stop_db() {
    echo "Stopping and removing the PostgreSQL container..."
    docker stop postgresql >/dev/null 2>&1
    docker rm postgresql >/dev/null 2>&1
    echo "The database has been successfully stopped and removed."
}

if [ "$1" = "-u" ]; then
    start_db
elif [ "$1" = "-d" ]; then
    stop_db
else
    echo "Error: No flag provided or invalid flag used."
    echo "Usage: ./start_db.sh [-u | -d]"
    echo "  -u   Start the database (Up)"
    echo "  -d   Stop and remove the database (Down)"
    exit 1
fi