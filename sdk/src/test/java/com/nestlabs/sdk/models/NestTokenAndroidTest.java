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

import com.nestlabs.sdk.BuildConfig;
import com.nestlabs.sdk.models.NestToken;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class NestTokenAndroidTest {

    @Test
    public void testCameraToParcel() {
        String testToken = "test-token";
        long testExpiresIn = 123;

        NestToken token = new NestToken(testToken, testExpiresIn);

        Parcel parcel = Parcel.obtain();
        token.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);

        NestToken tokenFromParcel = NestToken.CREATOR.createFromParcel(parcel);
        assertEquals(token, tokenFromParcel);
    }
}

