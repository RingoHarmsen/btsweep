package com.example.btsweep;


public class BtEvent implements Comparable<BtEvent> {
	public  String macAdresBtDevice;
	public  String btDeviceName;  
	public  String foundBtDateTime; 
	public  long unixTime =0; 

	String getMacAdresBtDevice() {    // are uniq in the world without hacking ...  so it is the prim key
		return(macAdresBtDevice);
	}

	void setMacAdresBtDevice(String ttMacAdresBtDevice) {    // other varname dunno why of the tt prefix
		macAdresBtDevice = ttMacAdresBtDevice;  
	}
	
	long getUnixTime() {    // are uniq in the world without hacking ...  so it is the prim key
		return(unixTime);
	}

	void setUnixTime(long qqunixTime) {    // other varname dunno why of the tt prefix
		unixTime =  qqunixTime; 
	}

	
	String getBtDeviceName() {
		return(btDeviceName);
	}

	void setBtDeviceName(String ttbtDeviceName) {
		btDeviceName = ttbtDeviceName;  
	}

	
	String getFoundBtDateTime() {
		return(foundBtDateTime);
	}

	void setFoundBtDateTime(String ttfoundBtDateTime) {
		foundBtDateTime = ttfoundBtDateTime;  
	}
	
	public int compareTo(BtEvent compareBtEvent) {
		long compareQuantity = ((BtEvent) compareBtEvent).getUnixTime();
//		return (int) (this.unixTime - compareQuantity); 
		return (int) (compareQuantity - this.unixTime); 
	}

	String getScreenString(){
		String formatStringForList="";
		
		formatStringForList =  btDeviceName  +   "  ("+  macAdresBtDevice + ")"+   "\n" + foundBtDateTime ;
		return (formatStringForList); 
	}
	
}