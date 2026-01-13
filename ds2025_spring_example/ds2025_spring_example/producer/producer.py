import pika
import json
import time
import random
from datetime import datetime
import os

# ==========================
# LOAD CONFIG FROM FILE
# ==========================
CONFIG_FILE = "producer_config.json"

def load_config():
    """Load configuration from JSON file"""
    if not os.path.exists(CONFIG_FILE):
        print(f"[!] Config file '{CONFIG_FILE}' not found!")
        print("[!] Creating default config file...")
        default_config = {
            "rabbitmq_url": "amqps://jjzdoqha:ZDgoASIn_byMxyvseOM3lswd-ST0sipZ@cow.rmq2.cloudamqp.com/jjzdoqha",
            "exchange": "data_exchange",
            "routing_key": "device.data.key",
            "device_id": "device_001",
            "send_interval_seconds": 600
        }
        with open(CONFIG_FILE, 'w') as f:
            json.dump(default_config, f, indent=2)
        print(f"[*] Created '{CONFIG_FILE}'. Please edit it with your device ID and restart.")
        return default_config
    
    with open(CONFIG_FILE, 'r') as f:
        config = json.load(f)
    
    print(f"[*] Loaded configuration from '{CONFIG_FILE}'")
    print(f"    - Device ID: {config['device_id']}")
    print(f"    - Send Interval: {config['send_interval_seconds']} seconds")
    return config

# Load configuration
config = load_config()

RABBIT_URL = config["rabbitmq_url"]
EXCHANGE = config["exchange"]
ROUTING_KEY = config["routing_key"]
DEVICE_ID = config["device_id"]
SEND_INTERVAL_SECONDS = config["send_interval_seconds"]

# ==========================
# CONNECT TO CLOUDAMQP
# ==========================
print("\n" + "="*60)
print("  DEVICE DATA SIMULATOR - Energy Management System")
print("="*60)
print(f"\n[*] Connecting to RabbitMQ...")

def connect_to_rabbitmq():
    """Connect to RabbitMQ with retry logic"""
    max_retries = 3
    retry_delay = 5
    
    for attempt in range(max_retries):
        try:
            params = pika.URLParameters(RABBIT_URL)
            params.socket_timeout = 10
            params.heartbeat = 600  # Keep connection alive
            conn = pika.BlockingConnection(params)
            ch = conn.channel()
            return conn, ch
        except Exception as e:
            if attempt < max_retries - 1:
                print(f"[!] Connection failed (attempt {attempt + 1}/{max_retries}): {e}")
                print(f"[*] Retrying in {retry_delay} seconds...")
                time.sleep(retry_delay)
            else:
                raise

connection, channel = connect_to_rabbitmq()

print("[✓] Connected to RabbitMQ CloudAMQP")
print(f"[*] Simulating device: {DEVICE_ID}")
print(f"[*] Sending measurements every {SEND_INTERVAL_SECONDS} seconds")
print(f"[*] Press Ctrl+C to stop\n")
print("="*60 + "\n")


# ==========================
# PRODUCER LOOP
# ==========================
def generate_measurement():
    """
    Simulează un consum realist:
    - consum mai mic noaptea
    - consum mai mare seara
    - fluctuații mici
    """
    hour = datetime.now().hour

    base_load = 0
    if 0 <= hour < 6:
        base_load = random.uniform(0.1, 0.3)
    elif 6 <= hour < 12:
        base_load = random.uniform(0.2, 0.5)
    elif 12 <= hour < 18:
        base_load = random.uniform(0.3, 0.6)
    else:
        base_load = random.uniform(0.4, 0.8)

    # fluctuație mică
    return round(base_load + random.uniform(-0.05, 0.05), 3)


def send_message():
    value = generate_measurement()

    msg = {
        "timestamp": datetime.utcnow().isoformat(),
        "deviceId": DEVICE_ID,
        "measurementValue": value
    }

    channel.basic_publish(
        exchange=EXCHANGE,
        routing_key=ROUTING_KEY,
        body=json.dumps(msg),
        properties=pika.BasicProperties(
            delivery_mode=2  # persistent
        )
    )

    print(f"[>] Sent: {msg}")


# ==========================
# MAIN LOOP
# ==========================
try:
    while True:
        send_message()
        time.sleep(SEND_INTERVAL_SECONDS)

except KeyboardInterrupt:
    print("\n[!] Stopped by user")

except Exception as e:
    print(f"\n[!] Error: {e}")
    print("[!] Connection lost. Please restart the producer.")

finally:
    try:
        if connection and connection.is_open:
            connection.close()
            print("[*] Connection closed")
    except:
        pass  # Connection already closed
