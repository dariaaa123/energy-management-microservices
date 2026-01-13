#!/usr/bin/env python3
"""
Helper script to list all devices and their IDs
"""
import requests
import json

API_URL = "http://localhost/api/devices"

print("\n" + "="*60)
print("  DEVICE ID FINDER - Energy Management System")
print("="*60 + "\n")

try:
    print("[*] Fetching devices from API...")
    response = requests.get(API_URL)
    
    if response.status_code == 200:
        devices = response.json()
        
        if not devices:
            print("[!] No devices found in the system.")
            print("[*] Create a device first via the web interface at http://localhost")
        else:
            print(f"[✓] Found {len(devices)} device(s):\n")
            print("-" * 60)
            
            for i, device in enumerate(devices, 1):
                print(f"\nDevice #{i}:")
                print(f"  Name: {device.get('name', 'N/A')}")
                print(f"  ID:   {device.get('id', 'N/A')}")
                print(f"  Max Consumption: {device.get('maximumConsumptionValue', 'N/A')} kWh")
                
                if device.get('userId'):
                    print(f"  Assigned to User: {device.get('userId')}")
                else:
                    print(f"  Status: Unassigned")
            
            print("\n" + "-" * 60)
            print("\n[*] Copy one of the IDs above and paste it into producer_config.json")
            print(f"[*] Edit: producer_config.json")
            print(f'[*] Change: "device_id": "paste-id-here"')
            
    else:
        print(f"[!] Error: API returned status code {response.status_code}")
        print("[!] Make sure the services are running: docker-compose ps")
        
except requests.exceptions.ConnectionError:
    print("[!] Error: Cannot connect to API")
    print("[!] Make sure services are running:")
    print("    docker-compose up -d")
    print("[!] Then try again")
    
except Exception as e:
    print(f"[!] Error: {e}")

print("\n" + "="*60 + "\n")
