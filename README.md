# plutocon#

##Installation
### Gradle via jCenter
Declare in your Gradle's `build.gradle` dependency to this library.
```gradle
repositories {
	jcenter()
}

dependencies {
	compile 'com.kongtech.plutocon.sdk:plutocon_sdk:1.0.0'
}
```
##Permissions
### Basic permissions
The following permissions are included in the sdk
  - 'android.permission.BLUETOOTH'
  - 'android.permission.BLUETOOTH_ADMIN'

### Android 6.0 runtime permissions
  - If running on Android 6.0 or later, Location Services must be turned on.
  - If running on Android 6.0 or later and your app is targeting SDK < 23 (M), any location permission (`ACCESS_COARSE_LOCATION` or `ACCESS_FINE_LOCATION`) must be granted for <b>background</b> beacon detection.
  - If running on Android 6.0 or later and your app is targeting SDK >= 23 (M), any location permission (`ACCESS_COARSE_LOCATION` or `ACCESS_FINE_LOCATION` must be granted.

## Tutorials
### Quick start for monitoring plutocons

````java
private PlutoconManager plutoconManager;

// Initialization
plutoconManager = new PlutoconManager(context);
plutoconManager.connectService(new PlutoconManager.OnReadyServiceListener() {
	@Override
	public void onReady() {
		//do something
	}
});


// Start monitoring foreground with listener
plutoconManager.startMonitoring(PlutoconManager.MONITORING_FOREGROUND, new PlutoconManager.OnMonitoringPlutoconListener() {
	@Override
	public void onPlutoconDiscovered(Plutocon plutocon, List<Plutocon> plutocons) {
		//do something	
	}
});

// Start monitoring background
plutoconManager.startMonitoring(PlutoconManager.MONITORING_BACKGROUND, new PlutoconManager.OnMonitoringPlutoconListener() {
	@Override
	public void onPlutoconDiscovered(Plutocon plutocon, List<Plutocon> plutocons) {
		//do something	
	}
});

// Stop Monitoring
plutoconManager.stopMonitoring();

// Disconnect from manager service.
plutoconManager.close();
```

### Quick start for connecting sensor
````java
// Initialization
PlutoconConnection plutoconConnection = new PlutoconConnection(context);

// Connect to plutocon
plutoconConnection.connect(new PlutoconConnection.OnConnectionStateChangeCallback() {
	@Override
	public void onConnectionStateDisconnected() {
		//do something		
	}

	@Override
	public void onConnectionStateConnected() {
		//do something		
	}
});

// Read plutocon property
plutoconConnection.getBatteryVoltage();
plutoconConnection.getBroadcastingPower();
plutoconConnection.getAdvertisingInterval();

plutoconConnection.getUuid();
plutoconConnection.getLatitude();
plutoconConnection.getLongitude();

plutoconConnection.getSoftwareVersion();
plutoconConnection.getHardwareVersion();
plutoconConnection.getManufactureName();
plutoconConnection.getModelNumber();

// Disconnect from plutocon
plutoconConnection.disconnect();
````

### Quick start for edit plutocon property
````java
PlutoconEditor editor = plutoconConnection.editor();
editor
	.setGeofence(latitude, longitude)
	.setProperty(uuid, value)
	.setUUID(uuid)
	.setOnOperationCompleteCallback(new PlutoconOperator.OnOperationCompleteCallback() {
		@Override
		public void onOperationComplete(BluetoothGattCharacteristic characteristic, boolean isLast) {
			//do something
		}
	})
	.commit();

````