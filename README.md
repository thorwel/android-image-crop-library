android-image-crop-library
==========================

android image or picture crop library. (by gallery or camera)


this library base <a href='https://github.com/edmodo/cropper#cropper'>cropper</a>

<b>USAGE</b>

Just add click event
PhotoTakerUtils.openTaker(this, this);

Korean : 버튼에 원클릭 리스너에서 이벤트를 줄때 위 함수를 넣으시면 다이얼로그가 뜹니다.

and

implement OnCropFinishListener
@Override
	public boolean OnCropFinsh(Bitmap bitmap) {
		// here bitmap set imageview.
		return false;
}

Korean : 이미지 처리가 끝나면 onCropFinish에서 해당 비트맵을 이미지뷰에 넣어주면됩니다.
