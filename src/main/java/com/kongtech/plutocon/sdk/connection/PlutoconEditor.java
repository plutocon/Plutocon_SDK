package com.kongtech.plutocon.sdk.connection;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.ParcelUuid;
import android.util.Log;

import com.kongtech.plutocon.sdk.util.PlutoconUuid;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlutoconEditor extends PlutoconOperator {
    private static final List<Integer> TX_LEVELS = Arrays.asList(-40, -30, -20, -16, -12, -8, -4, 0, 4);
    private int[] versionInfo;

    public PlutoconEditor(BluetoothGatt sensorGatt, int[] versionInfo) {
        super(sensorGatt);
        this.versionInfo = versionInfo;
    }

    private String getUuidFromPlace(String baseUuid, double latitude, double longitude) {
        String uuid = baseUuid;
        Log.d("PlutoconEditor", latitude + ", " + longitude);

        int iLatitude = latitude < 0 ? -1 : 1;
        int iLongitude = longitude < 0 ? -1 : 1;

        String slatitude = String.format("%04d%06d", latitude >= 0 ? (int) latitude : 1000 + (int) latitude * -1, (int) ((latitude - (int) latitude) * 1000000 * iLatitude));
        String slongitude = String.format("%04d%06d", longitude >= 0 ? (int) longitude : 1000 + (int) longitude * -1, (int) ((longitude - (int) longitude) * 1000000 * iLongitude));

        Log.d("PlutoconEditor", "UUID: " + uuid);
        Log.d("PlutoconEditor", slatitude + ", " + slongitude);

        uuid = uuid.substring(0, 9) + slatitude.substring(0, 4) + uuid.substring(13, uuid.length());
        uuid = uuid.substring(0, 14) + slatitude.substring(4, 8) + uuid.substring(18, uuid.length());
        uuid = uuid.substring(0, 19) + slatitude.substring(8, 10) + uuid.substring(21, uuid.length());

        uuid = uuid.substring(0, 21) + slongitude.substring(0, 2) + uuid.substring(23, uuid.length());
        uuid = uuid.substring(0, 24) + slongitude.substring(2, 4) + uuid.substring(26, uuid.length());
        uuid = uuid.substring(0, 26) + slongitude.substring(4, 10) + uuid.substring(32, uuid.length());

        Log.d("PlutoconEditor", uuid);
        return uuid;
    }

    public PlutoconEditor setGeofence(double latitude, double longitude) {
        BluetoothGattCharacteristic characteristic = this.getCharacteristics(PlutoconUuid.UUID_CHARACTERISTIC);
        if (characteristic != null && this.characteristicValidate(characteristic)) {
            byte[] data = characteristic.getValue();
            ByteBuffer bb = ByteBuffer.wrap(data);
            long high = bb.getLong();
            long low = bb.getLong();
            String uuid = new UUID(high, low).toString();
            UUID Uuid = UUID.fromString(getUuidFromPlace(uuid, latitude, longitude));

            bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(Uuid.getMostSignificantBits());
            bb.putLong(Uuid.getLeastSignificantBits());

            characteristic.setValue(bb.array());
            this.addCharacteristic(characteristic);
        }
        return this;
    }

    public PlutoconEditor setUUID(ParcelUuid uuid) {
        BluetoothGattCharacteristic characteristic = this.getCharacteristics(PlutoconUuid.UUID_CHARACTERISTIC);
        if (characteristic != null && this.characteristicValidate(characteristic)) {
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(uuid.getUuid().getMostSignificantBits());
            bb.putLong(uuid.getUuid().getLeastSignificantBits());
            characteristic.setValue(bb.array());
            this.addCharacteristic(characteristic);
        }
        return this;
    }

    /**
     * @param packetFormat <ul>
     *                     <li>{@link PlutoconUuid#MAJOR_CHARACTERISTIC}
     *                     <li>{@link PlutoconUuid#MINOR_CHARACTERISTIC}
     *                     <li>{@link PlutoconUuid#TX_LEVEL_CHARACTERISTIC}
     *                     <li>{@link PlutoconUuid#ADV_INTERVAL_CHARACTERISTIC}
     *                     </ul>
     * @return {@link PlutoconEditor}
     */
    public PlutoconEditor setPacketFormat(@NotNull PlutoconConnection.PacketFormat packetFormat) {
        BluetoothGattCharacteristic characteristic = this.getCharacteristics(PlutoconUuid.PACKET_FORMAT_CHARACTERISTIC);
        if (characteristic != null && this.characteristicValidate(characteristic)) {
            if (packetFormat == PlutoconConnection.PacketFormat.KongTech) {
                characteristic.setValue(new byte[]{0x00, 0x59});
            } else if (packetFormat == PlutoconConnection.PacketFormat.iBeacon) {
                characteristic.setValue(new byte[]{0x00, 0x4C});
            }
            this.addCharacteristic(characteristic);
        } else {
            throw new InvalidParameterException("PacketFormat can't change this version");
        }
        return this;
    }

    /**
     * Set property with value
     *
     * @param uuid  <ul>
     *              <li>{@link PlutoconUuid#MAJOR_CHARACTERISTIC}
     *              <li>{@link PlutoconUuid#MINOR_CHARACTERISTIC}
     *              <li>{@link PlutoconUuid#TX_LEVEL_CHARACTERISTIC}
     *              <li>{@link PlutoconUuid#ADV_INTERVAL_CHARACTERISTIC}
     *              </ul>
     * @param value <ul>
     *              <li>{@link PlutoconUuid#MAJOR_CHARACTERISTIC} : 0 ~ 65535
     *              <li>{@link PlutoconUuid#MINOR_CHARACTERISTIC} : 0 ~ 65535
     *              <li>{@link PlutoconUuid#TX_LEVEL_CHARACTERISTIC} : -40, -30, -20, -16, -12, -8, -4, 0, 4
     *              <li>{@link PlutoconUuid#ADV_INTERVAL_CHARACTERISTIC} : 100 ~ 5000
     *              </ul>
     * @return {@link PlutoconEditor}
     * @throws InvalidParameterException
     */
    public PlutoconEditor setProperty(ParcelUuid uuid, int value) {
        BluetoothGattCharacteristic characteristic = this.getCharacteristics(uuid);

        if (characteristic != null && this.characteristicValidate(characteristic)) {
            validate(uuid, value);
            byte[] d = new byte[2];
            short v = (short) value;
            d[0] = (byte) (v >> 8);
            d[1] = (byte) (v & 0xFF);
            characteristic.setValue(d);
            this.addCharacteristic(characteristic);
        }
        return this;
    }

    /**
     * Set property with value
     *
     * @param uuid  <ul>
     *              <li>{@link PlutoconUuid#DEVICE_NAME_CHARACTERISTIC}
     *              </ul>
     * @param value <ul>
     *              <li>{@link PlutoconUuid#ADV_INTERVAL_CHARACTERISTIC} : {@literal (value.length <= 16)}
     *              </ul>
     * @return {@link PlutoconEditor}
     * @throws InvalidParameterException
     */
    public PlutoconEditor setProperty(ParcelUuid uuid, String value) {
        BluetoothGattCharacteristic characteristic = this.getCharacteristics(uuid);
        if (characteristic != null && this.characteristicValidate(characteristic)) {
            validate(uuid, value);
            characteristic.setValue(value);
            this.addCharacteristic(characteristic);
        }
        return this;
    }

    /**
     * Set property with value(*Don't use this method)
     *
     * @param uuid  Plutocon Uuid
     * @param value Raw value
     * @return {@link PlutoconEditor}
     */
    public PlutoconEditor setProperty(ParcelUuid uuid, byte[] value) {
        BluetoothGattCharacteristic characteristic = this.getCharacteristics(uuid);
        if (characteristic != null && this.characteristicValidate(characteristic)) {
            characteristic.setValue(value);
            this.addCharacteristic(characteristic);
        }
        return this;
    }

    private void validate(ParcelUuid uuid, int value) {
        if (PlutoconUuid.TX_LEVEL_CHARACTERISTIC.equals(uuid)) {
            if (versionInfo[0] == 1 && versionInfo[1] < 3)
                throw new InvalidParameterException("Tx level can't change this version");
        }

        if (PlutoconUuid.ADV_INTERVAL_CHARACTERISTIC.equals(uuid)) {
            if (versionInfo[0] == 1 && versionInfo[1] < 3
                    && (value < 100 || value > 5000)) {
                throw new InvalidParameterException();
            } else if (value < 20 || value > 5000) {
                throw new InvalidParameterException();
            }
        } else if (!((PlutoconUuid.MAJOR_CHARACTERISTIC.equals(uuid) && value >= 0 && value <= 65535)
                || (PlutoconUuid.MINOR_CHARACTERISTIC.equals(uuid) && value >= 0 && value <= 65535)
                || (PlutoconUuid.TX_LEVEL_CHARACTERISTIC.equals(uuid) && TX_LEVELS.indexOf(value) >= 0))) {
            throw new InvalidParameterException();
        }
    }

    private void validate(ParcelUuid uuid, String value) {
        if (!(PlutoconUuid.DEVICE_NAME_CHARACTERISTIC.equals(uuid) && value.length() <= 14))
            throw new InvalidParameterException();
    }

    @Override
    public boolean characteristicValidate(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return (bluetoothGattCharacteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
    }

    @Override
    public boolean execute() {
        BluetoothGattCharacteristic characteristic = this.nextOperation();
        if (characteristic != null) {
            plutoconGatt.writeCharacteristic(characteristic);
            return true;
        }
        return false;
    }
}
