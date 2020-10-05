Operation | Data | Description
----------|------|------------
0000 0000 | UID of the RFID tag (4-bytes). | Request clock in/out with UID from a scanned RFID tag. From safety console to mobile device.
0000 0001 | Response (1-byte). 0000 0010 = Clock in successful, 0000 0001 = Clock out successful, 0000 0000 = Request failed. | Response to clock in/out request. From mobile device to safety console.
0000 0010 | None. | System-wide warning. From mobile device to safety console.
0000 0011 | Hours left (1-byte), then remaining minutes left (1-byte). | Time left. From mobile device to safety console.
0000 0100 | Radiation level (1-byte). Value between 1-100. | Simulated radiation level. From safety console to mobile device.
0000 0101 | Room (1-byte). 0000 0000 = Break room, 0000 0001 = Control room, 0000 0010 = Reactor room. | Change room. From safety console to mobile device.
0000 0110 | Equipped (1-byte). 0000 0001 = Equipped, 0000 0000 = Unequipped. | Equip or unequip hazmat suit. From safety console to mobile device.
0000 0111 | Radiation exposure (1-byte). Represented as an integer. | Radiation exposure. From mobile device to safety console.
