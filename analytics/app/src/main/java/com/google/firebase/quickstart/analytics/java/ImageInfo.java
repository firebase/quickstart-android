/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.analytics.java;

/**
 * Pair of resource IDs representing an image and its title.
 */
public class ImageInfo {

    public final int image;
    public final int title;
    public final int id;

    /**
     * Create a new ImageInfo.
     *
     * @param image resource of image
     * @param title resource of title
     * @param id resource of id
     */
    public ImageInfo(int image, int title, int id) {
        this.image = image;
        this.title = title;
        this.id = id;
    }

}
