package nz.co.senanque.ws.client;

import nz.co.senanque.ws.server.MyService;
import nz.co.senanque.ws.server.MyServiceImplService;

public class MyServiceClient {
    public static void main(String[] args){
    MyServiceImplService serviceImpl = new MyServiceImplService();
    MyService service = serviceImpl.getMyServiceImplPort(); 
    System.out.println(service.reverse("123456"));
    }
}

