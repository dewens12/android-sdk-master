/*
 * Copyright 2016, Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nestlabs.sdk.models;

import android.os.Build;
import android.os.Parcel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nestlabs.sdk.BuildConfig;
import com.nestlabs.sdk.models.Camera;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import junit.framework.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class CameraAndroidTest {

    private static final String TEST_CAMERA_JSON = "/test-camera.json";
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCameraToParcel() {
        try {
            String json = IOUtils.toString(this.getClass().getResourceAsStream(TEST_CAMERA_JSON),
                    "utf-8");
            Camera camera = mapper.readValue(json, Camera.class);

            Parcel parcel = Parcel.obtain();
            camera.writeToParcel(parcel, 0);

            parcel.setDataPosition(0);

            Camera cameraFromParcel = Camera.CREATOR.createFromParcel(parcel);
            assertEquals(camera, cameraFromParcel);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    public static class CameraTest {

        public static final String TEST_CAMERA_JSON = "/test-camera.json";
        public static final String TEST_EMPTY_CAMERA = "/test-empty-camera.json";
        public static final String TEST_CAMERA_UNKNOWN_JSON = "/test-camera-unknown.json";

        ObjectMapper mapper = new ObjectMapper();

        @Test
        public void testCameraConstructor_shouldCreateCameraFromJsonUsingJacksonBindings() {
            try {
                Camera camera = mapper.readValue("{}", Camera.class);

                assertFalse(camera.isStreaming());
                assertFalse(camera.isAudioInputEnabled());
                assertNull(camera.getLastIsOnlineChange());
                assertFalse(camera.isVideoHistoryEnabled());
                assertNull(camera.getWebUrl());
                assertNull(camera.getAppUrl());
                assertFalse(camera.getIsPublicShareEnabled());
                assertTrue(camera.getActivityZones().isEmpty());
                assertNull(camera.getPublicShareUrl());
                assertNull(camera.getSnapshotUrl());
                assertNull(camera.getLastEvent());
            } catch (IOException e) {
                e.printStackTrace();
                org.junit.Assert.fail();
            }
        }

        @Test
        public void testCreateCameraWithJacksonMapper_shouldSetAllValuesCorrectly() {
            try {
                String json = IOUtils.toString(
                        this.getClass().getResourceAsStream(TEST_CAMERA_JSON), "utf-8");
                Camera camera = mapper.readValue(json, Camera.class);

                assertEquals(camera.getDeviceId(), "awJo6rH");
                assertEquals(camera.getSoftwareVersion(), "4.0");
                assertEquals(camera.getStructureId(), "VqFabWH21nwV...");
                assertEquals(camera.getWhereId(), "d6reb_OZTM...");
                assertEquals(camera.getName(), "Hallway (upstairs)");
                assertEquals(camera.getNameLong(), "Hallway Camera (upstairs)");
                assertEquals(camera.isOnline(), true);

                assertEquals(camera.getLastIsOnlineChange(), "2015-12-29T18:42:00.000Z");
                assertEquals(camera.isVideoHistoryEnabled(), true);
                assertEquals(camera.isStreaming(), true);
                assertEquals(camera.isAudioInputEnabled(), true);
                assertEquals(camera.getWebUrl(),
                        "https://home.nest.com/cameras/device_id?auth=access_token");
                assertEquals(camera.getAppUrl(), "nestmobile://cameras/device_id?auth=access_token");
                assertEquals(camera.getIsPublicShareEnabled(), true);

                assertEquals(camera.getActivityZones().get(0).getName(), "Walkway");
                assertEquals(camera.getActivityZones().get(0).getId(), "244083");

                assertEquals(camera.getActivityZones().get(1).getName(), "Porch");
                assertEquals(camera.getActivityZones().get(1).getId(), "237084");

                assertEquals(camera.getPublicShareUrl(), "https://video.nest.com/live/STRING1?STRING2");
                assertEquals(camera.getSnapshotUrl(), "STRING1/device_id/STRING2?auth=access_token");

                Camera.LastEvent lastEvent = camera.getLastEvent();

                String expectedWebUrl =
                        "https://home.nest.com/cameras/device_id/cuepoints/STRING?auth=access_token";
                String expectedAppUrl =
                        "nestmobile://cameras/device_id/cuepoints/STRING?auth=access_token";
                String expectedImageUrl = "STRING1/device_id/STRING2?auth=access_token";
                String expectedAnimatedImageUrl = "STRING1/device_id/STRING2?auth=access_token";

                ArrayList<String> expectedActivityZoneIds = new ArrayList<>();
                expectedActivityZoneIds.add("244083");
                expectedActivityZoneIds.add("237084");

                assertEquals(lastEvent.getHasSound(), true);
                assertEquals(lastEvent.getHasMotion(), true);
                assertEquals(lastEvent.getStartTime(), "2015-12-29T00:00:00.000Z");
                assertEquals(lastEvent.getEndTime(), "2015-12-29T18:42:00.000Z");
                assertEquals(lastEvent.getUrlsExpireTime(), "2015-10-31T23:59:59.000Z");
                assertEquals(lastEvent.getWebUrl(), expectedWebUrl);
                assertEquals(lastEvent.getAppUrl(), expectedAppUrl);
                assertEquals(lastEvent.getImageUrl(), expectedImageUrl);
                assertEquals(lastEvent.getAnimatedImageUrl(), expectedAnimatedImageUrl);
                assertEquals(expectedActivityZoneIds, lastEvent.getActivityZoneIds());
            } catch (IOException e) {
                e.printStackTrace();
                org.junit.Assert.fail();
            }
        }

        @Test
        public void testCreateCameraWithJacksonMapperUnknownProperties_shouldSetAllValuesCorrectly() {
            try {
                String json = IOUtils.toString(
                        this.getClass().getResourceAsStream(TEST_CAMERA_UNKNOWN_JSON), "utf-8");
                Camera camera = mapper.readValue(json, Camera.class);

                assertEquals(camera.getDeviceId(), "awJo6rH");
                assertEquals(camera.getSoftwareVersion(), "4.0");
                assertEquals(camera.getStructureId(), "VqFabWH21nwV...");
                assertEquals(camera.getWhereId(), "d6reb_OZTM...");
                assertEquals(camera.getName(), "Hallway (upstairs)");
                assertEquals(camera.getNameLong(), "Hallway Camera (upstairs)");
                assertEquals(camera.isOnline(), true);

                assertEquals(camera.getLastIsOnlineChange(), "2015-12-29T18:42:00.000Z");
                assertEquals(camera.isVideoHistoryEnabled(), true);
                assertEquals(camera.isStreaming(), true);
                assertEquals(camera.isAudioInputEnabled(), true);
                assertEquals(camera.getWebUrl(),
                        "https://home.nest.com/cameras/device_id?auth=access_token");
                assertEquals(camera.getAppUrl(), "nestmobile://cameras/device_id?auth=access_token");

                assertEquals(camera.getIsPublicShareEnabled(), true);

                assertEquals(camera.getActivityZones().get(0).getName(), "Walkway");
                assertEquals(camera.getActivityZones().get(0).getId(), "244083");

                assertEquals(camera.getActivityZones().get(1).getName(), "Porch");
                assertEquals(camera.getActivityZones().get(1).getId(), "237084");

                assertEquals(camera.getPublicShareUrl(), "https://video.nest.com/live/STRING1?STRING2");
                assertEquals(camera.getSnapshotUrl(), "STRING1/device_id/STRING2?auth=access_token");

                Camera.LastEvent lastEvent = camera.getLastEvent();

                String expectedWebUrl =
                        "https://home.nest.com/cameras/device_id/cuepoints/STRING?auth=access_token";
                String expectedAppUrl =
                        "nestmobile://cameras/device_id/cuepoints/STRING?auth=access_token";
                String expectedImageUrl = "STRING1/device_id/STRING2?auth=access_token";
                String expectedAnimatedImageUrl = "STRING1/device_id/STRING2?auth=access_token";

                ArrayList<String> expectedActivityZoneIds = new ArrayList<>();
                expectedActivityZoneIds.add("244083");
                expectedActivityZoneIds.add("237084");

                assertEquals(lastEvent.getHasSound(), true);
                assertEquals(lastEvent.getHasMotion(), true);
                assertEquals(lastEvent.getStartTime(), "2015-12-29T00:00:00.000Z");
                assertEquals(lastEvent.getEndTime(), "2015-12-29T18:42:00.000Z");
                assertEquals(lastEvent.getUrlsExpireTime(), "2015-10-31T23:59:59.000Z");
                assertEquals(lastEvent.getWebUrl(), expectedWebUrl);
                assertEquals(lastEvent.getAppUrl(), expectedAppUrl);
                assertEquals(lastEvent.getImageUrl(), expectedImageUrl);
                assertEquals(lastEvent.getAnimatedImageUrl(), expectedAnimatedImageUrl);

                assertEquals(lastEvent.getActivityZoneIds(), expectedActivityZoneIds);
            } catch (IOException e) {
                e.printStackTrace();
                org.junit.Assert.fail();
            }
        }

        @Test
        public void testDescribeContents_shouldReturnZero() {
            Camera c = new Camera();
            assertEquals(c.describeContents(), 0);
        }

        @Test
        public void testLastEventDescribeContents_shouldReturnZero() {
            try {
                String json = IOUtils.toString(
                        this.getClass().getResourceAsStream(TEST_CAMERA_JSON), "utf-8");
                Camera camera = mapper.readValue(json, Camera.class);
                assertEquals(camera.getLastEvent().describeContents(), 0);
            } catch (IOException e) {
                e.printStackTrace();
                org.junit.Assert.fail();
            }
        }

        @Test
        public void testNewArray_shouldReturnArrayOfCorrectSize() {
            int cameraSize = new Random().nextInt(9) + 1;

            Camera[] cameras = Camera.CREATOR.newArray(cameraSize);
            assertEquals(cameras.length, cameraSize);
        }

        @Test
        public void testLastEventNewArray_shouldReturnArrayOfCorrectSize() {
            int cameraLastEventSize = new Random().nextInt(9) + 1;

            Camera.LastEvent[] lastEvents = Camera.LastEvent.CREATOR.newArray(cameraLastEventSize);
            assertEquals(lastEvents.length, cameraLastEventSize);
        }

        @Test
        public void testEquals_shouldReturnFalseWithNonCamera() {
            Object o = new Object();
            Camera c = new Camera();
            assertFalse(c.equals(o));
        }

        @Test
        public void testToString_shouldReturnNicelyFormattedString() {
            Camera c = new Camera();
            try {
                String json = IOUtils.toString(
                        this.getClass().getResourceAsStream(TEST_EMPTY_CAMERA), "utf-8").trim();
                assertEquals(json, c.toString());
            } catch (IOException e) {
                e.printStackTrace();
                org.junit.Assert.fail();
            }
        }
    }
}
