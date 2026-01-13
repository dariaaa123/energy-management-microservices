import pika
import json
import time
import random
from datetime import datetime
import os
import threading

# ==========================
# LOAD CONFIG FROM FILE
# ==========================
CONFIG_FILE = "multi_producer_config.json"

def load_config():
    """Load configuration from JSON file"""
    if not os.path.exists(CONFIG_FILE):
        print(f"[!] Config file '{CONFIG_FILE}' not found!")
        print("[!] Creating default config file...")
        default_config = {
            "rabbitmq_url": "amqps://jjzdoqha:ZDgoASIn_byMxyvseOM3lswd-ST0sipZ@cow.rmq2.cloudamqp.com/jjzdoqha",
            "exchange": "data_exchange",
            "routing_key": "device.data.key",
            "send_interval_seconds": 600,
            "devices": [
                {"device_id": "device_001", "name": "Smart Meter 1"},
                {"device_id": "device_002", "name": "Smart Meter 2"},
                {"device_id": "device_003", "name": "Smart Meter 3"}
            ]
        }
        with open(CONFIG_FILE, 'w') as f:
            json.dump(default_config, f, indent=2)
        print(f"[*] Created '{CONFIG_FILE}'. Please edit it with your device IDs and restart.")
        return default_config
    
    with open(CONFIG_FILE, 'r') as f:
        config = json.load(f)
    
    print(f"[*] Loaded configuration from '{CONFIG_FILE}'")
    print(f"    - Number of devices: {len(config['devices'])}")
    print(f"    - Send Interval: {config['send_interval_seconds']} seconds")
    return config

# Load configuration
config = load_config()

RABBIT_URL = config["rabbitmq_url"]
EXCHANGE = config["exchange"]
ROUTING_KEY = config["routing_key"]
SEND_INTERVAL_SECONDS = config["send_interval_seconds"]
DEVICES = config["devices"]

# ==========================
# CONNECT TO CLOUDAMQP
# ==========================
print("\n" + "="*60)
print("  MULTI-DEVICE DATA SIMULATOR - Energy Management System")
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
            params.heartbeat = 600
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
print(f"[*] Simulating {len(DEVICES)} devices:")
for device in DEVICES:
    print(f"    - {device['name']}: {device['device_id']}")
print(f"[*] Sending measurements every {SEND_INTERVAL_SECONDS} seconds")
print(f"[*] Press Ctrl+C to stop\n")
print("="*60 + "\n")

# ==========================
# PRODUCER LOGIC
# ==========================
def generate_measurement():
    """Generate realistic energy consumption value based on time of day"""
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
    
    return round(base_load + random.uniform(-0.05, 0.05), 3)

def send_message_for_device(device):
    """Send a measurement for a specific device"""
    value = generate_measurement()
    
    msg = {
        "timestamp": datetime.utcnow().isoformat(),
        "deviceId": device['device_id'],
        "measurementValue": value
    }
    
    try:
        channel.basic_publish(
            exchange=EXCHANGE,
            routing_key=ROUTING_KEY,
            body=json.dumps(msg),
            properties=pika.BasicProperties(
                delivery_mode=2  # persistent
            )
        )
        
        print(f"[>] {device['name']}: {value} kWh")
        return True
    except Exception as e:
        print(f"[!] Error sending for {device['name']}: {e}")
        return False

def send_messages_for_all_devices():
    """Send measurements for all devices"""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"\n[{timestamp}] Sending measurements for all devices:")
    
    success_count = 0
    for device in DEVICES:
        if send_message_for_device(device):
            success_count += 1
    
    print(f"[✓] Sent {success_count}/{len(DEVICES)} measurements successfully\n")

# ==========================
# MAIN LOOP
# ==========================
try:
    while True:
        send_messages_for_all_devices()
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
        pass
