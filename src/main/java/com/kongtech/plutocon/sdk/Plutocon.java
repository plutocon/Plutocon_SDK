package com.kongtech.plutocon.sdk;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.Log;

import com.kongtech.plutocon.sdk.repackaged.ScanRecord;
import com.kongtech.plutocon.sdk.util.PlutoconUuid;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class Plutocon implements Parcelable, Comparable<Plutocon> {
    private String name;
    private String macAddress;

    private ParcelUuid uuid;

    private int major;
    private int minor;

    private int rssi;
    private int interval;
    private long lastSeenMillis;

    private int battery;

    private boolean isMonitoring;
    private boolean isCertification;

    public Plutocon(String name, String macAddress, int rssi, int battery, byte[] manufacturerSpecificData, boolean isCertification) {
        this.name = name;
        this.macAddress = macAddress;
        this.rssi = rssi;
        this.lastSeenMillis = System.currentTimeMillis();
        this.interval = 0;
        this.isMonitoring = true;
        this.battery = battery;
        this.isCertification = isCertification;
        this.parsingManufacturerSpecificData(manufacturerSpecificData);
    }

    public Plutocon(String name, String macAddress) {
        this.name = name;
        this.macAddress = macAddress;
        this.rssi = 0;
        this.lastSeenMillis = 0;
        this.interval = 0;
        this.isMonitoring = false;
        this.major = 0;
        this.minor = 0;
        this.uuid = null;
    }

    public void setiBeacon(String uuid, String major, String minor) {
        this.major = Integer.parseInt(major);
        this.minor = Integer.parseInt(minor);
        this.uuid = ParcelUuid.fromString(uuid);
    }

    public Plutocon(Parcel source) {
        this.name = source.readString();
        this.macAddress = source.readString();
        this.rssi = source.readInt();
        this.interval = source.readInt();
        this.lastSeenMillis = source.readLong();
        this.isMonitoring = source.readByte() != 0;
        this.isCertification = source.readByte() != 0;
        this.major = source.readInt();
        this.minor = source.readInt();
        this.battery = source.readInt();
        this.uuid = source.readParcelable(ParcelUuid.class.getClassLoader());
    }


    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public ParcelUuid getUuid() {
        return uuid;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRssi() {
        return rssi;
    }

    public int getInterval() {
        return interval;
    }

    public void disappeared() {
        isMonitoring = false;
    }

    public void appeared() {
        isMonitoring = true;
    }

    public boolean isMonitoring() {
        return isMonitoring;
    }

    public boolean isCertification() {
        return isCertification;
    }

    public long getLastSeenMillis() {
        return lastSeenMillis;
    }

    public int getBattery() {
        return battery;
    }

    public void updateInterval(long oldLastSeenMillis) {
        this.interval = (int) (this.lastSeenMillis - oldLastSeenMillis);
    }

    public double getLatitude() {
        if (uuid == null) return 0;
        try {
            double latitudeHigh = Double.parseDouble(uuid.toString().substring(9, 13));
            double latitudeLow = Double.parseDouble(uuid.toString().substring(14, 18)
                    + uuid.toString().substring(19, 21));
            return (latitudeHigh * 1000000 + latitudeLow) / 1000000;
        } catch (Exception e) {
            return 0;
        }
    }

    public double getLongitude() {
        if (uuid == null) return 0;
        try {
            double longitudeHigh = Double.parseDouble(uuid.toString().substring(21, 23)
                    + uuid.toString().substring(24, 26));
            double longitudeLow = Double.parseDouble(uuid.toString().substring(26, 32));
            return (longitudeLow * 1000000 + longitudeLow) / 1000000;
        } catch (Exception e) {
            return 0;
        }
    }

    private void parsingManufacturerSpecificData(byte[] manufacturerSpecificData) {
        if (manufacturerSpecificData == null) return;

        byte[] majorBytes = Arrays.copyOfRange(manufacturerSpecificData, 18, 20);
        byte[] minorBytes = Arrays.copyOfRange(manufacturerSpecificData, 20, 22);
        byte[] uuidBytes = Arrays.copyOfRange(manufacturerSpecificData, 2, 18);

        ByteBuffer proximityUUIDBuffer = ByteBuffer.wrap(uuidBytes);

        this.major = ((majorBytes[0] & 0xff) << 8) | (majorBytes[1] & 0xff);
        this.minor = ((minorBytes[0] & 0xff) << 8) | (minorBytes[1] & 0xff);
        this.uuid = new ParcelUuid(new UUID(proximityUUIDBuffer.getLong(), proximityUUIDBuffer.getLong()));
    }

    @Override
    public int hashCode() {
        return this.macAddress.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o != null && this.getClass() == o.getClass()) {
            Plutocon plutocon = (Plutocon) o;
            if (this.macAddress.equals(plutocon.getMacAddress())) return true;
        }
        return false;
    }

    @Override
    public int compareTo(Plutocon another) {
        return another.isMonitoring() == this.isMonitoring() ? 0 : another.isMonitoring() ? 1 : -1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.macAddress);
        dest.writeInt(this.rssi);
        dest.writeInt(this.interval);
        dest.writeLong(this.lastSeenMillis);
        dest.writeByte((byte) (isMonitoring ? 1 : 0));
        dest.writeByte((byte) (isCertification ? 1 : 0));
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeInt(this.battery);
        dest.writeParcelable(uuid, flags);
    }

    public static final Parcelable.Creator<Plutocon> CREATOR = new Creator<Plutocon>() {
        @Override
        public Plutocon createFromParcel(Parcel source) {
            return new Plutocon(source);
        }

        @Override
        public Plutocon[] newArray(int size) {
            return new Plutocon[size];
        }
    };

    public static Plutocon createFromScanResult(BluetoothDevice device, ScanRecord scanRecord, int rssi) {

        byte[] serviceData = scanRecord.getServiceData(PlutoconUuid.SERVICE_DATA_UUID);
        byte[] manufacturerSpecificData = scanRecord.getManufacturerSpecificData(76);

        if (serviceData != null && manufacturerSpecificData != null
                && serviceData.length == 11) {
            if (serviceData[0] != 1) return null;
            int battery = (((int) serviceData[8]) << 8) | ((int) serviceData[9] & 0xFF);
            return new Plutocon(scanRecord.getDeviceName(), device.getAddress() , rssi, battery, manufacturerSpecificData, true);
        }
        return null;
    }

    ;
}
