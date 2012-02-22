package de.lorbeer.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class NoImageView extends ImageView {

	public NoImageView(Context context) {
		super(context);
	}

	public NoImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = width * getDrawable().getIntrinsicHeight()
				/ getDrawable().getIntrinsicWidth();
		setMeasuredDimension(width, height);
	}
}
