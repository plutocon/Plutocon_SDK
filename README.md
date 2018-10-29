# Plutocon SDK

## Relases
### - 1.5.1
  - Change the maximum length of device name to 14

### - 1.5.0
  - Change gradle version
  - Remove  `Plutocon Service `
  - Add `Software version detail` 
  - Add JavaDoc `PlutoconEditor`
  - Change TxLevel list [-40, -30, -20, -16, -12, -8, -4, 0, 4]
  - `Prevent change TxLevel` in `Software version A1.1.0`

## Installation
### Gradle via jCenter
Declare in your Gradle's `build.gradle` dependency to this library.
```gradle
repositories {
	jcenter()
}

dependencies {
	compile 'com.kongtech.plutocon.sdk:plutocon_sdk:1.5.1'
}
```
## Permissions
### Basic permissions
The following permissions are included in the sdk
  - 'android.permission.BLUETOOTH'
  - 'android.permission.BLUETOOTH_ADMIN'

### Android 6.0 runtime permissions
  - If running on Android 6.0 or later, Location Services must be turned on.
  - If running on Android 6.0 or later and your app is targeting SDK >= 23 (M), any location permission (`ACCESS_COARSE_LOCATION` or `ACCESS_FINE_LOCATION` must be granted.

## Tutorials
### Quick start for monitoring plutocons

```java
private PlutoconManager plutoconManager;

// Initialization
plutoconManager = new PlutoconManager(context, macAddress);

// Start monitoring foreground(fastest scan) with listener
plutoconManager.startMonitoring(PlutoconManager.MONITORING_FOREGROUND, new PlutoconManager.OnMonitoringPlutoconListener() {
	@Override
	public void onPlutoconDiscovered(Plutocon plutocon, List<Plutocon> plutocons) {
		//do something	
	}
});

// Start monitoring background(normal scan)
plutoconManager.startMonitoring(PlutoconManager.MONITORING_BACKGROUND, new PlutoconManager.OnMonitoringPlutoconListener() {
	@Override
	public void onPlutoconDiscovered(Plutocon plutocon, List<Plutocon> plutocons) {
		//do something	
	}
});

// Stop Monitoring
plutoconManager.stopMonitoring();

```

### Quick start for connecting plutocon
```java
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
plutoconConnection.getSoftwareVersionDetail();
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
