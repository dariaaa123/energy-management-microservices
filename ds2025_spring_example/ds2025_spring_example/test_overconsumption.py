import pika
import json
from datetime import datetime

# Config
RABBIT_URL = "amqps://jjzdoqha:ZDgoASIn_byMxyvseOM3lswd-ST0sipZ@cow.rmq2.cloudamqp.com/jjzdoqha"
EXCHANGE = "data_exchange"
ROUTING_KEY = "device.data.key"

# Device "masina de tuns iarba" - max 200, belongs to maria
DEVICE_ID = "54c6160d-f9b5-4d20-9c69-44450e230cdc"
HIGH_VALUE = 350  # Over the 200 limit!

print(f"[*] Connecting to RabbitMQ...")
params = pika.URLParameters(RABBIT_URL)
connection = pika.BlockingConnection(params)
channel = connection.channel()
print(f"[✓] Connected!")

msg = {
    "timestamp": datetime.utcnow().isoformat(),
    "deviceId": DEVICE_ID,
    "measurementValue": HIGH_VALUE
}

channel.basic_publish(
    exchange=EXCHANGE,
    routing_key=ROUTING_KEY,
    body=json.dumps(msg),
    properties=pika.BasicProperties(delivery_mode=2)
)

print(f"[✓] Sent overconsumption message: {msg}")
print(f"[!] Maria should receive a notification!")

connection.close()
