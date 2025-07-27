package serverManager;

/**
 *
 */
public class MarketServerManager extends ServerManager{
    private MarketServerManager(){
        serverIP = "34.47.125.114";
        serverPort = 2000;
        connectServer();
    }
}