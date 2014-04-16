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
import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.widget.ArrayAdapter;

public class PhotoTakerUtils {
	
	private static Context context;
	private static OnCropFinishListener mOnFinishListener = null;
	
	public static void openTaker(final Context context, OnCropFinishListener listener) {
		
		PhotoTakerUtils.context = context;
		mOnFinishListener = listener;
		
		AlertDialog.Builder dlg = new AlertDialog.Builder(context);
		ArrayList<String> array = new ArrayList<String>();
		array.add("사진 촬영");
		array.add("앨범 선택");
		array.add("취소");
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_item, array);

		dlg.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent i = null;
				switch (which) {
				case 0:
					i = new Intent(context, PhotoTaker.class);
					i.putExtra("mode", "camera");
					context.startActivity(i);
					break;
				case 1:
					i = new Intent(context, PhotoTaker.class);
					i.putExtra("mode", "picture");
					context.startActivity(i);
					break;
				case 2:
					dialog.dismiss();
					break;
				}
			}
		});
		dlg.setCancelable(true);
		dlg.show();
	}
	
	public interface OnCropFinishListener {
		public boolean OnCropFinsh(Bitmap bitmap);
	}
	
	public static void setImageUri(Uri uri) {
		
		Bitmap bitmap = null;
		try {
			bitmap = Media.getBitmap(context.getContentResolver(), uri);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		File fdelete = new File(uri.getPath());
		if (fdelete.exists()) {
			fdelete.delete();
		}
		
		if(mOnFinishListener != null)
			mOnFinishListener.OnCropFinsh(bitmap);
	}
}
