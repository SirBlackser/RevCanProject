package examples;
import core.Canlib;
import obj.CanlibException;
import obj.Handle;
import obj.Message;

public class CanDump {
	
	public static void main(String[] args) throws CanlibException {
		
		//Setting up the channel and going on bus
        Handle handle = new Handle(0);
    	handle.setBusParams(Canlib.canBITRATE_500K, 0, 0, 0, 0, 0);
    	handle.busOn();

        //Start dumping messages
        DumpMessageLoop(handle);
                
        //Going off bus and closing channel
    	handle.busOff();
    	handle.close();
        
    }

	/*
	 * Waits for messages and prints them to the screen
	 */
    private static void DumpMessageLoop(Handle handle)
    {
        boolean finished = false;

        System.out.println("Channel 0 opened.");
        System.out.println("   ID    DLC DATA                      Timestamp");

        while (!finished)
        {
        	try{
        		while(handle.hasMessage()){
        			Message m = handle.read();
        			dumpMessage(m);
        		}
        	}
        	catch (CanlibException e){
            	e.printStackTrace();
        		System.err.println("An error occurred while reading messages");
        		finished = true;
        	}
        }
    }
    
    /*
     * Prints a received message
     */
    private static void dumpMessage(Message m){
    	if(m.isErrorFrame()){
    		System.out.println("***Error frame received***");
    	}
    	else{
	    	String idString = String.format("%8s", Integer.toUnsignedString(m.id)).replace(' ', '0');
			String hexData = bytesToHex(m.data);
			System.out.printf("%s\t%d  %s\t\t%d\n",
					idString, m.length, hexData, m.time);
    	}
    }

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
