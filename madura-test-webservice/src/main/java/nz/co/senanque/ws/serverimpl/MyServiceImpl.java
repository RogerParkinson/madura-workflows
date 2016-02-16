package nz.co.senanque.ws.serverimpl;

import javax.jws.WebService;

import nz.co.senanque.ws.server.MyService;
@WebService(endpointInterface = "nz.co.senanque.ws.server.MyService")
public class MyServiceImpl implements MyService {
    @Override
    public String reverse(String value) {
        if (value == null) return null;
        return new StringBuffer(value).reverse().toString();
    }
}



