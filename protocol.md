Operation | Data | Description
----------|------|------------
0000 | UID of the RFID tag (4-bytes). | Request clock in/out with UID from a scanned RFID tag. From safety console to mobile device.
0001 | Response (1-byte). 0001 = Success, 0000 = Fail. | Response to clock in/out request. From mobile device to safety console.
