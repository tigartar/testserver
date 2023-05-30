package com.wurmonline.server.webinterface;

import java.net.InetAddress;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class RegistryStarter {
   public static final String namingInterface = "wuinterface";

   private RegistryStarter() {
   }

   public static void startRegistry(WebInterfaceImpl webInterface, InetAddress inetaddress, int rmiPort) throws RemoteException, AlreadyBoundException {
      AnchorSocketFactory sf = new AnchorSocketFactory(inetaddress);
      Registry registry = LocateRegistry.createRegistry(rmiPort, null, sf);
      registry.bind("wuinterface", webInterface);
   }
}
