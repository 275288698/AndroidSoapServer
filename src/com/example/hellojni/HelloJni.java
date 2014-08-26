/*
 * Copyright (C) 2009 The Android Open Source Project
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
package com.example.hellojni;

import android.content.res.AssetManager;
import android.util.Log;

public class HelloJni implements Runnable {
	
	/*
	 * this is used to load the 'hello-jni' library on application startup. The
	 * library has already been unpacked into
	 * /data/data/com.example.hellojni/lib/libhello-jni.so at installation time
	 * by the package manager.
	 */
	static {
		System.loadLibrary("stdc++");
		System.loadLibrary("m");
		System.loadLibrary("dl");
		System.loadLibrary("c");
		System.loadLibrary("gnustl_shared");
		System.loadLibrary("magicsquare");
	}
	
	public native int runServer();
	public native boolean stopServer();
	public native int setAssetManager(AssetManager assetManager);

	private AssetManager assetMgr;
	
	public void setAssetMgr(AssetManager assetMgr) {
		this.assetMgr = assetMgr;
	}
	@Override
	public void run() {
		setAssetManager(assetMgr);
		Log.i("HelloJni", "Running server");
		runServer();
		Log.i("HelloJni", "Stopping server");
	}
}