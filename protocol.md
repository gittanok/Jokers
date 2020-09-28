Operation | Data | Description
----------|------|------------
0000 | UID of the RFID tag (4-bytes). | Request clock in/out with UID from a scanned RFID tag. From safety console to mobile device.
0001 | Response (1-byte). 0010 = Clock in successful, 0001 = Clock out successful, 0000 = Request failed. | Response to clock in/out request. From mobile device to safety console.
0010 | None | System-wide warning. From mobile device to safety console.
0011 | Hours left (1-byte), then remaining minutes left (1-byte) | Time left. From mobile device to safety console.
