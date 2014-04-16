/*
 * Copyright 2014, Yang Chang Geun. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" 
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
 */

package co.kr.keypin.util;

import android.app.Activity;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class MediaUriFinder implements MediaScannerConnectionClient {

	private android.media.MediaScannerConnection msc = null;
	private String mFilePath;
	private MediaScannedListener mListener;

	public MediaUriFinder(Activity activity, String filePath,
			MediaScannedListener listener) {
		msc = new android.media.MediaScannerConnection(
				activity.getApplicationContext(), this);
		msc.connect();
		mFilePath = filePath;
		mListener = listener;
	}

	@Override
	public void onMediaScannerConnected() {
		// Scan for temp file
		msc.scanFile(mFilePath, "image/*");
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		mListener.OnScanned(uri);
		msc.disconnect();
	}

	public static MediaUriFinder create(Activity activity, String filePath,
			MediaScannedListener listener) {
		return new MediaUriFinder(activity, filePath, listener);
	}

	public static interface MediaScannedListener {
		boolean OnScanned(Uri uri);
	}

}
