'21 Positioning and Metadata with MQTT
===


## Notes

How to access MQTT through OpenShift TLS route.

### OpenShift

1. Deploy a mqtt image to openshift
   - quay.io/kboone/mosquitto-ephemeral:latest 
2. Create a route
```
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: mosquitto-ephemeral-tls
spec:
  host: mqtt.xxxx.xxxx
  port:
    targetPort: 8883
  tls:
    termination: passthrough
  to:
    kind: Service
    name: mosquitto-ephemeral-tls
    weight: 100
  wildcardPolicy: None
```

### DNS

Add a CNAME to provider (eg GoDaddy) matching the status.ingress.routerCanonicalHostname on the route

```
Type 	Name 	Value 	                                    TTL 	Actions
cname 	mqtt 	elb.b9ad.pro-us-east-1.openshiftapps.com 	1 Hour
```

## TLS / MQTT Server
1. Generate ca.key + csr
2. Generate server.key + csr
3. Generate extfile with commonName and subjectAltName DNS
4. Generate x509 server.crt
5. Create tls secrets for server.crt/key and ca.crt/key
6. Mount secrets and ensure mosquitto.conf is updated
7. Use `mosquitto_passwd` to create a password file, mount from secret
8. Generate der server.pem
   - `openssl x509 -inform pem -in server.crt -outform der -out server.pem`

### Android / MQTT client
1. What was done with the pem from here?
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
