'21 Positioning and Metadata with MQTT
===


## MQTT to OpenShift via TLS on Android

1. Generate ca.key + csr
2. Generate server.key + csr
3. Generate extfile with commonName and subjectAltName DNS
4. Generate x509 server.crt
5. Create tls secrets for server.crt/key and ca.crt/key
6. Mount secrets and ensure mosquitto.conf is updated
7. Use `mosquitto_passwd` to create a password file, mount from secret
8. Generate der server.pem
   - `openssl x509 -inform pem -in server.crt -outform der -out server.pem`
9. What was done with the pem from here?
   - does not look to be loaded anywhere in the code
   - it is checked in under res/raw/server.pem
   - it may have also been manually added as a User Certificate on the test device

## references
- https://stackoverflow.com/a/68641725
- https://developer.android.com/training/articles/security-config.html
- https://www.hivemq.com/blog/mqtt-essentials-part-5-mqtt-topics-best-practices
- https://kotlinlang.org/docs/serialization.html#example-json-serialization
- https://gis.stackexchange.com/a/8674
- https://developers.arcgis.com/android/
