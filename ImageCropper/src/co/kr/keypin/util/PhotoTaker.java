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

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import com.edmodo.cropper.R;

public class PhotoTaker extends Activity {

	private final String TEMP_PREFIX = "tmp_";
	private final int IMAGE_CAPTURE = 101;
	private final int CROP_IMAGE = 102;
	private final int PICK_FROM_FILE = 103;

	private boolean return_data = true;
	private boolean scale = true;
	private boolean faceDetection = true;

	//	private String mOutput;
	private String mTemp;
	private File mDirectory;
	private OnNotFoundCropIntentListener mNotFoundCropIntentListener;
	private Uri mCropUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setOutput(Environment.getExternalStorageDirectory().getPath()+"/touchschool/", "touchschool_photo_preview.jpg");		
		String mode = getIntent().getStringExtra("mode");
		if(mode.equals("camera")) {
			doImageCapture();
		} else if(mode.equals("picture")) {
			doPickImage();
		} else {
			finish();
		}
	}

	public void setOutput(String path, String name) {
		mDirectory = createDirectory(path);
		//		mOutput = name;
		mTemp = TEMP_PREFIX.concat(name);
	}

	public void setOutput(String name) {
		mTemp = TEMP_PREFIX.concat(name);
	}

	public void setNotFoundCropIntentListener(
			OnNotFoundCropIntentListener listener) {
		mNotFoundCropIntentListener = listener;
	}

	private MediaUriFinder.MediaScannedListener mScanner = new MediaUriFinder.MediaScannedListener() {

		@Override
		public boolean OnScanned(Uri uri) {
			/*
			 * Start Crop Activity with URI that we get once scanned if not
			 * found Support Crop Activity then run OnNotFoundCropIntent()
			 */
			if (!doCropImage(uri) && mNotFoundCropIntentListener != null)
				mNotFoundCropIntentListener.OnNotFoundCropIntent(
						mDirectory.getAbsolutePath(), mCropUri);
			return false;
		}
	};
	
	@Override
	protected void onPause() {
		if(dlg != null) dlg.hide();
		super.onPause();
	}

	private ProgressDialog dlg = null;
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case IMAGE_CAPTURE:
				dlg = new ProgressDialog(this, R.style.progressDialog);
				dlg.setMessage("잠시만 기다려주세요");
				dlg.show();
				File tempFile = getFile(mDirectory, mTemp);
				MediaUriFinder.create(this, tempFile.getAbsolutePath(), mScanner);
				break;
			case PICK_FROM_FILE:
				Uri dataUri = data.getData();
				if (dataUri != null) {
					if (dataUri.getScheme().trim().equalsIgnoreCase("content"))
						doCropImage(data.getData());
					// if Scheme URI is File then scan for content then Crop it!
					else if (dataUri.getScheme().trim().equalsIgnoreCase("file"))
						MediaUriFinder.create(this, dataUri.getPath(),	mScanner);
				}
				break;
			case CROP_IMAGE:
				getFile(mDirectory, mTemp).delete();
				PhotoTakerUtils.setImageUri(data.getData());
				finish();				
				break;
			}// end Switch case
		} else {
			finish();			
			getFile(mDirectory, mTemp).delete();
		}		
	}

	public boolean doCropImage(Uri uri) {

		if (uri.getScheme().trim().equalsIgnoreCase("file")) {
			return false;
		}

		mCropUri = uri;

		Intent intent = new Intent(this, PhotoCropActivity.class);

		intent.setType("image/*");
		intent.setData(uri);
		intent.putExtra("noFaceDetection", !faceDetection);
		intent.putExtra("scale", scale);
		intent.putExtra("return-data", return_data);		
		startActivityForResult(intent, CROP_IMAGE);
		
		return true;
	}

	public boolean doImageCapture() {
		try {

			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
				String url = String.valueOf(System.currentTimeMillis()) + ".jpg";				
				setOutput(url);
			}

			File temp = getFile(mDirectory, mTemp);

			// Take Image for Camera and write to tempfile
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra("outputFormat", CompressFormat.JPEG.toString());
			intent.putExtra("return-data", true);
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(temp));
			
			startActivityForResult(intent, IMAGE_CAPTURE);
			
			return true;
		} catch (ActivityNotFoundException anfe) {
			anfe.printStackTrace();
			return false;
		}
	}

	public boolean doPickImage() {
		try {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			intent.setType("image/*");
			intent.putExtra("return-data", return_data);
			startActivityForResult(intent, PICK_FROM_FILE);
			return true;
		} catch (ActivityNotFoundException anfe) {
			anfe.printStackTrace();
			return false;
		}
	}

	private File createDirectory(String path) {
		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}

	private File getFile(File dir, String name) {
		File output = new File(dir, name);
		if (!output.exists()) {
			try {
				output.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return output;
	}

	@Override
	protected void finalize() throws Throwable {
		mCropUri = null;
		mNotFoundCropIntentListener = null;
		super.finalize();
	}

	public static interface OnNotFoundCropIntentListener {
		public boolean OnNotFoundCropIntent(String path, Uri uri);
	}

}
