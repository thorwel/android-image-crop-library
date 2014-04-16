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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.edmodo.cropper.CropImageView;
import com.edmodo.cropper.R;

public class PhotoCropActivity extends Activity {

	// Static final constants
	private static final int DEFAULT_ASPECT_RATIO_VALUES = 10;
	private static final int ROTATE_NINETY_DEGREES = 90;
	private static final String ASPECT_RATIO_X = "ASPECT_RATIO_X";
	private static final String ASPECT_RATIO_Y = "ASPECT_RATIO_Y";

	private Uri mSaveUri = null;
	private Bitmap mBitmap;
	// Instance variables
	private int mAspectRatioX = DEFAULT_ASPECT_RATIO_VALUES;
	private int mAspectRatioY = DEFAULT_ASPECT_RATIO_VALUES;

	Bitmap croppedImage;

	// Saves the state upon rotating the screen/restarting the activity
	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putInt(ASPECT_RATIO_X, mAspectRatioX);
		bundle.putInt(ASPECT_RATIO_Y, mAspectRatioY);
	}

	// Restores the state upon rotating the screen/restarting the activity
	@Override
	protected void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		mAspectRatioX = bundle.getInt(ASPECT_RATIO_X);
		mAspectRatioY = bundle.getInt(ASPECT_RATIO_Y);
	}

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cropimage);

		// Initialize components of the app
		final CropImageView cropImageView = (CropImageView) findViewById(R.id.image);
		
		mSaveUri = getIntent().getData();
		mBitmap = getBitmap(getIntent().getData());

		cropImageView.setImageBitmap(mBitmap);		

		final TextView rotateButton = (TextView) findViewById(R.id.rotateRight);
		rotateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cropImageView.rotateImage(ROTATE_NINETY_DEGREES);
			}
		});

		// Sets initial aspect ratio to 10/10, for demonstration purposes
		cropImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES, DEFAULT_ASPECT_RATIO_VALUES);
		cropImageView.setGuidelines(0);
		cropImageView.setFixedAspectRatio(true);

		final TextView cropButton = (TextView) findViewById(R.id.save);
		cropButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				croppedImage = cropImageView.getCroppedImage();
				onSaveClicked();
			}
		});

	}

	boolean mSaving;  // Whether the "save" button is already clicked.

	private void onSaveClicked() {

		if (mSaving) return;

		mSaving = true;

		// Return the cropped image directly or save it to the specified URI.
		Bundle myExtras = getIntent().getExtras();
		if (myExtras != null && (myExtras.getParcelable("data") != null || myExtras.getBoolean("return-data"))) {

			File mDirectory = createDirectory(Environment.getExternalStorageDirectory().getPath()+"/touchschool/");
			File output = getFile(mDirectory, "touchschool_photo_preview.jpg");
			boolean writed = writeBitmapToFile(croppedImage, output);

			Intent intent = new Intent();
			
			if(writed)
				intent.setData(Uri.fromFile(output));
			
			//			Bundle extras = new Bundle();
			//			extras.putParcelable("data", croppedImage);			
			//			intent.putExtras(extrx`as);
			setResult(RESULT_OK, intent);
			finish();
		} else {
			final Bitmap b = croppedImage;
			saveOutput(b);

		}
	}    

	private void saveOutput(Bitmap croppedImage) {
		if (mSaveUri != null) {
			OutputStream outputStream = null;
			try {
				outputStream = getContentResolver().openOutputStream(mSaveUri);
				if (outputStream != null) {
					croppedImage.compress(CompressFormat.JPEG, 100, outputStream);
				}
			} catch (IOException ex) {
				// TODO: report error to caller
				Log.e("Cropper", "Cannot open file: " + mSaveUri, ex);
			} finally {
				finish();
			}
			Bundle extras = new Bundle();
			setResult(RESULT_OK, new Intent(mSaveUri.toString())
			.putExtras(extras));
		} else {
			Log.e("Cropper", "not defined image url");
		}
		croppedImage.recycle();
		finish();
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

	private File createDirectory(String path) {
		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}	

	private boolean writeBitmapToFile(Bitmap bitmap, File file) {
		try {
			if(bitmap != null) {
				FileOutputStream fops = new FileOutputStream(file);
				bitmap.compress(CompressFormat.JPEG, 100, fops);
				fops.flush();
				fops.close();
				fops = null;
				return true;
			} else {
				return false;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}		

	private Bitmap getBitmap(Uri uri) {
		//		Uri uri = getImageUri(path);
		InputStream in = null;
		try {
			Log.v("test", "uri.getPath() : "+uri.getPath());
			final int IMAGE_MAX_SIZE = 2048;
			in = getContentResolver().openInputStream(uri);

			//Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			BitmapFactory.decodeStream(in, null, o);
			in.close();

			int scale = 1;
			if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
				scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			in = getContentResolver().openInputStream(uri);
			Bitmap b = BitmapFactory.decodeStream(in, null, o2);
			in.close();

			return b;
		} catch (FileNotFoundException e) {
			Log.e("Cropper", "file " + uri.toString() + " not found");
		} catch (IOException e) {
			Log.e("Cropper", "file " + uri.getPath() + " not io");
		}
		return null;
	}    

}
