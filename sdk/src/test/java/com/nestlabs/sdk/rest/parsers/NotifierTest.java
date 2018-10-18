package com.nestlabs.sdk.rest.parsers;

import android.support.annotation.NonNull;

import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestListener;
import com.nestlabs.sdk.models.Camera;
import com.nestlabs.sdk.models.DeviceUpdate;
import com.nestlabs.sdk.models.GlobalUpdate;
import com.nestlabs.sdk.models.Metadata;
import com.nestlabs.sdk.models.SmokeCOAlarm;
import com.nestlabs.sdk.models.Structure;
import com.nestlabs.sdk.models.Thermostat;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NotifierTest {

    private class ErrorListener implements NestListener.ErrorListener {
        private ErrorMessage errorMessage = null;

        @Override
        public void onError(ErrorMessage errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    private class AuthListener implements NestListener.AuthListener {
        private Boolean authFailure = false;
        private Boolean authRevoked = false;

        @Override
        public void onAuthFailure(NestException exception) {
            authFailure = true;
        }

        @Override
        public void onAuthRevoked() {
            authRevoked = true;
        }
    }

    @Test
    public void testHandleError_shouldReceiveErrorNotification() {

        String error = "genericError";
        ErrorListener dummyErrorListener = new ErrorListener();
        AuthListener dummyAuthListener = new AuthListener();

        Notifier notifier = new Notifier();
        notifier.addListener(dummyAuthListener);
        notifier.addListener(dummyErrorListener);
        notifier.handleError(new ErrorMessage().setError(error));

        assertFalse(dummyAuthListener.authFailure);
        assertFalse(dummyAuthListener.authRevoked);
        assertEquals(dummyErrorListener.errorMessage.getError(), error);
    }

    @Test
    public void testHandleError_shouldReceiveAuthErrorNotification() {

        String error = "unauthorized";
        ErrorListener dummyErrorListener = new ErrorListener();
        AuthListener dummyAuthListener = new AuthListener();

        Notifier notifier = new Notifier();
        notifier.addListener(dummyAuthListener);
        notifier.addListener(dummyErrorListener);
        notifier.handleError(new ErrorMessage().setError(error));

        assertTrue(dummyAuthListener.authFailure);
        assertFalse(dummyAuthListener.authRevoked);
        assertNull(dummyErrorListener.errorMessage);

        notifier.handleAuthRevoked();
        assertTrue(dummyAuthListener.authRevoked);
    }

    @Test
    public void testHandleData_shouldReceiveGlobalNotification() {
        class GlobalListener implements NestListener.GlobalListener {
            private GlobalUpdate update;

            @Override
            public void onUpdate(@NonNull GlobalUpdate update) {
                this.update = update;
            }
        }

        class DeviceListener implements NestListener.DeviceListener {
            private DeviceUpdate update;

            @Override
            public void onUpdate(@NonNull DeviceUpdate update) {
                this.update = update;
            }
        }

        class CameraListener implements NestListener.CameraListener {
            private List<Camera> cameras;

            @Override
            public void onUpdate(@NonNull List<Camera> cameras) {
                this.cameras = cameras;
            }
        }

        class ThermostatListener implements NestListener.ThermostatListener {
            private List<Thermostat> thermostats;

            @Override
            public void onUpdate(@NonNull List<Thermostat> thermostats) {
                this.thermostats = thermostats;
            }
        }

        class SmokeCOAlarmListener implements NestListener.SmokeCOAlarmListener {
            private List<SmokeCOAlarm> smokeCOAlarms;

            @Override
            public void onUpdate(@NonNull List<SmokeCOAlarm> smokeCOAlarms) {
                this.smokeCOAlarms = smokeCOAlarms;
            }
        }

        class StructureListener implements NestListener.StructureListener {
            private List<Structure> structures;

            @Override
            public void onUpdate(@NonNull List<Structure> structures) {
                this.structures = structures;
            }
        }

        class MetadataListener implements NestListener.MetadataListener {
            private Metadata metadata;

            @Override
            public void onUpdate(@NonNull Metadata metadata) {
                this.metadata = metadata;
            }
        }

        GlobalListener dummyGlobalListener = new GlobalListener();
        DeviceListener dummyDeviceListener = new DeviceListener();
        CameraListener dummyCameraListener = new CameraListener();
        ThermostatListener dummyThermostatListener = new ThermostatListener();
        SmokeCOAlarmListener dummySmokeCOAlarmListener = new SmokeCOAlarmListener();
        StructureListener dummyStructureListener = new StructureListener();
        MetadataListener dummyMetadataListener = new MetadataListener();

        Notifier notifier = new Notifier();
        notifier.addListener(dummyGlobalListener);
        notifier.addListener(dummyDeviceListener);
        notifier.addListener(dummyCameraListener);
        notifier.addListener(dummyThermostatListener);
        notifier.addListener(dummySmokeCOAlarmListener);
        notifier.addListener(dummyStructureListener);
        notifier.addListener(dummyMetadataListener);

        List<Thermostat> thermostats = new ArrayList<>();
        List<Camera> cameras = new ArrayList<>();
        List<SmokeCOAlarm> smokeAlarms = new ArrayList<>();
        List<Structure> structures = new ArrayList<>();

        notifier.handleData(new GlobalUpdate(
                thermostats, smokeAlarms, cameras, structures, new Metadata()));

        assertNotNull(dummyGlobalListener.update);
        assertNotNull(dummyDeviceListener.update);
        assertNotNull(dummyCameraListener.cameras);
        assertNotNull(dummyThermostatListener.thermostats);
        assertNotNull(dummySmokeCOAlarmListener.smokeCOAlarms);
        assertNotNull(dummyStructureListener.structures);
        assertNotNull(dummyMetadataListener.metadata);
    }

    @Test
    public void testRemoveListener_shouldNotReceiveNotification() {

        String error = "unauthorized";
        AuthListener dummyAuthListener = new AuthListener();

        Notifier notifier = new Notifier();
        notifier.addListener(dummyAuthListener);
        notifier.removeListener(dummyAuthListener);
        notifier.handleError(new ErrorMessage().setError(error));

        assertFalse(dummyAuthListener.authFailure);
        assertFalse(dummyAuthListener.authRevoked);
    }
}
