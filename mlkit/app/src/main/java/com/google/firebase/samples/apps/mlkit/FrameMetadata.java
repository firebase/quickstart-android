// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.firebase.samples.apps.mlkit;

/** Describing a frame info. */
public class FrameMetadata {

  private final int width;
  private final int height;
  private final int rotation;
  private final int cameraFacing;

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getRotation() {
    return rotation;
  }

  public int getCameraFacing() {
    return cameraFacing;
  }

  private FrameMetadata(int width, int height, int rotation, int facing) {
    this.width = width;
    this.height = height;
    this.rotation = rotation;
    cameraFacing = facing;
  }

  /** Builder of {@link FrameMetadata}. */
  public static class Builder {

    private int width;
    private int height;
    private int rotation;
    private int cameraFacing;

    public Builder setWidth(int width) {
      this.width = width;
      return this;
    }

    public Builder setHeight(int height) {
      this.height = height;
      return this;
    }

    public Builder setRotation(int rotation) {
      this.rotation = rotation;
      return this;
    }

    public Builder setCameraFacing(int facing) {
      cameraFacing = facing;
      return this;
    }

    public FrameMetadata build() {
      return new FrameMetadata(width, height, rotation, cameraFacing);
    }
  }
}
