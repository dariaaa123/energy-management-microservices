FROM python:3.12-slim

WORKDIR /app

# Install pika
RUN pip install --no-cache-dir pika

# Copy producer script
COPY producer.py .

# Copy default config (will be overridden by volume mount)
COPY producer_config.json .

# Run the producer
CMD ["python", "-u", "producer.py"]
