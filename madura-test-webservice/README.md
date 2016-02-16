Madura Test Webservice
===

In order to integration test the workflows properly we need a simple web service for them to call. This project includes both the server and the client ends of the test web service.

It can be started by:
```java
Endpoint.publish("http://localhost:8080/WS/MyService",new MyServiceImpl())
```
and stopped by
```java
Endpoint.stop()
```

The client code is only needed to verify the web service is working and that is in `nz.co.senanque.ws.client.MyServiceClient`.

To invoke it from an external client (eg FF plugin RESTClient) you need to set:
URL: `http://localhost:8080/WS/MyService`
header `Content-Type: text/xml`
Body
```xml
<?xml version="1.0" ?><S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/"><S:Body><ns3:reverse xmlns:ns2="http://server.ws.senanque.co.nz/" xmlns:ns3="http://serverimpl.ws.senanque.co.nz/"><arg0>123456</arg0></ns3:reverse></S:Body></S:Envelope>
```

expected response

```xml
<?xml version="1.0" ?><S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/"><S:Body><ns3:reverseResponse xmlns:ns2="http://server.ws.senanque.co.nz/" xmlns:ns3="http://serverimpl.ws.senanque.co.nz/"><return>654321</return></ns3:reverseResponse></S:Body></S:Envelope>
```
